package me.xginko.flyspeedlimits;

import com.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import me.xginko.flyspeedlimits.commands.PluginYMLCmd;
import me.xginko.flyspeedlimits.config.Config;
import me.xginko.flyspeedlimits.config.LanguageCache;
import me.xginko.flyspeedlimits.modules.SpeedLimitModule;
import me.xginko.flyspeedlimits.struct.Permissions;
import me.xginko.flyspeedlimits.utils.tickdata.TickReporter;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.bstats.bukkit.Metrics;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import space.arim.morepaperlib.MorePaperLib;
import space.arim.morepaperlib.commands.CommandRegistration;
import space.arim.morepaperlib.scheduling.GracefulScheduling;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;

public final class FlySpeedLimits extends JavaPlugin {

    private static FlySpeedLimits instance;
    private static Map<String, LanguageCache> languageCacheMap;
    private static Config config;

    private static CommandRegistration commandRegistration;
    private static GracefulScheduling scheduling;
    private static TickReporter tickReporter;
    private static BukkitAudiences audiences;
    private static ComponentLogger logger;

    private static Metrics bStats;
    private static boolean isPacketEventsInstalled;

    @Override
    public void onLoad() {
        String shadedLibs = getClass().getPackage().getName() + ".libs";
        Configurator.setLevel(shadedLibs + ".reflections.Reflections", Level.OFF);
        isPacketEventsInstalled = getServer().getPluginManager().getPlugin("packetevents") != null;
        if (isPacketEventsInstalled) {
            PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
            PacketEvents.getAPI().getSettings().kickOnPacketException(true).reEncodeByDefault(false);
            PacketEvents.getAPI().load();
        }
    }

    @Override
    public void onEnable() {
        logger = ComponentLogger.logger(getLogger().getName());

        if (!isPacketEventsInstalled) {
            Stream.of("                                                               ",
                    "       _   _   _             _   _                             ",
                    "      / \\ | |_| |_ ___ _ __ | |_(_) ___  _ __                 ",
                    "     / _ \\| __| __/ _ \\ '_ \\| __| |/ _ \\| '_ \\            ",
                    "    / ___ \\ |_| ||  __/ | | | |_| | (_) | | | |               ",
                    "   /_/   \\_\\__|\\__\\___|_| |_|\\__|_|\\___/|_| |_|          ",
                    "                                                               ",
                    "   This plugin depends on PacketEvents to function!            ",
                    "   You can either download the latest release on modrinth:     ",
                    "   https://modrinth.com/plugin/packetevents/                   ",
                    "   or choose a dev build on their jenkins:                     ",
                    "   https://ci.codemc.io/job/retrooper/job/packetevents/        ",
                    "                                                               "
            ).forEach(logger::error);
            getServer().shutdown();
            return;
        }

        instance = this;
        MorePaperLib morePaperLib = new MorePaperLib(instance);
        commandRegistration = morePaperLib.commandRegistration();
        scheduling = morePaperLib.scheduling();
        audiences = BukkitAudiences.create(instance);
        bStats = new Metrics(instance, 23725);
        Permissions.registerAll(getServer().getPluginManager());

        if (!reloadPlugin()) {
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        logger.info("Done.");
    }

    @Override
    public void onDisable() {
        PluginYMLCmd.disableAll();
        SpeedLimitModule.disableAll();
        Permissions.unregisterAll(getServer().getPluginManager());
        if (languageCacheMap != null) {
            languageCacheMap.clear();
            languageCacheMap = null;
        }
        if (scheduling != null) {
            scheduling.cancelGlobalTasks();
            scheduling = null;
        }
        if (audiences != null) {
            audiences.close();
            audiences = null;
        }
        if (bStats != null) {
            bStats.shutdown();
            bStats = null;
        }
        commandRegistration = null;
        instance = null;
        config = null;
        logger = null;
    }

    public static FlySpeedLimits getInstance() {
        return instance;
    }

    public static ComponentLogger logger() {
        return logger;
    }

    public static Config config() {
        return config;
    }

    public static TickReporter tickReporter() {
        return tickReporter;
    }

    public static CommandRegistration cmdRegistration() {
        return commandRegistration;
    }

    public static GracefulScheduling scheduling() {
        return scheduling;
    }

    public static BukkitAudiences audiences() {
        return audiences;
    }

    public static @NotNull LanguageCache getLang(Locale locale) {
        return getLang(locale.toString().toLowerCase());
    }

    public static @NotNull LanguageCache getLang(CommandSender commandSender) {
        return getLang(audiences.sender(commandSender).pointers().get(Identity.LOCALE).orElse(config.default_locale));
    }

    public static @NotNull LanguageCache getLang(String lang) {
        if (!config.auto_lang) return languageCacheMap.get(config.default_locale.toString().toLowerCase());
        return languageCacheMap.getOrDefault(lang.replace("-", "_"), languageCacheMap.get(config.default_locale.toString().toLowerCase()));
    }

    public boolean reloadPlugin() {
        return reloadConfiguration() && reloadTranslations();
    }

    public boolean reloadConfiguration() {
        try {
            Files.createDirectories(getDataFolder().toPath());

            config = new Config();
            tickReporter = TickReporter.create(this, config.tpsCacheTime);

            SpeedLimitModule.reloadModules();
            PluginYMLCmd.reloadCommands();

            return config.saveConfig();
        } catch (Exception e) {
            logger.error("Error loading config!", e);
            return false;
        }
    }

    public boolean reloadTranslations() {
        Map<String, LanguageCache> newLangMap = new HashMap<>();
        try {
            for (String localeString : getAvailableTranslations()) {
                newLangMap.put(localeString, new LanguageCache(localeString));
            }
        } catch (Throwable t) {
            logger.error("Error loading language files!", t);
        }
        if (!newLangMap.isEmpty()) {
            languageCacheMap = newLangMap;
            return true;
        }
        logger.error("Couldn't load any translations.");
        if (!languageCacheMap.isEmpty()) {
            logger.warn("Keeping previous translations to avoid errors.");
        } else {
            logger.error("Disabling plugin to avoid more errors.");
            getServer().getPluginManager().disablePlugin(this);
        }
        return false;
    }

    private @NotNull SortedSet<String> getAvailableTranslations() {
        try (final JarFile pluginJar = new JarFile(getFile())) {
            Files.createDirectories(new File(getDataFolder(), "/lang").toPath());
            final Pattern langPattern = Pattern.compile("([a-z]{1,3}_[a-z]{1,3})(\\.yml)", Pattern.CASE_INSENSITIVE);
            final File[] langDirFiles = new File(getDataFolder() + "/lang").listFiles();
            return Stream.concat(pluginJar.stream().map(ZipEntry::getName), Arrays.stream(langDirFiles).map(File::getName))
                    .map(langPattern::matcher)
                    .filter(Matcher::find)
                    .map(matcher -> matcher.group(1))
                    .collect(Collectors.toCollection(TreeSet::new));
        } catch (Throwable t) {
            logger.error("Failed while looking for available translations!", t);
            return Collections.emptySortedSet();
        }
    }
}
