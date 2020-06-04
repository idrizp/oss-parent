package me.idriz.oss.commands;

import java.util.HashSet;
import java.util.Set;

public interface Command {

    default Set<?> getSubCommands() {
        return new HashSet<>();
    }

    default String[] getUsage() {
        return null;
    }

}
