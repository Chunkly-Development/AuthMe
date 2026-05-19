package org.chunkly.authme.auth.storage;

import org.chunkly.authme.auth.AuthType;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public record AuthUser(
        UUID uniqueId,
        String name,
        String lowerName,
        AuthType type,
        Optional<String> passwordHash,
        Instant registeredAt,
        Optional<Instant> lastLoginAt,
        Optional<String> lastLoginIp
) {

    public AuthUser {
        Objects.requireNonNull(uniqueId, "uniqueId");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(lowerName, "lowerName");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(passwordHash, "passwordHash");
        Objects.requireNonNull(registeredAt, "registeredAt");
        Objects.requireNonNull(lastLoginAt, "lastLoginAt");
        Objects.requireNonNull(lastLoginIp, "lastLoginIp");
    }
}
