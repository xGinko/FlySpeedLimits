package me.xginko.flyspeedlimits.utils.tickdata;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.papermc.paper.threadedregions.RegionizedServer;
import io.papermc.paper.threadedregions.ThreadedRegionizer;
import io.papermc.paper.threadedregions.TickRegionScheduler;
import io.papermc.paper.threadedregions.TickRegions;
import me.xginko.flyspeedlimits.utils.Crafty;

import java.time.Duration;

@SuppressWarnings("DataFlowIssue")
public final class FoliaTickReporter implements TickReporter {

    private final Cache<TickRegionScheduler.RegionScheduleHandle, Double> tps_cache, mspt_cache;

    public FoliaTickReporter(Duration cacheTime) {
        this.tps_cache = Caffeine.newBuilder().expireAfterWrite(cacheTime).build();
        this.mspt_cache = Caffeine.newBuilder().expireAfterWrite(cacheTime).build();
    }

    public static boolean isSupported() {
        return      Crafty.hasClass("io.papermc.paper.threadedregions.RegionizedServer")
                &&  Crafty.hasClass("io.papermc.paper.threadedregions.ThreadedRegionizer")
                &&  Crafty.hasClass("io.papermc.paper.threadedregions.TickRegionScheduler")
                &&  Crafty.hasClass("io.papermc.paper.threadedregions.TickRegions");
    }

    @Override
    public void disable() {
        tps_cache.invalidateAll();
        tps_cache.cleanUp();
        mspt_cache.invalidateAll();
        mspt_cache.cleanUp();
    }

    @Override
    public double getGlobalTPS() {
        return tps_cache.get(RegionizedServer.getGlobalTickData(), regionScheduleHandle ->
                regionScheduleHandle.getTickReport5s(System.nanoTime()).tpsData().segmentAll().average());
    }

    @Override
    public double getTPS() {
        final ThreadedRegionizer.ThreadedRegion<TickRegions.TickRegionData, TickRegions.TickRegionSectionData>
                region = TickRegionScheduler.getCurrentRegion();
        if (region == null)
            return getGlobalTPS();
        return tps_cache.get(region.getData().getRegionSchedulingHandle(), regionScheduleHandle ->
                regionScheduleHandle.getTickReport5s(System.nanoTime()).tpsData().segmentAll().average());
    }

    @Override
    public double getGlobalMSPT() {
        return mspt_cache.get(RegionizedServer.getGlobalTickData(), regionScheduleHandle ->
                regionScheduleHandle.getTickReport5s(System.nanoTime()).timePerTickData().segmentAll().average() / 1000000);
    }

    @Override
    public double getMSPT() {
        final ThreadedRegionizer.ThreadedRegion<TickRegions.TickRegionData, TickRegions.TickRegionSectionData>
                region = TickRegionScheduler.getCurrentRegion();
        if (region == null)
            return getGlobalMSPT();
        return mspt_cache.get(region.getData().getRegionSchedulingHandle(), regionScheduleHandle ->
                regionScheduleHandle.getTickReport5s(System.nanoTime()).timePerTickData().segmentAll().average() / 1000000);
    }
}
