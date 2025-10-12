package me.hsgamer.topper.fabric.template;

import me.hsgamer.hscore.database.client.sql.java.JavaSqlClient;
import me.hsgamer.topper.fabric.TopperFabric;
import me.hsgamer.topper.storage.core.DataStorage;
import me.hsgamer.topper.storage.flat.core.FlatValueConverter;
import me.hsgamer.topper.storage.flat.properties.PropertiesDataStorage;
import me.hsgamer.topper.storage.sql.core.SqlValueConverter;
import me.hsgamer.topper.storage.sql.mysql.MySqlDataStorageSupplier;
import me.hsgamer.topper.storage.sql.sqlite.NewSqliteDataStorageSupplier;
import me.hsgamer.topper.template.storagesupplier.StorageSupplierTemplate;
import me.hsgamer.topper.template.storagesupplier.storage.DataStorageSupplier;

import java.util.Locale;

public class FabricStorageSupplierTemplate implements StorageSupplierTemplate {
    @Override
    public DataStorageSupplier getDataStorageSupplier(Settings settings) {
        String type = settings.storageType();
        if (type.toLowerCase(Locale.ROOT).equals("mysql")) {
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
        } else if (type.toLowerCase(Locale.ROOT).equals("sqlite")) {
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
        return new DataStorageSupplier() {
            @Override
            public <K, V> DataStorage<K, V> getStorage(String name, FlatValueConverter<K> keyConverter, FlatValueConverter<V> valueConverter, SqlValueConverter<K> sqlKeyConverter, SqlValueConverter<V> sqlValueConverter) {
                return new PropertiesDataStorage<>(settings.baseFolder(), name, keyConverter, valueConverter);
            }
        };
    }
}
