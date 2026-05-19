package org.chunkly.authme.lib.command.converter.impl;

import org.chunkly.authme.lib.command.converter.IConverter;
import net.md_5.bungee.api.CommandSender;

import java.util.Collections;
import java.util.List;

public class ByteConverter implements IConverter<Byte> {

    @Override
    public Byte fromString(String string, CommandSender sender) {
        try {
            return Byte.parseByte(string);
        } catch (NumberFormatException exception) {
            return -1;
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender) {
        return Collections.emptyList();
    }

}
