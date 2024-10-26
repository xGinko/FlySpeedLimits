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

public class ElytraOnCeiling extends AEFModule implements Listener {

    private final double ceiling_SpeedOldChunks, ceiling_SpeedNewChunks, ceiling_BurstSpeedOldChunks,
            ceiling_BurstSpeedNewChunks, ceiling_BurstOldChunk_TPS, ceiling_BurstNewChunk_TPS, ceiling_DenyElytraTPS;
    private final boolean ceiling_shouldCheckPermission, ceiling_DenyElytra, ceiling_EnableBursting, ceiling_DenyOnLowTPS,
            ceiling_AlsoRemoveOnLowTPS;

    public ElytraOnCeiling() {
        super("elytra.elytra-speed.Nether-Ceiling");
        config.addComment(configPath + ".enable",
                "Use separate values for players above the nether ceiling.");
        this.ceiling_shouldCheckPermission = config.getBoolean(configPath + ".use-bypass-permission", false,
                "Can be slow with a lot of players. Enable only if needed.");
        this.ceiling_DenyElytra = config.getBoolean(configPath + ".deny-elytra-usage", false);
        this.ceiling_SpeedOldChunks = config.getDouble(configPath + ".speed-old-chunks", 0.5);
        this.ceiling_SpeedNewChunks = config.getDouble(configPath + ".speed-new-chunks", 0.5);
        this.ceiling_EnableBursting = config.getBoolean(configPath + ".enable-bursting", true);
        this.ceiling_BurstSpeedOldChunks = config.getDouble(configPath + ".burst-speed-old-chunks", 1.0);
        this.ceiling_BurstOldChunk_TPS = config.getDouble(configPath + ".burst-speed-old-chunk-TPS", 18.0);
        this.ceiling_BurstSpeedNewChunks = config.getDouble(configPath + ".burst-speed-new-chunks", 1.0);
        this.ceiling_BurstNewChunk_TPS = config.getDouble(configPath + ".burst-speed-new-chunk-TPS", 18.0);
        this.ceiling_DenyOnLowTPS = config.getBoolean(configPath + ".deny-elytra-on-low-TPS", true);
        this.ceiling_DenyElytraTPS = config.getDouble(configPath + ".deny-elytra-TPS", 12.0);
        this.ceiling_AlsoRemoveOnLowTPS = config.getBoolean(configPath + ".also-remove-elytra-on-low-TPS", true);
    }

    @Override
    public void enable() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public boolean shouldEnable() {
        return config.elytra_enable_netherceiling;
    }

    @Override
    public void disable() {
        HandlerList.unregisterAll(this);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!player.isGliding()) return;
        if (ceiling_shouldCheckPermission && CachingPermTool.hasPermission(AEFPermission.BYPASS_ELYTRA, player)) return;
        Location playerLoc = player.getLocation();
        if (!LocationUtil.isNetherCeiling(playerLoc)) return;

        if (ceiling_DenyElytra) {
            if (config.elytra_teleport_back) player.teleport(ElytraHelper.getInstance().getSetbackLocation(event));
            else event.setCancelled(true);

            if (config.elytra_play_too_fast_sound)
                player.playSound(player.getEyeLocation(), config.elytra_too_fast_sound, 1.0F, 1.0F);

            if (config.elytra_actionbar_enabled)
                player.sendActionBar(AnarchyExploitFixes.getLang(player.getLocale()).elytra_ceiling_DisabledHere);
            return;
        }

        if (ceiling_DenyOnLowTPS && AnarchyExploitFixes.getTickReporter().getTPS() <= ceiling_DenyElytraTPS) {
            if (config.elytra_teleport_back) player.teleport(ElytraHelper.getInstance().getSetbackLocation(event));
            else event.setCancelled(true);

            if (config.elytra_play_too_fast_sound)
                player.playSound(player.getEyeLocation(), config.elytra_too_fast_sound, 1.0F, 1.0F);

            if (config.elytra_actionbar_enabled)
                player.sendActionBar(AnarchyExploitFixes.getLang(player.getLocale()).elytra_ceiling_DisabledLowTPS
                    .replace("%tps%", String.valueOf(ceiling_DenyElytraTPS)));

            if (ceiling_AlsoRemoveOnLowTPS) {
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
            if (ceiling_EnableBursting && AnarchyExploitFixes.getTickReporter().getTPS() >= ceiling_BurstNewChunk_TPS) {
                // Burst Speed New Chunks
                if (flySpeed > ceiling_BurstSpeedNewChunks) {
                    if (config.elytra_teleport_back) player.teleport(ElytraHelper.getInstance().getSetbackLocation(event));
                    else event.setCancelled(true);

                    if (config.elytra_play_too_fast_sound)
                        player.playSound(player.getEyeLocation(), config.elytra_too_fast_sound, 1.0F, 1.0F);

                    if (!config.elytra_actionbar_enabled) return;
                    LanguageCache lang = AnarchyExploitFixes.getLang(player.getLocale());
                    if (config.elytra_show_chunk_age) {
                        player.sendActionBar(lang.elytra_ceiling_TooFastChunkInfo
                                .replace("%neworold%", lang.elytra_ceiling_New)
                                .replace("%chunks%", lang.elytra_ceiling_Chunks)
                        );
                    } else {
                        player.sendActionBar(lang.elytra_ceiling_TooFast);
                    }
                } else {
                    if (!config.elytra_actionbar_enabled) return;
                    LanguageCache lang = AnarchyExploitFixes.getLang(player.getLocale());
                    if (config.elytra_show_chunk_age) {
                        player.sendActionBar(lang.elytra_ceiling_YouAreFlyingIn
                                .replace("%neworold%", lang.elytra_ceiling_New_Color+lang.elytra_ceiling_New.toUpperCase())
                                .replace("%chunks%", lang.elytra_ceiling_Chunks)
                                +" "+lang.elytra_ceiling_Speed
                                .replace("%maxspeed%", String.valueOf(ceiling_BurstSpeedNewChunks))
                                .replace("%speed%", String.format("%.2f", flySpeed))
                        );
                    } else {
                        player.sendActionBar(lang.elytra_ceiling_Speed
                                .replace("%speed%", String.format("%.2f", flySpeed))
                                .replace("%maxspeed%", String.valueOf(ceiling_BurstSpeedNewChunks))
                        );
                    }
                }
            } else {
                // Normal Speed New Chunks
                if (flySpeed > ceiling_SpeedNewChunks) {
                    if (config.elytra_teleport_back) player.teleport(ElytraHelper.getInstance().getSetbackLocation(event));
                    else event.setCancelled(true);

                    if (config.elytra_play_too_fast_sound)
                        player.playSound(player.getEyeLocation(), config.elytra_too_fast_sound, 1.0F, 1.0F);

                    if (!config.elytra_actionbar_enabled) return;
                    LanguageCache lang = AnarchyExploitFixes.getLang(player.getLocale());
                    if (ceiling_EnableBursting) {
                        player.sendActionBar(lang.elytra_ceiling_TooFastLowTPS);
                    } else {
                        if (config.elytra_show_chunk_age) {
                            player.sendActionBar(lang.elytra_ceiling_TooFastChunkInfo
                                    .replace("%neworold%", lang.elytra_ceiling_New)
                                    .replace("%chunks%", lang.elytra_ceiling_Chunks)
                            );
                        } else {
                            player.sendActionBar(lang.elytra_ceiling_TooFast);
                        }
                    }
                } else {
                    if (!config.elytra_actionbar_enabled) return;
                    LanguageCache lang = AnarchyExploitFixes.getLang(player.getLocale());
                    if (config.elytra_show_chunk_age) {
                        player.sendActionBar(lang.elytra_ceiling_YouAreFlyingIn
                                .replace("%neworold%", lang.elytra_ceiling_New_Color+lang.elytra_ceiling_New.toUpperCase())
                                .replace("%chunks%", lang.elytra_ceiling_Chunks)
                                +" "+lang.elytra_ceiling_Speed
                                .replace("%maxspeed%", String.valueOf(ceiling_SpeedOldChunks))
                                .replace("%speed%", String.format("%.2f", flySpeed)));
                    } else {
                        player.sendActionBar(lang.elytra_ceiling_Speed
                                .replace("%speed%", String.format("%.2f", flySpeed))
                                .replace("%maxspeed%", String.valueOf(ceiling_SpeedOldChunks)));
                    }
                }
            }
        } else {
            // Speed Old Chunks
            if (ceiling_EnableBursting && AnarchyExploitFixes.getTickReporter().getTPS() >= ceiling_BurstOldChunk_TPS) {
                // Burst Speed Old Chunks
                if (flySpeed > ceiling_BurstSpeedOldChunks) {
                    if (config.elytra_teleport_back) player.teleport(ElytraHelper.getInstance().getSetbackLocation(event));
                    else event.setCancelled(true);

                    if (config.elytra_play_too_fast_sound) player.playSound(player.getEyeLocation(), config.elytra_too_fast_sound, 1.0F, 1.0F);

                    if (!config.elytra_actionbar_enabled) return;
                    LanguageCache lang = AnarchyExploitFixes.getLang(player.getLocale());
                    if (config.elytra_show_chunk_age) {
                        player.sendActionBar(lang.elytra_ceiling_TooFastChunkInfo
                                .replace("%neworold%", lang.elytra_ceiling_Old)
                                .replace("%chunks%", lang.elytra_ceiling_Chunks));
                    } else {
                        player.sendActionBar(lang.elytra_ceiling_TooFast);
                    }
                } else {
                    if (!config.elytra_actionbar_enabled) return;
                    LanguageCache lang = AnarchyExploitFixes.getLang(player.getLocale());
                    if (config.elytra_show_chunk_age) {
                        player.sendActionBar(lang.elytra_ceiling_YouAreFlyingIn
                                .replace("%neworold%", lang.elytra_ceiling_Old_Color+lang.elytra_ceiling_Old.toUpperCase())
                                .replace("%chunks%", lang.elytra_ceiling_Chunks)
                                +" "+lang.elytra_ceiling_Speed
                                .replace("%maxspeed%", String.valueOf(ceiling_BurstSpeedOldChunks))
                                .replace("%speed%", String.format("%.2f", flySpeed)));
                    } else {
                        player.sendActionBar(lang.elytra_ceiling_Speed
                                .replace("%speed%", String.format("%.2f", flySpeed))
                                .replace("%maxspeed%", String.valueOf(ceiling_BurstSpeedOldChunks)));
                    }
                }
            } else {
                // Normal Speed Old Chunks
                if (flySpeed > ceiling_SpeedOldChunks) {
                    if (config.elytra_teleport_back) player.teleport(ElytraHelper.getInstance().getSetbackLocation(event));
                    else event.setCancelled(true);

                    if (config.elytra_play_too_fast_sound) player.playSound(player.getEyeLocation(), config.elytra_too_fast_sound, 1.0F, 1.0F);

                    if (!config.elytra_actionbar_enabled) return;
                    LanguageCache lang = AnarchyExploitFixes.getLang(player.getLocale());
                    if (ceiling_EnableBursting) {
                        player.sendActionBar(lang.elytra_ceiling_TooFastLowTPS);
                    } else {
                        if (config.elytra_show_chunk_age) {
                            player.sendActionBar(lang.elytra_ceiling_TooFastChunkInfo
                                    .replace("%neworold%", lang.elytra_ceiling_Old)
                                    .replace("%chunks%", lang.elytra_ceiling_Chunks)
                            );
                        } else {
                            player.sendActionBar(lang.elytra_ceiling_TooFast);
                        }
                    }
                } else {
                    if (!config.elytra_actionbar_enabled) return;
                    LanguageCache lang = AnarchyExploitFixes.getLang(player.getLocale());
                    if (config.elytra_show_chunk_age) {
                        player.sendActionBar(lang.elytra_ceiling_YouAreFlyingIn
                                .replace("%neworold%", lang.elytra_ceiling_Old_Color+lang.elytra_ceiling_Old.toUpperCase())
                                .replace("%chunks%", lang.elytra_ceiling_Chunks)
                                +" "+lang.elytra_ceiling_Speed
                                .replace("%maxspeed%", String.valueOf(ceiling_SpeedOldChunks))
                                .replace("%speed%", String.format("%.2f", flySpeed)));
                    } else {
                        player.sendActionBar(lang.elytra_ceiling_Speed
                                .replace("%speed%", String.format("%.2f", flySpeed))
                                .replace("%maxspeed%", String.valueOf(ceiling_SpeedOldChunks)));
                    }
                }
            }
        }
    }
}
