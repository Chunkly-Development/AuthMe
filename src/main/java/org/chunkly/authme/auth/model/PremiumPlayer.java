package org.chunkly.authme.auth.model;

import org.chunkly.authme.auth.AuthType;

import java.util.UUID;

public final class PremiumPlayer extends AuthPlayer {
    public PremiumPlayer(UUID uniqueId, String name) {
        super(uniqueId, name, AuthType.PREMIUM);
    }
}
