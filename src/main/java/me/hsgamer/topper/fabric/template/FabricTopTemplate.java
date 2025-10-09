package me.hsgamer.topper.fabric.template;

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
        return switch (type.toLowerCase(Locale.ROOT)) {
            case "mysql" -> {
                MySqlDataStorageSupplier mySqlDataStorageSupplier = new MySqlDataStorageSupplier(setting.databaseSetting(), JavaSqlClient::new);
                yield new DataStorageSupplier() {
                    @Override
                    public <K, V> DataStorage<K, V> getStorage(String name, FlatValueConverter<K> keyConverter, FlatValueConverter<V> valueConverter, SqlValueConverter<K> sqlKeyConverter, SqlValueConverter<V> sqlValueConverter) {
                        return mySqlDataStorageSupplier.getStorage(name, sqlKeyConverter, sqlValueConverter);
                    }
                };
            }
            case "sqlite" -> {
                NewSqliteDataStorageSupplier newSqliteDataStorageSupplier = new NewSqliteDataStorageSupplier(setting.baseFolder(), setting.databaseSetting(), JavaSqlClient::new);
                yield new DataStorageSupplier() {
                    @Override
                    public <K, V> DataStorage<K, V> getStorage(String name, FlatValueConverter<K> keyConverter, FlatValueConverter<V> valueConverter, SqlValueConverter<K> sqlKeyConverter, SqlValueConverter<V> sqlValueConverter) {
                        return newSqliteDataStorageSupplier.getStorage(name, sqlKeyConverter, sqlValueConverter);
                    }
                };
            }
            default -> new DataStorageSupplier() {
                @Override
                public <K, V> DataStorage<K, V> getStorage(String name, FlatValueConverter<K> keyConverter, FlatValueConverter<V> valueConverter, SqlValueConverter<K> sqlKeyConverter, SqlValueConverter<V> sqlValueConverter) {
                    return new PropertiesDataStorage<>(setting.baseFolder(), name, keyConverter, valueConverter);
                }
            };
        };
    }

    @Override
    public Optional<ValueProvider<UUID, Double>> createValueProvider(Map<String, Object> settings) {
        return Optional.empty();
    }

    @Override
    public boolean isOnline(UUID uuid) {
        return mod.getServer().getPlayerManager().getPlayer(uuid) != null;
    }

    @Override
    public String getName(UUID uuid) {
        // TODO: Figure out a way to get offline player name
        return "";
    }

    @Override
    public boolean hasPermission(UUID uuid, String permission) {
        ServerPlayerEntity player = mod.getServer().getPlayerManager().getPlayer(uuid);
        if (player == null) return false;
        // TODO: Figure out a way to check permission string of a player
        return false;
    }

    @Override
    public Agent createTaskAgent(Runnable runnable, boolean async, long delay) {
        // TODO: Seriously, Where the hell is the server scheduler?
        return null;
    }

    @Override
    public void logWarning(String message, @Nullable Throwable throwable) {
        TopperFabric.LOGGER.warn(message, throwable);
    }
}
