package me.hsgamer.topper.fabric.util;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.UUID;

public interface PermissionUtil {
    static boolean hasPermissionLevel(ServerCommandSource commandSource, int level) {
        //? if >= 1.21.11 {
        var permissionPredicate = commandSource.getPermissions();
        if (permissionPredicate instanceof net.minecraft.command.permission.LeveledPermissionPredicate leveledPermissionPredicate) {
            return leveledPermissionPredicate.getLevel().getLevel() >= level;
        }
        return level == 0;
        //?} else {
        /*return commandSource.hasPermissionLevel(level);
        *///?}
    }

    static boolean hasPermission(CommandSource commandSource, String permission) {
        if (!FabricLoader.getInstance().isModLoaded("fabric-permissions-api-v0")) return false;
        return Permissions.check(commandSource, permission);
    }

    static boolean hasPermission(Entity entity, String permission) {
        if (!FabricLoader.getInstance().isModLoaded("fabric-permissions-api-v0")) return false;
        return Permissions.check(entity, permission);
    }

    static boolean hasPermission(MinecraftServer server, UUID uuid, String permission) {
        ServerPlayerEntity player = ProfileUtil.getPlayer(server, uuid);
        if (player == null) return false;
        return hasPermission(player, permission);
    }
}
