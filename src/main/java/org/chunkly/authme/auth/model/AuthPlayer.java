package org.chunkly.authme.auth.model;

import org.chunkly.authme.auth.AuthType;

import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public abstract class AuthPlayer implements IPlayer {
    private final UUID uniqueId;
    private final String name;
    private final String lowerName;
    private final AuthType type;

    private boolean authenticated;
    private boolean session;

    protected AuthPlayer(UUID uniqueId, String name, AuthType type) {
        this.uniqueId = Objects.requireNonNull(uniqueId, "uniqueId");
        this.name = Objects.requireNonNull(name, "name");
        this.lowerName = name.toLowerCase(Locale.ROOT);
        this.type = Objects.requireNonNull(type, "type");
        this.authenticated = false;
        this.session = false;
    }

    @Override
    public UUID getUniqueId() {
        return uniqueId;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getLowerName() {
        return lowerName;
    }

    @Override
    public AuthType getType() {
        return type;
    }

    @Override
    public boolean isAuthenticated() {
        return authenticated;
    }

    @Override
    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }

    @Override
    public boolean hasSession() {
        return session;
    }

    @Override
    public void setSession(boolean session) {
        this.session = session;
    }
}
