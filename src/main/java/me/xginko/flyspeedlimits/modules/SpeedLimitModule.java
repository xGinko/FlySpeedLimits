package me.xginko.flyspeedlimits.modules;

import com.google.common.collect.ImmutableSet;
import me.xginko.flyspeedlimits.FlySpeedLimits;
import me.xginko.flyspeedlimits.config.Config;
import me.xginko.flyspeedlimits.config.LocationConfig;
import me.xginko.flyspeedlimits.manager.PlayerManager;
import me.xginko.flyspeedlimits.manager.WrappedPlayer;
import me.xginko.flyspeedlimits.struct.Disableable;
import me.xginko.flyspeedlimits.struct.Enableable;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
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
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class SpeedLimitModule implements Enableable, Disableable {

    protected static final Set<Class<SpeedLimitModule>> AVAILABLE_MODULES;
    protected static final Map<FlightType, SpeedLimitModule> ENABLED_MODULES;

    static {
        // Disable reflection logging for this operation because its just confusing and provides no value.
        Configurator.setLevel(FlySpeedLimits.class.getPackage().getName() + ".libs.reflections.Reflections", Level.OFF);
        AVAILABLE_MODULES = new Reflections(SpeedLimitModule.class.getPackage().getName())
                .get(Scanners.SubTypes.of(SpeedLimitModule.class).asClass())
                .stream()
                .filter(clazz -> !clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers()))
                .map(clazz -> (Class<SpeedLimitModule>) clazz)
                .sorted(Comparator.comparing(Class::getSimpleName))
                .collect(Collectors.collectingAndThen(Collectors.toList(), ImmutableSet::copyOf));
        ENABLED_MODULES = new EnumMap<>(FlightType.class);
    }

    protected final FlySpeedLimits plugin = FlySpeedLimits.getInstance();
    protected final GracefulScheduling scheduling = FlySpeedLimits.scheduling();
    protected final Config config = FlySpeedLimits.config();

    protected final FlightType flightType;
    protected final String configPath, logFormat;
    protected final List<LocationConfig> configuredLocations;
    protected final boolean enabledInConfig;

    public SpeedLimitModule(FlightType flightType, String configPath, boolean defEnabled) {
        this.flightType = flightType;
        this.configPath = configPath;
        this.enabledInConfig = config.getBoolean(configPath + ".enable", defEnabled);
        this.configuredLocations = LocationConfig.readInOrder(configPath);
        final String[] paths = configPath.split("\\.");
        this.logFormat = paths.length <= 2 ? "<" + configPath + "> {}" : "<" + paths[paths.length - 2] + "." + paths[paths.length - 1] + "> {}";
    }

    public FlightType getFlightType() {
        return flightType;
    }

    public boolean shouldEnable() {
        return enabledInConfig && !configuredLocations.isEmpty();
    }

    public @Nullable LocationConfig getConfigAt(Location location) {
        for (LocationConfig locationConfig : configuredLocations) {
            if (locationConfig.appliesAt(location)) {
                return locationConfig;
            }
        }
        return null;
    }

    public static void disableAll() {
        ENABLED_MODULES.forEach((flightType1, speedLimitModule) -> speedLimitModule.disable());
        ENABLED_MODULES.clear();
    }

    public static void reloadModules() {
        disableAll();

        for (Class<SpeedLimitModule> moduleClass : AVAILABLE_MODULES) {
            try {
                SpeedLimitModule module = moduleClass.getDeclaredConstructor().newInstance();
                if (module.shouldEnable()) {
                    ENABLED_MODULES.put(module.getFlightType(), module);
                }
            } catch (Throwable t) { // We want to catch everything here if it fails to init
                FlySpeedLimits.logger().warn("Failed initialising module class '{}'.", moduleClass.getSimpleName(), t);
            }
        }

        ENABLED_MODULES.forEach((flightType1, speedLimitModule) -> speedLimitModule.enable());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void on(PlayerMoveEvent event) {
        WrappedPlayer wrappedPlayer = PlayerManager.getPlayer(event);
        if (!isFlying(wrappedPlayer)) return;

        LocationConfig locationConfig = getConfigAt(wrappedPlayer.player.getLocation());
        if (locationConfig == null) return;

        if (locationConfig.flight.denyFlight || locationConfig.flight.shouldDenyDueToLag()) {
            onPlayerFlightDenied(wrappedPlayer, locationConfig);
            return;
        }

        if (wrappedPlayer.isInNewChunks()) {
            if (locationConfig.flight.canBurstNewChunks()) {
                if (wrappedPlayer.getXZSpeedSquared(locationConfig.flight.speedUnit) > locationConfig.flight.newChunksXZBurstSpeed) {
                    onPlayerExceedSpeedNewChunksBurst(wrappedPlayer, locationConfig);
                } else {
                    onPlayerFlyNewChunksBurst(wrappedPlayer, locationConfig);
                }
            }

            else {
                if (wrappedPlayer.getXZSpeedSquared(locationConfig.flight.speedUnit) > locationConfig.flight.newChunksXZSpeed) {
                    onPlayerExceedSpeedNewChunks(wrappedPlayer, locationConfig);
                } else {
                    onPlayerFlyNewChunks(wrappedPlayer, locationConfig);
                }
            }
        }

        else {
            if (locationConfig.flight.canBurstOldChunks()) {
                if (wrappedPlayer.getXZSpeedSquared(locationConfig.flight.speedUnit) > locationConfig.flight.oldChunksXZBurstSpeed) {
                    onPlayerExceedSpeedOldChunksBurst(wrappedPlayer, locationConfig);
                } else {
                    onPlayerFlyOldChunksBurst(wrappedPlayer, locationConfig);
                }
            }

            else {
                if (wrappedPlayer.getXZSpeedSquared(locationConfig.flight.speedUnit) > locationConfig.flight.oldChunksXZSpeed) {
                    onPlayerExceedSpeedOldChunks(wrappedPlayer, locationConfig);
                } else {
                    onPlayerFlyOldChunks(wrappedPlayer, locationConfig);
                }
            }
        }
    }

    public abstract boolean isFlying(WrappedPlayer wrappedPlayer);
    public abstract void onPlayerFlightDenied(WrappedPlayer wrappedPlayer, LocationConfig locationConfig);
    public abstract void onPlayerExceedSpeedNewChunksBurst(WrappedPlayer wrappedPlayer, LocationConfig locationConfig);
    public abstract void onPlayerFlyNewChunksBurst(WrappedPlayer wrappedPlayer, LocationConfig locationConfig);
    public abstract void onPlayerExceedSpeedNewChunks(WrappedPlayer wrappedPlayer, LocationConfig locationConfig);
    public abstract void onPlayerFlyNewChunks(WrappedPlayer wrappedPlayer, LocationConfig locationConfig);
    public abstract void onPlayerExceedSpeedOldChunksBurst(WrappedPlayer wrappedPlayer, LocationConfig locationConfig);
    public abstract void onPlayerFlyOldChunksBurst(WrappedPlayer wrappedPlayer, LocationConfig locationConfig);
    public abstract void onPlayerExceedSpeedOldChunks(WrappedPlayer wrappedPlayer, LocationConfig locationConfig);
    public abstract void onPlayerFlyOldChunks(WrappedPlayer wrappedPlayer, LocationConfig locationConfig);

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
