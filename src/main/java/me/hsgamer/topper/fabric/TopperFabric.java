package me.hsgamer.topper.fabric;

import me.hsgamer.hscore.config.configurate.ConfigurateConfig;
import me.hsgamer.hscore.config.proxy.ConfigGenerator;
import me.hsgamer.topper.fabric.config.MainConfig;
import me.hsgamer.topper.fabric.template.FabricTopTemplate;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.configurate.gson.GsonConfigurationLoader;

import java.nio.file.Path;

public class TopperFabric implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger(TopperFabric.class);

    private FabricTopTemplate template;
    private MinecraftServer server;
    private Path configFolder;
    private Path dataFolder;
    private MainConfig mainConfig;

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

        template = new FabricTopTemplate(this);
        ServerLifecycleEvents.SERVER_STARTING.register(this::onServerStart);
        ServerLifecycleEvents.SERVER_STOPPING.register(this::onServerStop);
    }

    private void onServerStart(MinecraftServer server) {
        this.server = server;
        template.enable();
    }

    private void onServerStop(MinecraftServer server) {
        template.disable();
        this.server = null;
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
}