package me.idriz.oss.commands.processor;

import me.idriz.oss.commands.BaseCommand;
import me.idriz.oss.commands.CommandAdapter;
import me.idriz.oss.commands.CommandProvider;
import me.idriz.oss.commands.annotation.Argument;
import me.idriz.oss.commands.annotation.ConsoleOnly;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TabProcessor {

    public static ExecutorService tabService = Executors.newSingleThreadScheduledExecutor();

    public static char colorChar = '&';

    private final BaseCommand command;
    private final Parameter[] parameters;
    private final Parameter[] noSenderParameters;
    private final Map<BaseCommand, TabProcessor> subCommandProcessors = new HashMap<>();
    private final Plugin plugin;
    private boolean hasSender = false;

    public TabProcessor(Plugin plugin, BaseCommand command) {
        this.plugin = plugin;
        this.command = command;
        this.parameters = command.getMethod().getParameters();
        this.hasSender = CommandSender.class.isAssignableFrom(parameters[0].getType());
        this.noSenderParameters = hasSender ? Arrays.copyOfRange(parameters, 1, parameters.length) : parameters;


        String[] aliases = command.getCommandInfo().aliases();

        command.getSubCommands().forEach(subCommand -> {
            this.subCommandProcessors.put((BaseCommand) subCommand, new TabProcessor(plugin, (BaseCommand) subCommand));
        });
    }

    private static String[] translate(String[] input) {
        for (int i = 0; i < input.length; i++) {
            input[i] = ChatColor.translateAlternateColorCodes(colorChar, input[i]);
        }
        return input;
    }

    private BaseCommand<?> getSubCommand(String input) {
        for (Object sub : command.getSubCommands()) {
            BaseCommand<?> subCommand = (BaseCommand<?>) sub;
            if (subCommand.isAlias(input)) {
                return subCommand;
            }
        }
        return null;
    }

    private List<String> autoComplete(Parameter parameter, CommandSender sender, String[] args, String arg) {
        CommandAdapter adapter = null;
        if (parameter.isAnnotationPresent(Argument.class)) {
            Argument argument = parameter.getAnnotation(Argument.class);
            return Arrays.asList(argument.completions());
        }
        if (adapter == null) adapter = CommandProvider.getByClass(parameter.getType());
        return adapter.tabComplete(sender, args, arg, parameter);
    }

    public List<String> execute(CommandSender sender, String[] args) {

        List<String> list = new ArrayList<>();


        //If we have a parameter, don't supply sub commands

        boolean console = sender instanceof ConsoleCommandSender;
        boolean player = sender instanceof Player;
        boolean consoleOnly = command.getMethod().isAnnotationPresent(ConsoleOnly.class);
        boolean playerOnly = command.getMethod().isAnnotationPresent(ConsoleOnly.class);

        if ((consoleOnly && !console) || (playerOnly && !player)) {
            return Collections.emptyList();
        }

        if (!sender.hasPermission(command.getCommandInfo().permission())) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            command.getSubCommands()
                    .stream()
                    .filter(obj -> sender.hasPermission(((BaseCommand) obj).getCommandInfo().permission()))
                    .forEach(obj -> list.add(((BaseCommand) obj).getCommandInfo().value()));
        }

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            BaseCommand command = getSubCommand(arg);
            if (command == null) continue;
            if (command.isAlias(arg)) {
                String[] copiedArgs = Arrays.copyOfRange(args, i + 1, args.length);
                return subCommandProcessors.get(command).execute(sender, copiedArgs);
            }
        }

        String arg = args[args.length - 1];

        if (noSenderParameters.length < args.length) return list;

        Parameter lastParameter = noSenderParameters[args.length - 1];
        if (lastParameter == null) return list;

        list.addAll(autoComplete(lastParameter, sender, args, arg));

        return list;
    }
}
