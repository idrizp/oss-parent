package me.idriz.oss.commands.adapter;

import me.idriz.oss.commands.CommandAdapter;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;

public class IntegerCommandAdapter implements CommandAdapter<Integer> {

    @Override
    public Integer convert(CommandSender sender, String argument, String[] args, String[] originalArgs, Parameter parameter) {
        try {
            return Integer.parseInt(argument);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    @Override
    public Integer getDefaultValue() {
        return 0;
    }

    @Override
    public void onError(CommandSender sender, String argument, String[] args, String[] originalArgs, Parameter parameter) {
        sender.sendMessage(ChatColor.RED + "Invalid integer(don't provide decimals!).");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args, String argument, Parameter parameter) {
        return Arrays.asList("1", "2", "3");
    }
}
