package me.hsgamer.topper.fabric;

import me.hsgamer.hscore.config.configurate.ConfigurateConfig;
import me.hsgamer.hscore.config.proxy.ConfigGenerator;
import me.hsgamer.topper.fabric.config.MainConfig;
import me.hsgamer.topper.fabric.manager.TaskManager;
import me.hsgamer.topper.fabric.manager.ValueProviderManager;
import me.hsgamer.topper.fabric.template.FabricTopTemplate;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.configurate.gson.GsonConfigurationLoader;

import java.nio.file.Path;

public class TopperFabric implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger(TopperFabric.class);

    private MinecraftServer server;
    private Path configFolder;
    private Path dataFolder;

    private MainConfig mainConfig;

    private ValueProviderManager valueProviderManager;
    private TaskManager taskManager;

    private FabricTopTemplate template;

    @Override
    public void onInitialize() {
        configFolder = FabricLoader.getInstance().getConfigDir().resolve("topper");
        if (!configFolder.toFile().exists()) {
            configFolder.toFile().mkdirs();
        }
        dataFolder = FabricLoader.getInstance().getGameDir().resolve("topper_data");
        if (!dataFolder.toFile().exists()) {
            dataFolder.toFile().mkdirs();
        }

        mainConfig = ConfigGenerator.newInstance(MainConfig.class, new ConfigurateConfig(
                configFolder.resolve("config.json").toFile(),
                GsonConfigurationLoader.builder().indent(2)
        ));

        valueProviderManager = new ValueProviderManager(this);
        taskManager = new TaskManager();

        template = new FabricTopTemplate(this);

        ServerLifecycleEvents.SERVER_STARTING.register(this::onServerStart);
        ServerLifecycleEvents.SERVER_STOPPING.register(this::onServerStop);
        ServerPlayConnectionEvents.JOIN.register(this::onPlayerJoin);
        ServerTickEvents.START_SERVER_TICK.register(this::onServerTick);
    }

    private void onServerStart(MinecraftServer server) {
        this.server = server;
        template.enable();
    }

    private void onServerStop(MinecraftServer server) {
        template.disable();
        this.server = null;
    }

    private void onPlayerJoin(ServerPlayNetworkHandler handler, PacketSender sender, MinecraftServer server) {
        template.getTopManager().create(handler.getPlayer().getUuid());
    }

    private void onServerTick(MinecraftServer server) {
        taskManager.onTick();
    }

    public MinecraftServer getServer() {
        return server;
    }

    public Path getConfigFolder() {
        return configFolder;
    }

    public Path getDataFolder() {
        return dataFolder;
    }

    public MainConfig getMainConfig() {
        return mainConfig;
    }

    public ValueProviderManager getValueProviderManager() {
        return valueProviderManager;
    }

    public TaskManager getTaskManager() {
        return taskManager;
    }
}