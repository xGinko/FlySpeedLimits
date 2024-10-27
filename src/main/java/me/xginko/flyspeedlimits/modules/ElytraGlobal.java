package me.xginko.flyspeedlimits.modules;

import me.xginko.flyspeedlimits.FlySpeedLimits;
import me.xginko.flyspeedlimits.manager.PlayerManager;
import me.xginko.flyspeedlimits.manager.WrappedPlayer;
import me.xginko.flyspeedlimits.utils.GeneralUtil;
import me.xginko.flyspeedlimits.utils.MaterialUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class ElytraGlobal extends SpeedLimitModule implements Listener {

    private final double global_SpeedOldChunks, global_SpeedNewChunks, global_BurstSpeedOldChunks,
            global_BurstSpeedNewChunks, global_BurstOldChunk_TPS, global_BurstNewChunk_TPS, global_DenyElytraTPS;
    private final boolean global_DenyElytra, global_EnableBursting, global_DenyOnLowTPS,
            global_AlsoRemoveOnLowTPS;
    
    public ElytraGlobal() {
        super("speeds.elytra", true);
        config.master().addComment("elytra.elytra-speed",
                "NOTE: Set nocheatplus horizontal elytra settings to 500 or higher.");
        config.master().addComment(configPath + ".enable",
                "Global settings. If nothing else is enabled, this will be used for all environments.");
        this.global_DenyElytra = config.getBoolean(configPath + ".deny-elytra-usage", false);
        this.global_SpeedOldChunks = config.getDouble(configPath + ".speed-old-chunks", 1.81);
        this.global_SpeedNewChunks = config.getDouble(configPath + ".speed-new-chunks", 1.81);
        this.global_EnableBursting = config.getBoolean(configPath + ".enable-bursting", true);
        this.global_BurstSpeedOldChunks = config.getDouble(configPath + ".burst-speed-old-chunks", 5.0);
        this.global_BurstOldChunk_TPS = config.getDouble(configPath + ".burst-speed-old-chunk-TPS", 18.0);
        this.global_BurstSpeedNewChunks = config.getDouble(configPath + ".burst-speed-new-chunks", 3.12);
        this.global_BurstNewChunk_TPS = config.getDouble(configPath + ".burst-speed-new-chunk-TPS", 19.0);
        this.global_DenyOnLowTPS = config.getBoolean(configPath + ".deny-elytra-on-low-TPS", true);
        this.global_DenyElytraTPS = config.getDouble(configPath + ".deny-elytra-TPS", 12.0);
        this.global_AlsoRemoveOnLowTPS = config.getBoolean(configPath + ".also-remove-elytra-on-low-TPS", true);
    }

    @Override
    public void enable() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public boolean shouldEnable() {
        return true;
    }

    @Override
    public void disable() {
        HandlerList.unregisterAll(this);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void on(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (global_DenyElytra) {
            event.setCancelled(true);
            return;
        }

        if (global_DenyOnLowTPS && FlySpeedLimits.getTickReporter().getTPS() <= global_DenyElytraTPS) {
            event.setCancelled(true);
            if (global_AlsoRemoveOnLowTPS) {
                if (MaterialUtil.isElytra(player.getInventory().getChestplate())) {
                    GeneralUtil.dropChestplate(player);
                }
            }
            return;
        }

        WrappedPlayer wrappedPlayer = PlayerManager.getPlayer(event);

        if (wrappedPlayer.isInNewChunks()) { // If is in new chunks
            // Speed New Chunks
            if (global_EnableBursting && FlySpeedLimits.getTickReporter().getTPS() >= global_BurstNewChunk_TPS) {
                // Burst Speed New Chunks
                if (wrappedPlayer.getXZSpeedSquared() > global_BurstSpeedNewChunks) {
                    // Too fast
                    event.setCancelled(true);

                } else {

                }
            } else {
                // Normal Speed New Chunks
                if (wrappedPlayer.getXZSpeedSquared() > global_SpeedNewChunks) {

                } else {
                    // Speed old chunks

                }
            }
        } else {
            // Speed Old Chunks
            if (global_EnableBursting && FlySpeedLimits.getTickReporter().getTPS() >= global_BurstOldChunk_TPS) {
                // Burst Speed Old Chunks
                if (wrappedPlayer.getXZSpeedSquared() > global_BurstSpeedOldChunks) {
                    // Too fast

                } else {

                }
            } else {
                // Normal Speed Old Chunks
                if (wrappedPlayer.getXZSpeedSquared() > global_SpeedOldChunks) {

                } else {

                }
            }
        }
    }
}
