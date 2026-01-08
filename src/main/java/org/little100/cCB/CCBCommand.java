package org.little100.cCB;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CCBCommand implements CommandExecutor, TabCompleter {
    private final CCB plugin;

    public CCBCommand(CCB plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§c只有玩家可以使用此命令");
            return true;
        }
        if (args.length == 0) {
            player.sendMessage("§e用法: /ccb <sex|on|off>");
            return true;
        }
        PlayerData data = plugin.getDb().getPlayer(player.getUniqueId());
        switch (args[0].toLowerCase()) {
            case "sex" -> {
                if (args.length < 2) {
                    player.sendMessage("§e用法: /ccb sex <female|male>");
                    return true;
                }
                String sex = args[1].toLowerCase();
                if (!sex.equals("female") && !sex.equals("male")) {
                    player.sendMessage("§c请选择 female 或 male");
                    return true;
                }
                plugin.getDb().savePlayer(new PlayerData(player.getUniqueId(), sex, data.enabled(), data.offsetX(), data.offsetY(), data.offsetZ(), data.alwaysOn(), data.soundThorns(), data.soundSlime()));
                player.sendMessage("§a性别已设置为: " + sex);
            }
            case "on" -> {
                if (data.sex() == null) {
                    player.sendMessage("§c请先使用 /ccb sex <female|male> 选择性别");
                    return true;
                }
                plugin.getDb().savePlayer(new PlayerData(player.getUniqueId(), data.sex(), true, data.offsetX(), data.offsetY(), data.offsetZ(), data.alwaysOn(), data.soundThorns(), data.soundSlime()));
                player.sendMessage("§a功能已开启");
            }
            case "off" -> {
                plugin.getDb().savePlayer(new PlayerData(player.getUniqueId(), data.sex(), false, data.offsetX(), data.offsetY(), data.offsetZ(), data.alwaysOn(), data.soundThorns(), data.soundSlime()));
                player.sendMessage("§a功能已关闭");
            }
            case "offset" -> {
                if (args.length < 4) {
                    player.sendMessage("§e用法: /ccb offset <x> <y> <z>");
                    player.sendMessage("§7当前偏移: X=" + data.offsetX() + " Y=" + data.offsetY() + " Z=" + data.offsetZ());
                    return true;
                }
                try {
                    double x = Double.parseDouble(args[1]);
                    double y = Double.parseDouble(args[2]);
                    double z = Double.parseDouble(args[3]);
                    plugin.getDb().savePlayer(new PlayerData(player.getUniqueId(), data.sex(), data.enabled(), x, y, z, data.alwaysOn(), data.soundThorns(), data.soundSlime()));
                    player.sendMessage("§a偏移已设置: X=" + x + " Y=" + y + " Z=" + z);
                } catch (NumberFormatException e) {
                    player.sendMessage("§c请输入有效的数字");
                }
            }
            case "alwayson" -> {
                if (!player.hasPermission("ccb.alwayson")) {
                    player.sendMessage("§c你没有权限使用此命令");
                    return true;
                }
                boolean newState = !data.alwaysOn();
                plugin.getDb().savePlayer(new PlayerData(player.getUniqueId(), data.sex(), data.enabled(), data.offsetX(), data.offsetY(), data.offsetZ(), newState, data.soundThorns(), data.soundSlime()));
                player.sendMessage(newState ? "§a始终显示已开启" : "§a始终显示已关闭");
            }
            case "sound" -> {
                if (args.length < 2) {
                    player.sendMessage("§e用法: /ccb sound <thorns|slime>");
                    player.sendMessage("§7当前状态: 荆棘=" + (data.soundThorns() ? "开" : "关") + ", 史莱姆=" + (data.soundSlime() ? "开" : "关"));
                    return true;
                }
                switch (args[1].toLowerCase()) {
                    case "thorns" -> {
                        boolean newState = !data.soundThorns();
                        plugin.getDb().savePlayer(new PlayerData(player.getUniqueId(), data.sex(), data.enabled(), data.offsetX(), data.offsetY(), data.offsetZ(), data.alwaysOn(), newState, data.soundSlime()));
                        player.sendMessage(newState ? "§a荆棘声音已开启" : "§a荆棘声音已关闭");
                    }
                    case "slime" -> {
                        boolean newState = !data.soundSlime();
                        plugin.getDb().savePlayer(new PlayerData(player.getUniqueId(), data.sex(), data.enabled(), data.offsetX(), data.offsetY(), data.offsetZ(), data.alwaysOn(), data.soundThorns(), newState));
                        player.sendMessage(newState ? "§a史莱姆声音已开启" : "§a史莱姆声音已关闭");
                    }
                    default -> player.sendMessage("§e用法: /ccb sound <thorns|slime>");
                }
            }
            case "size" -> {
                if (args.length < 2) {
                    player.sendMessage("§e用法: /ccb size <small|large>");
                    player.sendMessage("§7当前大小: " + (data.sizeSmall() ? "small" : "large"));
                    return true;
                }
                switch (args[1].toLowerCase()) {
                    case "small" -> {
                        plugin.getDb().savePlayer(new PlayerData(player.getUniqueId(), data.sex(), data.enabled(), data.offsetX(), data.offsetY(), data.offsetZ(), data.alwaysOn(), data.soundThorns(), data.soundSlime(), true));
                        player.sendMessage("§a大小已设置为: small");
                    }
                    case "large" -> {
                        plugin.getDb().savePlayer(new PlayerData(player.getUniqueId(), data.sex(), data.enabled(), data.offsetX(), data.offsetY(), data.offsetZ(), data.alwaysOn(), data.soundThorns(), data.soundSlime(), false));
                        player.sendMessage("§a大小已设置为: large");
                    }
                    default -> player.sendMessage("§e用法: /ccb size <small|large>");
                }
            }
            default -> player.sendMessage("§e用法: /ccb <sex|on|off|offset|sound|size|alwayson>");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            java.util.List<String> list = new java.util.ArrayList<>(Arrays.asList("sex", "on", "off", "offset", "sound", "size"));
            if (sender.hasPermission("ccb.alwayson")) list.add("alwayson");
            return list;
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("sex")) return Arrays.asList("female", "male");
        if (args.length == 2 && args[0].equalsIgnoreCase("sound")) return Arrays.asList("thorns", "slime");
        if (args.length == 2 && args[0].equalsIgnoreCase("size")) return Arrays.asList("small", "large");
        if (args.length >= 2 && args.length <= 4 && args[0].equalsIgnoreCase("offset")) return Arrays.asList("0", "0.5", "-0.5", "1", "-1");
        return Collections.emptyList();
    }
}