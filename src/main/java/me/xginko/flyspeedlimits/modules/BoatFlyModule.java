package me.xginko.flyspeedlimits.modules;

import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

public class BoatFlyModule extends SpeedLimitModule implements Listener {

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
