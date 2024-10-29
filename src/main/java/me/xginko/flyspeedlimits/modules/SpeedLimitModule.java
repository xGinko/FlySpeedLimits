package me.xginko.flyspeedlimits.modules;

import com.google.common.collect.ImmutableSet;
import me.xginko.flyspeedlimits.FlySpeedLimits;
import me.xginko.flyspeedlimits.config.Config;
import me.xginko.flyspeedlimits.config.LocationConfig;
import me.xginko.flyspeedlimits.manager.PlayerManager;
import me.xginko.flyspeedlimits.manager.WrappedPlayer;
import me.xginko.flyspeedlimits.struct.Disableable;
import me.xginko.flyspeedlimits.struct.Enableable;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import space.arim.morepaperlib.scheduling.GracefulScheduling;

import javax.annotation.Nullable;
import java.lang.reflect.Modifier;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
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
    protected final GracefulScheduling scheduling;
    protected final Config config;
    protected final List<LocationConfig> configuredLocations;
    protected final String configPath, logFormat;
    protected final boolean enabledInConfig;

    public SpeedLimitModule(String configPath, boolean defEnabled) {
        this.plugin = FlySpeedLimits.getInstance();
        this.config = FlySpeedLimits.config();
        this.scheduling = FlySpeedLimits.scheduling();
        this.configPath = configPath;
        this.enabledInConfig = config.getBoolean(configPath + ".enable", defEnabled);
        this.configuredLocations = LocationConfig.readInOrder(configPath);
        String[] paths = configPath.split("\\.");
        if (paths.length <= 2) {
            this.logFormat = "<" + configPath + "> {}";
        } else {
            this.logFormat = "<" + paths[paths.length - 2] + "." + paths[paths.length - 1] + "> {}";
        }
    }

    public boolean shouldEnable() {
        return enabledInConfig && !configuredLocations.isEmpty();
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

    public abstract boolean checkPreconditions(WrappedPlayer wrappedPlayer);
    public abstract void onPlayerFlightDenied(WrappedPlayer wrappedPlayer);
    public abstract void onPlayerExceedSpeedNewChunksBurst(WrappedPlayer wrappedPlayer);
    public abstract void onPlayerFlyNewChunksBurst(WrappedPlayer wrappedPlayer);
    public abstract void onPlayerExceedSpeedNewChunks(WrappedPlayer wrappedPlayer);
    public abstract void onPlayerFlyNewChunks(WrappedPlayer wrappedPlayer);
    public abstract void onPlayerExceedSpeedOldChunksBurst(WrappedPlayer wrappedPlayer);
    public abstract void onPlayerFlyOldChunksBurst(WrappedPlayer wrappedPlayer);
    public abstract void onPlayerExceedSpeedOldChunks(WrappedPlayer wrappedPlayer);
    public abstract void onPlayerFlyOldChunks(WrappedPlayer wrappedPlayer);

    public @Nullable LocationConfig getConfigAt(Location location) {
        for (LocationConfig locationConfig : configuredLocations) {
            if (locationConfig.appliesAt(location)) {
                return locationConfig;
            }
        }
        return null;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void on(PlayerMoveEvent event) {
        WrappedPlayer wrappedPlayer = PlayerManager.getPlayer(event);
        if (!checkPreconditions(wrappedPlayer)) return;

        LocationConfig locationConfig = getConfigAt(wrappedPlayer.player.getLocation());
        if (locationConfig == null) return;

        if (locationConfig.flight.denyFlight || locationConfig.flight.shouldDenyDueToLag()) {
            onPlayerFlightDenied(wrappedPlayer);
            return;
        }

        if (wrappedPlayer.isInNewChunks()) {
            if (locationConfig.flight.canBurstNewChunks()) {
                if (wrappedPlayer.getXZSpeedSquared(locationConfig.flight.speedUnit) > locationConfig.flight.newChunksXZBurstSpeed) {
                    onPlayerExceedSpeedNewChunksBurst(wrappedPlayer);
                } else {
                    onPlayerFlyNewChunksBurst(wrappedPlayer);
                }
            }

            else {
                if (wrappedPlayer.getXZSpeedSquared(locationConfig.flight.speedUnit) > locationConfig.flight.newChunksXZSpeed) {
                    onPlayerExceedSpeedNewChunks(wrappedPlayer);
                } else {
                    onPlayerFlyNewChunks(wrappedPlayer);
                }
            }
        }

        else {
            if (locationConfig.flight.canBurstOldChunks()) {
                if (wrappedPlayer.getXZSpeedSquared(locationConfig.flight.speedUnit) > locationConfig.flight.oldChunksXZBurstSpeed) {
                    onPlayerExceedSpeedOldChunksBurst(wrappedPlayer);
                } else {
                    onPlayerFlyOldChunksBurst(wrappedPlayer);
                }
            }

            else {
                if (wrappedPlayer.getXZSpeedSquared(locationConfig.flight.speedUnit) > locationConfig.flight.oldChunksXZSpeed) {
                    onPlayerExceedSpeedOldChunks(wrappedPlayer);
                } else {
                    onPlayerFlyOldChunks(wrappedPlayer);
                }
            }
        }
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
