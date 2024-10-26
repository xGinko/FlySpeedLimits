package me.xginko.flyspeedlimits.utils;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.NumberConversions;

public class MathHelper {

    public static double getBlockDistanceTo00Squared(Location location) {
        return getDistanceSquaredFMA(location.getX(), location.getZ());
    }

    public static double getBlockDistanceYSquared(Location from, Location to) {
        return NumberConversions.square(from.getY() - to.getY());
    }

    public static double getBlockDistanceXZSquared(Location from, Location to) {
        float toX = (float) to.getX();
        float toZ = (float) to.getZ();
        float fromX = (float) from.getX();
        float fromZ = (float) from.getZ();

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

        return getDistanceSquaredFMA(toX - fromX, toZ - fromZ);
    }

    public static double getBlockDistanceXYZSquared(Location from, Location to) {
        float toX = (float) to.getX();
        float toZ = (float) to.getZ();
        float fromX = (float) from.getX();
        float fromZ = (float) from.getZ();

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

        return getDistanceSquaredFMA(toX - fromX, (float) (from.getY() - to.getY()), toZ - fromZ);
    }

    public static double getChunkDistanceSquared(Chunk chunk1, Chunk chunk2) {
        return getDistanceSquaredFMA(
                chunk1.getX() - chunk2.getX(),
                chunk1.getZ() - chunk2.getZ());
    }

    public static double getChunkDistanceSquared(Chunk chunk, Location location) {
        return getDistanceSquaredFMA(
                chunk.getX() - (location.getBlockX() >> 4),
                chunk.getZ() - (location.getBlockZ() >> 4));
    }

    public static double getChunkDistanceSquared(Location location1, Location location2) {
        return getDistanceSquaredFMA(
                (location1.getBlockX() >> 4) - (location2.getBlockX() >> 4),
                (location1.getBlockZ() >> 4) - (location2.getBlockZ() >> 4));
    }

    // Fused multiply-add :o
    // Requires -XX:+UseFMA in flags
    private static double getDistanceSquaredFMA(double deltaX, double deltaZ) {
        return org.joml.Math.fma(deltaX, deltaX, deltaZ * deltaZ);
    }

    private static double getDistanceSquaredFMA(double deltaX, double deltaY, double deltaZ) {
        return org.joml.Math.fma(deltaX, deltaX, org.joml.Math.fma(deltaY, deltaY, deltaZ * deltaZ));
    }
}
