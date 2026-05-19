package org.chunkly.authme.lib.command.converter.impl;

import org.chunkly.authme.lib.command.converter.IConverter;
import net.md_5.bungee.api.CommandSender;

import java.util.Collections;
import java.util.List;

public class ShortConverter implements IConverter<Short> {
    @Override
    public Short fromString(String string, CommandSender sender) {
        try {
            return Short.parseShort(string);
        } catch (NumberFormatException exception) {
            return -1;
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender) {
        return Collections.emptyList();
    }
}
