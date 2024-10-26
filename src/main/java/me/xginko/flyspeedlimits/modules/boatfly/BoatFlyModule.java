package me.xginko.flyspeedlimits.modules.boatfly;

import me.xginko.flyspeedlimits.modules.SpeedLimitModule;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

public class BoatFlyModule extends SpeedLimitModule implements Listener {

    /**
     * implement boatfly for now, do abstraction later when learnt from that
     */

    public BoatFlyModule() {
        super("speeds.boat", false);
    }

    @Override
    public void disable() {
        HandlerList.unregisterAll(this);
    }

    @Override
    public void enable() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }


}
