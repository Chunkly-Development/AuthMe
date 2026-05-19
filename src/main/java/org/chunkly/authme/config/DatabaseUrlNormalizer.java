package org.chunkly.authme.config;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public final class DatabaseUrlNormalizer {

    private DatabaseUrlNormalizer() {
    }

    public static String normalize(String url) {
        if (url.startsWith("jdbc:")) {
            return url;
        }

        if (url.startsWith("mariadb://")) {
            return normalizeMariaDbUri(url);
        }

        throw new IllegalArgumentException("Unsupported database URL: " + url);
    }

    private static String normalizeMariaDbUri(String url) {
        URI uri = URI.create(url);
        String userInfo = uri.getUserInfo();
        if (userInfo == null || userInfo.isBlank()) {
            return "jdbc:" + url;
        }

        String[] credentials = userInfo.split(":", 2);
        String username = credentials[0];
        String password = credentials.length == 2 ? credentials[1] : "";
        String host = uri.getHost();
        int port = uri.getPort();
        String path = uri.getRawPath() == null ? "" : uri.getRawPath();
        String query = uri.getRawQuery();

        StringBuilder jdbcUrl = new StringBuilder("jdbc:mariadb://")
                .append(host);

        if (port > 0) {
            jdbcUrl.append(":").append(port);
        }

        jdbcUrl.append(path)
                .append(query == null || query.isBlank() ? "?" : "?" + query + "&")
                .append("user=").append(encode(username))
                .append("&password=").append(encode(password));

        return jdbcUrl.toString();
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
