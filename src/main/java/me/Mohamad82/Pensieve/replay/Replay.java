package me.Mohamad82.Pensieve.replay;

import me.Mohamad82.Pensieve.nms.NMSProvider;
import me.Mohamad82.Pensieve.nms.enums.EntityMetadata;
import me.Mohamad82.Pensieve.nms.enums.EntityNPCType;
import me.Mohamad82.Pensieve.nms.enums.NPCAnimation;
import me.Mohamad82.Pensieve.nms.npc.EntityNPC;
import me.Mohamad82.Pensieve.nms.npc.NPC;
import me.Mohamad82.Pensieve.nms.npc.PlayerNPC;
import me.Mohamad82.Pensieve.record.EntityRecord;
import me.Mohamad82.Pensieve.record.PlayerRecord;
import me.Mohamad82.Pensieve.record.RecordTick;
import me.Mohamad82.Pensieve.record.enums.DamageType;
import me.Mohamad82.Pensieve.utils.BlockSoundUtils;
import me.Mohamad82.RUoM.Ruom;
import me.Mohamad82.RUoM.XSeries.XMaterial;
import me.Mohamad82.RUoM.XSeries.XSound;
import me.Mohamad82.RUoM.utils.BlockUtils;
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

    private static final Sound ENTITY_ARROW_HIT, ENTITY_ARROW_SHOOT, ENTITY_PLAYER_HURT, ENTITY_PLAYER_ATTACK_CRIT, ENTITY_PLAYER_ATTACK_WEAK,
            ENTITY_SPLASH_POTION_BREAK, ENTITY_SPLASH_POTION_THROW, ENTITY_GENERIC_EAT, ENTITY_GENERIC_DRINK, ITEM_CROSSBOW_SHOOT, ITEM_CROSSBOW_HIT,
            ITEM_CROSSBOW_LOADING_START, ITEM_CROSSBOW_LOADING_MIDDLE, ITEM_CROSSBOW_LOADING_END;

    static {
        ENTITY_ARROW_HIT = XSound.ENTITY_ARROW_HIT.parseSound();
        ENTITY_ARROW_SHOOT = XSound.ENTITY_ARROW_SHOOT.parseSound();
        ENTITY_PLAYER_HURT = XSound.ENTITY_PLAYER_HURT.parseSound();
        ENTITY_PLAYER_ATTACK_CRIT = XSound.ENTITY_PLAYER_ATTACK_CRIT.parseSound();
        ENTITY_PLAYER_ATTACK_WEAK = XSound.ENTITY_PLAYER_ATTACK_WEAK.parseSound();
        ENTITY_SPLASH_POTION_BREAK = XSound.ENTITY_SPLASH_POTION_BREAK.parseSound();
        ENTITY_SPLASH_POTION_THROW = XSound.ENTITY_SPLASH_POTION_THROW.parseSound();
        ENTITY_GENERIC_EAT = XSound.ENTITY_GENERIC_EAT.parseSound();
        ENTITY_GENERIC_DRINK = XSound.ENTITY_GENERIC_DRINK.parseSound();
        ITEM_CROSSBOW_SHOOT = XSound.ITEM_CROSSBOW_SHOOT.parseSound();
        ITEM_CROSSBOW_HIT = XSound.ITEM_CROSSBOW_HIT.parseSound();
        ITEM_CROSSBOW_LOADING_START = XSound.ITEM_CROSSBOW_LOADING_START.parseSound();
        ITEM_CROSSBOW_LOADING_MIDDLE = XSound.ITEM_CROSSBOW_LOADING_MIDDLE.parseSound();
        ITEM_CROSSBOW_LOADING_END = XSound.ITEM_CROSSBOW_LOADING_END.parseSound();
    }

    private final Map<PlayerRecord, PlayerNPC> playerRecords = new HashMap<>();
    private final Map<EntityRecord, EntityNPC> entityRecords = new HashMap<>();
    private final Map<UUID, RecordTick> lastNonNullTicks = new HashMap<>();
    private final Map<UUID, ReplayCache> replayCache = new HashMap<>();
    private final Map<UUID, UUID> modifiedUuids = new HashMap<>();

    private final JavaPlugin plugin;
    private final World world;
    private final Vector3 center;

    private BukkitTask replayRunnable;
    private final Random random = new Random();

    public Replay(JavaPlugin plugin, Set<PlayerRecord> playerRecords, Set<EntityRecord> entityRecords, World world, Vector3 center) {
        this.plugin = plugin;
        this.world = world;
        this.center = Vector3Utils.simplifyToCenter(center);

        for (PlayerRecord record : playerRecords) {
            Vector3 centerOffSetDistance = Vector3Utils.getTravelDistance(record.getCenter(), record.getStartLocation());
            Vector3 centerOffSet = center.clone().add(centerOffSetDistance).add(0.5, 0, 0.5);
            this.playerRecords.put(record, new PlayerNPC(
                    record.getName(),
                    Vector3Utils.toLocation(world, centerOffSet),
                    record.getSkin()
            ));
            ReplayCache cache = new ReplayCache();
            replayCache.put(record.getUuid(), cache);
            cache.setCentersDistance(Vector3Utils.getTravelDistance(record.getCenter(), center));
        }

        for (EntityRecord record : entityRecords) {
            Vector3 centerOffSetDistance = Vector3Utils.getTravelDistance(record.getCenter(), record.getStartLocation());
            Vector3 centerOffSet = center.clone().add(centerOffSetDistance).add(0.5, 0, 0.5);
            UUID uuid = UUID.randomUUID();
            modifiedUuids.put(record.getUuid(), uuid);
            this.entityRecords.put(record, new EntityNPC(
                    uuid,
                    Vector3Utils.toLocation(world, centerOffSet),
                    record.getEntityType()
            ));
        }
    }

    public Replay(JavaPlugin plugin, Set<PlayerRecord> playerRecords, Set<EntityRecord> entityRecords, World world) {
        this(plugin, playerRecords, entityRecords, world, Vector3.at(0.5, 100, 0.5));
    }

    public PlayBackControl start() {
        PlayBackControl playbackControl = new PlayBackControl();

        int maxTicks = 0;

        for (PlayerRecord record : playerRecords.keySet()) {
            replayCache.get(record.getUuid()).setPlaying(true);
            playerRecords.get(record).getViewers().addAll(Ruom.getOnlinePlayers());
            playerRecords.get(record).addNPCPacket();

            if (record.getRecordTicks().size() > maxTicks)
                maxTicks = record.getRecordTicks().size();
        }
        playbackControl.setMaxProgress(maxTicks);
        replayRunnable = new BukkitRunnable() {
            int tickIndex = 0;
            int lowSpeedHelpIndex = 0;
            boolean shouldPlayThisTick;
            int finishedRecords = 0;
            final int totalRecords = playerRecords.size();
            public void run() {
                try {
                    for (Player player : Ruom.getOnlinePlayers()) {
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.translateAlternateColorCodes('&',
                                "&9Playing Tick &d" + tickIndex + " / " + playbackControl.getMaxProgress())));
                    }
                    if (playbackControl.getProgress() + 1 == tickIndex) {
                        playbackControl.addProgress(1);
                    } else {
                        //Viewer changed the tick index
                        //TODO: lastNonNullTick will make issue here, we should save lastNonNullTicks for every tick to fix this.
                        tickIndex = playbackControl.getProgress();
                    }
                    if (playbackControl.isPause()) {
                        return;
                    }
                    switch (playbackControl.getSpeed()) {
                        case x5: {
                            playbackControl.addProgress(4);
                            tickIndex = playbackControl.getProgress();
                            break;
                        }
                        case x2: {
                            playbackControl.addProgress(1);
                            tickIndex = playbackControl.getProgress();
                            break;
                        }
                        case x1: {
                            shouldPlayThisTick = true;
                            lowSpeedHelpIndex = 0;
                            break;
                        }
                        case x050: {
                            lowSpeedHelpIndex++;
                            if (lowSpeedHelpIndex % 2 == 0) {
                                shouldPlayThisTick = true;
                                lowSpeedHelpIndex = 0;
                            }
                            break;
                        }
                        case x025: {
                            lowSpeedHelpIndex++;
                            if (lowSpeedHelpIndex % 4 == 0) {
                                shouldPlayThisTick = true;
                                lowSpeedHelpIndex = 0;
                            }
                            break;
                        }
                    }

                    for (EntityRecord record : entityRecords.keySet()) {
                        UUID uuid = modifiedUuids.get(record.getUuid());
                        if (replayCache.containsKey(uuid) && !replayCache.get(uuid).isPlaying()) continue;
                        if (tickIndex < record.getStartingTick()) continue;
                        EntityNPC npc = entityRecords.get(record);
                        if (!lastNonNullTicks.containsKey(uuid)) {
                            ReplayCache cache = new ReplayCache();
                            cache.setPlaying(true);
                            replayCache.put(uuid, cache);

                            npc.addViewers(Ruom.getOnlinePlayers());
                            npc.addNPCPacket();
                            if (record.getItem() != null) {
                                if (record.getItem().getType().equals(XMaterial.SPLASH_POTION.parseMaterial())) {
                                    npc.setMetadata(EntityMetadata.getPotionMetadataId(), NMSProvider.getNmsItemStack(record.getItem()));
                                }
                            }
                            lastNonNullTicks.put(uuid, record.getRecordTicks().get(0).clone());
                        } else {
                            int entityTickIndex = tickIndex - (record.getStartingTick() + 1);
                            if (entityTickIndex >= record.getRecordTicks().size()) {
                                replayCache.get(uuid).setPlaying(false);
                                npc.removeNPCPacket();
                            } else {
                                RecordTick tick = record.getRecordTicks().get(entityTickIndex);
                                RecordTick lastNonNullTick = lastNonNullTicks.get(uuid);

                                if (tick.getLocation() != null) {
                                    moveNPC(tick, lastNonNullTick, npc, record.getCenter(), shouldPlayThisTick, lowSpeedHelpIndex, playbackControl.getSpeed(), false);
                                }

                                Location location = Vector3Utils.toLocation(world,
                                        center.clone().add(Vector3Utils.getTravelDistance(record.getCenter(), lastNonNullTick.getLocation())));

                                if (tick.getVelocity() != null) {
                                    npc.velocity(tick.getVelocity());

                                    if (lastNonNullTick.getLocation() != null) {
                                        if (entityTickIndex != 0) {
                                            if (record.getEntityType().equals(EntityNPCType.ARROW)) {
                                                float soundPitch = (float) (1.1 + (random.nextInt(2) / 10));
                                                for (Player player : npc.getViewers()) {
                                                    player.playSound(location, ENTITY_ARROW_HIT, playbackControl.getVolume(), soundPitch);
                                                }
                                            }
                                        }
                                    }
                                }
                                if (entityTickIndex == record.getRecordTicks().size() - 1) {
                                    if (record.getEntityType().equals(EntityNPCType.POTION)) {
                                        for (Player player : npc.getViewers()) {
                                            Ruom.log("Playing");
                                            player.playSound(location, ENTITY_SPLASH_POTION_BREAK, playbackControl.getVolume() - 0.3f, 1);
                                            //noinspection deprecation
                                            player.playEffect(location, Effect.POTION_BREAK, NMSProvider.getPotionColor(record.getItem()));
                                        }
                                    }
                                }
                            }
                        }
                    }

                    for (PlayerRecord record : playerRecords.keySet()) {
                        if (replayCache.get(record.getUuid()).isPlaying()) {
                            PlayerNPC npc = playerRecords.get(record);
                            if (tickIndex == record.getTotalTicks()) {
                                //This record is finished
                                npc.removeNPCPacket();
                                replayCache.get(record.getUuid()).setPlaying(false);
                                finishedRecords++;
                                if (finishedRecords == totalRecords) {
                                    entityRecords.forEach((entityRecord, entityNPC) -> {
                                        entityNPC.removeNPCPacket();
                                    });
                                    cancel();
                                    return;
                                }
                                continue;
                            }
                            RecordTick tick = record.getRecordTicks().get(tickIndex);
                            RecordTick lastNonNullTick;
                            RecordTick nextTick;
                            try {
                                nextTick = record.getRecordTicks().get(tickIndex + 1);
                            } catch (IndexOutOfBoundsException e) {
                                //It's the last tickIndex
                                nextTick = null;
                            }

                            if (tickIndex == 0) {
                                npc.setState(tick.getState());

                                npc.setEquipment(EquipmentSlot.HAND, tick.getHand());
                                npc.setEquipment(EquipmentSlot.OFF_HAND, tick.getOffHand());
                                npc.setEquipment(EquipmentSlot.HEAD, tick.getHelmet());
                                npc.setEquipment(EquipmentSlot.CHEST, tick.getChestplate());
                                npc.setEquipment(EquipmentSlot.LEGS, tick.getLeggings());
                                npc.setEquipment(EquipmentSlot.FEET, tick.getBoots());

                                lastNonNullTicks.put(record.getUuid(), tick.clone());
                                lastNonNullTick = lastNonNullTicks.get(record.getUuid());
                            } else {
                                lastNonNullTick = lastNonNullTicks.get(record.getUuid());

                                moveNPC(tick, lastNonNullTick, npc, record.getCenter(), shouldPlayThisTick, lowSpeedHelpIndex, playbackControl.getSpeed(), true);

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

                            Location location = Vector3Utils.toLocation(world,
                                    center.clone().add(Vector3Utils.getTravelDistance(record.getCenter(), lastNonNullTick.getLocation())));

                            if (tick.didSwing())
                                npc.animate(NPCAnimation.SWING_MAIN_ARM);
                            if (tick.tookDamage()) {
                                npc.animate(NPCAnimation.TAKE_DAMAGE);
                                if (tick.getTakenDamageType().equals(DamageType.CRITICAL)) {
                                    npc.animate(NPCAnimation.CRITICAL_EFFECT);
                                    for (Player player : npc.getViewers()) {
                                        player.playSound(location, ENTITY_PLAYER_ATTACK_CRIT, playbackControl.getVolume(), 1);
                                        player.playSound(location, ENTITY_PLAYER_HURT, playbackControl.getVolume(), 1);
                                    }
                                } else {
                                    for (Player player : npc.getViewers()) {
                                        player.playSound(location, ENTITY_PLAYER_HURT, playbackControl.getVolume(), 1);
                                        player.playSound(location, ENTITY_PLAYER_ATTACK_WEAK, playbackControl.getVolume(), 1);
                                    }
                                }
                            }
                            if (tick.threwPotion()) {
                                for (Player player : npc.getViewers()) {
                                    player.playSound(location, ENTITY_SPLASH_POTION_THROW, playbackControl.getVolume() - 0.5f, 0.1f);
                                }
                            }
                            if (tick.getDrawBow() > 0) {
                                EntityMetadata.ItemUseKey itemUseKey;
                                if (tick.drawnBowWithOffHand())
                                    itemUseKey = EntityMetadata.ItemUseKey.OFFHAND_RELEASE;
                                else
                                    itemUseKey = EntityMetadata.ItemUseKey.RELEASE;
                                npc.setMetadata(EntityMetadata.ItemUseKey.getMetadataId(), itemUseKey.getBitMask());

                                if (!tick.drawnCrossbow()) {
                                    float soundPitch;
                                    if (tick.getDrawBow() < 300) {
                                        soundPitch = 0.8f;
                                    } else if (tick.getDrawBow() < 500) {
                                        soundPitch = 0.9f;
                                    } else if (tick.getDrawBow() < 750) {
                                        soundPitch = 1.0f;
                                    } else if (tick.getDrawBow() < 1000) {
                                        soundPitch = 1.1f;
                                    } else {
                                        soundPitch = 1.2f;
                                    }
                                    for (Player player : npc.getViewers()) {
                                        player.playSound(location, ENTITY_ARROW_SHOOT, playbackControl.getVolume(), soundPitch + (float) (random.nextInt(2) - 1) / 10);
                                    }
                                }
                            } else if (tick.getDrawBow() == 0) {
                                EntityMetadata.ItemUseKey itemUseKey;
                                if (tick.drawnBowWithOffHand())
                                    itemUseKey = EntityMetadata.ItemUseKey.OFFHAND_HOLD;
                                else
                                    itemUseKey = EntityMetadata.ItemUseKey.HOLD;
                                npc.setMetadata(EntityMetadata.ItemUseKey.getMetadataId(), itemUseKey.getBitMask());
                            }
                            if (tick.shotCrossbow()) {
                                for (Player player : npc.getViewers()) {
                                    player.playSound(location, ITEM_CROSSBOW_SHOOT, playbackControl.getVolume(), 1f);
                                }
                            }
                            Sound crossbowLoadingSound = null;
                            switch (tick.getCrossbowChargeLevel()) {
                                case 2: {
                                    crossbowLoadingSound = ITEM_CROSSBOW_LOADING_END;
                                    break;
                                }
                                case 1: {
                                    crossbowLoadingSound = ITEM_CROSSBOW_LOADING_MIDDLE;
                                    break;
                                }
                                case 0: {
                                    crossbowLoadingSound = ITEM_CROSSBOW_LOADING_START;
                                    break;
                                }
                            }
                            if (crossbowLoadingSound != null) {
                                for (Player player : npc.getViewers()) {
                                    player.playSound(location, crossbowLoadingSound, playbackControl.getVolume() - 0.3f, 1);
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
                                                BlockSoundUtils.getBlockSound(BlockSoundUtils.SoundType.PLACE, blockMaterial), playbackControl.getVolume(), 0.8f);
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
                                                BlockSoundUtils.getBlockSound(BlockSoundUtils.SoundType.BREAK, blockMaterial), playbackControl.getVolume(), 0.8f);
                                    }
                                    NMSProvider.sendBlockBreakAnimation(npc.getViewers(), blockLocFinal, -1);
                                    BlockUtils.spawnBlockBreakParticles(blockLocation, tick.getBlockBreaks().get(blockLoc));
                                }
                            }

                            ReplayCache cache = replayCache.get(record.getUuid());

                            if (tick.getPendingBlockBreak() != null) {
                                npc.animate(NPCAnimation.SWING_MAIN_ARM);
                                if (!cache.getPendingBlockBreakOffSetLocations().containsKey(record.getUuid())) {
                                    cache.getPendingBlockBreakOffSetLocations().put(record.getUuid(),
                                            center.clone().add(Vector3Utils.getTravelDistance(record.getCenter(), tick.getPendingBlockBreak().getLocation())));
                                }
                                if (!cache.getPendingBlockBreakSkippedParticleSpawns().containsKey(record.getUuid())) {
                                    cache.getPendingBlockBreakSkippedParticleSpawns().put(record.getUuid(), 0);
                                }

                                if (cache.getPendingBlockBreakSkippedParticleSpawns().get(record.getUuid()) % 2 == 0) {
                                    tick.getPendingBlockBreak().spawnParticle(world, cache.getPendingBlockBreakOffSetLocations().get(record.getUuid()));
                                }
                                cache.getPendingBlockBreakSkippedParticleSpawns().put(record.getUuid(),
                                        cache.getPendingBlockBreakSkippedParticleSpawns().get(record.getUuid()) + 1);

                                if (!cache.getPendingBlockBreakStages().containsKey(record.getUuid())) {
                                    cache.getPendingBlockBreakStages().put(record.getUuid(), 0);
                                }

                                tick.getPendingBlockBreak().animateBlockBreak(npc.getViewers(),
                                        cache.getPendingBlockBreakStages().get(record.getUuid()),
                                        cache.getPendingBlockBreakOffSetLocations().get(record.getUuid()));
                                cache.getPendingBlockBreakStages().put(record.getUuid(),
                                        cache.getPendingBlockBreakStages().get(record.getUuid()) + 1);

                                if (nextTick != null && nextTick.getPendingBlockBreak() != null) {
                                    if (!tick.getPendingBlockBreak().getUuid().equals(nextTick.getPendingBlockBreak().getUuid())) {
                                        cache.getPendingBlockBreakStages().remove(record.getUuid());
                                        cache.getPendingBlockBreakOffSetLocations().remove(record.getUuid());
                                        cache.getPendingBlockBreakSkippedParticleSpawns().remove(record.getUuid());
                                    }
                                }
                            } else {
                                cache.getPendingBlockBreakStages().remove(record.getUuid());
                                cache.getPendingBlockBreakOffSetLocations().remove(record.getUuid());
                                cache.getPendingBlockBreakSkippedParticleSpawns().remove(record.getUuid());
                            }

                            if (tick.getEatingItem() != null) {
                                if (!cache.getPendingFoodEatSkippedTicks().containsKey(record.getUuid())) {
                                    cache.getPendingFoodEatSkippedTicks().put(record.getUuid(), 1);
                                }
                                if (cache.getPendingFoodEatSkippedTicks().get(record.getUuid()) % 7 == 0) {
                                    Location locationYawFixed = location.clone();
                                    locationYawFixed.setYaw(lastNonNullTick.getYaw());

                                    PlayerUtils.spawnFoodEatParticles(locationYawFixed, tick.getEatingItem());
                                } else if (cache.getPendingFoodEatSkippedTicks().get(record.getUuid()) % 4 == 0) {
                                    for (Player player : npc.getViewers()) {
                                        player.playSound(location, ENTITY_GENERIC_EAT, playbackControl.getVolume(), 1f);
                                    }
                                }
                                cache.getPendingFoodEatSkippedTicks().put(record.getUuid(),
                                        cache.getPendingFoodEatSkippedTicks().get(record.getUuid()) + 1);
                            } else {
                                replayCache.get(record.getUuid()).getPendingFoodEatSkippedTicks().remove(record.getUuid());
                            }
                        }
                    }

                    tickIndex++;
                } catch (Exception e) {
                    e.printStackTrace();
                    Ruom.error("An error occured while playing a replay, The replay was terminated to prevent furthur errors." +
                            " Please report errors you see above to the developer.");
                    for (PlayerRecord record : playerRecords.keySet()) {
                        playerRecords.get(record).removeNPCPacket();
                    }
                    for (EntityRecord record : entityRecords.keySet()) {
                        entityRecords.get(record).removeNPCPacket();
                    }
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0, 2);

        return playbackControl;
    }

    public void suspend() {
        replayRunnable.cancel();
        for (PlayerRecord record : playerRecords.keySet()) {
            PlayerNPC npc = playerRecords.get(record);
            npc.removeNPCPacket();
        }
    }

    private void moveNPC(RecordTick tick, RecordTick lastNonNullTick, NPC npc, Vector3 recordCenter, boolean shouldPlayThisTick, int lowSpeedHelpIndex, PlayBackControl.Speed speed, boolean onGround) {
        Vector3 travelDistance = null;
        if (tick.getLocation() != null) {
            Vector3 centerOffSet = Vector3Utils.getTravelDistance(recordCenter, tick.getLocation());
            Vector3 lastPoint = lastNonNullTick.getLocation().clone().add(centerOffSet);
            Vector3 newPoint = tick.getLocation().clone().add(centerOffSet);

            if (!shouldPlayThisTick) {
                travelDistance = getCalculatedLowSpeedTravelDistance(lastNonNullTick, lastPoint, newPoint, speed, lowSpeedHelpIndex);
            } else {
                travelDistance = Vector3Utils.getTravelDistance(lastPoint, newPoint);
                lastNonNullTick.setLocation(tick.getLocation());
            }
        }

        float yaw;
        float pitch;
        float newYaw = tick.getYaw();
        float newPitch = tick.getPitch();
        float lastYaw = lastNonNullTick.getYaw();
        float lastPitch = lastNonNullTick.getPitch();
        yaw = getCalculatedLookAngle(lastYaw, newYaw, speed, lowSpeedHelpIndex);
        pitch = getCalculatedLookAngle(lastPitch, newPitch, speed, lowSpeedHelpIndex);
        if (!shouldPlayThisTick) {
            if (lowSpeedHelpIndex == 3)
                lastNonNullTick.setYaw(yaw);
            if (lowSpeedHelpIndex == 3)
                lastNonNullTick.setPitch(pitch);
        } else {
            if (yaw == -999)
                yaw = lastYaw;

            if (pitch == -999)
                pitch = lastPitch;
        }

        lastNonNullTick.setYaw(yaw);
        lastNonNullTick.setPitch(pitch);

        //TravelDistance will be null if player don't move
        if (travelDistance != null && !travelDistance.equals(Vector3.at(0, 0, 0))) {
            //Returns false if distance was more than 8 blocks, Move packet does not support more than 8 blocks.
            if (!npc.moveAndLook(travelDistance.getX(), travelDistance.getY(), travelDistance.getZ(), yaw, pitch, onGround)) {
                //TODO Probably broken
                Vector3 centerOffSet = Vector3Utils.getTravelDistance(recordCenter, tick.getLocation());
                Vector3 location = tick.getLocation().clone().add(centerOffSet);
                npc.teleport(location, yaw, pitch, true);
            }
        } else {
            if (!(yaw == lastYaw && pitch == lastPitch))
                npc.look(yaw, pitch);
        }
    }

    private Vector3 getCalculatedLowSpeedTravelDistance(RecordTick lastNonNullTick, Vector3 from, Vector3 to, PlayBackControl.Speed speed, int lowSpeedHelpIndex) {
        Vector3 travelDistance = null;
        Vector3 centerPoint = Vector3Utils.getCenter(from, to);
        if (speed.equals(PlayBackControl.Speed.x050)) {
            travelDistance = Vector3Utils.getTravelDistance(from, centerPoint);
            lastNonNullTick.setLocation(centerPoint);
        } else if (speed.equals(PlayBackControl.Speed.x025)) {
            switch (lowSpeedHelpIndex) {
                case 1: {
                    Vector3 centerOfCenterPoint = Vector3Utils.getCenter(from, centerPoint);
                    travelDistance = Vector3Utils.getTravelDistance(from, centerOfCenterPoint);
                    break;
                }
                case 2: {
                    Vector3 centerOfCenterPoint = Vector3Utils.getCenter(from, centerPoint);
                    travelDistance = Vector3Utils.getTravelDistance(centerOfCenterPoint, centerPoint);
                    break;
                }
                case 3: {
                    Vector3 centerOfCenterPoint = Vector3Utils.getCenter(centerPoint, to);
                    travelDistance = Vector3Utils.getTravelDistance(centerPoint, centerOfCenterPoint);
                    lastNonNullTick.setLocation(centerOfCenterPoint);
                    break;
                }
            }
        }

        return travelDistance;
    }

    private float getCalculatedLookAngle(float oldAngle, float newAngle, PlayBackControl.Speed speed, int lowSpeedHelpIndex) {
        float angle = 0;
        float centerAngle = (newAngle + oldAngle) / 2;
        if (speed.equals(PlayBackControl.Speed.x1)) {
            angle = newAngle;
        } else if (speed.equals(PlayBackControl.Speed.x050)) {
            if (newAngle != -999) {
                angle = centerAngle;
            }
        } else {
            switch (lowSpeedHelpIndex) {
                case 1: {
                    if (newAngle != -999)
                        angle = (oldAngle + centerAngle) / 2;
                    break;
                }
                case 2: {
                    if (newAngle != -999)
                        angle = centerAngle;
                    break;
                }
                case 3: {
                    if (newAngle != -999) {
                        angle = (centerAngle + newAngle) / 2;
                    }
                    break;
                }
            }
        }
        return angle;
    }

}
