package me.idriz.oss.commands;

import me.idriz.oss.commands.adapter.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;

public class CommandRegistrar {

    public static final Map<Class<?>, CommandAdapter<?>> COMMAND_ADAPTERS = new HashMap<>();
    private static final Map<Class<?>, CommandAdapter<?>> CLASS_TO_COMMAND_ADAPTER_MAP = new HashMap<>();

    static {

        registerAdapter(String.class, new StringCommandAdapter());

        registerAdapter(Double.class, new DoubleCommandAdapter());
        registerAdapter(double.class, new DoubleCommandAdapter());

        registerAdapter(Integer.class, new IntegerCommandAdapter());
        registerAdapter(int.class, new IntegerCommandAdapter());

        registerAdapter(Float.class, new FloatCommandAdapter());
        registerAdapter(float.class, new FloatCommandAdapter());

        registerAdapter(Boolean.class, new BooleanCommandAdapter());
        registerAdapter(boolean.class, new BooleanCommandAdapter());

        registerAdapter(Long.class, new LongCommandAdapter());
        registerAdapter(long.class, new BooleanCommandAdapter());

        registerAdapter(Player.class, new PlayerCommandAdapter());

    }

    private final Plugin plugin;
    private final Map<String, BaseCommand> commands = new HashMap<>();

    public CommandRegistrar(Plugin plugin) {
        this.plugin = plugin;
    }

    public static CommandAdapter getByAdapterClass(Class clazz) {
        return CLASS_TO_COMMAND_ADAPTER_MAP.get(clazz);
    }

    public static <T> CommandAdapter<T> getByClass(Class<?> clazz) {
        return (CommandAdapter<T>) COMMAND_ADAPTERS.get(clazz);
    }

    public static void registerAdapter(Class<?> clazz, CommandAdapter<?> adapter) {
        COMMAND_ADAPTERS.put(clazz, adapter);
        CLASS_TO_COMMAND_ADAPTER_MAP.put(adapter.getClass(), adapter);
    }

    public static <T> void registerCustomAdapter(CommandAdapter<T> adapter) {
        CLASS_TO_COMMAND_ADAPTER_MAP.put(adapter.getClass(), adapter);
    }

    public <T> void registerCommand(T object) {
        BaseCommand<T> command = new BaseCommand<>(object);
        commands.put(command.getCommandInfo().value(), command);
        plugin.getServer().getCommandMap().register(command.getCommandInfo().value(), new CommandExecutor(command, plugin));

    }

}
