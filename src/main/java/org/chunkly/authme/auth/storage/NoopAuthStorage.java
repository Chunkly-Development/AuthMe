package org.chunkly.authme.auth.storage;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class NoopAuthStorage implements AuthStorage {

    @Override
    public CompletableFuture<Void> initialize() {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Optional<AuthUser>> findUser(String username) {
        return CompletableFuture.completedFuture(Optional.empty());
    }

    @Override
    public CompletableFuture<Void> createUser(AuthUser user) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> updateLastLogin(String username, String ipAddress, Instant lastLoginAt) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> createSession(String username, String ipAddress, Instant expiresAt) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Boolean> hasValidSession(String username, String ipAddress, Instant now) {
        return CompletableFuture.completedFuture(false);
    }

    @Override
    public CompletableFuture<Void> deleteSessions(UUID uniqueId) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void close() {
    }
}
