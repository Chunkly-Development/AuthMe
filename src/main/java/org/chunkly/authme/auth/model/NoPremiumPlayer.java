package org.chunkly.authme.auth.model;

import org.chunkly.authme.auth.AuthType;

import java.util.UUID;

public final class NoPremiumPlayer extends AuthPlayer {
    public NoPremiumPlayer(UUID uniqueId, String name) {
        super(uniqueId, name, AuthType.NO_PREMIUM);
    }
}
