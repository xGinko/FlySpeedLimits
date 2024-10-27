package me.xginko.flyspeedlimits.manager;

import me.xginko.flyspeedlimits.FlySpeedLimits;
import me.xginko.flyspeedlimits.events.WrappedPlayerUpdateEvent;
import me.xginko.flyspeedlimits.struct.Disableable;
import me.xginko.flyspeedlimits.struct.Enableable;
import me.xginko.flyspeedlimits.utils.Crafty;
import me.xginko.flyspeedlimits.utils.MathHelper;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

import java.util.Map;
import java.util.UUID;

public class ChunkListener implements Enableable, Disableable, Listener {

    private static final boolean GET_INHABITED_TIME_AVAILABLE = Crafty.hasMethod(Chunk.class, "getInhabitedTime");

    private AlternativeNewChunksListener altNewChunksListener;

    @Override
    public void enable() {
        FlySpeedLimits plugin = FlySpeedLimits.getInstance();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        if (!GET_INHABITED_TIME_AVAILABLE) {
            altNewChunksListener = new AlternativeNewChunksListener();
            altNewChunksListener.enable();
        }
    }

    @Override
    public void disable() {
        HandlerList.unregisterAll(this);
        if (altNewChunksListener != null) {
            altNewChunksListener.disable();
            altNewChunksListener = null;
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    private void on(WrappedPlayerUpdateEvent event) {
        WrappedPlayer wrappedPlayer = event.getWrappedPlayer();

        if (GET_INHABITED_TIME_AVAILABLE) {
            wrappedPlayer.setInNewChunks(wrappedPlayer.player
                    .getChunk().getInhabitedTime() <= FlySpeedLimits.config().newChunkMaxInhTimeTicks);
        }

        if (wrappedPlayer.isFlying()) {
            // If flying, adjust player view distance to hopefully reduce chunk generation

        } else {
            // Set view distance back to normal

        }
    }

    private static class AlternativeNewChunksListener implements Enableable, Disableable, Listener {

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        private void on(ChunkLoadEvent event) {
            for (Map.Entry<UUID, WrappedPlayer> entry : PlayerManager.getPlayers().entrySet()) {
                if (isInRenderDistance(event.getChunk(), entry.getValue().player)) {
                    entry.getValue().setInNewChunks(event.isNewChunk());
                }
            }
        }

        private boolean isInRenderDistance(Chunk chunk, Player player) {
            return MathHelper.getChunkDistanceSquared(chunk, player.getLocation()) <= MathHelper.square(player.getViewDistance());
        }

        @Override
        public void enable() {
            FlySpeedLimits plugin = FlySpeedLimits.getInstance();
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
        }

        @Override
        public void disable() {
            HandlerList.unregisterAll(this);
        }
    }
}
