package me.hsgamer.topper.fabric.template;

import me.hsgamer.topper.agent.core.Agent;
import me.hsgamer.topper.fabric.TopperFabric;
import me.hsgamer.topper.fabric.util.PermissionUtil;
import me.hsgamer.topper.fabric.util.ProfileUtil;
import me.hsgamer.topper.storage.core.DataStorage;
import me.hsgamer.topper.template.topplayernumber.TopPlayerNumberTemplate;
import me.hsgamer.topper.value.core.ValueProvider;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import static me.hsgamer.topper.fabric.util.ProfileUtil.getPlayer;

public class FabricTopTemplate extends TopPlayerNumberTemplate {
    private final TopperFabric mod;

    public FabricTopTemplate(TopperFabric mod) {
        super(new FabricTopTemplateSettings(mod));
        this.mod = mod;
    }

    @Override
    public Function<String, DataStorage<UUID, Double>> getStorageSupplier() {
        return mod.getStorageSupplierTemplate().getNumberStorageSupplier();
    }

    @Override
    public Optional<ValueProvider<UUID, Double>> createValueProvider(Map<String, Object> settings) {
        return mod.getValueProviderManager().build(settings);
    }

    @Override
    public boolean isOnline(UUID uuid) {
        return getPlayer(mod.getServer(), uuid) != null;
    }

    @Override
    public String getName(UUID uuid) {
        ServerPlayerEntity player = getPlayer(mod.getServer(), uuid);
        return player != null ? ProfileUtil.getName(player.getGameProfile()) : ProfileUtil.getOfflineName(mod.getServer(), uuid);
    }

    @Override
    public boolean hasPermission(UUID uuid, String permission) {
        return PermissionUtil.hasPermission(mod.getServer(), uuid, permission);
    }

    @Override
    public Agent createTaskAgent(Runnable runnable, boolean async, long delay) {
        return mod.getTaskManager().createTaskAgent(runnable, async, delay);
    }

    @Override
    public void logWarning(String message, @Nullable Throwable throwable) {
        TopperFabric.LOGGER.warn(message, throwable);
    }
}
