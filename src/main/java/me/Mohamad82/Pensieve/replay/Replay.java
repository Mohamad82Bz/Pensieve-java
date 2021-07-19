package me.Mohamad82.Pensieve.replay;

import me.Mohamad82.Pensieve.nms.enums.NPCAnimation;
import me.Mohamad82.Pensieve.nms.NMSUtils;
import me.Mohamad82.Pensieve.nms.NPC;
import me.Mohamad82.Pensieve.record.DamageType;
import me.Mohamad82.Pensieve.record.Record;
import me.Mohamad82.Pensieve.record.RecordTick;
import me.Mohamad82.RUoM.LocUtils;
import me.Mohamad82.RUoM.Vector3;
import me.Mohamad82.RUoM.Vector3Utils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
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
            Vector3 centerOffSetDistance = getTravelDistance(record.getCenter(), record.getStartLocation());
            Vector3 centerOffSet = center.clone().add(centerOffSetDistance.getX(), centerOffSetDistance.getY(), centerOffSetDistance.getZ());
            this.records.put(record, new NPC(Bukkit.getPlayer(record.getPlayerUUID()),
                    new Location(world, centerOffSet.getX(), centerOffSet.getY(), centerOffSet.getZ())));

            ReplayCache cache = new ReplayCache();
            replayCache.put(record.getPlayerUUID(), cache);
            cache.setCentersDistance(getTravelDistance(record.getCenter(), center));
        }
    }

    public Replay(JavaPlugin plugin, Set<Record> records, World world) {
        this(plugin, records, world, null);
    }

    public PlayBackControl start() {
        PlayBackControl control = new PlayBackControl();
        for (Record record : records.keySet()) {
            replayCache.get(record.getPlayerUUID()).setPlaying(true);

            records.get(record).getViewers().addAll(Bukkit.getOnlinePlayers());
            records.get(record).addNPCPacket();
        }
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

                            Vector3 travelDistance = Vector3.at(0, 0, 0);
                            if (tick.getLocation() != null) {
                                Vector3 centerOffSet = getTravelDistance(record.getCenter(), tick.getLocation());
                                travelDistance = getTravelDistance(lastNonNullTick.getLocation().clone().add(centerOffSet),
                                        tick.getLocation().clone().add(centerOffSet));
                                lastNonNullTick.setLocation(tick.getLocation());
                            }

                            float yaw = tick.getYaw();
                            if (yaw == -999)
                                yaw = lastNonNullTick.getYaw();

                            float pitch = tick.getPitch();
                            if (pitch == -999)
                                pitch = lastNonNullTick.getPitch();

                            npc.moveAndLook(travelDistance.getX(), travelDistance.getY(), travelDistance.getZ(),
                                    yaw, pitch);

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
                            if (tick.getTakenDamageType().equals(DamageType.CRITICAL))
                                npc.animate(NPCAnimation.CRITICAL_EFFECT);
                            else
                                npc.animate(NPCAnimation.TAKE_DAMAGE);
                        }

                        if (tick.getBlockPlaces() != null) {
                            for (Vector3 blockLoc : tick.getBlockPlaces().keySet()) {
                                Vector3 blockLocFinal = center.clone().add(getTravelDistance(record.getCenter(), blockLoc));
                                Location blockLocation = new Location(world, blockLocFinal.getBlockX(), blockLocFinal.getBlockY(), blockLocFinal.getBlockZ());
                                for (Player player : npc.getViewers()) {
                                    player.sendBlockChange(blockLocation, tick.getBlockPlaces().get(blockLoc).createBlockData());
                                }
                            }
                        }

                        if (tick.getBlockBreaks() != null) {
                            for (Vector3 blockLoc : tick.getBlockBreaks().keySet()) {
                                Vector3 blockLocFinal = center.clone().add(getTravelDistance(record.getCenter(), blockLoc));
                                Location blockLocation = new Location(world, blockLocFinal.getBlockX(), blockLocFinal.getBlockY(), blockLocFinal.getBlockZ());
                                for (Player player : npc.getViewers()) {
                                    player.sendBlockChange(blockLocation, Material.AIR.createBlockData());
                                }
                                NMSUtils.sendBlockBreakAnimation(npc.getViewers(), blockLocFinal, -1);
                                spawnBlockBreakParticle(blockLocation, tick.getBlockBreaks().get(blockLoc));
                            }
                        }

                        if (tick.getPendingBlockBreak() != null) {
                            npc.animate(NPCAnimation.SWING_MAIN_ARM);
                            ReplayCache cache = replayCache.get(record.getPlayerUUID());
                            if (!cache.getPendingBlockBreakOffSetLocations().containsKey(record.getPlayerUUID())) {
                                cache.getPendingBlockBreakOffSetLocations().put(record.getPlayerUUID(),
                                        center.clone().add(getTravelDistance(record.getCenter(), tick.getPendingBlockBreak().getLocation())));
                            }
                            tick.getPendingBlockBreak().spawnParticle(world, cache.getPendingBlockBreakOffSetLocations().get(record.getPlayerUUID()));

                            if (!cache.getPendingBlockBreakStages().containsKey(record.getPlayerUUID())) {
                                cache.getPendingBlockBreakStages().put(record.getPlayerUUID(), 0);
                            }

                            tick.getPendingBlockBreak().animateBlockBreak(npc.getViewers(),
                                    cache.getPendingBlockBreakStages().get(record.getPlayerUUID()),
                                    cache.getPendingBlockBreakOffSetLocations().get(record.getPlayerUUID()));
                            cache.getPendingBlockBreakStages().put(record.getPlayerUUID(),
                                    cache.getPendingBlockBreakStages().get(record.getPlayerUUID()) + 1);
                        } else {
                            ReplayCache cache = replayCache.get(record.getPlayerUUID());
                            cache.getPendingBlockBreakStages().remove(record.getPlayerUUID());
                            cache.getPendingBlockBreakOffSetLocations().remove(record.getPlayerUUID());
                        }

                        if (tick.getEatingItem() != null) {
                            ReplayCache cache = replayCache.get(record.getPlayerUUID());

                            if (!cache.getPendingFoodEatSkippedTicks().containsKey(record.getPlayerUUID())) {
                                cache.getPendingFoodEatSkippedTicks().put(record.getPlayerUUID(), 1);
                            }
                            if (cache.getPendingFoodEatSkippedTicks().get(record.getPlayerUUID()) % 5 == 0) {
                                RecordTick lastNonNullTick = lastNonNullTicks.get(record.getPlayerUUID());
                                Location location = Vector3Utils.toLocation(world,
                                        center.clone().add(getTravelDistance(record.getCenter(), lastNonNullTick.getLocation())));
                                animateFoodEat(location, lastNonNullTick.getYaw(), tick.getEatingItem());
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

        return control;
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
        Random random = new Random();
        for (int i = 0; i <= 30; i++) {
            blockLoc.getWorld().spawnParticle(Particle.BLOCK_CRACK,
                    center.clone().add(getRandomInBlock(), getRandomInBlock() + 0.5, getRandomInBlock()),
                    2, material.createBlockData());
        }
    }

    private float getRandomInBlock() {
        return (float) (new Random().nextInt(10) - 5) / 10;
    }

    private void animateFoodEat(Location location, float yaw, ItemStack item) {
        Random random = new Random();
        Location rightSide = getRightSide(location, yaw).add(0, -0.25, 0);
        for (int i = 0; i < 11; i++) {
            if (random.nextInt(7) < 1) continue;
            float a1 = (float) (random.nextInt(4) - 2) / 10;
            float a2 = (float) (random.nextInt(4) - 2) / 10;
            float a3 = (float) (random.nextInt(15) - 5) / 100;

            location.getWorld().spawnParticle(Particle.ITEM_CRACK, rightSide,
                    0, 0 + a1, 1, 0 + a2, 0.23 + a3,
                    item);
        }
    }

    private Location getRightSide(Location location, float yaw) {
        double yawRightHandDirection = Math.toRadians(-1 * yaw);
        double x = 0.5 * Math.sin(yawRightHandDirection) + location.getX();
        double y = location.getY() + 1;
        double z = 0.5 * Math.cos(yawRightHandDirection) + location.getZ();
        return new Location(location.getWorld(), x, y, z);
    }

    private Vector3 getTravelDistance(Vector3 from, Vector3 to) {
        double xD = Math.abs(from.getX() - to.getX());
        double yD = Math.abs(from.getY() - to.getY());
        double zD = Math.abs(from.getZ() - to.getZ());
        if (from.getX() > to.getX()) {
            xD *= -1;
        }

        if (from.getY() > to.getY()) {
            yD *= -1;
        }

        if (from.getZ() > to.getZ()) {
            zD *= -1;
        }

        return Vector3.at(xD, yD, zD);
    }

}
