package me.hsgamer.topper.fabric.template;

import com.mojang.authlib.GameProfile;
import me.hsgamer.hscore.database.client.sql.java.JavaSqlClient;
import me.hsgamer.topper.agent.core.Agent;
import me.hsgamer.topper.fabric.TopperFabric;
import me.hsgamer.topper.storage.core.DataStorage;
import me.hsgamer.topper.storage.flat.core.FlatValueConverter;
import me.hsgamer.topper.storage.flat.properties.PropertiesDataStorage;
import me.hsgamer.topper.storage.sql.core.SqlValueConverter;
import me.hsgamer.topper.storage.sql.mysql.MySqlDataStorageSupplier;
import me.hsgamer.topper.storage.sql.sqlite.NewSqliteDataStorageSupplier;
import me.hsgamer.topper.template.topplayernumber.TopPlayerNumberTemplate;
import me.hsgamer.topper.template.topplayernumber.storage.DataStorageSupplier;
import me.hsgamer.topper.value.core.ValueProvider;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class FabricTopTemplate extends TopPlayerNumberTemplate {
    private final TopperFabric mod;

    public FabricTopTemplate(TopperFabric mod) {
        super(new FabricTopTemplateSettings(mod));
        this.mod = mod;
    }

    @Override
    public DataStorageSupplier getDataStorageSupplier(String type, DataStorageSupplier.Settings setting) {
        if (type.toLowerCase(Locale.ROOT).equals("mysql")) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                MySqlDataStorageSupplier mySqlDataStorageSupplier = new MySqlDataStorageSupplier(setting.databaseSetting(), JavaSqlClient::new);
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
                NewSqliteDataStorageSupplier newSqliteDataStorageSupplier = new NewSqliteDataStorageSupplier(setting.baseFolder(), setting.databaseSetting(), JavaSqlClient::new);
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
                return new PropertiesDataStorage<>(setting.baseFolder(), name, keyConverter, valueConverter);
            }
        };
    }

    @Override
    public Optional<ValueProvider<UUID, Double>> createValueProvider(Map<String, Object> settings) {
        return mod.getValueProviderManager().build(settings);
    }

    private ServerPlayerEntity getPlayer(UUID uuid) {
        PlayerManager playerManager = mod.getServer().getPlayerManager();
        if (playerManager == null) return null;
        return playerManager.getPlayer(uuid);
    }

    @Override
    public boolean isOnline(UUID uuid) {
        return getPlayer(uuid) != null;
    }

    @Override
    public String getName(UUID uuid) {
        ServerPlayerEntity player = getPlayer(uuid);
        return player != null ? getName(player.getGameProfile()) : getOfflineName(uuid);
    }

    private String getName(GameProfile profile) {
        //? if >= 1.21.9 {
        return profile.name();
         //?} else {
        /*return profile.getName();
        *///?}
    }

    private String getOfflineName(UUID uuid) {
        //? if >= 1.21.9 {
        Optional<net.minecraft.server.PlayerConfigEntry> playerConfigEntryOptional = mod.getServer().getApiServices().nameToIdCache().getByUuid(uuid);
        return playerConfigEntryOptional.map(net.minecraft.server.PlayerConfigEntry::name).orElse(null);
        //?} else {
        /*return mod.getServer().getUserCache().getByUuid(uuid).map(GameProfile::getName).orElse(null);
        *///?}
    }

    @Override
    public boolean hasPermission(UUID uuid, String permission) {
        ServerPlayerEntity player = getPlayer(uuid);
        if (player == null) return false;
        if (!FabricLoader.getInstance().isModLoaded("fabric-permissions-api-v0")) return false;
        return Permissions.check(player, permission);
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
