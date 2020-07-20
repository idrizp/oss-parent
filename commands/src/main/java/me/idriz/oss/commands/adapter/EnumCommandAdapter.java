package me.idriz.oss.commands.adapter;

import me.idriz.oss.commands.CommandAdapter;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class EnumCommandAdapter<T extends Enum<T>> implements CommandAdapter<T> {

    private final Class<T> clazz;

    public EnumCommandAdapter(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public T convert(CommandSender sender, String argument, String[] args, String[] originalArgs, Parameter parameter) {
        return T.valueOf(clazz, argument.toUpperCase());
    }

    @Override
    public T getDefaultValue() {
        return null;
    }

    @Override
    public void onError(CommandSender sender, String argument, String[] args, String[] originalArgs, Parameter parameter) {
        sender.sendMessage(ChatColor.RED + "Unknown argument " + argument + ".");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args, String argument, Parameter parameter) {
        return Arrays
                .stream(clazz.getEnumConstants())
                .map(T::name)
                .filter(target -> target.startsWith(argument))
                .collect(Collectors.toList());
    }
}
