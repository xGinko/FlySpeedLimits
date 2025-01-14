package me.xginko.flyspeedlimits.config;

import io.github.thatsmusic99.configurationmaster.api.ConfigFile;
import io.github.thatsmusic99.configurationmaster.api.Title;
import me.xginko.flyspeedlimits.FlySpeedLimits;
import me.xginko.flyspeedlimits.modules.FlightType;
import me.xginko.flyspeedlimits.utils.GeneralUtil;
import me.xginko.flyspeedlimits.utils.KyoriUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
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

    public final @NotNull Translation new_chunks_burst_flight, new_chunks_burst_exceed, new_chunks_flight, new_chunks_exceed,
        old_chunks_burst_flight, old_chunks_burst_exceed, old_chunks_flight, old_chunks_exceed;

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

        this.new_chunks_burst_flight = getTranslation("new-chunks.burst-flight.flying",
                "<gray>Flying %flighttype% in <red>NEW <gray>chunks. Speed: %speed%/%maxspeed% TPS: %tps% MSPT: %mspt%");
        this.new_chunks_burst_exceed = getTranslation("new-chunks.burst-flight.exceed",
                "<gray>Flying %flighttype% in <red>NEW <gray>chunks. Speed: <red>%speed%/%maxspeed% <gray>TPS: %tps% MSPT: %mspt%");
        this.new_chunks_flight = getTranslation("new-chunks.regular-flight.flying",
                "<gray>Flying %flighttype% in <red>NEW <gray>chunks. Speed: %speed%/%maxspeed% TPS: %tps% MSPT: %mspt%");
        this.new_chunks_exceed = getTranslation("new-chunks.regular-flight.exceed",
                "<gray>Flying %flighttype% in <red>NEW <gray>chunks. Speed: <red>%speed%/%maxspeed% <gray>TPS: %tps% MSPT: %mspt%");

        this.old_chunks_burst_flight = getTranslation("old-chunks.burst-flight.flying",
                "<gray>Flying %flighttype% in <red>OLD <gray>chunks. Speed: %speed%/%maxspeed% TPS: %tps% MSPT: %mspt%");
        this.old_chunks_burst_exceed = getTranslation("old-chunks.burst-flight.exceed",
                "<gray>Flying %flighttype% in <red>OLD <gray>chunks. Speed: <red>%speed%/%maxspeed% <gray>TPS: %tps% MSPT: %mspt%");
        this.old_chunks_flight = getTranslation("old-chunks.regular-flight.flying",
                "<gray>Flying %flighttype% in <red>OLD <gray>chunks. Speed: %speed%/%maxspeed% TPS: %tps% MSPT: %mspt%");
        this.old_chunks_exceed = getTranslation("old-chunks.regular-flight.exceed",
                "<gray>Flying %flighttype% in <red>OLD <gray>chunks. Speed: <red>%speed%/%maxspeed% <gray>TPS: %tps% MSPT: %mspt%");

        try {
            this.langFile.save();
        } catch (Exception e) {
            FlySpeedLimits.logger().error("Failed to save translation file.", e);
        }
    }

    private @NotNull LanguageCache.Translation getTranslation(@NotNull String path, @NotNull String defaultTranslation) {
        this.langFile.addDefault(path, defaultTranslation);
        return new Translation(MiniMessage.miniMessage().deserialize(KyoriUtil.replaceAmpersand(this.langFile.getString(path, defaultTranslation))));
    }

    private @NotNull LanguageCache.Translation getTranslation(@NotNull String path, @NotNull String defaultTranslation, @NotNull String comment) {
        this.langFile.addDefault(path, defaultTranslation, comment);
        return new Translation(MiniMessage.miniMessage().deserialize(KyoriUtil.replaceAmpersand(this.langFile.getString(path, defaultTranslation))));
    }

    private @NotNull List<Component> getListTranslation(@NotNull String path, @NotNull List<String> defaultTranslation) {
        this.langFile.addDefault(path, defaultTranslation);
        return this.langFile.getStringList(path).stream().map(KyoriUtil::replaceAmpersand).map(MiniMessage.miniMessage()::deserialize).collect(Collectors.toList());
    }

    private @NotNull List<Component> getListTranslation(@NotNull String path, @NotNull List<String> defaultTranslation, @NotNull String comment) {
        this.langFile.addDefault(path, defaultTranslation, comment);
        return this.langFile.getStringList(path).stream().map(KyoriUtil::replaceAmpersand).map(MiniMessage.miniMessage()::deserialize).collect(Collectors.toList());
    }

    public static class Translation {

        private final Component component;

        public Translation(Component component) {
            this.component = component;
        }

        public Component get(FlightType flightType, double speed, double maxSpeed) {
            return component
                    .replaceText(TextReplacementConfig.builder()
                            .matchLiteral("%flighttype%")
                            .replacement(flightType.name())
                            .build())
                    .replaceText(TextReplacementConfig.builder()
                            .matchLiteral("%speed%")
                            .replacement(GeneralUtil.formatDouble(speed))
                            .build())
                    .replaceText(TextReplacementConfig.builder()
                            .matchLiteral("%maxspeed%")
                            .replacement(GeneralUtil.formatDouble(maxSpeed))
                            .build())
                    .replaceText(TextReplacementConfig.builder()
                            .matchLiteral("%tps%")
                            .replacement(GeneralUtil.formatDouble(FlySpeedLimits.tickReporter().getTPS()))
                            .build())
                    .replaceText(TextReplacementConfig.builder()
                            .matchLiteral("%mspt%")
                            .replacement(GeneralUtil.formatDouble(FlySpeedLimits.tickReporter().getMSPT()))
                            .build());
        }
    }
}