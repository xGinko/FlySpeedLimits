package me.xginko.flyspeedlimits.manager;

import com.cryptomorin.xseries.XMaterial;
import com.github.retrooper.packetevents.util.Vector3d;
import io.papermc.lib.PaperLib;
import me.xginko.flyspeedlimits.FlySpeedLimits;
import me.xginko.flyspeedlimits.config.LanguageCache;
import me.xginko.flyspeedlimits.events.AsyncWrappedPlayerUpdateEvent;
import me.xginko.flyspeedlimits.struct.Lazy;
import me.xginko.flyspeedlimits.struct.SpeedUnit;
import me.xginko.flyspeedlimits.utils.MathHelper;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class WrappedPlayer {

    public final @NotNull Player player;

    public final @NotNull Location periodicFrom;
    public final @NotNull Location mostRecentTo;

    private final Lazy<Double> blocksPerSecXZSquared, blocksPerTickXZSquared, blocksPerSecYSquared, blocksPerTickYSquared;
    private final Lazy<Float> blocksPerSecXZ, blocksPerTickXZ, blocksPerSecY, blocksPerTickY;

    private double distanceXZSquared;
    private double distanceYSquared;

    private boolean inNewChunks;

    private WrappedPlayer(@NotNull Player player, @NotNull Location from, @NotNull Location to) {
        this.player = player;
        this.periodicFrom = from;
        this.mostRecentTo = to;

        this.blocksPerSecXZSquared = Lazy.of(() -> SpeedUnit.BLOCKS_PER_SECOND.fromDistance(getDistanceXZSquared()));
        this.blocksPerSecXZ = Lazy.of(() -> MathHelper.quakeSqrt(blocksPerSecXZSquared.get().floatValue()));
        this.blocksPerTickXZSquared = Lazy.of(() -> SpeedUnit.BLOCKS_PER_TICK.fromDistance(getDistanceXZSquared()));
        this.blocksPerTickXZ = Lazy.of(() -> MathHelper.quakeSqrt(blocksPerTickXZSquared.get().floatValue()));

        this.blocksPerSecYSquared = Lazy.of(() -> SpeedUnit.BLOCKS_PER_SECOND.fromDistance(getDistanceYSquared()));
        this.blocksPerSecY = Lazy.of(() -> MathHelper.quakeSqrt(blocksPerSecYSquared.get().floatValue()));
        this.blocksPerTickYSquared = Lazy.of(() -> SpeedUnit.BLOCKS_PER_TICK.fromDistance(getDistanceYSquared()));
        this.blocksPerTickY = Lazy.of(() -> MathHelper.quakeSqrt(blocksPerTickYSquared.get().floatValue()));

        this.distanceXZSquared = 0.0D;
        this.distanceYSquared = 0.0D;
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

    public LanguageCache translations() {
        return FlySpeedLimits.getLang(player);
    }

    public CompletableFuture<Boolean> teleportAsync(Location location) {
        return PaperLib.teleportAsync(player, location);
    }

    public void sendMessage(Component component) {
        FlySpeedLimits.audiences().player(player).sendMessage(component);
    }

    public void sendActionbar(Component component) {
        FlySpeedLimits.audiences().player(player).sendActionBar(component);
    }

    public void playSound(Sound sound) {
        FlySpeedLimits.audiences().player(player).playSound(sound);
    }

    public void showTitle(Title title) {
        FlySpeedLimits.audiences().player(player).showTitle(title);
    }

    public void clearTitle() {
        FlySpeedLimits.audiences().player(player).clearTitle();
    }

    protected double getDistanceXZSquared() {
        return distanceXZSquared;
    }

    protected double getDistanceYSquared() {
        return distanceYSquared;
    }

    public boolean isInNewChunks() {
        return inNewChunks;
    }

    protected void setInNewChunks(boolean isInNewChunks) {
        this.inNewChunks = isInNewChunks;
    }

    public double getXZSpeedSquared(SpeedUnit speedUnit) {
        return speedUnit == SpeedUnit.BLOCKS_PER_SECOND ? blocksPerSecXZSquared.get() : blocksPerTickXZSquared.get();
    }

    public double getYSpeedSquared(SpeedUnit speedUnit) {
        return speedUnit == SpeedUnit.BLOCKS_PER_SECOND ? blocksPerSecYSquared.get() : blocksPerTickYSquared.get();
    }

    public float getXZSpeed(SpeedUnit speedUnit) {
        return speedUnit == SpeedUnit.BLOCKS_PER_SECOND ? blocksPerSecXZ.get() : blocksPerTickXZ.get();
    }

    public float getYSpeed(SpeedUnit speedUnit) {
        return speedUnit == SpeedUnit.BLOCKS_PER_SECOND ? blocksPerSecY.get() : blocksPerTickY.get();
    }

    protected void doPeriodicUpdate() {
        distanceXZSquared = MathHelper.getBlockDistanceXZSquared(periodicFrom, mostRecentTo);
        distanceYSquared = MathHelper.getBlockDistanceYSquared(periodicFrom, mostRecentTo);

        // Reset lazy
        blocksPerSecXZSquared.clear();
        blocksPerTickXZSquared.clear();
        blocksPerSecYSquared.clear();
        blocksPerTickYSquared.clear();
        blocksPerSecXZ.clear();
        blocksPerTickXZ.clear();
        blocksPerSecY.clear();
        blocksPerTickY.clear();

        Bukkit.getPluginManager().callEvent(new AsyncWrappedPlayerUpdateEvent(this));

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
}
