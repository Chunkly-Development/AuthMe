package org.chunkly.authme.auth.mojang;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class MojangProfileParser {

    private static final Pattern ID_PATTERN = Pattern.compile("\"id\"\\s*:\\s*\"([0-9a-fA-F]{32})\"");
    private static final Pattern NAME_PATTERN = Pattern.compile("\"name\"\\s*:\\s*\"([A-Za-z0-9_]{3,16})\"");

    private MojangProfileParser() {
    }

    static MojangProfile parse(String json) {
        String id = findRequired(ID_PATTERN, json, "id");
        String name = findRequired(NAME_PATTERN, json, "name");
        return new MojangProfile(parseUndashedUuid(id), name);
    }

    private static String findRequired(Pattern pattern, String json, String fieldName) {
        Matcher matcher = pattern.matcher(json);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Mojang response does not contain profile " + fieldName);
        }

        return matcher.group(1);
    }

    private static UUID parseUndashedUuid(String value) {
        return UUID.fromString(value.substring(0, 8)
                + "-"
                + value.substring(8, 12)
                + "-"
                + value.substring(12, 16)
                + "-"
                + value.substring(16, 20)
                + "-"
                + value.substring(20));
    }
}
