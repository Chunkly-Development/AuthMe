package org.chunkly.authme.auth.command;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.chunkly.authme.lib.command.annotation.Command;
import org.chunkly.authme.lib.command.annotation.Parameter;

public final class RegisterCommand {

    private final AuthCommandService authCommandService;

    public RegisterCommand(AuthCommandService authCommandService) {
        this.authCommandService = authCommandService;
    }

    @Command(label = "register", aliases = {"reg"})
    public void register(
            ProxiedPlayer player,
            @Parameter(name = "password") String password,
            @Parameter(name = "confirmPassword") String confirmation
    ) {
        authCommandService.register(player, password, confirmation);
    }
}
