package me.xginko.flyspeedlimits.utils;

import me.xginko.flyspeedlimits.FlySpeedLimits;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class KyoriUtil {

    public static final TextColor GINKOBLUE = TextColor.color(0, 237, 255);

    public static void sendMessage(@NotNull CommandSender sender, @NotNull Component message) {
        FlySpeedLimits.audiences().sender(sender).sendMessage(message);
    }

    public static void sendActionBar(@NotNull CommandSender sender, @NotNull Component message) {
        FlySpeedLimits.audiences().sender(sender).sendActionBar(message);
    }

    public static @NotNull String replaceAmpersand(@NotNull String string) {
        string = string.replace("&0", "<black>");
        string = string.replace("&1", "<dark_blue>");
        string = string.replace("&2", "<dark_green>");
        string = string.replace("&3", "<dark_aqua>");
        string = string.replace("&4", "<dark_red>");
        string = string.replace("&5", "<dark_purple>");
        string = string.replace("&6", "<gold>");
        string = string.replace("&7", "<gray>");
        string = string.replace("&8", "<dark_gray>");
        string = string.replace("&9", "<blue>");
        string = string.replace("&a", "<green>");
        string = string.replace("&b", "<aqua>");
        string = string.replace("&c", "<red>");
        string = string.replace("&d", "<light_purple>");
        string = string.replace("&e", "<yellow>");
        string = string.replace("&f", "<white>");
        string = string.replace("&k", "<obfuscated>");
        string = string.replace("&l", "<bold>");
        string = string.replace("&m", "<strikethrough>");
        string = string.replace("&n", "<underlined>");
        string = string.replace("&o", "<italic>");
        string = string.replace("&r", "<reset>");
        return string;
    }
}
