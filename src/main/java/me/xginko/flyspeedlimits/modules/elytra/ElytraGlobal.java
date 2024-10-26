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

public class ElytraGlobal extends AEFModule implements Listener {

    private final double global_SpeedOldChunks, global_SpeedNewChunks, global_BurstSpeedOldChunks,
            global_BurstSpeedNewChunks, global_BurstOldChunk_TPS, global_BurstNewChunk_TPS, global_DenyElytraTPS;
    private final boolean global_shouldCheckPermission, global_DenyElytra, global_EnableBursting, global_DenyOnLowTPS,
            global_AlsoRemoveOnLowTPS;
    
    public ElytraGlobal() {
        super("elytra.elytra-speed.Global-Settings");
        config.addComment("elytra.elytra-speed",
                "NOTE: Set nocheatplus horizontal elytra settings to 500 or higher.");
        config.addComment(configPath + ".enable",
                "Global settings. If nothing else is enabled, this will be used for all environments.");
        this.global_shouldCheckPermission = config.getBoolean(configPath + ".use-bypass-permission", false,
                "Can be slow with a lot of players. Enable only if needed.");
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
        return config.elytra_enable_global;
    }

    @Override
    public void disable() {
        HandlerList.unregisterAll(this);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!player.isGliding()) return;
        if (global_shouldCheckPermission && CachingPermTool.hasPermission(AEFPermission.BYPASS_ELYTRA, player)) return;
        Location playerLoc = player.getLocation();
        if (config.elytra_enable_netherceiling && LocationUtil.isNetherCeiling(playerLoc)) return;
        if (config.elytra_enable_at_spawn && LocationUtil.getDistance2DTo00(playerLoc) <= config.elytra_spawn_radius) return;

        if (global_DenyElytra) {
            if (config.elytra_teleport_back) player.teleport(ElytraHelper.getInstance().getSetbackLocation(event));
            else event.setCancelled(true);

            if (config.elytra_play_too_fast_sound) player.playSound(player.getEyeLocation(), config.elytra_too_fast_sound, 1.0F, 1.0F);
            if (config.elytra_actionbar_enabled) player.sendActionBar(AnarchyExploitFixes.getLang(player.getLocale()).elytra_global_DisabledHere);
            return;
        }

        if (global_DenyOnLowTPS && AnarchyExploitFixes.getTickReporter().getTPS() <= global_DenyElytraTPS) {
            if (config.elytra_teleport_back) player.teleport(ElytraHelper.getInstance().getSetbackLocation(event));
            else event.setCancelled(true);

            if (config.elytra_play_too_fast_sound) player.playSound(player.getEyeLocation(), config.elytra_too_fast_sound, 1.0F, 1.0F);

            if (config.elytra_actionbar_enabled) player.sendActionBar(AnarchyExploitFixes.getLang(player.getLocale()).elytra_global_DisabledLowTPS
                    .replace("%tps%", String.valueOf(global_DenyElytraTPS))
            );

            if (global_AlsoRemoveOnLowTPS) {
                PlayerInventory playerInv = player.getInventory();
                if (MaterialUtil.isElytra(playerInv.getChestplate())) {
                    ItemStack elytra = playerInv.getChestplate();
                    playerInv.setChestplate(null);
                    player.getWorld().dropItemNaturally(playerLoc, elytra);
                }
            }

            return;
        }

        double flySpeed = ElytraHelper.getInstance().getBlocksPerTick(event);

        if (ElytraHelper.getInstance().isInNewChunks(player)) {
            // Speed New Chunks
            if (global_EnableBursting && AnarchyExploitFixes.getTickReporter().getTPS() >= global_BurstNewChunk_TPS) {
                // Burst Speed New Chunks
                if (flySpeed > global_BurstSpeedNewChunks) {
                    // Too fast
                    if (config.elytra_teleport_back) player.teleport(ElytraHelper.getInstance().getSetbackLocation(event));
                    else event.setCancelled(true);

                    if (config.elytra_play_too_fast_sound) player.playSound(player.getEyeLocation(), config.elytra_too_fast_sound, 1.0F, 1.0F);

                    if (!config.elytra_actionbar_enabled) return;
                    LanguageCache lang = AnarchyExploitFixes.getLang(player.getLocale());
                    if (config.elytra_show_chunk_age) {
                        player.sendActionBar(lang.elytra_global_TooFastChunkInfo
                                .replace("%neworold%", lang.elytra_global_New)
                                .replace("%chunks%", lang.elytra_global_Chunks)
                        );
                    } else {
                        player.sendActionBar(lang.elytra_global_TooFast);
                    }
                } else {
                    if (!config.elytra_actionbar_enabled) return;
                    LanguageCache lang = AnarchyExploitFixes.getLang(player.getLocale());
                    if (config.elytra_show_chunk_age) {
                        player.sendActionBar(lang.elytra_global_YouAreFlyingIn
                                .replace("%neworold%", lang.elytra_global_New_Color+lang.elytra_global_New.toUpperCase())
                                .replace("%chunks%", lang.elytra_global_Chunks)
                                +" "+lang.elytra_global_Speed
                                .replace("%maxspeed%", String.valueOf(global_BurstSpeedNewChunks))
                                .replace("%speed%", String.format("%.2f", flySpeed))
                        );
                    } else {
                        player.sendActionBar(lang.elytra_global_Speed
                                .replace("%speed%", String.format("%.2f", flySpeed))
                                .replace("%maxspeed%", String.valueOf(global_BurstSpeedNewChunks))
                        );
                    }
                }
            } else {
                // Normal Speed New Chunks
                if (flySpeed > global_SpeedNewChunks) {
                    if (config.elytra_teleport_back) player.teleport(ElytraHelper.getInstance().getSetbackLocation(event));
                    else event.setCancelled(true);

                    if (config.elytra_play_too_fast_sound) player.playSound(player.getEyeLocation(), config.elytra_too_fast_sound, 1.0F, 1.0F);

                    if (!config.elytra_actionbar_enabled) return;
                    LanguageCache lang = AnarchyExploitFixes.getLang(player.getLocale());
                    if (global_EnableBursting) {
                        player.sendActionBar(lang.elytra_global_TooFastLowTPS);
                    } else {
                        if (config.elytra_show_chunk_age) {
                            player.sendActionBar(lang.elytra_global_TooFastChunkInfo
                                    .replace("%neworold%", lang.elytra_global_New)
                                    .replace("%chunks%", lang.elytra_global_Chunks)
                            );
                        } else {
                            player.sendActionBar(lang.elytra_global_TooFast);
                        }
                    }
                } else {
                    if (!config.elytra_actionbar_enabled) return;
                    LanguageCache lang = AnarchyExploitFixes.getLang(player.getLocale());
                    if (config.elytra_show_chunk_age) {
                        player.sendActionBar(lang.elytra_global_YouAreFlyingIn
                                .replace("%neworold%", lang.elytra_global_New_Color+lang.elytra_global_New.toUpperCase())
                                .replace("%chunks%", lang.elytra_global_Chunks)
                                +" "+lang.elytra_global_Speed
                                .replace("%maxspeed%", String.valueOf(global_SpeedOldChunks))
                                .replace("%speed%", String.format("%.2f", flySpeed))
                        );
                    } else {
                        player.sendActionBar(lang.elytra_global_Speed
                                .replace("%speed%", String.format("%.2f", flySpeed))
                                .replace("%maxspeed%", String.valueOf(global_SpeedOldChunks))
                        );
                    }
                }
            }
        } else {
            // Speed Old Chunks
            if (global_EnableBursting && AnarchyExploitFixes.getTickReporter().getTPS() >= global_BurstOldChunk_TPS) {
                // Burst Speed Old Chunks
                if (flySpeed > global_BurstSpeedOldChunks) {
                    if (config.elytra_teleport_back) player.teleport(ElytraHelper.getInstance().getSetbackLocation(event));
                    else event.setCancelled(true);

                    if (config.elytra_play_too_fast_sound) player.playSound(player.getEyeLocation(), config.elytra_too_fast_sound, 1.0F, 1.0F);

                    if (!config.elytra_actionbar_enabled) return;
                    LanguageCache lang = AnarchyExploitFixes.getLang(player.getLocale());
                    if (config.elytra_show_chunk_age) {
                        player.sendActionBar(lang.elytra_global_TooFastChunkInfo
                                .replace("%neworold%", lang.elytra_global_Old)
                                .replace("%chunks%", lang.elytra_global_Chunks)
                        );
                    } else {
                        player.sendActionBar(lang.elytra_global_TooFast);
                    }
                } else {
                    if (!config.elytra_actionbar_enabled) return;
                    LanguageCache lang = AnarchyExploitFixes.getLang(player.getLocale());
                    if (config.elytra_show_chunk_age) {
                        player.sendActionBar(lang.elytra_global_YouAreFlyingIn
                                .replace("%neworold%", lang.elytra_global_Old_Color+lang.elytra_global_Old.toUpperCase())
                                .replace("%chunks%", lang.elytra_global_Chunks)
                                +" "+lang.elytra_global_Speed
                                .replace("%maxspeed%", String.valueOf(global_BurstSpeedOldChunks))
                                .replace("%speed%", String.format("%.2f", flySpeed))
                        );
                    } else {
                        player.sendActionBar(lang.elytra_global_Speed
                                .replace("%speed%", String.format("%.2f", flySpeed))
                                .replace("%maxspeed%", String.valueOf(global_BurstSpeedOldChunks))
                        );
                    }
                }
            } else {
                // Normal Speed Old Chunks
                if (flySpeed > global_SpeedOldChunks) {
                    if (config.elytra_teleport_back) player.teleport(ElytraHelper.getInstance().getSetbackLocation(event));
                    else event.setCancelled(true);

                    if (config.elytra_play_too_fast_sound) player.playSound(player.getEyeLocation(), config.elytra_too_fast_sound, 1.0F, 1.0F);

                    if (!config.elytra_actionbar_enabled) return;
                    LanguageCache lang = AnarchyExploitFixes.getLang(player.getLocale());
                    if (global_EnableBursting) {
                        player.sendActionBar(lang.elytra_global_TooFastLowTPS);
                    } else {
                        if (config.elytra_show_chunk_age) {
                            player.sendActionBar(lang.elytra_global_TooFastChunkInfo
                                    .replace("%neworold%", lang.elytra_global_Old)
                                    .replace("%chunks%", lang.elytra_global_Chunks)
                            );
                        } else {
                            player.sendActionBar(lang.elytra_global_TooFast);
                        }
                    }
                } else {
                    if (!config.elytra_actionbar_enabled) return;
                    LanguageCache lang = AnarchyExploitFixes.getLang(player.getLocale());
                    if (config.elytra_show_chunk_age) {
                        player.sendActionBar(lang.elytra_global_YouAreFlyingIn
                                .replace("%neworold%", lang.elytra_global_Old_Color+lang.elytra_global_Old.toUpperCase())
                                .replace("%chunks%", lang.elytra_global_Chunks)
                                +" "+lang.elytra_global_Speed
                                .replace("%maxspeed%", String.valueOf(global_SpeedOldChunks))
                                .replace("%speed%", String.format("%.2f", flySpeed))
                        );
                    } else {
                        player.sendActionBar(lang.elytra_global_Speed
                                .replace("%speed%", String.format("%.2f", flySpeed))
                                .replace("%maxspeed%", String.valueOf(global_SpeedOldChunks))
                        );
                    }
                }
            }
        }
    }
}
