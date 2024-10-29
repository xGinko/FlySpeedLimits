package me.xginko.flyspeedlimits.manager;

import com.cryptomorin.xseries.XMaterial;
import com.github.retrooper.packetevents.util.Vector3d;
import me.xginko.flyspeedlimits.events.WrappedPlayerUpdateEvent;
import me.xginko.flyspeedlimits.struct.SpeedUnit;
import me.xginko.flyspeedlimits.utils.MathHelper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class WrappedPlayer {

    public final @NotNull Player player;
    public final @NotNull Location periodicFrom;
    public final @NotNull Location mostRecentTo;

    private @NotNull FlyingState flyingState;

    private double blocksPerSecXZSquared;
    private double blocksPerSecYSquared;

    private float blocksPerSecXZ;
    private float blocksPerSecY;

    private boolean inNewChunks;

    private WrappedPlayer(@NotNull Player player, @NotNull Location from, @NotNull Location to) {
        this.player = player;
        this.periodicFrom = from;
        this.mostRecentTo = to;
        this.flyingState = FlyingState.NONE;
        this.blocksPerSecXZSquared = 0.0D;
        this.blocksPerSecYSquared = 0.0D;
        this.blocksPerSecXZ = 0.0F;
        this.blocksPerSecY = 0.0F;
        this.inNewChunks = false;
    }

    protected static @NotNull WrappedPlayer of(@NotNull Player player, @NotNull Location from, @NotNull Location to) {
        return new WrappedPlayer(player, from, to);
    }

    protected static @NotNull WrappedPlayer of(@NotNull Player player) {
        return of(player, player.getLocation().clone(), player.getLocation().clone());
    }

    public void dropElytra() {
        ItemStack chestplate = player.getInventory().getChestplate();
        if (chestplate != null && chestplate.getType() == XMaterial.ELYTRA.parseMaterial()) {
            player.getInventory().setChestplate(null);
            player.getWorld().dropItem(player.getLocation(), chestplate);
        }
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
        return blocksPerSecXZSquared;
    }

    public double getYSpeedSquared() {
        return blocksPerSecYSquared;
    }

    public float getXZSpeed() {
        if (blocksPerSecXZ == -1.0D)
            blocksPerSecXZ = MathHelper.quakeSqrt((float) blocksPerSecXZSquared);
        return blocksPerSecXZ;
    }

    public float getYSpeed() {
        if (blocksPerSecY == -1.0D)
            blocksPerSecY = MathHelper.quakeSqrt((float) blocksPerSecYSquared);
        return blocksPerSecY;
    }

    protected void doPeriodicUpdate() {
        // Cant be lazy because timing is important
        blocksPerSecXZSquared = SpeedUnit.BLOCKS_PER_SECOND.fromDistance(MathHelper.getBlockDistanceXZSquared(periodicFrom, mostRecentTo));
        blocksPerSecYSquared = SpeedUnit.BLOCKS_PER_SECOND.fromDistance(MathHelper.getBlockDistanceYSquared(periodicFrom, mostRecentTo));

        // Reset for lazy get
        blocksPerSecXZ = -1.0F;
        blocksPerSecY = -1.0F;

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
