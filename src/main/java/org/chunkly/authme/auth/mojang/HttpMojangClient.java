package org.chunkly.authme.auth.mojang;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

public final class HttpMojangClient implements MojangClient {

    private static final URI PROFILE_ENDPOINT = URI.create("https://api.mojang.com/users/profiles/minecraft/");
    private static final URI SESSION_ENDPOINT = URI.create("https://sessionserver.mojang.com/session/minecraft/hasJoined");
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z0-9_]{3,16}$");
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(5);

    private final HttpClient httpClient;

    public HttpMojangClient() {
        this(HttpClient.newBuilder()
                .connectTimeout(REQUEST_TIMEOUT)
                .build());
    }

    public HttpMojangClient(HttpClient httpClient) {
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient");
    }

    @Override
    public CompletableFuture<Optional<MojangProfile>> findProfile(String username) {
        String normalizedUsername = normalizeUsername(username);
        URI uri = PROFILE_ENDPOINT.resolve(encode(normalizedUsername));
        return sendProfileRequest(uri);
    }

    @Override
    public CompletableFuture<Optional<MojangProfile>> hasJoined(String username, String serverId, InetAddress address) {
        String normalizedUsername = normalizeUsername(username);
        Objects.requireNonNull(serverId, "serverId");

        StringBuilder query = new StringBuilder()
                .append("?username=").append(encode(normalizedUsername))
                .append("&serverId=").append(encode(serverId));

        if (address != null) {
            query.append("&ip=").append(encode(address.getHostAddress()));
        }

        return sendProfileRequest(URI.create(SESSION_ENDPOINT + query.toString()));
    }

    private CompletableFuture<Optional<MojangProfile>> sendProfileRequest(URI uri) {
        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(REQUEST_TIMEOUT)
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
                .thenApply(response -> parseProfileResponse(uri, response));
    }

    private Optional<MojangProfile> parseProfileResponse(URI uri, HttpResponse<String> response) {
        int statusCode = response.statusCode();
        if (statusCode == 200) {
            return Optional.of(MojangProfileParser.parse(response.body()));
        }

        if (statusCode == 204 || statusCode == 404) {
            return Optional.empty();
        }

        throw new IllegalStateException(new IOException("Mojang request failed with HTTP " + statusCode + ": " + uri));
    }

    private String normalizeUsername(String username) {
        Objects.requireNonNull(username, "username");
        String normalizedUsername = username.trim();
        if (!USERNAME_PATTERN.matcher(normalizedUsername).matches()) {
            throw new IllegalArgumentException("Invalid Minecraft username: " + username);
        }

        return normalizedUsername.toLowerCase(Locale.ROOT);
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
