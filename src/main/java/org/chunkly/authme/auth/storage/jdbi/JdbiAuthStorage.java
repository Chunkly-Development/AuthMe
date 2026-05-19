package org.chunkly.authme.auth.storage.jdbi;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.chunkly.authme.auth.AuthType;
import org.chunkly.authme.auth.storage.AuthStorage;
import org.chunkly.authme.auth.storage.AuthUser;
import org.chunkly.authme.config.DatabaseConfig;
import org.chunkly.authme.config.DatabaseUrlNormalizer;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;

import java.time.Instant;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public final class JdbiAuthStorage implements AuthStorage {

    private final HikariDataSource dataSource;
    private final Jdbi jdbi;
    private final Executor executor;
    private final String usersTable;
    private final String sessionsTable;

    public JdbiAuthStorage(DatabaseConfig config, Executor executor) {
        this.executor = executor;
        this.usersTable = tableName(config.tablePrefix(), "users");
        this.sessionsTable = tableName(config.tablePrefix(), "sessions");
        this.dataSource = new HikariDataSource(createHikariConfig(config));
        this.jdbi = Jdbi.create(dataSource);
    }

    @Override
    public CompletableFuture<Void> initialize() {
        return runAsync(() -> jdbi.useHandle(handle -> {
            handle.execute("""
                    CREATE TABLE IF NOT EXISTS %s (
                        unique_id CHAR(36) NOT NULL PRIMARY KEY,
                        name VARCHAR(16) NOT NULL,
                        lower_name VARCHAR(16) NOT NULL UNIQUE,
                        auth_type VARCHAR(16) NOT NULL,
                        password_hash VARCHAR(255) NULL,
                        registered_at BIGINT NOT NULL,
                        last_login_at BIGINT NULL,
                        last_login_ip VARCHAR(45) NULL
                    )
                    """.formatted(usersTable));

            handle.execute("""
                    CREATE TABLE IF NOT EXISTS %s (
                        id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                        unique_id CHAR(36) NOT NULL,
                        ip_address VARCHAR(45) NOT NULL,
                        expires_at BIGINT NOT NULL,
                        created_at BIGINT NOT NULL,
                        INDEX idx_authme_sessions_user_ip (unique_id, ip_address),
                        INDEX idx_authme_sessions_expires_at (expires_at),
                        CONSTRAINT fk_authme_sessions_user
                            FOREIGN KEY (unique_id)
                            REFERENCES %s(unique_id)
                            ON DELETE CASCADE
                    )
                    """.formatted(sessionsTable, usersTable));
        }));
    }

    @Override
    public CompletableFuture<Optional<AuthUser>> findUser(String username) {
        String lowerName = normalizeUsername(username);
        return supplyAsync(() -> jdbi.withHandle(handle -> handle
                .createQuery("""
                        SELECT unique_id, name, lower_name, auth_type, password_hash, registered_at, last_login_at, last_login_ip
                        FROM %s
                        WHERE lower_name = :lower_name
                        """.formatted(usersTable))
                .bind("lower_name", lowerName)
                .map((rs, ctx) -> new AuthUser(
                        UUID.fromString(rs.getString("unique_id")),
                        rs.getString("name"),
                        rs.getString("lower_name"),
                        AuthType.valueOf(rs.getString("auth_type")),
                        Optional.ofNullable(rs.getString("password_hash")),
                        Instant.ofEpochMilli(rs.getLong("registered_at")),
                        optionalInstant(rs.getObject("last_login_at")),
                        Optional.ofNullable(rs.getString("last_login_ip"))
                ))
                .findOne()));
    }

    @Override
    public CompletableFuture<Void> createUser(AuthUser user) {
        return runAsync(() -> jdbi.useHandle(handle -> handle
                .createUpdate("""
                        INSERT INTO %s (unique_id, name, lower_name, auth_type, password_hash, registered_at, last_login_at, last_login_ip)
                        VALUES (:unique_id, :name, :lower_name, :auth_type, :password_hash, :registered_at, :last_login_at, :last_login_ip)
                        """.formatted(usersTable))
                .bind("unique_id", user.uniqueId().toString())
                .bind("name", user.name())
                .bind("lower_name", user.lowerName())
                .bind("auth_type", user.type().name())
                .bind("password_hash", user.passwordHash().orElse(null))
                .bind("registered_at", user.registeredAt().toEpochMilli())
                .bind("last_login_at", user.lastLoginAt().map(Instant::toEpochMilli).orElse(null))
                .bind("last_login_ip", user.lastLoginIp().orElse(null))
                .execute()));
    }

    @Override
    public CompletableFuture<Void> updateLastLogin(String username, String ipAddress, Instant lastLoginAt) {
        String lowerName = normalizeUsername(username);
        return runAsync(() -> jdbi.useHandle(handle -> handle
                .createUpdate("""
                        UPDATE %s
                        SET last_login_at = :last_login_at, last_login_ip = :last_login_ip
                        WHERE lower_name = :lower_name
                        """.formatted(usersTable))
                .bind("last_login_at", lastLoginAt.toEpochMilli())
                .bind("last_login_ip", ipAddress)
                .bind("lower_name", lowerName)
                .execute()));
    }

    @Override
    public CompletableFuture<Void> createSession(String username, String ipAddress, Instant expiresAt) {
        String lowerName = normalizeUsername(username);
        long now = Instant.now().toEpochMilli();
        return runAsync(() -> jdbi.useTransaction(handle -> {
            String uniqueId = handle
                    .createQuery("SELECT unique_id FROM %s WHERE lower_name = :lower_name".formatted(usersTable))
                    .bind("lower_name", lowerName)
                    .mapTo(String.class)
                    .findOne()
                    .orElseThrow(() -> new UnableToExecuteStatementException("Cannot create session for unknown user " + username));

            handle.createUpdate("""
                            INSERT INTO %s (unique_id, ip_address, expires_at, created_at)
                            VALUES (:unique_id, :ip_address, :expires_at, :created_at)
                            """.formatted(sessionsTable))
                    .bind("unique_id", uniqueId)
                    .bind("ip_address", ipAddress)
                    .bind("expires_at", expiresAt.toEpochMilli())
                    .bind("created_at", now)
                    .execute();
        }));
    }

    @Override
    public CompletableFuture<Boolean> hasValidSession(String username, String ipAddress, Instant now) {
        String lowerName = normalizeUsername(username);
        return supplyAsync(() -> jdbi.withHandle(handle -> handle
                .createQuery("""
                        SELECT COUNT(*)
                        FROM %s sessions
                        INNER JOIN %s users ON users.unique_id = sessions.unique_id
                        WHERE users.lower_name = :lower_name
                          AND sessions.ip_address = :ip_address
                          AND sessions.expires_at > :now
                        """.formatted(sessionsTable, usersTable))
                .bind("lower_name", lowerName)
                .bind("ip_address", ipAddress)
                .bind("now", now.toEpochMilli())
                .mapTo(Integer.class)
                .one() > 0));
    }

    @Override
    public CompletableFuture<Void> deleteSessions(UUID uniqueId) {
        return runAsync(() -> jdbi.useHandle(handle -> handle
                .createUpdate("DELETE FROM %s WHERE unique_id = :unique_id".formatted(sessionsTable))
                .bind("unique_id", uniqueId.toString())
                .execute()));
    }

    @Override
    public void close() {
        dataSource.close();
    }

    private CompletableFuture<Void> runAsync(Runnable runnable) {
        return CompletableFuture.runAsync(runnable, executor);
    }

    private <T> CompletableFuture<T> supplyAsync(java.util.function.Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, executor);
    }

    private HikariConfig createHikariConfig(DatabaseConfig config) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName("org.mariadb.jdbc.Driver");
        hikariConfig.setJdbcUrl(DatabaseUrlNormalizer.normalize(config.url()));
        hikariConfig.setPoolName(config.poolName());
        hikariConfig.setMaximumPoolSize(config.maximumPoolSize());
        hikariConfig.setMinimumIdle(config.minimumIdle());
        hikariConfig.setConnectionTimeout(config.connectionTimeoutMs());
        return hikariConfig;
    }

    private String normalizeUsername(String username) {
        return username.toLowerCase(Locale.ROOT);
    }

    private String tableName(String prefix, String name) {
        String tableName = prefix + name;
        if (!tableName.matches("[A-Za-z0-9_]+")) {
            throw new IllegalArgumentException("Invalid table name: " + tableName);
        }

        return tableName;
    }

    private Optional<Instant> optionalInstant(Object value) {
        if (value == null) {
            return Optional.empty();
        }

        return Optional.of(Instant.ofEpochMilli(((Number) value).longValue()));
    }
}
