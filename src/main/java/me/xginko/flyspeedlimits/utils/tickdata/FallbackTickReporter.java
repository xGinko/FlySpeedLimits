package me.xginko.flyspeedlimits.utils.tickdata;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.time.Duration;

@SuppressWarnings("DataFlowIssue")
public final class FallbackTickReporter implements TickReporter {

    private final Cache<Boolean, Double> cache;
    private final SpigotReflection spigotReflection;

    public FallbackTickReporter(Duration cacheDuration) {
        this.cache = Caffeine.newBuilder().expireAfterWrite(cacheDuration).build();
        this.spigotReflection = SpigotReflection.getInstance();
    }

    @Override
    public void disable() {
        cache.invalidateAll();
        cache.cleanUp();
    }

    @Override
    public double getGlobalTPS() {
        return cache.get(true, k -> spigotReflection.getTPS()[0]);
    }

    @Override
    public double getTPS() {
        return getGlobalTPS();
    }

    @Override
    public double getGlobalMSPT() {
        return cache.get(false, k -> spigotReflection.getAverageTickTime());
    }

    @Override
    public double getMSPT() {
        return getGlobalMSPT();
    }
}