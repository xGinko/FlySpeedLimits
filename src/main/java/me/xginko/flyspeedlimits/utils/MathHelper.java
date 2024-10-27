package me.xginko.flyspeedlimits.utils;

import me.xginko.flyspeedlimits.FlySpeedLimits;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;

public class MathHelper {

    public static double toDistancePerMillis(double distance, long millis) {
        return (distance / FlySpeedLimits.config().checkIntervalMillis) * millis;
    }

    public static double getBlockDistanceTo00Squared(Location location) {
        return square(location.getX(), location.getZ());
    }

    public static double getBlockDistanceYSquared(Location from, Location to) {
        return square(from.getY() - to.getY());
    }

    public static double getBlockDistanceXZSquared(Location from, Location to) {
        double toX = to.getX();
        double toZ = to.getZ();
        double fromX = from.getX();
        double fromZ = from.getZ();

        if (to.getWorld().getEnvironment() != from.getWorld().getEnvironment()) {
            if (from.getWorld().getEnvironment() == World.Environment.NETHER) {
                fromX *= 8;
                fromZ *= 8;
            }
            if (to.getWorld().getEnvironment() == World.Environment.NETHER) {
                toX *= 8;
                toZ *= 8;
            }
        }

        return square(toX - fromX, toZ - fromZ);
    }

    public static double getBlockDistanceXYZSquared(Location from, Location to) {
        double toX = to.getX();
        double toZ = to.getZ();
        double fromX = from.getX();
        double fromZ = from.getZ();

        if (to.getWorld().getEnvironment() != from.getWorld().getEnvironment()) {
            if (from.getWorld().getEnvironment() == World.Environment.NETHER) {
                fromX *= 8;
                fromZ *= 8;
            }
            if (to.getWorld().getEnvironment() == World.Environment.NETHER) {
                toX *= 8;
                toZ *= 8;
            }
        }

        return square(toX - fromX, from.getY() - to.getY(), toZ - fromZ);
    }

    public static double getChunkDistanceSquared(Chunk chunk1, Chunk chunk2) {
        return square(
                chunk1.getX() - chunk2.getX(),
                chunk1.getZ() - chunk2.getZ());
    }

    public static double getChunkDistanceSquared(Chunk chunk, Location location) {
        return square(
                chunk.getX() - (location.getBlockX() >> 4),
                chunk.getZ() - (location.getBlockZ() >> 4));
    }

    public static double getChunkDistanceSquared(Location location1, Location location2) {
        return square(
                (location1.getBlockX() >> 4) - (location2.getBlockX() >> 4),
                (location1.getBlockZ() >> 4) - (location2.getBlockZ() >> 4));
    }

    public static float quakeSqrt(float x) {
        float xHalf = 0.5F * x;
        int i = Float.floatToIntBits(x); // Get the bits of the float
        i = 0x5F3759DF - (i >> 1);       // Magic number and bit manipulation
        x = Float.intBitsToFloat(i);     // Convert bits back to float
        x = x * (1.5F - xHalf * x * x);  // Newton’s iteration for refinement
        return 1 / x;                    // Return the approximate square root
    }

    public static double square(double delta) {
        return delta * delta;
    }

    // Requires -XX:+UseFMA in flags
    public static double square(double deltaX, double deltaZ) {
        return org.joml.Math.fma(deltaX, deltaX, deltaZ * deltaZ);
    }

    // Requires -XX:+UseFMA in flags
    public static double square(double deltaX, double deltaY, double deltaZ) {
        return org.joml.Math.fma(deltaX, deltaX, org.joml.Math.fma(deltaY, deltaY, deltaZ * deltaZ));
    }
}
