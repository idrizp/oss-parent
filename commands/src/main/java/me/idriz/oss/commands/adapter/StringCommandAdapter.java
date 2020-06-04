package me.idriz.oss.commands.adapter;

import com.google.common.collect.Lists;
import me.idriz.oss.commands.CommandAdapter;
import me.idriz.oss.commands.adapter.annotation.Text;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.lang.reflect.Parameter;
import java.util.List;

public class StringCommandAdapter implements CommandAdapter<String> {

    @Override
    public String convert(CommandSender sender, String argument, String[] args, String[] originalArgs, Parameter parameter) {
        if(argument.replace(" ", "").length() == 0)
            return null;

        if(parameter.isAnnotationPresent(Text.class)) {
            return String.join(" ", args);
        }
        return argument;
    }

    @Override
    public String getDefaultValue() {
        return null;
    }

    @Override
    public void onError(CommandSender sender, String argument, String[] args, String[] originalArgs, Parameter parameter) {
        sender.sendMessage(ChatColor.RED + "Please provide a string.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args, String argument, Parameter parameter) {
        return Lists.newArrayList("message");
    }

}
