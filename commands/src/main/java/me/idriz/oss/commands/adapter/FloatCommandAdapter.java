package me.idriz.oss.commands.adapter;

import me.idriz.oss.commands.CommandAdapter;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;

public class FloatCommandAdapter implements CommandAdapter<Float> {

    @Override
    public Float convert(CommandSender sender, String argument, String[] args, String[] originalArgs, Parameter parameter) {
        try {
            return Float.parseFloat(argument);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    @Override
    public Float getDefaultValue() {
        return 0F;
    }

    @Override
    public void onError(CommandSender sender, String argument, String[] args, String[] originalArgs, Parameter parameter) {
        sender.sendMessage(ChatColor.RED + "Invalid number.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args, String argument, Parameter parameter) {
        return Arrays.asList("1.25", "2.5", "3.75");
    }
}
