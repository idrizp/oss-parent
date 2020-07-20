package me.idriz.oss.commands;

import me.idriz.oss.commands.annotation.CommandInfo;
import me.idriz.oss.commands.annotation.Default;
import me.idriz.oss.commands.validation.Validation;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class BaseCommand<T> {

    private final T parent;
    private final CommandInfo commandInfo;
    private final Set<BaseCommand<?>> subCommands = new HashSet<>();
    private final String[] allAliases;
    private Method method;
    private boolean subCommand = false;

    public BaseCommand(T object) {
        this.parent = object;
        if (!Validation.isCommandClass(object.getClass())) {
            throw new IllegalArgumentException("Not a valid class.");
        }
        commandInfo = object.getClass().getAnnotation(CommandInfo.class);

        allAliases = new String[commandInfo.aliases().length + 1];
        for (int i = 0; i < commandInfo.aliases().length; i++) allAliases[i] = commandInfo.aliases()[i];
        allAliases[allAliases.length - 1] = commandInfo.value();

        if (object instanceof Command) {
            ((Command) object).getSubCommands().forEach(obj -> subCommands.add(new BaseCommand<>(obj)));
        }

        for (Method method : object.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(Default.class)) {
                this.method = method;
                continue;
            }
            if (method.isAnnotationPresent(CommandInfo.class)) {
                BaseCommand<?> baseCommand = new BaseCommand<>(object, method);
                subCommands.add(baseCommand);
            }
        }
        if (this.method == null) {
            BaseCommand<?> first = subCommands.parallelStream().findFirst().orElse(null);
            if (first == null) throw new NullPointerException("Couldn't find any base commands.");
            this.method = first.getMethod();
            subCommands.remove(first);
        }
    }

    public BaseCommand(T parent, Method method) {
        this.method = method;
        this.parent = parent;
        this.commandInfo = method.getAnnotation(CommandInfo.class);
        this.subCommand = true;

        allAliases = new String[commandInfo.aliases().length + 1];
        for (int i = 0; i < commandInfo.aliases().length; i++) allAliases[i] = commandInfo.aliases()[i];
        allAliases[allAliases.length - 1] = commandInfo.value();

    }

    public CommandInfo getCommandInfo() {
        return commandInfo;
    }

    public Set<BaseCommand<?>> getSubCommands() {
        return subCommands;
    }

    /**
     * Whether or not the string is an alias of this command.
     * @param provided
     * @return
     */
    public boolean isAlias(String provided) {
        for (String s : allAliases) {
            if(s.equalsIgnoreCase(provided))
                return true;
        }
        return false;
    }

    public Method getMethod() {
        return method;
    }

    public BaseCommand<T> setMethod(Method method) {
        this.method = method;
        return this;
    }

    public boolean isSubCommand() {
        return subCommand;
    }

    public BaseCommand<T> setSubCommand(boolean subCommand) {
        this.subCommand = subCommand;
        return this;
    }

    public Object getParent() {
        return parent;
    }
}
