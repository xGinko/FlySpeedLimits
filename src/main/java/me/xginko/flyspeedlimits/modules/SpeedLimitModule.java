package me.xginko.flyspeedlimits.modules;

import com.google.common.collect.ImmutableSet;
import me.xginko.flyspeedlimits.FlySpeedLimits;
import me.xginko.flyspeedlimits.config.Config;
import me.xginko.flyspeedlimits.struct.Disableable;
import me.xginko.flyspeedlimits.struct.Enableable;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import space.arim.morepaperlib.scheduling.GracefulScheduling;

import java.lang.reflect.Modifier;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class SpeedLimitModule implements Enableable, Disableable {

    protected static final Set<Class<SpeedLimitModule>> AVAILABLE_MODULES;
    protected static final Set<SpeedLimitModule> ENABLED_MODULES;

    static {
        AVAILABLE_MODULES = new Reflections(SpeedLimitModule.class.getPackage().getName())
                .get(Scanners.SubTypes.of(SpeedLimitModule.class).asClass())
                .stream()
                .filter(clazz -> !clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers()))
                .map(clazz -> (Class<SpeedLimitModule>) clazz)
                .sorted(Comparator.comparing(Class::getSimpleName))
                .collect(Collectors.collectingAndThen(Collectors.toList(), ImmutableSet::copyOf));
        ENABLED_MODULES = new HashSet<>();
    }

    protected final FlySpeedLimits plugin;
    protected final Config config;
    protected final GracefulScheduling scheduling;
    protected final String configPath, logFormat;
    protected final boolean enabled_in_config;

    public SpeedLimitModule(String configPath, boolean defEnabled) {
        this.plugin = FlySpeedLimits.getInstance();
        this.config = FlySpeedLimits.config();
        this.scheduling = FlySpeedLimits.scheduling();
        this.configPath = configPath;
        this.enabled_in_config = config.getBoolean(configPath + ".enable", defEnabled);
        String[] paths = configPath.split("\\.");
        if (paths.length <= 2) {
            this.logFormat = "<" + configPath + "> {}";
        } else {
            this.logFormat = "<" + paths[paths.length - 2] + "." + paths[paths.length - 1] + "> {}";
        }
    }

    public boolean shouldEnable() {
        return enabled_in_config;
    }

    public static void disableAll() {
        ENABLED_MODULES.forEach(Disableable::disable);
        ENABLED_MODULES.clear();
    }

    public static void reloadModules() {
        disableAll();

        for (Class<SpeedLimitModule> moduleClass : AVAILABLE_MODULES) {
            try {
                SpeedLimitModule module = moduleClass.getDeclaredConstructor().newInstance();
                if (module.shouldEnable()) {
                    ENABLED_MODULES.add(module);
                }
            } catch (Throwable t) { // We want to catch everything here if it fails to init
                FlySpeedLimits.logger().warn("Failed initialising module class '{}'.", moduleClass.getSimpleName(), t);
            }
        }

        ENABLED_MODULES.forEach(Enableable::enable);
    }

    protected void error(String message, Throwable throwable) {
        FlySpeedLimits.logger().error(logFormat, message, throwable);
    }

    protected void error(String message) {
        FlySpeedLimits.logger().error(logFormat, message);
    }

    protected void warn(String message) {
        FlySpeedLimits.logger().warn(logFormat, message);
    }

    protected void info(String message) {
        FlySpeedLimits.logger().info(logFormat, message);
    }

    protected void notRecognized(Class<?> clazz, String unrecognized) {
        warn("Unable to parse " + clazz.getSimpleName() + " at '" + unrecognized + "'. Please check your configuration.");
    }
}
