package org.chunkly.authme.lib.command.utils;

import lombok.experimental.UtilityClass;

import java.util.regex.Pattern;

@UtilityClass
public class UUIDUtils {

    private final Pattern PATTERN = Pattern.compile("([a-f0-9]{8}(-[a-f0-9]{4}){4}[a-f0-9]{8})");

    public boolean isUUID(String string) {
        return PATTERN.matcher(string).find();
    }

}
