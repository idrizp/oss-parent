package me.idriz.oss.commands.processor;

import me.idriz.oss.commands.BaseCommand;
import me.idriz.oss.commands.Command;
import me.idriz.oss.commands.CommandAdapter;
import me.idriz.oss.commands.CommandRegistrar;
import me.idriz.oss.commands.adapter.EnumCommandAdapter;
import me.idriz.oss.commands.annotation.Argument;
import me.idriz.oss.commands.annotation.ConsoleOnly;
import me.idriz.oss.commands.annotation.OptionalArgument;
import me.idriz.oss.commands.annotation.PlayerOnly;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;

public class CommandProcessor {

    public static char colorChar = '&';
    public static String playerOnlyMessage = ChatColor.RED + "This command can only be ran by a player.";
    public static String noPermissionMessage = ChatColor.RED + "No permission.";
    public static String consoleOnlyMessage = ChatColor.RED + "This command can only be ran by the console.";

    private final BaseCommand<?> command;
    private final Parameter[] parameters;
    private final Set<Parameter> nonOptionalParameters;
    private final Map<BaseCommand<?>, CommandProcessor> subCommandProcessors = new HashMap<>();
    private final Plugin plugin;

    public CommandProcessor(Plugin plugin, BaseCommand<?> command) {
        this.plugin = plugin;
        this.command = command;
        this.parameters = command.getMethod().getParameters();

        this.nonOptionalParameters = Arrays.stream(parameters)
                .filter(parameter -> !parameter.isAnnotationPresent(OptionalArgument.class))
                .collect(Collectors.toSet());

        command.getSubCommands().forEach(subCommand -> {
            this.subCommandProcessors.put(subCommand, new CommandProcessor(plugin, subCommand));
        });
    }

    private static String[] translate(String[] input) {
        for (int i = 0; i < input.length; i++) {
            input[i] = ChatColor.translateAlternateColorCodes(colorChar, input[i]);
        }
        return input;
    }

    private void sync(Runnable runnable) {
        plugin.getServer().getScheduler().runTask(plugin, runnable);
    }

    private void invokeMethod(Object object, Object... parameters) {
        try {
            command.getMethod().invoke(object, parameters);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void invoke(boolean async, Object object, Object... parameters) {
        if (!async) sync(() -> invokeMethod(object, parameters));
        else invokeMethod(object, parameters);
    }

    private CommandAdapter<?> getAdapter(Parameter parameter) {
        CommandAdapter<?> adapter = null;
        if (parameter.isAnnotationPresent(Argument.class)) {
            Argument argument = parameter.getAnnotation(Argument.class);
            if (argument.adapter() != CommandAdapter.class) {
                adapter = CommandRegistrar.getByAdapterClass(argument.adapter());
                if (adapter == null)
                    throw new NullPointerException("Couldn't find adapter by class " + argument.adapter());
            }
        }
        if (adapter == null) adapter = CommandRegistrar.getByClass(parameter.getType());
        if (adapter == null && parameter.getType().isEnum()) {
            adapter = new EnumCommandAdapter(parameter.getType());
        }
        return adapter;
    }

    public void execute(CommandSender sender, String[] args) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {

            boolean async = command.getCommandInfo().async();
            boolean hasSender = CommandSender.class.isAssignableFrom(command.getMethod().getParameterTypes()[0]);
            boolean onlySenderParameter = hasSender && parameters.length == 1;

            if (command.getMethod().isAnnotationPresent(ConsoleOnly.class) && !(sender instanceof ConsoleCommandSender)) {
                sender.sendMessage(consoleOnlyMessage);
                return;
            }

            if (command.getMethod().isAnnotationPresent(PlayerOnly.class) && !(sender instanceof Player)) {
                sender.sendMessage(playerOnlyMessage);
                return;
            }

            if (!command.getCommandInfo().permission().isEmpty() && !sender.hasPermission(command.getCommandInfo().permission())) {
                sender.sendMessage(noPermissionMessage);
                return;
            }

            if (onlySenderParameter && args.length == 0) {
                invoke(async, command.getParent(), sender);
                return;
            }

            if (onlySenderParameter && command.getSubCommands().size() == 0) {
                invoke(async, command.getParent(), sender);
                return;
            }

            int senderParameterLength = hasSender ? nonOptionalParameters.size() - 1 : nonOptionalParameters.size();
            if (args.length < senderParameterLength) {
                String[] usage = null;
                if (command.getParent() instanceof Command)
                    usage = ((Command) command.getParent()).getUsage();

                if (usage == null)
                    usage = translate(command.getCommandInfo().usage());

                sender.sendMessage(usage);
                return;
            }

            StringBuilder buffer = new StringBuilder();
            List<Object> objects = new ArrayList<>(parameters.length);
            if (hasSender) objects.add(sender);

            for (int i = 0; i < args.length; i++) {

                String[] argCopy = Arrays.copyOfRange(args, i, args.length);
                String[] subCommandArgsCopy = Arrays.copyOfRange(args, i + 1, args.length);
                buffer.append(args[i]);
                for (Object object : command.getSubCommands()) {
                    BaseCommand<?> subCommand = (BaseCommand<?>) object;
                    if (buffer.toString().equalsIgnoreCase(subCommand.getCommandInfo().value())) {
                        subCommandProcessors.get(subCommand).execute(sender, subCommandArgsCopy);
                        return;
                    }
                    if (subCommand.getCommandInfo().aliases().length == 0) continue;
                    for (String alias : subCommand.getCommandInfo().aliases()) {
                        if (buffer.toString().equalsIgnoreCase(alias)) {
                            subCommandProcessors.get(subCommand).execute(sender, subCommandArgsCopy);
                            return;
                        }
                    }
                }
                buffer.append(" ");

                if (parameters.length < i) continue;
                int parameterIdx = hasSender ? i + 1 : i;
                try {
                    Parameter parameter = parameters[parameterIdx];
                    CommandAdapter adapter = getAdapter(parameter);
                    Object result = adapter.convert(sender, args[i], argCopy, args, parameter);
                    if (result == null) {
                        result = adapter.getDefaultValue();
                        if (result == null) {
                            adapter.onError(sender, args[i], argCopy, args, parameter);
                            return;
                        }
                    }
                    objects.add(result);
                } catch (ArrayIndexOutOfBoundsException ex) {
                    continue;
                }
            }
            if (onlySenderParameter) {
                invoke(async, command.getParent(), sender);
                return;
            }

            int objectSize = hasSender ? objects.size() - 1 : objects.size();
            int parameterLength = hasSender ? parameters.length - 1 : parameters.length;
            if (objectSize < parameterLength) {
                for (int i = objectSize; i < parameterLength; i++) {
                    Parameter parameter = parameters[hasSender ? i + 1 : i];
                    if (!parameter.isAnnotationPresent(OptionalArgument.class)) continue;
                    OptionalArgument argument = parameter.getAnnotation(OptionalArgument.class);
                    if (argument.value().equals("")) {
                        objects.add(null);
                        continue;
                    }
                    objects.add(getAdapter(parameter).convert(sender, argument.value(), Arrays.copyOfRange(args, i, args.length), args, parameter));
                }
            }

            invoke(async, command.getParent(), objects.toArray());
        });
    }
}
