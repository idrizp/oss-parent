package me.idriz.oss.commands;

import org.bukkit.command.CommandSender;

import java.lang.reflect.Parameter;
import java.util.List;

public interface CommandAdapter<T> {

    T convert(CommandSender sender, String argument, String[] args, String[] originalArgs, Parameter parameter);

    T getDefaultValue();

    void onError(CommandSender sender, String argument, String[] args, String[] originalArgs, Parameter parameter);

    List<String> tabComplete(CommandSender sender, String[] args, String argument, Parameter parameter);

}
