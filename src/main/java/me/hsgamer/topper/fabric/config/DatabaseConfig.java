package me.hsgamer.topper.fabric.config;

import me.fzzyhmstrs.fzzy_config.config.Config;
import me.fzzyhmstrs.fzzy_config.validation.collection.ValidatedStringMap;
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedAny;
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedString;
import me.hsgamer.topper.storage.sql.core.SqlDatabaseSetting;
import net.minecraft.util.Identifier;

import java.util.Map;

public class DatabaseConfig extends Config implements SqlDatabaseSetting {
    public String host = "localhost";
    public String port = "3306";
    public String username = "root";
    public String password = "";
    public String database = "topper";
    public boolean isSSL = false;
    public ValidatedStringMap<Object> driverProperties = new ValidatedStringMap<>(
            Map.of(),
            new ValidatedString(),
            new ValidatedAny<>("") // TODO: This may not be the way to do Any Object
    );
    public ValidatedStringMap<Object> clientProperties = new ValidatedStringMap<>(
            Map.of(),
            new ValidatedString(),
            new ValidatedAny<>("") // TODO: This may not be the way to do Any Object
    );

    public DatabaseConfig() {
        super(Identifier.of("topper", "database_config"));
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public String getPort() {
        return port;
    }

    @Override
    public String getDatabase() {
        return database;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isUseSSL() {
        return isSSL;
    }

    @Override
    public Map<String, Object> getDriverProperties() {
        return driverProperties;
    }

    @Override
    public Map<String, Object> getClientProperties() {
        return clientProperties;
    }
}
