package org.chunkly.authme.lib.command;

import lombok.Getter;
import lombok.SneakyThrows;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import org.chunkly.authme.lib.command.annotation.Optional;
import org.chunkly.authme.lib.command.annotation.Parameter;
import org.chunkly.authme.lib.command.converter.IConverter;
import org.chunkly.authme.lib.command.data.CommandData;
import org.chunkly.authme.lib.command.data.SubCommandData;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

@Getter
public class CustomCommand extends Command {

    private final CommandData commandData;
    private final CommandHandler commandHandler;

    public CustomCommand(CommandData commandData, CommandHandler commandHandler) {
        super(commandData.getCommand().label(), commandData.getCommand().permission(), commandData.getCommand().aliases());

        this.commandData = commandData;
        this.commandHandler = commandHandler;
    }

    @Override
    @SneakyThrows
    public void execute(CommandSender commandSender, String[] arguments) {
        String[] args;
        Method method;
        Object obj;
        String permission;
        boolean append;

        String subName = null;

        if (arguments.length >= 1 && !this.commandData.getSubCommands().isEmpty() && this.commandData.getSubCommands().stream().anyMatch(subCommand -> !subCommand.getSubCommand().label().isEmpty() && subCommand.getSubCommand().label().equalsIgnoreCase(arguments[0]) || Arrays.stream(subCommand.getSubCommand().aliases()).anyMatch(str -> str.equalsIgnoreCase(arguments[0])))) {
            args = Arrays.copyOfRange(arguments, 1, arguments.length);
            SubCommandData subCommandData = this.commandData.getSubCommands().stream().filter(subCommandData1 -> subCommandData1.getSubCommand().label().equalsIgnoreCase(arguments[0]) || Arrays.stream(subCommandData1.getSubCommand().aliases()).anyMatch(str -> str.equalsIgnoreCase(arguments[0]))).findFirst().orElse(null);
            if (subCommandData != null) {
                append = subCommandData.getSubCommand().appendStrings();
                obj = subCommandData.getObject();
                method = subCommandData.getMethod();
                subName = subCommandData.getSubCommand().label();
                permission = subCommandData.getSubCommand().permission();
            }

            else {
                obj = commandData.getObject();
                method = commandData.getMethod();
                permission = commandData.getCommand().permission();
                append = commandData.getCommand().appendStrings();
            }
        }

        else {
            obj = commandData.getObject();
            method = commandData.getMethod();
            args = arguments;
            permission = commandData.getCommand().permission();
            append = commandData.getCommand().appendStrings();
        }


        if (method.getParameters()[0].getType().equals(ProxiedPlayer.class) && !(commandSender instanceof ProxiedPlayer)) {
            commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&',"&cOnly players may execute this command"));
            return;
        }

        if (!permission.isEmpty() && !commandSender.hasPermission(permission)) {
            commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&',"&cNo permission."));
            return;
        }

        java.lang.reflect.Parameter[] parameters = Arrays.copyOfRange(method.getParameters(), 1, method.getParameters().length);

        Object[] objects = new Object[parameters.length];

        if (parameters.length > 0 && !parameters[0].getType().isArray()) {

            StringBuilder stringBuilder = new StringBuilder();

            for (int i = 0; i < parameters.length; i++) {
                java.lang.reflect.Parameter parameter = parameters[i];
                Optional optional = parameter.getAnnotation(Optional.class);

                if (i >= args.length && optional == null) {
                    if (subName == null) {
                        commandSender.sendMessage(ChatColor.RED + "Usage: /" + this.commandData.getCommand().label() + " " + formatUsage(parameters));
                    }

                    else {
                        commandSender.sendMessage(ChatColor.RED + "Usage: /" + this.commandData.getCommand().label() + " " + subName + " " + formatUsage(parameters));
                    }

                    return;
                }

                Object converted = null;

                IConverter<?> converter = commandHandler.getConverter(parameter.getType());
                if ((parameters.length == i + 1) && parameters[parameters.length - 1].getType().equals(String.class) && args.length >= i && append) {
                    String[] appendArgs = Arrays.copyOfRange(args, i, args.length);
                    for (String str : appendArgs) {
                        stringBuilder.append(str).append(" ");
                    }
                } else {
                    if (converter != null) {
                        converted = converter.fromString(i >= args.length ? optional.value().replace("self", commandSender.getName()) : args[i], commandSender);
                    } else {
                        throw new NullPointerException("No Converter Found");
                    }
                }
                objects[i] = stringBuilder.toString().isEmpty() ? converted : stringBuilder.toString().trim();
            }

            Object[] invocationArguments = new Object[objects.length + 1];
            invocationArguments[0] = method.getParameters()[0].getType().cast(commandSender);
            System.arraycopy(objects, 0, invocationArguments, 1, objects.length);
            method.invoke(obj, invocationArguments);
        }

        else if (parameters.length == 1 && parameters[0].getType().isArray()) {
            method.invoke(obj, commandSender, args);
        }

        else {
            method.invoke(obj, commandSender);
        }
    }

    private String formatUsage(java.lang.reflect.Parameter[] parameters) {
        return Arrays.stream(parameters)
                .map(parameter -> "<" + getParameterName(parameter) + ">")
                .collect(Collectors.joining(" "));
    }

    private String getParameterName(java.lang.reflect.Parameter parameter) {
        Parameter commandParameter = parameter.getAnnotation(Parameter.class);
        return commandParameter == null ? parameter.getName() : commandParameter.name();
    }

//    @Override
//    public List<String> onTabComplete(CommandSender commandSender, Command command, String label, String[] strings) {
//        String[] args;
//        Method method;
//        if (strings.length >= 1 && !this.commandData.getSubCommands().isEmpty() && this.commandData.getSubCommands().stream().anyMatch(subCommand -> !subCommand.getSubCommand().label().isEmpty() && subCommand.getSubCommand().label().equalsIgnoreCase(strings[1]) || Arrays.stream(subCommand.getSubCommand().aliases()).anyMatch(s -> s.equalsIgnoreCase(strings[1])))) {
//            args = Arrays.copyOfRange(strings, 1, strings.length);
//            SubCommandData subCommandData = this.commandData.getSubCommands().stream().filter(subCommandData1 -> subCommandData1.getSubCommand().label().equalsIgnoreCase(strings[1]) || Arrays.stream(subCommandData1.getSubCommand().aliases()).anyMatch(s -> s.equalsIgnoreCase(strings[1]))).findFirst().orElse(null);
//
//            if (subCommandData != null) {
//                method = subCommandData.getMethod();
//            } else {
//                method = commandData.getMethod();
//            }
//
//        } else {
//            method = commandData.getMethod();
//            args = strings;
//        }
//
//        IConverter<?> converter = method.getParameters().length > args.length ? this.commandHandler.getConverter(method.getParameters()[args.length].getType()) : this.commandHandler.getConverter(String.class);
//        return converter == null ? super.tabComplete(commandSender, label, args) : converter.tabComplete(commandSender);
//    }

}
