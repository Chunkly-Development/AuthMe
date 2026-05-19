package org.chunkly.authme.lib.command.converter;

import net.md_5.bungee.api.CommandSender;

import java.util.List;

public interface IConverter<T> {

    T fromString(String string, CommandSender sender);

    List<String> tabComplete(CommandSender sender);

}
