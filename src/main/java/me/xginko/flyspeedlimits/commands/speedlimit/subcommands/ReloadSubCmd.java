package me.xginko.flyspeedlimits.commands.speedlimit.subcommands;

import me.xginko.flyspeedlimits.FlySpeedLimits;
import me.xginko.flyspeedlimits.commands.BaseCommand;
import me.xginko.flyspeedlimits.utils.KyoriUtil;
import me.xginko.flyspeedlimits.struct.Permissions;
import net.kyori.adventure.audience.Audience;
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
        Audience audience = FlySpeedLimits.audiences().sender(sender);

        if (!sender.hasPermission(Permissions.RELOAD_CMD.bukkit())) {
            FlySpeedLimits.getLang(sender).cmd_no_permission.forEach(audience::sendMessage);
            return true;
        }

        audience.sendMessage(Component.empty());
        audience.sendMessage(Component.text("Reloading ...").color(NamedTextColor.WHITE));
        FlySpeedLimits.scheduling().asyncScheduler().run(() -> {
            FlySpeedLimits.getInstance().reloadPlugin();
            audience.sendMessage(Component.text("Reload complete.").color(KyoriUtil.ginkoblue()));
            audience.sendMessage(Component.empty());
        });

        return true;
    }
}