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
    public static final Path CONFIG_FOLDER = FabricLoader.getInstance().getConfigDir().resolve("topper");
    public static final Path DATA_FOLDER = FabricLoader.getInstance().getGameDir().resolve("topper_data");
    public static final MainConfig MAIN_CONFIG = ConfigGenerator.newInstance(MainConfig.class, new ConfigurateConfig(
            CONFIG_FOLDER.resolve("config.json").toFile(),
            GsonConfigurationLoader.builder().indent(2)
    ));

    private FabricTopTemplate template;
    private MinecraftServer server;

    @Override
    public void onInitialize() {
        template = new FabricTopTemplate(this);
        ServerLifecycleEvents.SERVER_STARTING.register(this::onServerStart);
        ServerLifecycleEvents.SERVER_STOPPING.register(this::onServerStop);
    }

    public MinecraftServer getServer() {
        return server;
    }

    private void onServerStart(MinecraftServer server) {
        this.server = server;
        template.enable();
    }

    private void onServerStop(MinecraftServer server) {
        template.disable();
        this.server = null;
    }
}