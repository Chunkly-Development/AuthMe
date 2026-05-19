package org.chunkly.authme.auth.mojang;

import java.net.InetAddress;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface MojangClient {

    CompletableFuture<Optional<MojangProfile>> findProfile(String username);

    CompletableFuture<Optional<MojangProfile>> hasJoined(String username, String serverId, InetAddress address);
}
