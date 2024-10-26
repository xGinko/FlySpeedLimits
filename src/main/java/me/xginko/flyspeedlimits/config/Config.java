package me.xginko.flyspeedlimits.config;

import io.github.thatsmusic99.configurationmaster.api.ConfigFile;
import io.github.thatsmusic99.configurationmaster.api.Title;
import me.xginko.flyspeedlimits.FlySpeedLimits;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;
import java.util.Locale;

public final class Config {

    private final @NotNull ConfigFile configFile;

    public final @NotNull Locale default_locale;
    public final long checkIntervalMillis, newChunkMaxInhTimeTicks;
    public final boolean auto_lang;

    public Config() throws Exception {
        FlySpeedLimits plugin = FlySpeedLimits.getInstance();
        // Load config.yml with ConfigMaster
        this.configFile = ConfigFile.loadConfig(new File(plugin.getDataFolder(), "config.yml"));

        this.configFile.setTitle(new Title().withWidth(80)
                .addSolidLine()
                .addLine(" ", Title.Pos.CENTER)
                .addLine(plugin.getName(), Title.Pos.CENTER)
                .addLine(" ", Title.Pos.CENTER)
                .addSolidLine());

        this.default_locale = Locale.forLanguageTag(getString("general.default-language", "en_us", """
                The default language that will be used if auto-language\s
                is false or no matching language file was found.""").replace("_", "-"));
        this.auto_lang = getBoolean("general.auto-language", true, """
                If set to true, the plugin will send messages to players\s
                based on what locale their client is set to.\s
                This of course requires that there is a translation file\s
                available for that locale inside the plugins lang folder.""");

        this.checkIntervalMillis = getLong("general.check-interval-millis", 1000L);
        this.newChunkMaxInhTimeTicks = getLong("general.new-chunks-max-inhabited-time-ticks", 200L);
    }

    public boolean saveConfig() {
        try {
            this.configFile.save();
            return true;
        } catch (Exception e) {
            FlySpeedLimits.logger().error("Failed to save config file!", e);
            return false;
        }
    }

    public boolean getBoolean(@NotNull String path, boolean def, @NotNull String comment) {
        this.configFile.addDefault(path, def, comment);
        return this.configFile.getBoolean(path, def);
    }

    public boolean getBoolean(@NotNull String path, boolean def) {
        this.configFile.addDefault(path, def);
        return this.configFile.getBoolean(path, def);
    }

    public @NotNull String getString(@NotNull String path, @NotNull String def, @NotNull String comment) {
        this.configFile.addDefault(path, def, comment);
        return this.configFile.getString(path, def);
    }

    public @NotNull String getString(@NotNull String path, @NotNull String def) {
        this.configFile.addDefault(path, def);
        return this.configFile.getString(path, def);
    }

    public double getDouble(@NotNull String path, double def, @NotNull String comment) {
        this.configFile.addDefault(path, def, comment);
        return this.configFile.getDouble(path, def);
    }

    public double getDouble(@NotNull String path, double def) {
        this.configFile.addDefault(path, def);
        return this.configFile.getDouble(path, def);
    }

    public int getInt(@NotNull String path, int def, @NotNull String comment) {
        this.configFile.addDefault(path, def, comment);
        return this.configFile.getInteger(path, def);
    }

    public int getInt(@NotNull String path, int def) {
        this.configFile.addDefault(path, def);
        return this.configFile.getInteger(path, def);
    }

    public long getLong(@NotNull String path, long def) {
        this.configFile.addDefault(path, def);
        return this.configFile.getLong(path, def);
    }

    public long getLong(@NotNull String path, long def, @NotNull String comment) {
        this.configFile.addDefault(path, def, comment);
        return this.configFile.getLong(path, def);
    }

    public @NotNull <T> List<T> getList(@NotNull String path, @NotNull List<T> def, @NotNull String comment) {
        this.configFile.addDefault(path, def, comment);
        return this.configFile.getList(path);
    }

    public @NotNull <T> List<T> getList(@NotNull String path, @NotNull List<T> def) {
        this.configFile.addDefault(path, def);
        return this.configFile.getList(path);
    }
}
