package me.hsgamer.topper.fabric.util;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Optional;
import java.util.UUID;

public interface ProfileUtil {
    static String getName(GameProfile profile) {
        //? if >= 1.21.9 {
        return profile.name();
        //?} else {
        /*return profile.getName();
         *///?}
    }

    static ServerPlayerEntity getPlayer(MinecraftServer server, UUID uuid) {
        PlayerManager playerManager = server.getPlayerManager();
        if (playerManager == null) return null;
        return playerManager.getPlayer(uuid);
    }

    static String getOfflineName(MinecraftServer server, UUID uuid) {
        //? if >= 1.21.9 {
        Optional<net.minecraft.server.PlayerConfigEntry> playerConfigEntryOptional = server.getApiServices().nameToIdCache().getByUuid(uuid);
        return playerConfigEntryOptional.map(net.minecraft.server.PlayerConfigEntry::name).orElse(null);
        //?} else {
        /*return mod.getServer().getUserCache().getByUuid(uuid).map(GameProfile::getName).orElse(null);
         *///?}
    }
}
