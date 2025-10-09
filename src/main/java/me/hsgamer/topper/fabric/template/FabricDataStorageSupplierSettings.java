package me.hsgamer.topper.fabric.template;

import me.hsgamer.hscore.config.configurate.ConfigurateConfig;
import me.hsgamer.topper.fabric.TopperFabric;
import me.hsgamer.topper.storage.sql.config.SqlDatabaseConfig;
import me.hsgamer.topper.storage.sql.core.SqlDatabaseSetting;
import me.hsgamer.topper.template.topplayernumber.storage.DataStorageSupplier;
import org.spongepowered.configurate.gson.GsonConfigurationLoader;

import java.io.File;

public record FabricDataStorageSupplierSettings(TopperFabric mod) implements DataStorageSupplier.Settings {
    @Override
    public SqlDatabaseSetting databaseSetting() {
        return new SqlDatabaseConfig("topper", new ConfigurateConfig(
                TopperFabric.CONFIG_FOLDER.resolve("config.json").toFile(),
                GsonConfigurationLoader.builder().indent(2)
        ));
    }

    @Override
    public File baseFolder() {
        File folder = TopperFabric.DATA_FOLDER.toFile();
        if (!folder.exists()) {
            folder.mkdirs();
        }
        return folder;
    }
}
