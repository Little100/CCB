package org.little100.cCB;

import java.io.File;
import java.sql.*;
import java.util.UUID;

public class DatabaseManager {
    private Connection connection;
    private final File dataFolder;

    public DatabaseManager(File dataFolder) {
        this.dataFolder = dataFolder;
    }

    public void init() throws SQLException {
        if (!dataFolder.exists()) dataFolder.mkdirs();
        connection = DriverManager.getConnection("jdbc:sqlite:" + new File(dataFolder, "data.db").getAbsolutePath());
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS players (uuid TEXT PRIMARY KEY, sex TEXT, enabled INTEGER DEFAULT 0, offsetX REAL DEFAULT 0, offsetY REAL DEFAULT -1, offsetZ REAL DEFAULT -0.7, alwaysOn INTEGER DEFAULT 0, soundThorns INTEGER DEFAULT 1, soundSlime INTEGER DEFAULT 1, sizeSmall INTEGER DEFAULT 1)");
            try { stmt.executeUpdate("ALTER TABLE players ADD COLUMN offsetX REAL DEFAULT 0"); } catch (SQLException ignored) {}
            try { stmt.executeUpdate("ALTER TABLE players ADD COLUMN offsetY REAL DEFAULT -1"); } catch (SQLException ignored) {}
            try { stmt.executeUpdate("ALTER TABLE players ADD COLUMN offsetZ REAL DEFAULT -0.7"); } catch (SQLException ignored) {}
            try { stmt.executeUpdate("ALTER TABLE players ADD COLUMN alwaysOn INTEGER DEFAULT 0"); } catch (SQLException ignored) {}
            try { stmt.executeUpdate("ALTER TABLE players ADD COLUMN soundThorns INTEGER DEFAULT 1"); } catch (SQLException ignored) {}
            try { stmt.executeUpdate("ALTER TABLE players ADD COLUMN soundSlime INTEGER DEFAULT 1"); } catch (SQLException ignored) {}
            try { stmt.executeUpdate("ALTER TABLE players ADD COLUMN sizeSmall INTEGER DEFAULT 1"); } catch (SQLException ignored) {}
        }
    }

    public void close() {
        try { if (connection != null) connection.close(); } catch (SQLException ignored) {}
    }

    public PlayerData getPlayer(UUID uuid) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT sex, enabled, offsetX, offsetY, offsetZ, alwaysOn, soundThorns, soundSlime, sizeSmall FROM players WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new PlayerData(uuid, rs.getString("sex"), rs.getInt("enabled") == 1,
                        rs.getDouble("offsetX"), rs.getDouble("offsetY"), rs.getDouble("offsetZ"),
                        rs.getInt("alwaysOn") == 1, rs.getInt("soundThorns") == 1, rs.getInt("soundSlime") == 1, rs.getInt("sizeSmall") == 1);
            }
        } catch (SQLException ignored) {}
        return new PlayerData(uuid, null, false, 0, -1, -0.75, false, true, true, true);
    }

    public void savePlayer(PlayerData data) {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT OR REPLACE INTO players (uuid, sex, enabled, offsetX, offsetY, offsetZ, alwaysOn, soundThorns, soundSlime, sizeSmall) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
            ps.setString(1, data.uuid().toString());
            ps.setString(2, data.sex());
            ps.setInt(3, data.enabled() ? 1 : 0);
            ps.setDouble(4, data.offsetX());
            ps.setDouble(5, data.offsetY());
            ps.setDouble(6, data.offsetZ());
            ps.setInt(7, data.alwaysOn() ? 1 : 0);
            ps.setInt(8, data.soundThorns() ? 1 : 0);
            ps.setInt(9, data.soundSlime() ? 1 : 0);
            ps.setInt(10, data.sizeSmall() ? 1 : 0);
            ps.executeUpdate();
        } catch (SQLException ignored) {}
    }
}