package org.little100.cCB;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class PlayerListener implements Listener {
    private final CCB plugin;
    private final Map<UUID, ArmorStand> armorStands = new HashMap<>();
    private final Map<UUID, Integer> sneakCounts = new HashMap<>();
    private final Map<UUID, Object> taskHandles = new HashMap<>();

    public PlayerListener(CCB plugin) {
        this.plugin = plugin;
    }

    private void debug(String msg) {
        if (plugin.getConfig().getBoolean("debug", false)) {
            plugin.getLogger().info("[DEBUG] " + msg);
        }
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent e) {
        Player player = e.getPlayer();
        PlayerData data = plugin.getDb().getPlayer(player.getUniqueId());
        debug("onSneak: player=" + player.getName() + ", enabled=" + data.enabled() + ", sex=" + data.sex() + ", isSneaking=" + e.isSneaking());
        if (!data.enabled() || data.sex() == null) {
            debug("onSneak: 跳过 - enabled或sex无效");
            return;
        }

        if (e.isSneaking()) {
            if ("female".equals(data.sex())) {
                debug("onSneak: 跳过 - female不生成盔甲架");
                return;
            }
            boolean alwaysOn = data.alwaysOn() && "male".equals(data.sex());
            Player partner = alwaysOn ? null : findPartner(player);
            boolean hasBed = alwaysOn || hasBedNearby(player);
            debug("onSneak: partner=" + (partner != null ? partner.getName() : "null") + ", hasBed=" + hasBed + ", alwaysOn=" + alwaysOn);
            if (!alwaysOn && (partner == null || !hasBed)) {
                debug("onSneak: 条件不满足，停止跟随");
                stopFollowing(player);
                return;
            }
            debug("onSneak: 开始生成盔甲架");
            spawnArmorStandWithAnimation(player);
            playSounds(player, data);
            incrementSneakCount(player);
            startFollowing(player);
            
            if (partner != null) {
                spawnWaterParticles(player, partner);
            }
        } else {
            debug("onSneak: 停止潜行，停止跟随");
            stopFollowing(player);
        }
    }

    private Player findPartner(Player player) {
        for (Player p : player.getWorld().getPlayers()) {
            if (p.equals(player)) continue;
            if (p.getLocation().distance(player.getLocation()) <= 1) {
                PlayerData pd = plugin.getDb().getPlayer(p.getUniqueId());
                if (pd.enabled()) return p;
            }
        }
        return null;
    }

    private boolean hasBedNearby(Player player) {
        Location loc = player.getLocation();
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    Block b = loc.clone().add(x, y, z).getBlock();
                    if (b.getType().name().contains("BED")) return true;
                }
            }
        }
        return false;
    }

    private Location getStandLocation(Player player, double forwardOffset) {
        PlayerData data = plugin.getDb().getPlayer(player.getUniqueId());
        Location base = player.getLocation().clone();
        base.setY(base.getY() + 0.5 + data.offsetY());
        
        org.bukkit.util.Vector forward = player.getLocation().getDirection().setY(0).normalize();
        org.bukkit.util.Vector right = forward.clone().crossProduct(new org.bukkit.util.Vector(0, 1, 0)).normalize();
        
        base.add(forward.multiply(forwardOffset + data.offsetZ()));
        base.add(right.multiply(data.offsetX()));
        
        return base;
    }

    private void spawnArmorStandWithAnimation(Player player) {
        ArmorStand stand = armorStands.get(player.getUniqueId());
        debug("spawnArmorStand: 现有stand=" + (stand != null ? stand.getEntityId() : "null") + ", isValid=" + (stand != null ? stand.isValid() : "N/A"));
        if (stand != null && stand.isValid()) {
            debug("spawnArmorStand: 盔甲架已存在，跳过");
            return;
        }
        
        PlayerData data = plugin.getDb().getPlayer(player.getUniqueId());
        Location startLoc = getStandLocation(player, 0.3);
        debug("spawnArmorStand: 生成位置=" + startLoc);
        boolean isSmall = data.sizeSmall();
        stand = player.getWorld().spawn(startLoc, ArmorStand.class, as -> {
            as.setVisible(false);
            as.setGravity(false);
            as.setInvulnerable(true);
            as.setSmall(isSmall);
            as.getEquipment().setHelmet(new ItemStack(Material.END_ROD));
        });
        debug("spawnArmorStand: 生成成功，entityId=" + stand.getEntityId());
        armorStands.put(player.getUniqueId(), stand);
        
        ArmorStand finalStand = stand;
        plugin.runLaterForEntity(finalStand, () -> {
            debug("spawnArmorStand: 动画回调，isValid=" + finalStand.isValid());
            if (finalStand.isValid()) plugin.teleportEntity(finalStand, getStandLocation(player, 0.5));
        }, 2L);
    }

    private void startFollowing(Player player) {
        debug("startFollowing: taskHandles.contains=" + taskHandles.containsKey(player.getUniqueId()) + ", isFolia=" + CCB.isFolia());
        if (taskHandles.containsKey(player.getUniqueId())) {
            debug("startFollowing: 任务已存在，跳过");
            return;
        }
        
        if (CCB.isFolia()) {
            debug("startFollowing: 使用Folia调度器");
            startFollowingFolia(player);
        } else {
            debug("startFollowing: 使用Bukkit调度器");
            int taskId = org.bukkit.Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
                ArmorStand stand = armorStands.get(player.getUniqueId());
                if (stand == null || !stand.isValid() || !player.isSneaking()) {
                    stopFollowing(player);
                    return;
                }
                stand.teleport(getStandLocation(player, 0.5));
            }, 1L, 1L);
            taskHandles.put(player.getUniqueId(), taskId);
            debug("startFollowing: Bukkit任务ID=" + taskId);
        }
    }

    private void scheduleFollowingFolia(Player player, boolean firstCall) {
        boolean hasHandle = taskHandles.containsKey(player.getUniqueId());
        ArmorStand stand = armorStands.get(player.getUniqueId());
        debug("scheduleFollowingFolia: hasHandle=" + hasHandle + ", stand=" + (stand != null ? stand.getEntityId() : "null") + ", isValid=" + (stand != null ? stand.isValid() : "N/A") + ", isSneaking=" + player.isSneaking() + ", firstCall=" + firstCall);
        if (!hasHandle) {
            debug("scheduleFollowingFolia: 无handle，停止递归");
            return;
        }
        if (stand == null || !stand.isValid()) {
            debug("scheduleFollowingFolia: stand无效，停止跟随");
            stopFollowing(player);
            return;
        }
        if (!firstCall && !player.isSneaking()) {
            debug("scheduleFollowingFolia: 玩家不再潜行，停止跟随");
            stopFollowing(player);
            return;
        }
        plugin.teleportEntity(stand, getStandLocation(player, 0.5));
        debug("scheduleFollowingFolia: 调度下一次");
        plugin.runLaterForEntity(stand, () -> scheduleFollowingFolia(player, false), 1L);
    }

    private void startFollowingFolia(Player player) {
        debug("startFollowingFolia: 开始");
        taskHandles.put(player.getUniqueId(), true);
        scheduleFollowingFolia(player, true);
    }

    private void stopFollowing(Player player) {
        debug("stopFollowing: 开始");
        Object handle = taskHandles.remove(player.getUniqueId());
        debug("stopFollowing: handle=" + handle);
        if (handle instanceof Integer taskId) {
            org.bukkit.Bukkit.getScheduler().cancelTask(taskId);
            debug("stopFollowing: 取消Bukkit任务");
        }
        ArmorStand stand = armorStands.remove(player.getUniqueId());
        debug("stopFollowing: stand=" + (stand != null ? stand.getEntityId() : "null"));
        if (stand != null) stand.remove();
    }

    private void playSounds(Player player, PlayerData data) {
        if (data.soundThorns()) {
            player.getWorld().playSound(player.getLocation(), "minecraft:enchant.thorns.hit", 1f, ThreadLocalRandom.current().nextFloat() * 0.4f + 0.8f);
        }
        if (data.soundSlime()) {
            player.getWorld().playSound(player.getLocation(), "minecraft:entity.slime.jump", 0.5f, ThreadLocalRandom.current().nextFloat() * 0.4f + 0.8f);
        }
    }

    private void spawnWaterParticles(Player player, Player partner) {
        Location playerLoc = player.getLocation();
        Location partnerLoc = partner.getLocation();
        double midX = (playerLoc.getX() + partnerLoc.getX()) / 2;
        double midY = (playerLoc.getY() + partnerLoc.getY()) / 2 - 0.5;
        double midZ = (playerLoc.getZ() + partnerLoc.getZ()) / 2;
        
        Location midLoc = new Location(player.getWorld(), midX, midY, midZ);
        debug("spawnWaterParticles: 中间位置=" + midLoc);
        
        player.getWorld().spawnParticle(Particle.SPLASH, midLoc, 15, 0.3, 0.1, 0.3, 0.05);
    }

    private void incrementSneakCount(Player player) {
        int min = plugin.getConfig().getInt("sneak-count.min", 10);
        int max = plugin.getConfig().getInt("sneak-count.max", 20);
        int target = min == max ? min : ThreadLocalRandom.current().nextInt(min, max + 1);
        
        int count = sneakCounts.getOrDefault(player.getUniqueId(), 0) + 1;
        if (count >= target) {
            sneakCounts.remove(player.getUniqueId());
            spawnEffects(player);
        } else {
            sneakCounts.put(player.getUniqueId(), count);
        }
    }

    private void spawnEffects(Player player) {
        Location loc = player.getLocation();
        player.getWorld().spawnParticle(Particle.FIREWORK, loc, 50, 1, 1, 1, 0.1);
        
        for (int i = 0; i < 5; i++) {
            Location dropLoc = loc.clone().add(ThreadLocalRandom.current().nextDouble(-1, 1), 0.5, ThreadLocalRandom.current().nextDouble(-1, 1));
            Item item = player.getWorld().dropItem(dropLoc, new ItemStack(Material.MILK_BUCKET));
            item.setPickupDelay(Integer.MAX_VALUE);
            item.setVelocity(item.getVelocity().multiply(1.5));
            plugin.runLaterForEntity(item, item::remove, 100L);
        }
    }

    public void cleanup() {
        taskHandles.forEach((uuid, handle) -> {
            if (handle instanceof Integer taskId) {
                org.bukkit.Bukkit.getScheduler().cancelTask(taskId);
            }
        });
        taskHandles.clear();
        armorStands.values().forEach(ArmorStand::remove);
        armorStands.clear();
    }
}