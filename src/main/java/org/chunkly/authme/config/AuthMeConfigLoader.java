package org.chunkly.authme.config;

import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import org.chunkly.authme.AuthMe;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public final class AuthMeConfigLoader {

    private final AuthMe plugin;

    public AuthMeConfigLoader(AuthMe plugin) {
        this.plugin = plugin;
    }

    public AuthMeConfig load() throws IOException {
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            Files.createDirectories(dataFolder.toPath());
        }

        File configFile = new File(dataFolder, "config.yml");
        if (!configFile.exists()) {
            saveDefaultConfig(configFile);
        }

        Configuration configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
        return new AuthMeConfig(loadDatabase(configuration.getSection("database")));
    }

    private DatabaseConfig loadDatabase(Configuration database) {
        return new DatabaseConfig(
                database.getBoolean("enabled", false),
                database.getString("url", database.getString("jdbc-url", "jdbc:mariadb://localhost:3306/authme?user=authme&password=change-me")),
                database.getString("pool-name", "AuthMe-Pool"),
                database.getInt("maximum-pool-size", 10),
                database.getInt("minimum-idle", 2),
                database.getLong("connection-timeout-ms", 5000L),
                database.getString("table-prefix", "authme_")
        );
    }

    private void saveDefaultConfig(File configFile) throws IOException {
        try (InputStream inputStream = plugin.getResourceAsStream("config.yml")) {
            if (inputStream == null) {
                throw new IOException("Default config.yml resource not found");
            }

            Files.copy(inputStream, configFile.toPath());
        }
    }
}
