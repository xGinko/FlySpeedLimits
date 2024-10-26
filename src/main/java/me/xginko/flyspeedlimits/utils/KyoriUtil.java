package me.xginko.flyspeedlimits.utils;

import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import org.jetbrains.annotations.NotNull;

public class KyoriUtil {

    private static MiniMessage minimessage_playerinput_safe;
    private static TextColor ginkoblue;

    public static void load() {
        ginkoblue = TextColor.color(0, 237, 255);
        minimessage_playerinput_safe = MiniMessage.builder()
                .tags(TagResolver.builder()
                        .resolver(StandardTags.color())
                        .resolver(StandardTags.decorations())
                        .resolver(StandardTags.gradient())
                        .resolver(StandardTags.rainbow())
                        .build())
                .build();
    }

    public static void unload() {
        minimessage_playerinput_safe = null;
        ginkoblue = null;
    }

    public static MiniMessage inputSafeMiniMessage() {
        return minimessage_playerinput_safe;
    }

    public static TextColor ginkoblue() {
        return ginkoblue;
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
