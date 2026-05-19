package org.chunkly.authme.auth.security;

import at.favre.lib.crypto.bcrypt.BCrypt;

import java.nio.charset.StandardCharsets;

public final class BCryptPasswordHasher implements PasswordHasher {

    private static final int COST = 12;

    @Override
    public String hash(String password) {
        return BCrypt.withDefaults().hashToString(COST, password.toCharArray());
    }

    @Override
    public boolean verify(String password, String hash) {
        return BCrypt.verifyer()
                .verify(password.getBytes(StandardCharsets.UTF_8), hash.getBytes(StandardCharsets.UTF_8))
                .verified;
    }
}
