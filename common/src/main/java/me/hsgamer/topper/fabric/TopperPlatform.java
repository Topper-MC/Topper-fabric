package me.hsgamer.topper.fabric;

import java.util.UUID;

public interface TopperPlatform {

    void initialize(TopperFabric mod);

    String getPlayerName(UUID uuid);

    boolean hasPermission(UUID uuid, String permission);
}
