package me.xginko.flyspeedlimits.manager;

import com.github.retrooper.packetevents.util.Vector3d;
import me.xginko.flyspeedlimits.events.WrappedPlayerUpdateEvent;
import me.xginko.flyspeedlimits.utils.MathHelper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class WrappedPlayer {

    public final @NotNull Player player;
    public final @NotNull Location periodicFrom;
    public final @NotNull Location mostRecentTo;

    private @NotNull FlyingState flyingState;

    private double periodicXZDistSquared;
    private double periodicXZDist;
    private double periodicYDistSquared;
    private double periodicYDist;

    private boolean inNewChunks;

    private WrappedPlayer(@NotNull Player player, @NotNull Location from, @NotNull Location to) {
        this.player = player;
        this.periodicFrom = from;
        this.mostRecentTo = to;
        this.flyingState = FlyingState.NONE;
        this.periodicXZDistSquared = 0.0D;
        this.periodicYDistSquared = 0.0D;
        this.periodicXZDist = 0.0D;
        this.periodicYDist = 0.0D;
        this.inNewChunks = false;
    }

    protected static @NotNull WrappedPlayer of(@NotNull Player player, @NotNull Location from, @NotNull Location to) {
        return new WrappedPlayer(player, from, to);
    }

    protected static @NotNull WrappedPlayer of(@NotNull Player player) {
        return of(player, player.getLocation().clone(), player.getLocation().clone());
    }

    public FlyingState getFlyingState() {
        return flyingState;
    }

    public boolean isFlying() {
        return flyingState != FlyingState.NONE;
    }

    protected void setFlyingState(@NotNull FlyingState flyingState) {
        this.flyingState = flyingState;
    }

    public boolean isInNewChunks() {
        return inNewChunks;
    }

    protected void setInNewChunks(boolean isInNewChunks) {
        this.inNewChunks = isInNewChunks;
    }

    public double getXZSpeedSquared() {
        return periodicXZDistSquared; // Requires conversion to Blocks per Second
    }

    public double getYSpeedSquared() {
        return periodicYDistSquared;
    }

    public double getXZSpeed() {
        if (periodicXZDist == -1.0D)
            periodicXZDist = StrictMath.sqrt(periodicXZDistSquared);
        return periodicXZDist;
    }

    public double getYSpeed() {
        if (periodicYDist == -1.0D)
            periodicYDist = StrictMath.sqrt(periodicYDistSquared);
        return periodicYDist;
    }

    protected void doPeriodicUpdate() {
        // Cant be lazy because timing is important
        periodicXZDistSquared = MathHelper.getBlockDistanceXZSquared(periodicFrom, mostRecentTo);
        periodicYDistSquared = MathHelper.getBlockDistanceYSquared(periodicFrom, mostRecentTo);

        // Reset for lazy get
        periodicXZDist = -1.0D;
        periodicYDist = -1.0D;

        Bukkit.getPluginManager().callEvent(new WrappedPlayerUpdateEvent(this));

        // Update from Location for next period
        setPeriodicFrom(mostRecentTo);
    }

    protected void setPeriodicFrom(@NotNull Location from) {
        periodicFrom.setWorld(from.getWorld());
        periodicFrom.setX(from.getX());
        periodicFrom.setY(from.getY());
        periodicFrom.setZ(from.getZ());
    }

    protected void setPeriodicFrom(@NotNull Vector3d from) {
        periodicFrom.setX(from.getX());
        periodicFrom.setY(from.getY());
        periodicFrom.setZ(from.getZ());
    }

    protected void setMostRecentTo(@NotNull Location to) {
        mostRecentTo.setWorld(to.getWorld());
        mostRecentTo.setX(to.getX());
        mostRecentTo.setY(to.getY());
        mostRecentTo.setZ(to.getZ());
    }

    protected void setMostRecentTo(@NotNull Vector3d to) {
        mostRecentTo.setX(to.getX());
        mostRecentTo.setY(to.getY());
        mostRecentTo.setZ(to.getZ());
    }

    public enum FlyingState {
        CREATIVE,
        ELYTRA,
        VEHICLE,
        NONE
    }
}
