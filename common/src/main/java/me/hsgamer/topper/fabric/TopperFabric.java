package me.hsgamer.topper.fabric;

import io.github.projectunified.craftconfig.configurate.ConfigurateConfig;
import io.github.projectunified.craftconfig.proxy.ConfigGenerator;
import me.hsgamer.topper.fabric.config.MainConfig;
import me.hsgamer.topper.fabric.config.MessageConfig;
import me.hsgamer.topper.fabric.manager.TaskManager;
import me.hsgamer.topper.fabric.manager.ValueProviderManager;
import me.hsgamer.topper.fabric.template.FabricStorageSupplierTemplate;
import me.hsgamer.topper.fabric.template.FabricTopTemplate;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.configurate.gson.GsonConfigurationLoader;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

public class TopperFabric implements ModInitializer {
    public static final String MOD_ID = "topper-fabric";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static TopperFabric instance;

    private final Path configFolder;
    private final Path dataFolder;
    private MainConfig mainConfig;
    private MessageConfig messageConfig;
    private ValueProviderManager valueProviderManager;
    private TaskManager taskManager;
    private FabricStorageSupplierTemplate storageSupplierTemplate;
    private FabricTopTemplate topTemplate;
    private TopperPlatform platform;

    public TopperFabric() {
        instance = this;
        configFolder = FabricLoader.getInstance().getConfigDir().resolve("topper");
        dataFolder = FabricLoader.getInstance().getGameDir().resolve("topper_data");
    }

    public static TopperFabric getInstance() {
        return instance;
    }

    private static void createFolder(Path folder) {
        if (!folder.toFile().exists()) {
            folder.toFile().mkdirs();
        }
    }

    private static TopperPlatform loadPlatform() {
        List<TopperPlatform> platforms = FabricLoader.getInstance()
                .getEntrypointContainers("topper", TopperPlatform.class)
                .stream()
                .map(EntrypointContainer::getEntrypoint)
                .toList();
        if (platforms.isEmpty()) {
            throw new IllegalStateException("No Topper module supports Minecraft " + FabricLoader.getInstance()
                    .getModContainer("minecraft")
                    .map(mod -> mod.getMetadata().getVersion().getFriendlyString())
                    .orElse("?"));
        }
        if (platforms.size() > 1) {
            LOGGER.warn("Multiple Topper modules are loaded, using the first one");
        }
        return platforms.get(0);
    }

    @Override
    public void onInitialize() {
        createFolder(configFolder);
        createFolder(dataFolder);

        mainConfig = ConfigGenerator.newInstance(MainConfig.class, new ConfigurateConfig(
                configFolder.resolve("config.json").toFile(),
                GsonConfigurationLoader.builder().indent(2)
        ));
        messageConfig = ConfigGenerator.newInstance(MessageConfig.class, new ConfigurateConfig(
                configFolder.resolve("messages.json").toFile(),
                GsonConfigurationLoader.builder().indent(2)
        ));

        valueProviderManager = new ValueProviderManager();
        taskManager = new TaskManager();
        storageSupplierTemplate = new FabricStorageSupplierTemplate(this);

        platform = loadPlatform();
        topTemplate = new FabricTopTemplate(this, platform);
        platform.initialize(this);
    }

    public void onServerStop() {
        taskManager.shutdown();
        topTemplate.disable();
    }

    public void reloadTemplate() {
        topTemplate.disable();
        mainConfig.reloadConfig();
        messageConfig.reloadConfig();
        topTemplate.enable();
    }

    public void onPlayerJoin(UUID uuid) {
        topTemplate.getTopManager().create(uuid);
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

    public MessageConfig getMessageConfig() {
        return messageConfig;
    }

    public ValueProviderManager getValueProviderManager() {
        return valueProviderManager;
    }

    public TaskManager getTaskManager() {
        return taskManager;
    }

    public FabricStorageSupplierTemplate getStorageSupplierTemplate() {
        return storageSupplierTemplate;
    }

    public FabricTopTemplate getTopTemplate() {
        return topTemplate;
    }

    public TopperPlatform getPlatform() {
        return platform;
    }
}
