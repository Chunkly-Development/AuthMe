package org.chunkly.authme.auth.listener;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.chunkly.authme.auth.AuthType;
import org.chunkly.authme.AuthMe;
import org.chunkly.authme.auth.storage.AuthUser;

import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;

public record AuthListener(AuthMe plugin) implements Listener {

    @EventHandler
    public void preLogin(PreLoginEvent event) {
        event.registerIntent(plugin);
        plugin.getMojangAuthService()
                .isPremiumUsername(event.getConnection().getName())
                .whenComplete((premium, throwable) -> {
                    try {
                        if (throwable != null) {
                            plugin.getLogger().warning("Could not verify Mojang profile for " + event.getConnection().getName() + ": " + throwable.getMessage());
                            event.setCancelled(true);
                            event.setCancelReason(new TextComponent("Unable to verify your Minecraft account. Try again later."));
                            return;
                        }

                        event.getConnection().setOnlineMode(premium);
                    } finally {
                        event.completeIntent(plugin);
                    }
                });
    }

    @EventHandler
    public void postLogin(PostLoginEvent event) {
        ProxiedPlayer player = event.getPlayer();
        boolean premium = player.getPendingConnection().isOnlineMode();
        AuthType authType = premium ? AuthType.PREMIUM : AuthType.NO_PREMIUM;

        plugin.getAuthManager().registerPlayer(player, authType);

        if (premium) {
            plugin.getAuthManager().authenticate(player.getUniqueId());
            player.sendMessage(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', "&4&lSuccesfully authenticated &a&l✔")));
            persistPremiumLogin(player);
        }

        plugin.getLogger().info(player.getName() + " joined as " + authType + " authenticated=" + premium);
    }

    @EventHandler
    public void disconnect(PlayerDisconnectEvent event) {
        plugin.getAuthManager().removePlayer(event.getPlayer().getUniqueId());
    }

    private void persistPremiumLogin(ProxiedPlayer player) {
        String ipAddress = getIpAddress(player);
        Instant now = Instant.now();
        plugin.getAuthStorage().findUser(player.getName())
                .thenCompose(existingUser -> {
                    if (existingUser.isPresent()) {
                        return plugin.getAuthStorage().updateLastLogin(player.getName(), ipAddress, now);
                    }

                    AuthUser user = new AuthUser(
                            player.getUniqueId(),
                            player.getName(),
                            player.getName().toLowerCase(Locale.ROOT),
                            AuthType.PREMIUM,
                            Optional.empty(),
                            now,
                            Optional.of(now),
                            Optional.of(ipAddress)
                    );

                    return plugin.getAuthStorage().createUser(user);
                })
                .exceptionally(throwable -> {
                    plugin.getLogger().warning("Could not persist premium login for " + player.getName() + ": " + throwable.getMessage());
                    return null;
                });
    }

    private String getIpAddress(ProxiedPlayer player) {
        if (player.getSocketAddress() instanceof InetSocketAddress address) {
            return address.getAddress().getHostAddress();
        }

        return player.getAddress().getAddress().getHostAddress();
    }

}
