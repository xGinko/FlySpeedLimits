package me.xginko.flyspeedlimits.config;

import me.xginko.flyspeedlimits.FlySpeedLimits;
import me.xginko.flyspeedlimits.struct.SpeedUnit;

public class FlightConfig {

    public final SpeedUnit speedUnit;

    public final double denyTPS, denyMSPT,
            oldChunksXZSpeed, oldChunksXZBurstSpeed, oldChunksYSpeed, oldChunksYBurstSpeed, oldChunksBurstTPS, oldChunksBurstMSPT,
            newChunksXZSpeed, newChunksXZBurstSpeed, newChunksYSpeed, newChunksYBurstSpeed, newChunksBurstTPS, newChunksBurstMSPT;

    public final boolean denyFlight, denyDuringLowTPS, oldChunksXZBurstEnabled, newChunksXZBurstEnabled;

    public FlightConfig(String configPath) {
        Config config = FlySpeedLimits.config();
        this.denyFlight = config.getBoolean(configPath + ".deny-flight.fully", false);
        this.denyDuringLowTPS = config.getBoolean(configPath + ".deny-flight.during-server-lag.enable", true);
        this.denyTPS = config.getDouble(configPath + ".deny-flight.during-server-lag.tps-limit", 12.0);
        this.denyMSPT = config.getDouble(configPath + ".deny-flight.during-server-lag.mspt-limit", 250.0);

        SpeedUnit configuredUnit;
        try {
            configuredUnit = SpeedUnit.valueOf(config.getString(configPath + ".speed-unit", "BLOCKS_PER_TICK"));
        } catch (IllegalArgumentException e) {
            configuredUnit = SpeedUnit.BLOCKS_PER_TICK;
        }
        this.speedUnit = configuredUnit;

        this.oldChunksXZSpeed = config.getDouble(configPath + "old-chunks.speed-xz", 2.4);
        this.oldChunksYSpeed = config.getDouble(configPath + "old-chunks.speed-y", 2.4);
        this.oldChunksXZBurstEnabled = config.getBoolean(configPath + "old-chunks.burst.enable", true);
        this.oldChunksXZBurstSpeed = config.getDouble(configPath + "old-chunks.burst.speed-xz", 5.0);
        this.oldChunksYBurstSpeed = config.getDouble(configPath + "old-chunks.burst.speed-y", 5.0);
        this.oldChunksBurstTPS = config.getDouble(configPath + ".old-chunks.burst.tps-limit", 16.0);
        this.oldChunksBurstMSPT = config.getDouble(configPath + ".old-chunks.burst.mspt-limit", 140.0);

        this.newChunksXZSpeed = config.getDouble(configPath + "new-chunks.speed-xz", 2.4);
        this.newChunksYSpeed = config.getDouble(configPath + "new-chunks.speed-y", 2.4);
        this.newChunksXZBurstEnabled = config.getBoolean(configPath + "new-chunks.burst.enable", true);
        this.newChunksXZBurstSpeed = config.getDouble(configPath + ".new-chunks.burst.speed-xz", 3.4);
        this.newChunksYBurstSpeed = config.getDouble(configPath + "new-chunks.burst.speed-y", 3.4);
        this.newChunksBurstTPS = config.getDouble(configPath + ".new-chunks.burst.tps-limit", 16.0);
        this.newChunksBurstMSPT = config.getDouble(configPath + ".new-chunks.burst.mspt-limit", 140.0);
    }

    public boolean shouldDenyDueToLag() {
        return  denyDuringLowTPS
                && (FlySpeedLimits.getTickReporter().getTPS() < denyTPS
                || FlySpeedLimits.getTickReporter().getMSPT() > denyMSPT);
    }

    public boolean canBurstOldChunks() {
        return  oldChunksXZBurstEnabled
                && FlySpeedLimits.getTickReporter().getTPS() >= oldChunksBurstTPS
                && FlySpeedLimits.getTickReporter().getMSPT() <= oldChunksBurstMSPT;
    }

    public boolean canBurstNewChunks() {
        return  newChunksXZBurstEnabled
                && FlySpeedLimits.getTickReporter().getTPS() >= newChunksBurstTPS
                && FlySpeedLimits.getTickReporter().getMSPT() <= newChunksBurstMSPT;
    }
}
