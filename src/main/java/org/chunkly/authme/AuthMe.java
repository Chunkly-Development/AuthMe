package org.chunkly.authme;

import lombok.Getter;
import net.md_5.bungee.api.plugin.Plugin;
import org.chunkly.authme.auth.command.AuthCommandService;
import org.chunkly.authme.auth.command.LoginCommand;
import org.chunkly.authme.auth.command.RegisterCommand;
import org.chunkly.authme.auth.listener.AuthListener;
import org.chunkly.authme.auth.manager.AuthManager;
import org.chunkly.authme.auth.mojang.HttpMojangClient;
import org.chunkly.authme.auth.mojang.MojangAuthService;
import org.chunkly.authme.auth.security.BCryptPasswordHasher;
import org.chunkly.authme.auth.storage.AuthStorage;
import org.chunkly.authme.auth.storage.NoopAuthStorage;
import org.chunkly.authme.auth.storage.jdbi.JdbiAuthStorage;
import org.chunkly.authme.config.AuthMeConfig;
import org.chunkly.authme.config.AuthMeConfigLoader;
import org.chunkly.authme.lib.command.CommandHandler;

import java.io.IOException;

@Getter
public final class AuthMe extends Plugin {

    private AuthManager authManager;
    private MojangAuthService mojangAuthService;
    private AuthStorage authStorage;
    private CommandHandler commandHandler;
    private AuthMeConfig config;

    @Override
    public void onEnable() {
        this.config = loadConfig();
        this.authManager = new AuthManager();
        this.mojangAuthService = new MojangAuthService(new HttpMojangClient());
        this.authStorage = createAuthStorage();
        this.authStorage.initialize().whenComplete((ignored, throwable) -> {
            if (throwable != null) {
                getLogger().severe("Could not initialize auth storage: " + throwable.getMessage());
                return;
            }

            getLogger().info("Auth storage initialized.");
        });

        getProxy().getPluginManager().registerListener(this, new AuthListener(this));
        registerCommands();
        getLogger().info("AuthMe enabled.");
    }

    @Override
    public void onDisable() {
        if (authStorage != null) {
            authStorage.close();
        }

        getLogger().info("AuthMe disabled.");
    }

    private void registerCommands() {
        AuthCommandService authCommandService = new AuthCommandService(this, new BCryptPasswordHasher());
        this.commandHandler = new CommandHandler(this);
        this.commandHandler.registerCommand(new LoginCommand(authCommandService));
        this.commandHandler.registerCommand(new RegisterCommand(authCommandService));
    }

    private AuthMeConfig loadConfig() {
        try {
            return new AuthMeConfigLoader(this).load();
        } catch (IOException exception) {
            throw new IllegalStateException("Could not load config.yml", exception);
        }
    }

    private AuthStorage createAuthStorage() {
        if (!config.database().enabled()) {
            getLogger().warning("Database storage is disabled. Auth data will not be persisted.");
            return new NoopAuthStorage();
        }

        return new JdbiAuthStorage(config.database(), getExecutorService());
    }
}
