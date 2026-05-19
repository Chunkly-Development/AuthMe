package org.chunkly.authme.lib.command.data;

import lombok.Getter;
import org.chunkly.authme.lib.command.annotation.SubCommand;

import java.lang.reflect.Method;

@Getter
public class SubCommandData {

    private final Method method;

    private final Object object;

    private final SubCommand subCommand;

    public SubCommandData(Method method, Object object, SubCommand subCommand) {
        this.method = method;
        this.object = object;
        this.subCommand = subCommand;
    }
}
