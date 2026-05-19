package org.chunkly.authme.auth.manager;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.chunkly.authme.auth.AuthType;
import org.chunkly.authme.auth.model.IPlayer;
import org.chunkly.authme.auth.model.NoPremiumPlayer;
import org.chunkly.authme.auth.model.PremiumPlayer;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class AuthManager {

    private final Map<UUID, IPlayer> playersByUniqueId = new ConcurrentHashMap<>();
    private final Map<String, UUID> uniqueIdsByLowerName = new ConcurrentHashMap<>();

    public IPlayer registerPlayer(ProxiedPlayer player, AuthType type) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(type, "type");

        IPlayer authPlayer = createPlayer(player.getUniqueId(), player.getName(), type);
        IPlayer previousPlayer = playersByUniqueId.put(player.getUniqueId(), authPlayer);

        if (previousPlayer != null) {
            uniqueIdsByLowerName.remove(previousPlayer.getLowerName());
        }

        uniqueIdsByLowerName.put(authPlayer.getLowerName(), authPlayer.getUniqueId());
        return authPlayer;
    }

    public Optional<IPlayer> getPlayer(UUID uniqueId) {
        Objects.requireNonNull(uniqueId, "uniqueId");
        return Optional.ofNullable(playersByUniqueId.get(uniqueId));
    }

    public Optional<IPlayer> getPlayer(String name) {
        Objects.requireNonNull(name, "name");

        UUID uniqueId = uniqueIdsByLowerName.get(name.toLowerCase(Locale.ROOT));
        if (uniqueId == null) {
            return Optional.empty();
        }

        return getPlayer(uniqueId);
    }

    public boolean isAuthenticated(ProxiedPlayer player) {
        Objects.requireNonNull(player, "player");
        return getPlayer(player.getUniqueId())
                .map(IPlayer::isAuthenticated)
                .orElse(false);
    }

    public void authenticate(UUID uniqueId) {
        getPlayer(uniqueId).ifPresent(player -> player.setAuthenticated(true));
    }

    public void deauthenticate(UUID uniqueId) {
        getPlayer(uniqueId).ifPresent(player -> player.setAuthenticated(false));
    }

    public void removePlayer(UUID uniqueId) {
        IPlayer removedPlayer = playersByUniqueId.remove(Objects.requireNonNull(uniqueId, "uniqueId"));
        if (removedPlayer != null) {
            uniqueIdsByLowerName.remove(removedPlayer.getLowerName());
        }
    }

    public int getOnlinePlayerCount() {
        return playersByUniqueId.size();
    }

    private IPlayer createPlayer(UUID uniqueId, String name, AuthType type) {
        return switch (type) {
            case PREMIUM -> new PremiumPlayer(uniqueId, name);
            case NO_PREMIUM -> new NoPremiumPlayer(uniqueId, name);
        };
    }
}
