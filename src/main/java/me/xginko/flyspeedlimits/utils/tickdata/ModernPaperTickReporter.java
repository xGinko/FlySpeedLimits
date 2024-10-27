package me.xginko.flyspeedlimits.utils.tickdata;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import me.xginko.flyspeedlimits.utils.Crafty;
import org.bukkit.Server;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Duration;

@SuppressWarnings("DataFlowIssue")
public final class ModernPaperTickReporter implements TickReporter {

    private final Server server;
    private final Cache<Boolean, Double> cache;

    public ModernPaperTickReporter(JavaPlugin plugin, Duration cacheTime) {
        this.server = plugin.getServer();
        this.cache = Caffeine.newBuilder().expireAfterWrite(cacheTime).build();
    }

    public static boolean isSupported() {
        return Crafty.hasMethod(Server.class, "getAverageTickTime");
    }

    @Override
    public void disable() {
        cache.invalidateAll();
        cache.cleanUp();
    }

    @Override
    public double getGlobalTPS() {
        return cache.get(true, k -> server.getTPS()[0]);
    }

    @Override
    public double getTPS() {
        return getGlobalTPS();
    }

    @Override
    public double getGlobalMSPT() {
        return cache.get(false, k -> server.getAverageTickTime());
    }

    @Override
    public double getMSPT() {
        return getGlobalMSPT();
    }
}
