package org.chunkly.authme.auth.storage;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface AuthStorage extends AutoCloseable {

    CompletableFuture<Void> initialize();

    CompletableFuture<Optional<AuthUser>> findUser(String username);

    CompletableFuture<Void> createUser(AuthUser user);

    CompletableFuture<Void> updateLastLogin(String username, String ipAddress, Instant lastLoginAt);

    CompletableFuture<Void> createSession(String username, String ipAddress, Instant expiresAt);

    CompletableFuture<Boolean> hasValidSession(String username, String ipAddress, Instant now);

    CompletableFuture<Void> deleteSessions(UUID uniqueId);

    @Override
    void close();
}
