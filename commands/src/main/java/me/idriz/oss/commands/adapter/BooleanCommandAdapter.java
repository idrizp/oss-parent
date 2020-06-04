package me.idriz.oss.commands.adapter;

import com.google.common.collect.Lists;
import me.idriz.oss.commands.CommandAdapter;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BooleanCommandAdapter implements CommandAdapter<Boolean> {

    private static final Map<String, Boolean> STRING_TO_BOOLEAN_MAPPING = new HashMap<>();

    static {
        STRING_TO_BOOLEAN_MAPPING.put("yes", true);
        STRING_TO_BOOLEAN_MAPPING.put("no", false);
        STRING_TO_BOOLEAN_MAPPING.put("true", true);
        STRING_TO_BOOLEAN_MAPPING.put("false", false);
    }

    @Override
    public Boolean convert(CommandSender sender, String argument, String[] args, String[] originalArgs, Parameter parameter) {
        return STRING_TO_BOOLEAN_MAPPING.get(argument.toLowerCase());
    }

    @Override
    public Boolean getDefaultValue() {
        return null;
    }

    @Override
    public void onError(CommandSender sender, String argument, String[] args, String[] originalArgs, Parameter parameter) {
        sender.sendMessage(ChatColor.RED + "Please provide a boolean response(yes, no, true, false)");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args, String argument, Parameter parameter) {
        return Lists.newArrayList("yes", "no", "true", "false");
    }
}
