package me.xginko.flyspeedlimits.config;

import io.github.thatsmusic99.configurationmaster.api.ConfigFile;
import io.github.thatsmusic99.configurationmaster.api.Title;
import me.xginko.flyspeedlimits.FlySpeedLimits;
import me.xginko.flyspeedlimits.utils.KyoriUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class LanguageCache {

    private final @NotNull ConfigFile langFile;
    public final @Nullable String translator;

    public final @NotNull List<Component> cmd_no_permission;

    public LanguageCache(@NotNull String langString) throws Exception {
        FlySpeedLimits plugin = FlySpeedLimits.getInstance();
        File langYML = new File(plugin.getDataFolder() + "/lang", langString + ".yml");
        // Check if the file already exists and save the one from the plugin's resources folder if it does not
        if (!langYML.exists()) {
            plugin.saveResource("lang/" + langString + ".yml", false);
        }
        // Finally, load the lang file with configmaster
        this.langFile = ConfigFile.loadConfig(langYML);

        this.langFile.setTitle(new Title().withWidth(120)
                .addSolidLine()
                .addLine(" ", Title.Pos.CENTER)
                .addLine(plugin.getName(), Title.Pos.CENTER)
                .addLine("Translation for locale: " + langString, Title.Pos.CENTER)
                .addLine(" ", Title.Pos.CENTER)
                .addLine("Please use MiniMessage format: https://docs.advntr.dev/minimessage/format.html", Title.Pos.CENTER)
                .addLine(" ", Title.Pos.CENTER)
                .addSolidLine());

        this.langFile.addDefault("translator", "xginko");
        this.translator = langFile.getString("translator");

        this.cmd_no_permission = getListTranslation("cmd.no-permission",
                Collections.singletonList("<red>You don't have permission to use this command."));

        try {
            this.langFile.save();
        } catch (Exception e) {
            FlySpeedLimits.logger().error("Failed to save translation file.", e);
        }
    }

    private @NotNull Component getTranslation(@NotNull String path, @NotNull String defaultTranslation) {
        this.langFile.addDefault(path, defaultTranslation);
        return MiniMessage.miniMessage().deserialize(KyoriUtil.replaceAmpersand(this.langFile.getString(path, defaultTranslation)));
    }

    private @NotNull Component getTranslation(@NotNull String path, @NotNull String defaultTranslation, @NotNull String comment) {
        this.langFile.addDefault(path, defaultTranslation, comment);
        return MiniMessage.miniMessage().deserialize(KyoriUtil.replaceAmpersand(this.langFile.getString(path, defaultTranslation)));
    }

    private @NotNull List<Component> getListTranslation(@NotNull String path, @NotNull List<String> defaultTranslation) {
        this.langFile.addDefault(path, defaultTranslation);
        return this.langFile.getStringList(path).stream().map(KyoriUtil::replaceAmpersand).map(MiniMessage.miniMessage()::deserialize).collect(Collectors.toList());
    }

    private @NotNull List<Component> getListTranslation(@NotNull String path, @NotNull List<String> defaultTranslation, @NotNull String comment) {
        this.langFile.addDefault(path, defaultTranslation, comment);
        return this.langFile.getStringList(path).stream().map(KyoriUtil::replaceAmpersand).map(MiniMessage.miniMessage()::deserialize).collect(Collectors.toList());
    }
}