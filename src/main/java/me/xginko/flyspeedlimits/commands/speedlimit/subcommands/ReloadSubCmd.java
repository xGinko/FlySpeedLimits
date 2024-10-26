package me.xginko.flyspeedlimits.commands.speedlimit.subcommands;

import me.xginko.flyspeedlimits.FlySpeedLimits;
import me.xginko.flyspeedlimits.commands.BaseCommand;
import me.xginko.flyspeedlimits.utils.KyoriUtil;
import me.xginko.flyspeedlimits.struct.Permissions;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class ReloadSubCmd extends BaseCommand {

    public ReloadSubCmd() {
        super("reload");
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args){
        return Collections.emptyList();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission(Permissions.RELOAD_CMD.bukkit())) {
            sender.sendMessage(FlySpeedLimits.getLang(sender).cmd_no_permission);
            return true;
        }

        sender.sendMessage(Component.empty());
        sender.sendMessage(Component.text("Reloading ...").color(NamedTextColor.WHITE));
        FlySpeedLimits plugin = FlySpeedLimits.getInstance();
        plugin.getServer().getAsyncScheduler().runNow(plugin, reload -> {
            FlySpeedLimits.getInstance().reloadPlugin();
            sender.sendMessage(Component.text("Reload complete.").color(KyoriUtil.ginkoblue()));
            sender.sendMessage(Component.empty());
        });

        return true;
    }
}