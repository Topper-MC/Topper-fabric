package me.hsgamer.topper.fabric.util;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.PlayerList;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;
import java.util.UUID;

public interface ProfileUtil {
    static String getName(GameProfile profile) {
        return profile.name();
    }

    static ServerPlayer getPlayer(MinecraftServer server, UUID uuid) {
        PlayerList playerManager = server.getPlayerList();
        return playerManager.getPlayer(uuid);
    }

    static String getOfflineName(MinecraftServer server, UUID uuid) {
        Optional<NameAndId> playerConfigEntryOptional = server.services().nameToIdCache().get(uuid);
        return playerConfigEntryOptional.map(NameAndId::name).orElse(null);
    }
}
