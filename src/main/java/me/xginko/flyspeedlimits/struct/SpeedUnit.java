package me.xginko.flyspeedlimits.struct;

import me.xginko.flyspeedlimits.FlySpeedLimits;

public enum SpeedUnit {

    BLOCKS_PER_TICK {
        public double fromDistance(double rawDist) {
            return toBlocksPerMillis(rawDist, 50);
        }
    },

    BLOCKS_PER_SECOND {
        public double fromDistance(double rawDist) {
            return toBlocksPerMillis(rawDist, 1000);
        }
    };

    public double fromDistance(double rawDist) {
        throw new AbstractMethodError();
    }

    private static double toBlocksPerMillis(double blocks, long millis) {
        return (blocks / FlySpeedLimits.config().checkIntervalMillis) * millis;
    }
}
