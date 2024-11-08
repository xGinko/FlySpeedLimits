package me.xginko.flyspeedlimits.modules;

import com.cryptomorin.xseries.XEntityType;
import com.cryptomorin.xseries.XMaterial;
import com.github.benmanes.caffeine.cache.Caffeine;
import me.xginko.flyspeedlimits.config.LocationConfig;
import me.xginko.flyspeedlimits.manager.WrappedPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.util.Vector;

import java.time.Duration;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

public class ElytraSpeedLimit extends SpeedLimitModule implements Listener {

    private final ElytraPacketFlyListener packetFlyListener;

    public ElytraSpeedLimit() {
        super(FlightType.ELYTRA, "speeds.elytra", true);
        this.packetFlyListener = new ElytraPacketFlyListener(Duration.ofMillis(
                config.getLong(configPath + ".packet-fly-max-delay-millis", 2000L)));
    }

    @Override
    public void enable() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void disable() {
        HandlerList.unregisterAll(this);
    }

    @Override
    public boolean shouldEnable() {
        return enabledInConfig && XMaterial.ELYTRA.isSupported();
    }

    @Override
    public boolean isFlying(WrappedPlayer wrappedPlayer) {
        return  wrappedPlayer.player.isGliding()
                || packetFlyListener.hasRecentlyToggledGlide(wrappedPlayer.player.getUniqueId());
    }

    @Override
    public void onPlayerFlightDenied(WrappedPlayer wrappedPlayer, LocationConfig locationConfig) {
        wrappedPlayer.player.setVelocity(new Vector());
        wrappedPlayer.teleportAsync(wrappedPlayer.periodicFrom);
        wrappedPlayer.dropElytra();
    }

    @Override
    public void onPlayerExceedSpeedNewChunksBurst(WrappedPlayer wrappedPlayer, LocationConfig locationConfig) {
        wrappedPlayer.sendMessage(wrappedPlayer.translations().new_chunks_burst_exceed
                .get(flightType, wrappedPlayer.getXZSpeed(locationConfig.flight.speedUnit), locationConfig.flight.newChunksXZBurstSpeed));
        wrappedPlayer.teleportAsync(wrappedPlayer.periodicFrom);
    }

    @Override
    public void onPlayerFlyNewChunksBurst(WrappedPlayer wrappedPlayer, LocationConfig locationConfig) {
        wrappedPlayer.sendMessage(wrappedPlayer.translations().new_chunks_burst_flight
                .get(flightType, wrappedPlayer.getXZSpeed(locationConfig.flight.speedUnit), locationConfig.flight.newChunksXZBurstSpeed));
    }

    @Override
    public void onPlayerExceedSpeedNewChunks(WrappedPlayer wrappedPlayer, LocationConfig locationConfig) {
        wrappedPlayer.sendMessage(wrappedPlayer.translations().new_chunks_exceed
                .get(flightType, wrappedPlayer.getXZSpeed(locationConfig.flight.speedUnit), locationConfig.flight.newChunksXZBurstSpeed));
        wrappedPlayer.teleportAsync(wrappedPlayer.periodicFrom);
    }

    @Override
    public void onPlayerFlyNewChunks(WrappedPlayer wrappedPlayer, LocationConfig locationConfig) {
        wrappedPlayer.sendMessage(wrappedPlayer.translations().new_chunks_flight
                .get(flightType, wrappedPlayer.getXZSpeed(locationConfig.flight.speedUnit), locationConfig.flight.newChunksXZBurstSpeed));
    }

    @Override
    public void onPlayerExceedSpeedOldChunksBurst(WrappedPlayer wrappedPlayer, LocationConfig locationConfig) {
        wrappedPlayer.sendMessage(wrappedPlayer.translations().old_chunks_burst_exceed
                .get(flightType, wrappedPlayer.getXZSpeed(locationConfig.flight.speedUnit), locationConfig.flight.newChunksXZBurstSpeed));
        wrappedPlayer.teleportAsync(wrappedPlayer.periodicFrom);
    }

    @Override
    public void onPlayerFlyOldChunksBurst(WrappedPlayer wrappedPlayer, LocationConfig locationConfig) {
        wrappedPlayer.sendMessage(wrappedPlayer.translations().old_chunks_burst_flight
                .get(flightType, wrappedPlayer.getXZSpeed(locationConfig.flight.speedUnit), locationConfig.flight.newChunksXZBurstSpeed));
    }

    @Override
    public void onPlayerExceedSpeedOldChunks(WrappedPlayer wrappedPlayer, LocationConfig locationConfig) {
        wrappedPlayer.sendMessage(wrappedPlayer.translations().old_chunks_exceed
                .get(flightType, wrappedPlayer.getXZSpeed(locationConfig.flight.speedUnit), locationConfig.flight.newChunksXZBurstSpeed));
        wrappedPlayer.teleportAsync(wrappedPlayer.periodicFrom);
    }

    @Override
    public void onPlayerFlyOldChunks(WrappedPlayer wrappedPlayer, LocationConfig locationConfig) {
        wrappedPlayer.sendMessage(wrappedPlayer.translations().old_chunks_flight
                .get(flightType, wrappedPlayer.getXZSpeed(locationConfig.flight.speedUnit), locationConfig.flight.newChunksXZBurstSpeed));
    }

    private static class ElytraPacketFlyListener implements Listener {
        // Yeah maybe this is a bit of a lazy solution. Could there be a better pattern to look for maybe?

        private final Set<UUID> toggleGlideCache;

        private ElytraPacketFlyListener(Duration duration) {
            this.toggleGlideCache = Collections.newSetFromMap(Caffeine.newBuilder()
                    .expireAfterWrite(duration).<UUID, Boolean>build().asMap());
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        private void on(EntityToggleGlideEvent event) {
            if (event.getEntityType() == XEntityType.PLAYER.get()) {
                this.toggleGlideCache.add(event.getEntity().getUniqueId());
            }
        }

        private boolean hasRecentlyToggledGlide(UUID player) {
            return this.toggleGlideCache.contains(player);
        }
    }
}
