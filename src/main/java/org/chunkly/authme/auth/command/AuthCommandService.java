package org.chunkly.authme.auth.command;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.chunkly.authme.AuthMe;
import org.chunkly.authme.auth.AuthType;
import org.chunkly.authme.auth.model.IPlayer;
import org.chunkly.authme.auth.security.PasswordHasher;
import org.chunkly.authme.auth.storage.AuthUser;

import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public final class AuthCommandService {

    private final AuthMe plugin;
    private final PasswordHasher passwordHasher;

    public AuthCommandService(AuthMe plugin, PasswordHasher passwordHasher) {
        this.plugin = plugin;
        this.passwordHasher = passwordHasher;
    }

    public void login(ProxiedPlayer player, String password) {
        plugin.getAuthStorage().findUser(player.getName())
                .thenAccept(user -> handleLoginResult(player, password, user))
                .exceptionally(throwable -> {
                    send(player, "&cCould not login right now. Try again later.");
                    plugin.getLogger().warning("Login failed for " + player.getName() + ": " + throwable.getMessage());
                    return null;
                });
    }

    public void register(ProxiedPlayer player, String password, String confirmation) {
        if (!password.equals(confirmation)) {
            send(player, "&cPasswords do not match.");
            return;
        }

        if (password.length() < 4) {
            send(player, "&cPassword must be at least 4 characters.");
            return;
        }

        Optional<IPlayer> authPlayer = plugin.getAuthManager().getPlayer(player.getUniqueId());
        if (authPlayer.map(IPlayer::getType).orElse(AuthType.NO_PREMIUM) == AuthType.PREMIUM) {
            send(player, "&cPremium players are already authenticated.");
            return;
        }

        plugin.getAuthStorage().findUser(player.getName())
                .thenCompose(existingUser -> {
                    if (existingUser.isPresent()) {
                        send(player, "&cThis account is already registered. Use /login <password>.");
                        return CompletableFuture.completedFuture(null);
                    }

                    Instant now = Instant.now();
                    AuthUser user = new AuthUser(
                            player.getUniqueId(),
                            player.getName(),
                            player.getName().toLowerCase(Locale.ROOT),
                            AuthType.NO_PREMIUM,
                            Optional.of(passwordHasher.hash(password)),
                            now,
                            Optional.of(now),
                            Optional.of(getIpAddress(player))
                    );

                    return plugin.getAuthStorage().createUser(user)
                            .thenRun(() -> {
                                plugin.getAuthManager().authenticate(player.getUniqueId());
                                send(player, "&aSuccessfully registered.");
                            });
                })
                .exceptionally(throwable -> {
                    send(player, "&cCould not register right now. Try again later.");
                    plugin.getLogger().warning("Register failed for " + player.getName() + ": " + throwable.getMessage());
                    return null;
                });
    }

    private void handleLoginResult(ProxiedPlayer player, String password, Optional<AuthUser> user) {
        if (plugin.getAuthManager().isAuthenticated(player)) {
            send(player, "&cYou are already authenticated.");
            return;
        }

        if (user.isEmpty()) {
            send(player, "&cThis account is not registered. Use /register <password> <password>.");
            return;
        }

        Optional<String> passwordHash = user.get().passwordHash();
        if (passwordHash.isEmpty()) {
            send(player, "&cThis account cannot use password login.");
            return;
        }

        if (!passwordHasher.verify(password, passwordHash.get())) {
            send(player, "&cInvalid password.");
            return;
        }

        plugin.getAuthStorage()
                .updateLastLogin(player.getName(), getIpAddress(player), Instant.now())
                .thenRun(() -> {
                    plugin.getAuthManager().authenticate(player.getUniqueId());
                    send(player, "&aSuccessfully logged in.");
                });
    }

    private String getIpAddress(ProxiedPlayer player) {
        if (player.getSocketAddress() instanceof InetSocketAddress address) {
            return address.getAddress().getHostAddress();
        }

        return player.getAddress().getAddress().getHostAddress();
    }

    private void send(ProxiedPlayer player, String message) {
        player.sendMessage(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', message)));
    }
}
