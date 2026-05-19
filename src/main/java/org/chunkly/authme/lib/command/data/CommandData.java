package org.chunkly.authme.lib.command.data;

import lombok.Getter;
import org.chunkly.authme.lib.command.annotation.Command;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@Getter
public class CommandData {

    private final Object object;

    private final Method method;

    private final Command command;

    private final List<SubCommandData> subCommands;

    public CommandData(Object object, Method method, Command command) {
        this.object = object;
        this.method = method;
        this.command = command;
        this.subCommands = new ArrayList<>();
    }
}
