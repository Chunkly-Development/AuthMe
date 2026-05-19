package org.chunkly.authme.auth.command;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.chunkly.authme.lib.command.annotation.Command;
import org.chunkly.authme.lib.command.annotation.Parameter;

public final class LoginCommand {

    private final AuthCommandService authCommandService;

    public LoginCommand(AuthCommandService authCommandService) {
        this.authCommandService = authCommandService;
    }

    @Command(label = "login", aliases = {"l"})
    public void login(ProxiedPlayer player, @Parameter(name = "password") String password) {
        authCommandService.login(player, password);
    }
}
