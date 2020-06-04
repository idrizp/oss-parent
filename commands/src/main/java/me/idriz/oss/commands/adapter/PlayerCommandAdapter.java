package me.idriz.oss.commands.adapter;

import me.idriz.oss.commands.CommandAdapter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.Parameter;
import java.util.List;
import java.util.stream.Collectors;

public class PlayerCommandAdapter implements CommandAdapter<Player> {

    @Override
    public Player convert(CommandSender sender, String argument, String[] args, String[] originalArgs, Parameter parameter) {
        return Bukkit.getPlayer(argument);
    }

    @Override
    public Player getDefaultValue() {
        return null;
    }

    @Override
    public void onError(CommandSender sender, String argument, String[] args, String[] originalArgs, Parameter parameter) {
        sender.sendMessage(ChatColor.RED + "Player by that name not found.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args, String argument, Parameter parameter) {
        return Bukkit
                .getOnlinePlayers()
                .parallelStream()
                .filter(player -> argument.startsWith(player.getName()))
                .map(Player::getName)
                .collect(Collectors.toList());
    }
}
