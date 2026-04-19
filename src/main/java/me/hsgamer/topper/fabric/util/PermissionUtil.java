package me.hsgamer.topper.fabric.util;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public interface PermissionUtil {
    static boolean hasPermissionLevel(CommandSourceStack commandSource, int level) {
        var permissionPredicate = commandSource.permissions();
        if (permissionPredicate instanceof LevelBasedPermissionSet leveledPermissionPredicate) {
            return leveledPermissionPredicate.level().id() >= level;
        }
        return level == 0;
    }

    static boolean hasPermission(SharedSuggestionProvider commandSource, String permission) {
        if (!FabricLoader.getInstance().isModLoaded("fabric-permissions-api-v0")) return false;
        return Permissions.check(commandSource, permission);
    }

    static boolean hasPermission(Entity entity, String permission) {
        if (!FabricLoader.getInstance().isModLoaded("fabric-permissions-api-v0")) return false;
        return Permissions.check(entity, permission);
    }

    static boolean hasPermission(MinecraftServer server, UUID uuid, String permission) {
        ServerPlayer player = ProfileUtil.getPlayer(server, uuid);
        if (player == null) return false;
        return hasPermission(player, permission);
    }
}
