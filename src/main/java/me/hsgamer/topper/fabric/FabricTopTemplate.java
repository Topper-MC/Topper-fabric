package me.hsgamer.topper.fabric;

import me.hsgamer.topper.agent.core.Agent;
import me.hsgamer.topper.template.topplayernumber.TopPlayerNumberTemplate;
import me.hsgamer.topper.template.topplayernumber.storage.DataStorageSupplier;
import me.hsgamer.topper.value.core.ValueProvider;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class FabricTopTemplate extends TopPlayerNumberTemplate {
    public FabricTopTemplate(Settings settings) {
        super(settings);
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
        return false;
    }

    @Override
    public String getName(UUID uuid) {
        return "";
    }

    @Override
    public boolean hasPermission(UUID uuid, String permission) {
        return false;
    }

    @Override
    public Agent createTaskAgent(Runnable runnable, boolean async, long delay) {
        return null;
    }

    @Override
    public void logWarning(String message, @Nullable Throwable throwable) {

    }
}
