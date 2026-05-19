package org.chunkly.authme.lib.command.converter.impl;

import org.chunkly.authme.lib.command.converter.IConverter;
import net.md_5.bungee.api.CommandSender;

import java.util.Collections;
import java.util.List;

public class LongConverter implements IConverter<Long> {
    @Override
    public Long fromString(String string, CommandSender sender) {
        try {
            return Long.parseLong(string);
        } catch (NumberFormatException exception) {
            return -1L;
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender) {
        return Collections.emptyList();
    }
}
