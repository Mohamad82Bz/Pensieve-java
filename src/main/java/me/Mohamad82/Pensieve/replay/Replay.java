package me.Mohamad82.Pensieve.replay;

import me.Mohamad82.Pensieve.nms.NMSUtils;
import me.Mohamad82.Pensieve.nms.NPC;
import me.Mohamad82.Pensieve.nms.enums.NPCAnimation;
import me.Mohamad82.Pensieve.record.Record;
import me.Mohamad82.Pensieve.record.RecordTick;
import me.Mohamad82.Pensieve.record.enums.DamageType;
import me.Mohamad82.Pensieve.utils.BlockSoundUtils;
import me.Mohamad82.RUoM.Ruom;
import me.Mohamad82.RUoM.XSeries.XSound;
import me.Mohamad82.RUoM.utils.BlockUtils;
import me.Mohamad82.RUoM.utils.LocUtils;
import me.Mohamad82.RUoM.utils.PlayerUtils;
import me.Mohamad82.RUoM.vector.Vector3;
import me.Mohamad82.RUoM.vector.Vector3Utils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class Replay {

    private final Map<Record, NPC> records = new HashMap<>();
    public final Map<UUID, RecordTick> lastNonNullTicks = new HashMap<>();
    public final Map<UUID, ReplayCache> replayCache = new HashMap<>();

    private final JavaPlugin plugin;
    private final World world;
    private final Vector3 center;
    private BukkitTask replayRunnable;

    public Replay(JavaPlugin plugin, Set<Record> records, World world, Vector3 center) {
        this.plugin = plugin;
        this.world = world;
        if (center == null)
            center = Vector3.at(0, 0, 0);
        else
            center = Vector3.at(center.getBlockX() + 0.5, center.getBlockY(), center.getBlockZ() + 0.5);
        this.center = center;
        for (Record record : records) {
            Vector3 centerOffSetDistance = Vector3Utils.getTravelDistance(record.getCenter(), record.getStartLocation());
            Vector3 centerOffSet = center.clone().add(centerOffSetDistance.getX(), centerOffSetDistance.getY(), centerOffSetDistance.getZ());
            this.records.put(record, new NPC(record.getPlayerName(),
                    new Location(world, centerOffSet.getX(), centerOffSet.getY(), centerOffSet.getZ())));

            ReplayCache cache = new ReplayCache();
            replayCache.put(record.getPlayerUUID(), cache);
            cache.setCentersDistance(Vector3Utils.getTravelDistance(record.getCenter(), center));
        }
    }

    public Replay(JavaPlugin plugin, Set<Record> records, World world) {
        this(plugin, records, world, null);
    }

    public PlayBackControl start() {
        PlayBackControl playbackControl = new PlayBackControl();
        for (Record record : records.keySet()) {
            replayCache.get(record.getPlayerUUID()).setPlaying(true);

            records.get(record).getViewers().addAll(Bukkit.getOnlinePlayers());
            records.get(record).addNPCPacket();
        }
        int maxTicks = 0;
        for (Record record : records.keySet()) {
            if (record.getRecordTicks().size() > maxTicks)
                maxTicks = record.getRecordTicks().size();
        }
        final int maxTicksFinal = maxTicks;
        replayRunnable = new BukkitRunnable() {
            int i = 0;
            int finishedRecords = 0;
            final int totalRecords = records.size();
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.translateAlternateColorCodes('&',
                            "&9Playing Tick &d" + i)));
                }

                for (Record record : records.keySet()) {
                    if (replayCache.get(record.getPlayerUUID()).isPlaying()) {
                        NPC npc = records.get(record);
                        if (i == record.getTotalTicks()) {
                            //This record is finished
                            npc.removeNPCPacket();
                            replayCache.get(record.getPlayerUUID()).setPlaying(false);
                            finishedRecords++;
                            if (finishedRecords == totalRecords) {
                                cancel();
                                return;
                            }
                            continue;
                        }
                        RecordTick tick = record.getRecordTicks().get(i);
                        RecordTick nextTick;
                        try {
                            nextTick = record.getRecordTicks().get(i + 1);
                        } catch (IndexOutOfBoundsException e) {
                            //It's in the last tick
                            nextTick = null;
                        }

                        if (i == 0) {
                            npc.setState(tick.getState());

                            npc.setEquipment(EquipmentSlot.HAND, tick.getHand());
                            npc.setEquipment(EquipmentSlot.OFF_HAND, tick.getOffHand());
                            npc.setEquipment(EquipmentSlot.HEAD, tick.getHelmet());
                            npc.setEquipment(EquipmentSlot.CHEST, tick.getChestplate());
                            npc.setEquipment(EquipmentSlot.LEGS, tick.getLeggings());
                            npc.setEquipment(EquipmentSlot.FEET, tick.getBoots());

                            lastNonNullTicks.put(record.getPlayerUUID(), tick.clone());
                        } else {
                            RecordTick lastNonNullTick = lastNonNullTicks.get(record.getPlayerUUID());

                            Vector3 travelDistance = null;
                            if (tick.getLocation() != null) {
                                Vector3 centerOffSet = Vector3Utils.getTravelDistance(record.getCenter(), tick.getLocation());
                                travelDistance = Vector3Utils.getTravelDistance(lastNonNullTick.getLocation().clone().add(centerOffSet),
                                        tick.getLocation().clone().add(centerOffSet));
                                lastNonNullTick.setLocation(tick.getLocation());
                            }

                            float yaw = tick.getYaw();
                            if (yaw == -999)
                                yaw = lastNonNullTick.getYaw();

                            float pitch = tick.getPitch();
                            if (pitch == -999)
                                pitch = lastNonNullTick.getPitch();

                            if (travelDistance != null) {
                                //TravelDistance will be null if player don't move
                                npc.moveAndLook(travelDistance.getX(), travelDistance.getY(), travelDistance.getZ(),
                                        yaw, pitch);
                            }

                            if (tick.getState() != null)
                                npc.setState(tick.getState());
                            if (tick.getHand() != null)
                                npc.setEquipment(EquipmentSlot.HAND, tick.getHand());
                            if (tick.getOffHand() != null)
                                npc.setEquipment(EquipmentSlot.OFF_HAND, tick.getOffHand());
                            if (tick.getHelmet() != null)
                                npc.setEquipment(EquipmentSlot.HEAD, tick.getHelmet());
                            if (tick.getChestplate() != null)
                                npc.setEquipment(EquipmentSlot.CHEST, tick.getChestplate());
                            if (tick.getLeggings() != null)
                                npc.setEquipment(EquipmentSlot.LEGS, tick.getLeggings());
                            if (tick.getBoots() != null)
                                npc.setEquipment(EquipmentSlot.FEET, tick.getBoots());

                            if (tick.getYaw() != -999)
                                lastNonNullTick.setYaw(tick.getYaw());
                            if (tick.getPitch() != -999)
                                lastNonNullTick.setPitch(tick.getPitch());
                            if (tick.getHealth() != -999)
                                lastNonNullTick.setHealth(tick.getHealth());
                            if (tick.getHunger() != -999)
                                lastNonNullTick.setHunger(tick.getHunger());
                            if (tick.getState() != null)
                                lastNonNullTick.setState(tick.getState());
                            if (tick.getHand() != null)
                                lastNonNullTick.setHand(tick.getHand());
                            if (tick.getOffHand() != null)
                                lastNonNullTick.setOffHand(tick.getOffHand());
                            if (tick.getHelmet() != null)
                                lastNonNullTick.setHelmet(tick.getHelmet());
                            if (tick.getChestplate() != null)
                                lastNonNullTick.setChestplate(tick.getChestplate());
                            if (tick.getLeggings() != null)
                                lastNonNullTick.setLeggings(tick.getLeggings());
                            if (tick.getBoots() != null)
                                lastNonNullTick.setBoots(tick.getBoots());
                        }

                        if (tick.didSwing())
                            npc.animate(NPCAnimation.SWING_MAIN_ARM);
                        if (tick.tookDamage()) {
                            Location npcLocation = Vector3Utils.toLocation(world, tick.getLocation());
                            if (tick.getTakenDamageType().equals(DamageType.CRITICAL)) {
                                npc.animate(NPCAnimation.CRITICAL_EFFECT);
                                for (Player player : npc.getViewers()) {
                                    player.playSound(npcLocation, XSound.ENTITY_PLAYER_ATTACK_CRIT.parseSound(), 1, 1);
                                }
                            } else {
                                npc.animate(NPCAnimation.TAKE_DAMAGE);
                                for (Player player : npc.getViewers()) {
                                    player.playSound(npcLocation, XSound.ENTITY_PLAYER_ATTACK_WEAK.parseSound(), 1, 1);
                                }
                            }
                        }

                        if (tick.getBlockPlaces() != null) {
                            for (Vector3 blockLoc : tick.getBlockPlaces().keySet()) {
                                Vector3 blockLocFinal = center.clone().add(Vector3Utils.getTravelDistance(record.getCenter(), blockLoc));
                                Location blockLocation = new Location(world, blockLocFinal.getBlockX(), blockLocFinal.getBlockY(), blockLocFinal.getBlockZ());
                                Material blockMaterial = tick.getBlockPlaces().get(blockLoc);
                                for (Player player : npc.getViewers()) {
                                    player.sendBlockChange(blockLocation, blockMaterial.createBlockData());
                                    player.playSound(blockLocation,
                                            BlockSoundUtils.getBlockSound(BlockSoundUtils.SoundType.PLACE, blockMaterial), 1, 1);
                                }
                            }
                        }

                        if (tick.getBlockBreaks() != null) {
                            for (Vector3 blockLoc : tick.getBlockBreaks().keySet()) {
                                Vector3 blockLocFinal = center.clone().add(Vector3Utils.getTravelDistance(record.getCenter(), blockLoc));
                                Location blockLocation = new Location(world, blockLocFinal.getBlockX(), blockLocFinal.getBlockY(), blockLocFinal.getBlockZ());
                                Material blockMaterial = tick.getBlockBreaks().get(blockLoc);
                                for (Player player : npc.getViewers()) {
                                    player.sendBlockChange(blockLocation, Material.AIR.createBlockData());
                                    player.playSound(blockLocation,
                                            BlockSoundUtils.getBlockSound(BlockSoundUtils.SoundType.BREAK, blockMaterial), 1, 1);
                                }
                                NMSUtils.sendBlockBreakAnimation(npc.getViewers(), blockLocFinal, -1);
                                BlockUtils.spawnBlockBreakParticles(blockLocation, tick.getBlockBreaks().get(blockLoc));
                            }
                        }

                        ReplayCache cache = replayCache.get(record.getPlayerUUID());

                        if (tick.getPendingBlockBreak() != null) {
                            npc.animate(NPCAnimation.SWING_MAIN_ARM);
                            if (!cache.getPendingBlockBreakOffSetLocations().containsKey(record.getPlayerUUID())) {
                                cache.getPendingBlockBreakOffSetLocations().put(record.getPlayerUUID(),
                                        center.clone().add(Vector3Utils.getTravelDistance(record.getCenter(), tick.getPendingBlockBreak().getLocation())));
                            }
                            if (!cache.getPendingBlockBreakSkippedParticleSpawns().containsKey(record.getPlayerUUID())) {
                                cache.getPendingBlockBreakSkippedParticleSpawns().put(record.getPlayerUUID(), 0);
                            }

                            if (cache.getPendingBlockBreakSkippedParticleSpawns().get(record.getPlayerUUID()) % 2 == 0) {
                                tick.getPendingBlockBreak().spawnParticle(world, cache.getPendingBlockBreakOffSetLocations().get(record.getPlayerUUID()));
                            }
                            cache.getPendingBlockBreakSkippedParticleSpawns().put(record.getPlayerUUID(),
                                    cache.getPendingBlockBreakSkippedParticleSpawns().get(record.getPlayerUUID()) + 1);

                            if (!cache.getPendingBlockBreakStages().containsKey(record.getPlayerUUID())) {
                                cache.getPendingBlockBreakStages().put(record.getPlayerUUID(), 0);
                            }

                            tick.getPendingBlockBreak().animateBlockBreak(npc.getViewers(),
                                    cache.getPendingBlockBreakStages().get(record.getPlayerUUID()),
                                    cache.getPendingBlockBreakOffSetLocations().get(record.getPlayerUUID()));
                            cache.getPendingBlockBreakStages().put(record.getPlayerUUID(),
                                    cache.getPendingBlockBreakStages().get(record.getPlayerUUID()) + 1);

                            if (nextTick != null && nextTick.getPendingBlockBreak() != null) {
                                if (!tick.getPendingBlockBreak().getUuid().equals(nextTick.getPendingBlockBreak().getUuid())) {
                                    cache.getPendingBlockBreakStages().remove(record.getPlayerUUID());
                                    cache.getPendingBlockBreakOffSetLocations().remove(record.getPlayerUUID());
                                    cache.getPendingBlockBreakSkippedParticleSpawns().remove(record.getPlayerUUID());
                                }
                            }
                        } else {
                            cache.getPendingBlockBreakStages().remove(record.getPlayerUUID());
                            cache.getPendingBlockBreakOffSetLocations().remove(record.getPlayerUUID());
                            cache.getPendingBlockBreakSkippedParticleSpawns().remove(record.getPlayerUUID());
                        }

                        if (tick.getEatingItem() != null) {
                            if (!cache.getPendingFoodEatSkippedTicks().containsKey(record.getPlayerUUID())) {
                                cache.getPendingFoodEatSkippedTicks().put(record.getPlayerUUID(), 1);
                            }
                            if (cache.getPendingFoodEatSkippedTicks().get(record.getPlayerUUID()) % 5 == 0) {
                                RecordTick lastNonNullTick = lastNonNullTicks.get(record.getPlayerUUID());
                                Location location = Vector3Utils.toLocation(world,
                                        center.clone().add(Vector3Utils.getTravelDistance(record.getCenter(), lastNonNullTick.getLocation())));
                                Location locationYawFixed = location.clone();
                                locationYawFixed.setYaw(lastNonNullTick.getYaw());

                                PlayerUtils.spawnFoodEatParticles(locationYawFixed, tick.getEatingItem());
                            }
                            cache.getPendingFoodEatSkippedTicks().put(record.getPlayerUUID(),
                                    cache.getPendingFoodEatSkippedTicks().get(record.getPlayerUUID()) + 1);
                        } else {
                            replayCache.get(record.getPlayerUUID()).getPendingFoodEatSkippedTicks().remove(record.getPlayerUUID());
                        }
                    }
                }

                i++;
            }
        }.runTaskTimer(plugin, 0, 1);

        return playbackControl;
    }

    public void suspend() {
        replayRunnable.cancel();
        for (Record record : records.keySet()) {
            NPC npc = records.get(record);
            npc.removeNPCPacket();
        }
    }

    private void spawnBlockBreakParticle(Location blockLoc, Material material) {
        Location center = LocUtils.simplifyToCenter(blockLoc);
        for (int i = 0; i <= 30; i++) {
            blockLoc.getWorld().spawnParticle(Particle.BLOCK_CRACK,
                    center.clone().add(getRandomInBlock(), getRandomInBlock() + 0.5, getRandomInBlock()),
                    2, material.createBlockData());
        }
    }

    private float getRandomInBlock() {
        return (float) (new Random().nextInt(10) - 5) / 10;
    }

}
