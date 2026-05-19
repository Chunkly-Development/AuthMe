package org.chunkly.authme.auth.model;

import org.chunkly.authme.auth.AuthType;

import java.util.UUID;

public interface IPlayer {
    UUID getUniqueId();

    String getName();

    String getLowerName();

    AuthType getType();

    boolean isAuthenticated();

    void setAuthenticated(boolean authenticated);

    boolean hasSession();

    void setSession(boolean session);
}
