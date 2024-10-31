package me.xginko.flyspeedlimits.modules;

import com.cryptomorin.xseries.XEntityType;
import com.cryptomorin.xseries.XMaterial;
import com.github.benmanes.caffeine.cache.Caffeine;
import me.xginko.flyspeedlimits.manager.WrappedPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityToggleGlideEvent;

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
    public boolean checkPreconditions(WrappedPlayer wrappedPlayer) {
        return  wrappedPlayer.player.isGliding()
                || packetFlyListener.hasRecentlyToggledGlide(wrappedPlayer.player.getUniqueId());
    }

    @Override
    public void onPlayerFlightDenied(WrappedPlayer wrappedPlayer) {
        wrappedPlayer.teleportAsync(wrappedPlayer.periodicFrom);
        wrappedPlayer.dropElytra();
    }

    @Override
    public void onPlayerExceedSpeedNewChunksBurst(WrappedPlayer wrappedPlayer) {

    }

    @Override
    public void onPlayerFlyNewChunksBurst(WrappedPlayer wrappedPlayer) {

    }

    @Override
    public void onPlayerExceedSpeedNewChunks(WrappedPlayer wrappedPlayer) {

    }

    @Override
    public void onPlayerFlyNewChunks(WrappedPlayer wrappedPlayer) {

    }

    @Override
    public void onPlayerExceedSpeedOldChunksBurst(WrappedPlayer wrappedPlayer) {

    }

    @Override
    public void onPlayerFlyOldChunksBurst(WrappedPlayer wrappedPlayer) {

    }

    @Override
    public void onPlayerExceedSpeedOldChunks(WrappedPlayer wrappedPlayer) {

    }

    @Override
    public void onPlayerFlyOldChunks(WrappedPlayer wrappedPlayer) {

    }

    private static class ElytraPacketFlyListener implements Listener {

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
