package org.little100.cCB;

import java.util.UUID;

public record PlayerData(UUID uuid, String sex, boolean enabled, double offsetX, double offsetY, double offsetZ, boolean alwaysOn, boolean soundThorns, boolean soundSlime, boolean sizeSmall) {
    public PlayerData(UUID uuid, String sex, boolean enabled) {
        this(uuid, sex, enabled, 0.0, -1.0, -0.75, false, true, true, true);
    }
    public PlayerData(UUID uuid, String sex, boolean enabled, double offsetX, double offsetY, double offsetZ) {
        this(uuid, sex, enabled, offsetX, offsetY, offsetZ, false, true, true, true);
    }
    public PlayerData(UUID uuid, String sex, boolean enabled, double offsetX, double offsetY, double offsetZ, boolean alwaysOn) {
        this(uuid, sex, enabled, offsetX, offsetY, offsetZ, alwaysOn, true, true, true);
    }
    public PlayerData(UUID uuid, String sex, boolean enabled, double offsetX, double offsetY, double offsetZ, boolean alwaysOn, boolean soundThorns, boolean soundSlime) {
        this(uuid, sex, enabled, offsetX, offsetY, offsetZ, alwaysOn, soundThorns, soundSlime, true);
    }
}