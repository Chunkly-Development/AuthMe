package org.chunkly.authme.lib.command.converter.impl;

import org.chunkly.authme.lib.command.converter.IConverter;
import net.md_5.bungee.api.CommandSender;

import java.util.Collections;
import java.util.List;

public class DoubleConverter implements IConverter<Double> {
    @Override
    public Double fromString(String string, CommandSender sender) {
        try {
            return Double.parseDouble(string);
        } catch (NumberFormatException exception) {
            return -1.0D;
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender) {
        return Collections.emptyList();
    }
}
