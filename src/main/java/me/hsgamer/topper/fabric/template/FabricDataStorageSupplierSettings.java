package me.hsgamer.topper.fabric.template;

import me.hsgamer.topper.fabric.TopperFabric;
import me.hsgamer.topper.storage.sql.core.SqlDatabaseSetting;
import me.hsgamer.topper.template.topplayernumber.storage.DataStorageSupplier;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;

public record FabricDataStorageSupplierSettings(TopperFabric mod) implements DataStorageSupplier.Settings {
    @Override
    public SqlDatabaseSetting databaseSetting() {
        return TopperFabric.DATABASE_CONFIG;
    }

    @Override
    public File baseFolder() {
        File folder = FabricLoader.getInstance().getGameDir().resolve("topper_data").toFile();
        if (!folder.exists()) {
            folder.mkdirs();
        }
        return folder;
    }
}
