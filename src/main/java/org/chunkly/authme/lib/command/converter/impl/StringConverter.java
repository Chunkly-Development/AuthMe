package org.chunkly.authme.lib.command.converter.impl;

import org.chunkly.authme.lib.command.converter.IConverter;
import net.md_5.bungee.api.CommandSender;

import java.util.Collections;
import java.util.List;

public class StringConverter implements IConverter<String> {
    @Override
    public String fromString(String string, CommandSender sender) {
        return string;
    }

    @Override
    public List<String> tabComplete(CommandSender sender) {
        return Collections.emptyList();
    }
}
