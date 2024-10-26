package me.xginko.flyspeedlimits.modules.elytra;

import me.xginko.aef.AnarchyExploitFixes;
import me.xginko.aef.config.LanguageCache;
import me.xginko.aef.enums.AEFPermission;
import me.xginko.aef.modules.AEFModule;
import me.xginko.aef.utils.CachingPermTool;
import me.xginko.aef.utils.LocationUtil;
import me.xginko.aef.utils.MaterialUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class ElytraAtSpawn extends AEFModule implements Listener {

    private final double spawn_SpeedOldChunks, spawn_SpeedNewChunks, spawn_DenyElytraTPS;
    private final boolean spawn_shouldCheckPermission, spawn_DenyElytra, spawn_DenyOnLowTPS, spawn_AlsoRemoveElytraOnLowTPS;

    public ElytraAtSpawn() {
        super("elytra.elytra-speed.At-Spawn");
        config.addComment(configPath + ".enable", "Use separate values for players at spawn.");
        this.spawn_shouldCheckPermission = config.getBoolean(configPath + ".use-bypass-permission", false,
                "Can be slow with a lot of players. Enable only if needed.");
        this.spawn_DenyElytra = config.getBoolean(configPath + ".deny-elytra-usage", false);
        this.spawn_SpeedOldChunks = config.getDouble(configPath + ".speed-old-chunks", 1.0);
        this.spawn_SpeedNewChunks = config.getDouble(configPath + ".speed-new-chunks", 0.8);
        this.spawn_DenyOnLowTPS = config.getBoolean(configPath + ".deny-elytra-on-low-TPS", true);
        this.spawn_DenyElytraTPS = config.getDouble(configPath + ".deny-elytra-TPS", 10.0);
        this.spawn_AlsoRemoveElytraOnLowTPS = config.getBoolean(configPath + ".also-remove-elytra-on-low-TPS", true);
    }

    @Override
    public void enable() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public boolean shouldEnable() {
        return config.elytra_enable_at_spawn;
    }

    @Override
    public void disable() {
        HandlerList.unregisterAll(this);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!player.isGliding()) return;
        if (spawn_shouldCheckPermission && CachingPermTool.hasPermission(AEFPermission.BYPASS_ELYTRA, player)) return;
        Location playerLoc = player.getLocation();
        if (config.elytra_enable_netherceiling && LocationUtil.isNetherCeiling(playerLoc)) return;
        if (LocationUtil.getDistance2DTo00(playerLoc) > config.elytra_spawn_radius) return;

        if (spawn_DenyElytra) {
            if (config.elytra_teleport_back) player.teleport(ElytraHelper.getInstance().getSetbackLocation(event));
            else event.setCancelled(true);

            if (config.elytra_play_too_fast_sound)
                player.playSound(player.getEyeLocation(), config.elytra_too_fast_sound, 1.0F, 1.0F);

            if (config.elytra_actionbar_enabled)
                player.sendActionBar(AnarchyExploitFixes.getLang(player.getLocale()).elytra_spawn_DisabledHere
                    .replace("%radius%", String.valueOf(config.elytra_spawn_radius)));
            return;
        }

        if (spawn_DenyOnLowTPS && AnarchyExploitFixes.getTickReporter().getTPS() <= spawn_DenyElytraTPS) {
            if (config.elytra_teleport_back) player.teleport(ElytraHelper.getInstance().getSetbackLocation(event));
            else event.setCancelled(true);

            if (config.elytra_play_too_fast_sound)
                player.playSound(player.getEyeLocation(), config.elytra_too_fast_sound, 1.0F, 1.0F);
            if (config.elytra_actionbar_enabled)
                player.sendActionBar(AnarchyExploitFixes.getLang(player.getLocale()).elytra_spawn_DisabledLowTPS
                    .replace("%tps%", String.valueOf(spawn_DenyElytraTPS)));

            if (spawn_AlsoRemoveElytraOnLowTPS) {
                PlayerInventory playerInv = player.getInventory();
                if (MaterialUtil.isElytra(playerInv.getChestplate())) {
                    ItemStack elytra = playerInv.getChestplate();
                    playerInv.setChestplate(null);
                    player.getWorld().dropItemNaturally(player.getLocation(), elytra);
                }
            }

            return;
        }

        double flySpeed = ElytraHelper.getInstance().getBlocksPerTick(event);

        if (ElytraHelper.getInstance().isInNewChunks(player)) {
            // Speed New Chunks
            if (flySpeed > spawn_SpeedNewChunks) {
                // too fast
                if (config.elytra_teleport_back) player.teleport(ElytraHelper.getInstance().getSetbackLocation(event));
                else event.setCancelled(true);

                if (config.elytra_play_too_fast_sound)
                    player.playSound(player.getEyeLocation(), config.elytra_too_fast_sound, 1.0F, 1.0F);

                if (!config.elytra_actionbar_enabled) return;
                LanguageCache lang = AnarchyExploitFixes.getLang(player.getLocale());
                if (config.elytra_show_chunk_age) {
                    player.sendActionBar(lang.elytra_spawn_TooFastChunkInfo
                            .replace("%neworold%", lang.elytra_spawn_New)
                            .replace("%chunks%", lang.elytra_spawn_Chunks)
                            .replace("%radius%", String.valueOf(config.elytra_spawn_radius)));
                } else {
                    player.sendActionBar(lang.elytra_spawn_TooFast
                            .replace("%radius%", String.valueOf(config.elytra_spawn_radius)));
                }
            } else {
                if (!config.elytra_actionbar_enabled) return;
                LanguageCache lang = AnarchyExploitFixes.getLang(player.getLocale());
                if (config.elytra_show_chunk_age) {
                    player.sendActionBar(lang.elytra_spawn_YouAreFlyingIn
                            .replace("%neworold%", lang.elytra_spawn_New_Color+lang.elytra_spawn_New.toUpperCase())
                            .replace("%chunks%", lang.elytra_spawn_Chunks)
                    +" "+lang.elytra_spawn_Speed
                            .replace("%maxspeed%", String.valueOf(spawn_SpeedNewChunks))
                            .replace("%speed%", String.format("%.2f", flySpeed)));
                } else {
                    player.sendActionBar(lang.elytra_spawn_Speed
                            .replace("%speed%", String.format("%.2f", flySpeed))
                            .replace("%maxspeed%", String.valueOf(spawn_SpeedNewChunks)));
                }
            }
        } else {
            // Speed Old Chunks
            if (flySpeed > spawn_SpeedOldChunks) {
                // too fast
                if (config.elytra_teleport_back) player.teleport(ElytraHelper.getInstance().getSetbackLocation(event));
                else event.setCancelled(true);

                if (config.elytra_play_too_fast_sound)
                    player.playSound(player.getEyeLocation(), config.elytra_too_fast_sound, 1.0F, 1.0F);

                if (!config.elytra_actionbar_enabled) return;
                LanguageCache lang = AnarchyExploitFixes.getLang(player.getLocale());
                if (config.elytra_show_chunk_age) {
                    player.sendActionBar(lang.elytra_spawn_TooFastChunkInfo
                            .replace("%neworold%", lang.elytra_spawn_Old)
                            .replace("%chunks%", lang.elytra_spawn_Chunks)
                            .replace("%radius%", String.valueOf(config.elytra_spawn_radius)));
                } else {
                    player.sendActionBar(lang.elytra_spawn_TooFast
                            .replace("%radius%", String.valueOf(config.elytra_spawn_radius)));
                }
            } else {
                if (!config.elytra_actionbar_enabled) return;
                LanguageCache lang = AnarchyExploitFixes.getLang(player.getLocale());
                if (config.elytra_show_chunk_age) {
                    player.sendActionBar(lang.elytra_spawn_YouAreFlyingIn
                            .replace("%neworold%", lang.elytra_spawn_Old_Color+lang.elytra_spawn_Old.toUpperCase())
                            .replace("%chunks%", lang.elytra_spawn_Chunks)
                    +" "+lang.elytra_spawn_Speed
                            .replace("%speed%", String.format("%.2f", flySpeed))
                            .replace("%maxspeed%", String.valueOf(spawn_SpeedOldChunks)));
                } else {
                    player.sendActionBar(lang.elytra_global_Speed
                            .replace("%speed%", String.format("%.2f", flySpeed))
                            .replace("%maxspeed%", String.valueOf(spawn_SpeedOldChunks)));
                }
            }
        }
    }
}
