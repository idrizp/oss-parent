package me.idriz.oss.commands;

import me.idriz.oss.commands.processor.CommandProcessor;
import me.idriz.oss.commands.processor.TabProcessor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.List;

public class CommandExecutor extends BukkitCommand {

    private final BaseCommand baseCommand;
    private final Plugin plugin;
    private final CommandProcessor commandProcessor;
    private final TabProcessor tabProcessor;

    public CommandExecutor(BaseCommand baseCommand, Plugin plugin) {
        super(baseCommand.getCommandInfo().value());

        setAliases(Arrays.asList(baseCommand.getCommandInfo().aliases()));
        this.baseCommand = baseCommand;
        this.plugin = plugin;
        commandProcessor = new CommandProcessor(plugin, baseCommand);
        tabProcessor = new TabProcessor(plugin, baseCommand);
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        commandProcessor.execute(sender, args);
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
        return tabProcessor.execute(sender, args);
    }
}
