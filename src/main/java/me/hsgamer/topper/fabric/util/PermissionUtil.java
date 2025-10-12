package me.hsgamer.topper.fabric.util;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.UUID;

public interface PermissionUtil {
    static boolean hasOp(MinecraftServer server, UUID uuid) {
        ServerPlayerEntity player = server.getPlayerManager().getPlayer(uuid);
        if (player == null) return false;
        return player.getPermissionLevel() > 0;
    }

    static boolean hasPermission(MinecraftServer server, UUID uuid, String permission) {
        ServerPlayerEntity player = ProfileUtil.getPlayer(server, uuid);
        if (player == null) return false;
        if (!FabricLoader.getInstance().isModLoaded("fabric-permissions-api-v0")) return false;
        return Permissions.check(player, permission);
    }
}
