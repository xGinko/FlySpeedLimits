package me.xginko.flyspeedlimits.modules;

import me.xginko.flyspeedlimits.manager.WrappedPlayer;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

public class ElytraSpeedLimit extends SpeedLimitModule implements Listener {
    
    public ElytraSpeedLimit() {
        super("speeds.elytra", true);
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
    public boolean checkPreconditions(WrappedPlayer wrappedPlayer) {
        return false;
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
}
