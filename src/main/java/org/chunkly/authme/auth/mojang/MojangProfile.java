package org.chunkly.authme.auth.mojang;

import java.util.Objects;
import java.util.UUID;

public record MojangProfile(UUID uniqueId, String name) {

    public MojangProfile {
        Objects.requireNonNull(uniqueId, "uniqueId");
        Objects.requireNonNull(name, "name");
    }
}
