package org.chunkly.authme.config;

public record DatabaseConfig(
        boolean enabled,
        String url,
        String poolName,
        int maximumPoolSize,
        int minimumIdle,
        long connectionTimeoutMs,
        String tablePrefix
) {
}
