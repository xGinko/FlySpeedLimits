package me.xginko.flyspeedlimits.commands;

import com.google.common.collect.ImmutableSet;
import me.xginko.flyspeedlimits.FlySpeedLimits;
import me.xginko.flyspeedlimits.struct.Disableable;
import me.xginko.flyspeedlimits.struct.Enableable;
import org.bukkit.command.CommandException;
import org.bukkit.command.PluginCommand;
import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import java.lang.reflect.Modifier;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is makes cross-platform compatible commands simple and easy to implement while allowing commands.
 * to be handled the same way as our modules. There are better ways to do this but for now, this is enough.
 * Only thing one must not forget is to also register any newly added main command in the plugin.yml
 */
public abstract class PluginYMLCmd extends BaseCommand implements Enableable, Disableable  {

    protected static final Set<Class<PluginYMLCmd>> AVAILABLE_COMMANDS;
    protected static final Set<PluginYMLCmd> ENABLED_COMMANDS;

    static {
        AVAILABLE_COMMANDS = new Reflections(PluginYMLCmd.class.getPackage().getName())
                .get(Scanners.SubTypes.of(PluginYMLCmd.class).asClass())
                .stream()
                .filter(clazz -> !clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers()))
                .map(clazz -> (Class<PluginYMLCmd>) clazz)
                .sorted(Comparator.comparing(Class::getSimpleName))
                .collect(Collectors.collectingAndThen(Collectors.toList(), ImmutableSet::copyOf));
        ENABLED_COMMANDS = new HashSet<>();
    }

    public final PluginCommand pluginCommand;

    protected PluginYMLCmd(@NotNull String label) throws CommandException {
        super(label);
        this.pluginCommand = FlySpeedLimits.getInstance().getCommand(label);
        if (pluginCommand == null) throw new CommandException("Command '/" + label + "' cannot be enabled because it's not defined in the plugin.yml.");
    }

    public static void disableAll() {
        ENABLED_COMMANDS.forEach(Disableable::disable);
        ENABLED_COMMANDS.clear();
    }

    public static void reloadCommands() {
        disableAll();

        for (Class<PluginYMLCmd> cmdClass : AVAILABLE_COMMANDS) {
            try {
                PluginYMLCmd pluginYMLCmd = cmdClass.getDeclaredConstructor().newInstance();
                pluginYMLCmd.enable();
                ENABLED_COMMANDS.add(pluginYMLCmd);
            } catch (Throwable t) {
                FlySpeedLimits.logger().warn("Failed initialising command class '{}'.", cmdClass.getSimpleName(), t);
            }
        }

        ENABLED_COMMANDS.forEach(Enableable::enable);
    }

    @Override
    public void enable() {
        pluginCommand.setExecutor(this);
        pluginCommand.setTabCompleter(this);
    }

    @Override
    public void disable() {
        pluginCommand.unregister(FlySpeedLimits.cmdRegistration().getServerCommandMap());
        pluginCommand.setTabCompleter(null);
        pluginCommand.setExecutor(null);
    }
}
