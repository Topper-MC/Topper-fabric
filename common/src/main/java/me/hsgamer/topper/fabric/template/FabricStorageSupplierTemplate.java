package me.hsgamer.topper.fabric.template;

import me.hsgamer.hscore.database.client.sql.java.JavaSqlClient;
import me.hsgamer.topper.fabric.TopperFabric;
import me.hsgamer.topper.storage.core.DataStorage;
import me.hsgamer.topper.storage.flat.converter.NumberFlatValueConverter;
import me.hsgamer.topper.storage.flat.converter.UUIDFlatValueConverter;
import me.hsgamer.topper.storage.flat.core.FlatValueConverter;
import me.hsgamer.topper.storage.flat.properties.PropertiesDataStorage;
import me.hsgamer.topper.storage.sql.converter.NumberSqlValueConverter;
import me.hsgamer.topper.storage.sql.converter.UUIDSqlValueConverter;
import me.hsgamer.topper.storage.sql.core.SqlValueConverter;
import me.hsgamer.topper.storage.sql.mysql.MySqlDataStorageSupplier;
import me.hsgamer.topper.storage.sql.sqlite.NewSqliteDataStorageSupplier;
import me.hsgamer.topper.template.storagesupplier.StorageSupplierTemplate;
import me.hsgamer.topper.template.storagesupplier.storage.DataStorageSupplier;

import java.util.Locale;
import java.util.UUID;
import java.util.function.Function;

public class FabricStorageSupplierTemplate implements StorageSupplierTemplate {
    private final FabricDataStorageSupplierSettings storageSupplierSettings;

    public FabricStorageSupplierTemplate(TopperFabric mod) {
        storageSupplierSettings = new FabricDataStorageSupplierSettings(mod);
    }

    @Override
    public DataStorageSupplier getDataStorageSupplier(Settings settings) {
        String type = settings.storageType();
        switch (type.toLowerCase(Locale.ROOT)) {
            case "mysql" -> {
                try {
                    Class.forName("com.mysql.jdbc.Driver");
                    MySqlDataStorageSupplier mySqlDataStorageSupplier = new MySqlDataStorageSupplier(settings.databaseSetting(), JavaSqlClient::new);
                    return new DataStorageSupplier() {
                        @Override
                        public <K, V> DataStorage<K, V> getStorage(String name, FlatValueConverter<K> keyConverter, FlatValueConverter<V> valueConverter, SqlValueConverter<K> sqlKeyConverter, SqlValueConverter<V> sqlValueConverter) {
                            return mySqlDataStorageSupplier.getStorage(name, sqlKeyConverter, sqlValueConverter);
                        }
                    };
                } catch (ClassNotFoundException ex) {
                    TopperFabric.LOGGER.warn("""
                            You set your database to be MYSQL but no driver is found.
                            You must install a driver to use this database type.
                            Recommended link: https://modrinth.com/plugin/mysql-jdbc
                            """);
                }
            }
            case "sqlite" -> {
                try {
                    Class.forName("org.sqlite.JDBC");
                    NewSqliteDataStorageSupplier newSqliteDataStorageSupplier = new NewSqliteDataStorageSupplier(settings.baseFolder(), settings.databaseSetting(), JavaSqlClient::new);
                    return new DataStorageSupplier() {
                        @Override
                        public <K, V> DataStorage<K, V> getStorage(String name, FlatValueConverter<K> keyConverter, FlatValueConverter<V> valueConverter, SqlValueConverter<K> sqlKeyConverter, SqlValueConverter<V> sqlValueConverter) {
                            return newSqliteDataStorageSupplier.getStorage(name, sqlKeyConverter, sqlValueConverter);
                        }
                    };
                } catch (ClassNotFoundException ex) {
                    TopperFabric.LOGGER.warn("""
                            You set your database to be SQLITE but no driver is found.
                            You must install a driver to use this database type.
                            Recommended link: https://modrinth.com/plugin/sqlite-jdbc
                            """);
                }
            }
        }
        return new DataStorageSupplier() {
            @Override
            public <K, V> DataStorage<K, V> getStorage(String name, FlatValueConverter<K> keyConverter, FlatValueConverter<V> valueConverter, SqlValueConverter<K> sqlKeyConverter, SqlValueConverter<V> sqlValueConverter) {
                return new PropertiesDataStorage<>(settings.baseFolder(), name, keyConverter, valueConverter);
            }
        };
    }

    public Function<String, DataStorage<UUID, Double>> getNumberStorageSupplier() {
        return getDataStorageSupplier(storageSupplierSettings).getStorageSupplier(
                new UUIDFlatValueConverter(),
                new NumberFlatValueConverter<>(Number::doubleValue),
                new UUIDSqlValueConverter("uuid"),
                new NumberSqlValueConverter<>("value", true, Number::doubleValue)
        );
    }
}
