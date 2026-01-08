package org.little100.cCB;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class CCB extends JavaPlugin {
    private DatabaseManager db;
    private PlayerListener listener;
    private static boolean isFolia;

    static {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            isFolia = true;
        } catch (ClassNotFoundException e) {
            isFolia = false;
        }
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        db = new DatabaseManager(getDataFolder());
        try {
            db.init();
        } catch (Exception e) {
            getLogger().severe("数据库初始化失败: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        listener = new PlayerListener(this);
        getServer().getPluginManager().registerEvents(listener, this);
        CCBCommand cmd = new CCBCommand(this);
        getCommand("ccb").setExecutor(cmd);
        getCommand("ccb").setTabCompleter(cmd);
        getLogger().info("CCB 插件已启用");
    }

    @Override
    public void onDisable() {
        if (listener != null) listener.cleanup();
        if (db != null) db.close();
        getLogger().info("CCB 插件已禁用");
    }

    public DatabaseManager getDb() {
        return db;
    }

    public void runLaterForEntity(org.bukkit.entity.Entity entity, Runnable task, long delayTicks) {
        if (isFolia) {
            try {
                Object scheduler = entity.getClass().getMethod("getScheduler").invoke(entity);
                scheduler.getClass().getMethod("runDelayed", org.bukkit.plugin.Plugin.class, java.util.function.Consumer.class, Runnable.class, long.class)
                    .invoke(scheduler, this, (java.util.function.Consumer<Object>) t -> task.run(), null, delayTicks);
            } catch (Exception e) {
                Bukkit.getScheduler().runTaskLater(this, task, delayTicks);
            }
        } else {
            Bukkit.getScheduler().runTaskLater(this, task, delayTicks);
        }
    }

    public void teleportEntity(org.bukkit.entity.Entity entity, org.bukkit.Location loc) {
        if (isFolia) {
            try {
                entity.getClass().getMethod("teleportAsync", org.bukkit.Location.class).invoke(entity, loc);
            } catch (Exception e) {
                entity.teleport(loc);
            }
        } else {
            entity.teleport(loc);
        }
    }

    public static boolean isFolia() {
        return isFolia;
    }
}