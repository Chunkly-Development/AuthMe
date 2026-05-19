package org.chunkly.authme.lib.command;

import lombok.Getter;
import  net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import org.chunkly.authme.lib.command.annotation.Command;
import org.chunkly.authme.lib.command.annotation.SubCommand;
import org.chunkly.authme.lib.command.converter.IConverter;
import org.chunkly.authme.lib.command.converter.impl.BooleanConverter;
import org.chunkly.authme.lib.command.converter.impl.ByteConverter;
import org.chunkly.authme.lib.command.converter.impl.DoubleConverter;
import org.chunkly.authme.lib.command.converter.impl.FloatConverter;
import org.chunkly.authme.lib.command.converter.impl.IntegerConverter;
import org.chunkly.authme.lib.command.converter.impl.LongConverter;
import org.chunkly.authme.lib.command.converter.impl.ProxiedPlayerConverter;
import org.chunkly.authme.lib.command.converter.impl.ShortConverter;
import org.chunkly.authme.lib.command.converter.impl.StringConverter;
import org.chunkly.authme.lib.command.data.CommandData;
import org.chunkly.authme.lib.command.data.SubCommandData;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;


@Getter
public class CommandHandler {

    private final Map<Class<?>, IConverter<?>> converterMap = new HashMap<>();
    private final Map<String, CustomCommand> commands = new HashMap<>();

    private final Plugin instance;

    public CommandHandler(Plugin plugin) {
        this.instance = plugin;
        this.registerConverter(Boolean.class, new BooleanConverter());
        this.registerConverter(boolean.class, new BooleanConverter());
        this.registerConverter(Byte.class, new ByteConverter());
        this.registerConverter(byte.class, new ByteConverter());
        this.registerConverter(Double.class, new DoubleConverter());
        this.registerConverter(double.class, new DoubleConverter());
        this.registerConverter(Float.class, new FloatConverter());
        this.registerConverter(float.class, new FloatConverter());
        this.registerConverter(Integer.class, new IntegerConverter());
        this.registerConverter(int.class, new IntegerConverter());
        this.registerConverter(Long.class, new LongConverter());
        this.registerConverter(long.class, new LongConverter());
        this.registerConverter(Short.class, new ShortConverter());
        this.registerConverter(short.class, new ShortConverter());
        this.registerConverter(String.class, new StringConverter());
        this.registerConverter(ProxiedPlayer.class, new ProxiedPlayerConverter());


    }

    public void registerCommand(Object object) {
        List<Method> commandMethods = Arrays.stream(object.getClass().getMethods()).filter(method -> method.isAnnotationPresent(Command.class)).collect(Collectors.toList());
        List<Method> subCommandMethods = Arrays.stream(object.getClass().getMethods()).filter(method -> method.isAnnotationPresent(SubCommand.class)).collect(Collectors.toList());

        for (Method method : commandMethods) {
            CommandData commandData = new CommandData(object, method, method.getAnnotation(Command.class));
            CustomCommand customCommand = new CustomCommand(commandData, this);
            this.commands.put(commandData.getCommand().label().toLowerCase(), customCommand);
            instance.getProxy().getPluginManager().registerCommand(instance, customCommand);
        }

        for (Method method : subCommandMethods) {
            SubCommandData subCommandData = new SubCommandData(method, object, method.getAnnotation(SubCommand.class));
            String parentLabel = subCommandData.getSubCommand().parent().toLowerCase();
            if (this.commands.containsKey(parentLabel)) {
                CustomCommand command = this.commands.get(parentLabel);

                if (command.getCommandData().getSubCommands().contains(subCommandData)) {
                    continue;
                }

                command.getCommandData().getSubCommands().add(subCommandData);
            }
        }
    }

    public IConverter<?> getConverter(Class<?> clazz) {
        return this.converterMap.getOrDefault(clazz, null);
    }

    public void registerConverter(Class<?> clazz, IConverter<?> converter) {
        this.converterMap.put(clazz, converter);
    }

}
