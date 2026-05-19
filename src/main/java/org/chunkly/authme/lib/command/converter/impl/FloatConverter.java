package org.chunkly.authme.lib.command.converter.impl;

import org.chunkly.authme.lib.command.converter.IConverter;
import net.md_5.bungee.api.CommandSender;

import java.util.Collections;
import java.util.List;

public class FloatConverter implements IConverter<Float> {
    @Override
    public Float fromString(String string, CommandSender sender) {
        try {
            return Float.parseFloat(string);
        } catch (NumberFormatException exception) {
            return -1f;
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender) {
        return Collections.emptyList();
    }
}
