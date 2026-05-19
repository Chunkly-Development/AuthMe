package org.chunkly.authme.lib.command.converter.impl;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.chunkly.authme.lib.command.converter.IConverter;
import net.md_5.bungee.api.CommandSender;
import org.chunkly.authme.lib.command.utils.UUIDUtils;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ProxiedPlayerConverter implements IConverter<ProxiedPlayer> {

    @Override
    public ProxiedPlayer fromString(String string, CommandSender sender) {
        if (UUIDUtils.isUUID(string)) {
            return ProxyServer.getInstance().getPlayer(UUID.fromString(string));
        }

        return ProxyServer.getInstance().getPlayer(string);
    }

    @Override
    public List<String> tabComplete(CommandSender sender) {
        return ProxyServer.getInstance().getPlayers().stream().map(ProxiedPlayer::getName).collect(Collectors.toList());
    }
}
