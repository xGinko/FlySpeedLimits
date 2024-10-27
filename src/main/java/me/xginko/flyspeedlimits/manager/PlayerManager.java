package me.xginko.flyspeedlimits.manager;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.ConnectionState;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientVehicleMove;
import me.xginko.flyspeedlimits.FlySpeedLimits;
import me.xginko.flyspeedlimits.struct.Disableable;
import me.xginko.flyspeedlimits.struct.Enableable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class PlayerManager extends PacketListenerAbstract implements Enableable, Disableable, Runnable, Listener {

    private static Map<UUID, WrappedPlayer> playerMap;

    private ScheduledExecutorService executorService;
    private ScheduledFuture<?> scheduledFuture;

    public PlayerManager() {
        super(PacketListenerPriority.MONITOR);
    }

    @Override
    public void enable() {
        playerMap = new ConcurrentHashMap<>();
        executorService = Executors.newSingleThreadScheduledExecutor();
        scheduledFuture = executorService.scheduleAtFixedRate(
                this,
                100L,
                FlySpeedLimits.config().checkIntervalMillis,
                TimeUnit.MILLISECONDS);
        PacketEvents.getAPI().getEventManager().registerListener(this);
        FlySpeedLimits plugin = FlySpeedLimits.getInstance();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void disable() {
        PacketEvents.getAPI().getEventManager().unregisterListener(this);
        HandlerList.unregisterAll(this);
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
            scheduledFuture = null;
        }
        if (executorService != null) {
            executorService.close();
            executorService = null;
        }
        if (playerMap != null) {
            playerMap.clear();
            playerMap = null;
        }
    }

    public static Map<UUID, WrappedPlayer> getPlayers() {
        return playerMap;
    }

    public static @Nullable WrappedPlayer getPlayer(@NotNull UUID uuid) {
        if (playerMap.containsKey(uuid))
            return playerMap.get(uuid);
        @Nullable Player player = FlySpeedLimits.getInstance().getServer().getPlayer(uuid);
        if (player == null || !player.isConnected())
            return null;
        return playerMap.put(uuid, WrappedPlayer.of(player));
    }

    public static @NotNull WrappedPlayer getPlayer(@NotNull Player player) {
        return playerMap.computeIfAbsent(player.getUniqueId(),
                uuid -> WrappedPlayer.of(player));
    }

    public static @NotNull WrappedPlayer getPlayer(@NotNull PlayerMoveEvent event) {
        return playerMap.computeIfAbsent(event.getPlayer().getUniqueId(),
                uuid -> WrappedPlayer.of(event.getPlayer(), event.getFrom(), event.getTo()));
    }

    @Override
    public void run() {
        for (Map.Entry<UUID, WrappedPlayer> entry : playerMap.entrySet()) {
            if (entry.getValue().player.isConnected()) {
                entry.getValue().doPeriodicUpdate();
            } else {
                playerMap.remove(entry.getKey());
            }
        }
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getConnectionState() != ConnectionState.PLAY) return;
        if (!playerMap.containsKey(event.getUser().getUUID())) return;

        if (event.getPacketType() == PacketType.Play.Client.VEHICLE_MOVE) {
            WrappedPlayer wrappedPlayer = playerMap.get(event.getUser().getUUID());
            if (wrappedPlayer.player.isInsideVehicle()) {
                wrappedPlayer.setMostRecentTo(new WrapperPlayClientVehicleMove(event).getPosition());
            }
        }

        else if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
            playerMap.get(event.getUser().getUUID())
                    .setMostRecentTo(new WrapperPlayClientPlayerFlying(event).getLocation().getPosition());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void on(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        playerMap.computeIfPresent(player.getUniqueId(), (uuid, wrappedPlayer) -> {
            wrappedPlayer.setPeriodicFrom(player.getLocation());
            wrappedPlayer.setMostRecentTo(player.getLocation());
            return wrappedPlayer;
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void on(PlayerQuitEvent event) {
        playerMap.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void on(PlayerKickEvent event) {
        playerMap.remove(event.getPlayer().getUniqueId());
    }
}
