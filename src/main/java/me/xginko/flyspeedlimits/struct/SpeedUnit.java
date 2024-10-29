package me.xginko.flyspeedlimits.struct;

import me.xginko.flyspeedlimits.FlySpeedLimits;

public enum SpeedUnit {

    BLOCKS_PER_TICK("bpt") {
        public double fromDistance(double rawDist) {
            return toBlocksPerMillis(rawDist, 50);
        }
    },

    BLOCKS_PER_SECOND("bps") {
        public double fromDistance(double rawDist) {
            return toBlocksPerMillis(rawDist, 1000);
        }
    };

    private final String suffix;

    SpeedUnit(String suffix) {
        this.suffix = suffix;
    }

    public double fromDistance(double rawDist) {
        throw new AbstractMethodError();
    }

    public String getSuffix() {
        return suffix;
    }

    private static double toBlocksPerMillis(double blocks, long millis) {
        return (blocks / FlySpeedLimits.config().checkIntervalMillis) * millis;
    }
}
