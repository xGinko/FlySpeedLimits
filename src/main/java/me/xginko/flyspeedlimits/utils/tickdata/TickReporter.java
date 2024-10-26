package me.xginko.flyspeedlimits.utils.tickdata;

import io.papermc.paper.threadedregions.scheduler.EntityScheduler;
import io.papermc.paper.threadedregions.scheduler.RegionScheduler;
import me.xginko.flyspeedlimits.struct.Disableable;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Duration;

public interface TickReporter extends Disableable {

    static TickReporter create(JavaPlugin plugin, Duration cacheDuration) {
        if (FoliaTickReporter.isSupported()) {
            return new FoliaTickReporter(cacheDuration);
        }

        if (ModernPaperTickReporter.isSupported()) {
            return new ModernPaperTickReporter(plugin, cacheDuration);
        }

        if (LegacyPaperTickReporter.isSupported()) {
            return new LegacyPaperTickReporter(plugin, cacheDuration);
        }

        return new FallbackTickReporter(cacheDuration);
    }

    /**
     * @return The most recent global TPS
     */
    double getGlobalTPS();

    /**
     * Folia note:
     * This method needs to be called from the same thread as the region you would like to get the TPS of.
     * The recommended way of doing so is by using either the {@link RegionScheduler} or {@link EntityScheduler}.
     * It can be called from within an event as well but there's no guarantee that the TPS for the correct region
     * is returned (Read the Folia README).
     *
     * @return The TPS of the region this method was called from, otherwise the global TPS
     */
    double getTPS();

    /**
     * @return The most recent average global tick time (MSPT)
     */
    double getGlobalMSPT();

    /**
     * Folia note:
     * This method needs to be called from the same thread as the region you would like to get the MSPT of.
     * The recommended way of doing so is by using either the {@link RegionScheduler} or {@link EntityScheduler}.
     * It can be called from within an event as well but there's no guarantee that the MSPT for the correct region
     * is returned (Read the Folia README).
     *
     * @return The average tick time (MSPT) of the region this method was called from, otherwise the global MSPT
     */
    double getMSPT();

}