package me.xginko.flyspeedlimits.commands;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public abstract class BaseCommand implements CommandExecutor, TabCompleter {

    private final String label;

    public BaseCommand(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public static @NotNull String mergeArgs(String[] args, int start) {
        return String.join(" ", Arrays.copyOfRange(args, start, args.length));
    }
}
