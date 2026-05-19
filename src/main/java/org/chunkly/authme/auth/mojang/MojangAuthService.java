package org.chunkly.authme.auth.mojang;

import java.net.InetAddress;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public final class MojangAuthService {

    private final MojangClient mojangClient;

    public MojangAuthService(MojangClient mojangClient) {
        this.mojangClient = Objects.requireNonNull(mojangClient, "mojangClient");
    }

    public CompletableFuture<Boolean> isPremiumUsername(String username) {
        return mojangClient.findProfile(username).thenApply(Optional::isPresent);
    }

    public CompletableFuture<MojangOwnershipResult> verifyOwnership(String username, String serverId, InetAddress address) {
        return mojangClient.findProfile(username).thenCompose(profile -> {
            if (profile.isEmpty()) {
                return CompletableFuture.completedFuture(MojangOwnershipResult.noPremium());
            }

            return mojangClient.hasJoined(username, serverId, address)
                    .thenApply(joinedProfile -> joinedProfile
                            .filter(joined -> joined.uniqueId().equals(profile.get().uniqueId()))
                            .map(MojangOwnershipResult::verifiedOwner)
                            .orElseGet(() -> MojangOwnershipResult.premiumButNotOwner(profile.get())));
        });
    }
}
