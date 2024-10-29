package me.xginko.flyspeedlimits.modules;

import me.xginko.flyspeedlimits.config.LocationConfig;
import me.xginko.flyspeedlimits.manager.PlayerManager;
import me.xginko.flyspeedlimits.manager.WrappedPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.ArrayList;
import java.util.List;

public class ElytraSpeedLimit extends SpeedLimitModule implements Listener {

    private final List<LocationConfig> locations;
    
    public ElytraSpeedLimit() {
        super("speeds.elytra", true);
        this.locations = new ArrayList<>();
        LocationConfig.addExamples(configPath);
        for (String key : config.master().getConfigSection(configPath).getKeys(false)) {
            locations.add(new LocationConfig(configPath + "." + key));
        }
    }

    @Override
    public void enable() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public boolean shouldEnable() {
        return true;
    }

    @Override
    public void disable() {
        HandlerList.unregisterAll(this);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void on(PlayerMoveEvent event) {
        WrappedPlayer wrappedPlayer = PlayerManager.getPlayer(event);
        if (!wrappedPlayer.isFlying()) return; // Not sure if I like this

        for (LocationConfig locationConfig : locations) {
            if (!locationConfig.appliesAt(wrappedPlayer.player.getLocation())) continue;

            if (locationConfig.flight.denyFlight || locationConfig.flight.shouldDenyDueToLag()) {
                event.setCancelled(true);
                // Missing config toggle
                wrappedPlayer.dropElytra();
                return;
            }

            if (wrappedPlayer.isInNewChunks()) {
                if (locationConfig.flight.canBurstNewChunks()) {
                    if (wrappedPlayer.getXZSpeedSquared(locationConfig.flight.speedUnit) > locationConfig.flight.newChunksXZBurstSpeed) {
                        event.setCancelled(true);

                    } else {

                    }
                } else {
                    if (wrappedPlayer.getXZSpeedSquared(locationConfig.flight.speedUnit) > locationConfig.flight.newChunksXZSpeed) {
                        event.setCancelled(true);

                    } else {

                    }
                }
            } else {
                if (locationConfig.flight.canBurstOldChunks()) {
                    if (wrappedPlayer.getXZSpeedSquared(locationConfig.flight.speedUnit) > locationConfig.flight.oldChunksXZBurstSpeed) {
                        event.setCancelled(true);

                    } else {

                    }
                } else {
                    if (wrappedPlayer.getXZSpeedSquared(locationConfig.flight.speedUnit) > locationConfig.flight.oldChunksXZSpeed) {
                        event.setCancelled(true);

                    } else {

                    }
                }
            }

            return;
        }
    }
}
