package me.xginko.flyspeedlimits.commands.speedlimit.subcommands;

import me.xginko.flyspeedlimits.FlySpeedLimits;
import me.xginko.flyspeedlimits.commands.BaseCommand;
import me.xginko.flyspeedlimits.utils.KyoriUtil;
import me.xginko.flyspeedlimits.struct.Permissions;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class VersionSubCmd extends BaseCommand {

    public VersionSubCmd() {
        super("version");
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return Collections.emptyList();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Audience audience = FlySpeedLimits.audiences().sender(sender);

        if (!sender.hasPermission(Permissions.VERSION_CMD.bukkit())) {
            FlySpeedLimits.getLang(sender).cmd_no_permission.forEach(audience::sendMessage);
            return true;
        }

        PluginDescriptionFile pluginYML = FlySpeedLimits.getInstance().getDescription();

        audience.sendMessage(Component.newline()
                .append(
                        Component.text(String.join(" ", pluginYML.getName(), pluginYML.getVersion()))
                                .color(KyoriUtil.ginkoblue())
                                .clickEvent(ClickEvent.openUrl(pluginYML.getWebsite()))
                )
                .append(Component.text(" by ").color(NamedTextColor.DARK_GRAY))
                .append(
                        Component.text(String.join(", ", pluginYML.getAuthors()))
                                .color(KyoriUtil.ginkoblue())
                                .clickEvent(ClickEvent.openUrl("https://github.com/xGinko"))
                )
                .append(Component.newline())
        );

        return true;
    }
}