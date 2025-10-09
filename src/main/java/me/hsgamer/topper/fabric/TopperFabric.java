package me.hsgamer.topper.fabric;

import me.fzzyhmstrs.fzzy_config.api.ConfigApiJava;
import me.hsgamer.topper.fabric.config.DatabaseConfig;
import me.hsgamer.topper.fabric.config.MainConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TopperFabric implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger(TopperFabric.class);
    public static final MainConfig MAIN_CONFIG = ConfigApiJava.registerAndLoadConfig(MainConfig::new);
    public static final DatabaseConfig DATABASE_CONFIG = ConfigApiJava.registerAndLoadConfig(DatabaseConfig::new);

    private MinecraftServer server;

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTING.register(this::onServerStart);
        ServerLifecycleEvents.SERVER_STOPPING.register(this::onServerStop);
    }

    public MinecraftServer getServer() {
        return server;
    }

    private void onServerStart(MinecraftServer server) {
        this.server = server;
    }

    private void onServerStop(MinecraftServer server) {
        this.server = null;
    }
}