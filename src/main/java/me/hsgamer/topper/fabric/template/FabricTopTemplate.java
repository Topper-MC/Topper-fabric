package me.hsgamer.topper.fabric.template;

import me.hsgamer.topper.agent.core.Agent;
import me.hsgamer.topper.fabric.TopperFabric;
import me.hsgamer.topper.template.topplayernumber.TopPlayerNumberTemplate;
import me.hsgamer.topper.template.topplayernumber.storage.DataStorageSupplier;
import me.hsgamer.topper.value.core.ValueProvider;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class FabricTopTemplate extends TopPlayerNumberTemplate {
    private final TopperFabric mod;

    public FabricTopTemplate(TopperFabric mod) {
        super(null);
        this.mod = mod;
    }

    @Override
    public DataStorageSupplier getDataStorageSupplier(String type, DataStorageSupplier.Settings setting) {
        return null;
    }

    @Override
    public Optional<ValueProvider<UUID, Double>> createValueProvider(Map<String, Object> settings) {
        return Optional.empty();
    }

    @Override
    public boolean isOnline(UUID uuid) {
        return mod.getServer().getPlayerManager().getPlayer(uuid) != null;
    }

    @Override
    public String getName(UUID uuid) {
        // TODO: Figure out a way to get offline player name
        return "";
    }

    @Override
    public boolean hasPermission(UUID uuid, String permission) {
        ServerPlayerEntity player = mod.getServer().getPlayerManager().getPlayer(uuid);
        if (player == null) return false;
        // TODO: Figure out a way to check permission string of a player
        return false;
    }

    @Override
    public Agent createTaskAgent(Runnable runnable, boolean async, long delay) {
        // TODO: Seriously, Where the hell is the server scheduler?
        return null;
    }

    @Override
    public void logWarning(String message, @Nullable Throwable throwable) {
        TopperFabric.LOGGER.warn(message, throwable);
    }
}
