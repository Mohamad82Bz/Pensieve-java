package me.mohamad82.pensieve.replaying;

import me.Mohamad82.RUoM.Ruom;
import me.Mohamad82.RUoM.XSeries.XMaterial;
import me.Mohamad82.RUoM.XSeries.XSound;
import me.Mohamad82.RUoM.adventureapi.ComponentUtils;
import me.Mohamad82.RUoM.adventureapi.adventure.platform.bukkit.MinecraftComponentSerializer;
import me.Mohamad82.RUoM.adventureapi.adventure.text.Component;
import me.Mohamad82.RUoM.utils.BlockUtils;
import me.Mohamad82.RUoM.utils.ListUtils;
import me.Mohamad82.RUoM.utils.PlayerUtils;
import me.Mohamad82.RUoM.vector.Vector3;
import me.Mohamad82.RUoM.vector.Vector3Utils;
import me.mohamad82.pensieve.nms.EntityMetadata;
import me.mohamad82.pensieve.nms.NMSProvider;
import me.mohamad82.pensieve.nms.hologram.Hologram;
import me.mohamad82.pensieve.nms.hologram.HologramLine;
import me.mohamad82.pensieve.nms.npc.EntityNPC;
import me.mohamad82.pensieve.nms.npc.NPC;
import me.mohamad82.pensieve.nms.npc.PlayerNPC;
import me.mohamad82.pensieve.nms.npc.enums.EntityNPCType;
import me.mohamad82.pensieve.nms.npc.enums.NPCAnimation;
import me.mohamad82.pensieve.recording.EntityRecord;
import me.mohamad82.pensieve.recording.PlayerRecord;
import me.mohamad82.pensieve.recording.RecordTick;
import me.mohamad82.pensieve.recording.enums.DamageType;
import me.mohamad82.pensieve.utils.BlockSoundUtils;
import me.mohamad82.pensieve.utils.SoundContainer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Replay {

    private static final String HOLOGRAM_LINE_PING = String.format("<gradient:aqua:blue>Ping: <gradient:blue:aqua>%s", 0);
    private static final String HOLOGRAM_LINE_CPS = String.format("<gradient:aqua:blue>CPS: <gradient:blue:aqua>%s", 0);

    private final Map<PlayerRecord, PlayerNPC> playerRecords = new HashMap<>();
    private final Map<EntityRecord, EntityNPC> entityRecords = new HashMap<>();
    private final Map<UUID, RecordTick> lastNonNullTicks = new HashMap<>();
    private final Map<UUID, ReplayCache> replayCache = new HashMap<>();
    private final Map<UUID, Hologram> playerHolograms = new HashMap<>();
    private final Map<UUID, UUID> modifiedUuids = new HashMap<>();

    private final World world;
    private final Vector3 center;

    private BukkitTask replayRunnable;
    private final Random random = new Random();

    public Replay(Set<PlayerRecord> playerRecords, Set<EntityRecord> entityRecords, World world, Vector3 center) {
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

            Hologram hologram = Hologram.hologram(
                    ListUtils.toList(
                            HologramLine.hologramLine(ComponentUtils.parse(String.format("<gold>%s", record.getName())), 0),
                            HologramLine.hologramLine(ComponentUtils.parse(HOLOGRAM_LINE_PING), 0.3f),
                            HologramLine.hologramLine(ComponentUtils.parse(HOLOGRAM_LINE_CPS), 0.225f)
                    ),
                    Vector3Utils.toLocation(world, record.getStartLocation()).add(0, 2.5, 0)
            );
            playerHolograms.put(record.getUuid(), hologram);
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

    public Replay(Set<PlayerRecord> playerRecords, Set<EntityRecord> entityRecords, World world) {
        this(playerRecords, entityRecords, world, Vector3.at(0.5, 100, 0.5));
    }

    public PlayBackControl start() {
        PlayBackControl playbackControl = new PlayBackControl();
        playbackControl.setSpeed(PlayBackControl.Speed.x050);

        int maxTicks = 0;

        for (PlayerRecord record : playerRecords.keySet()) {
            replayCache.get(record.getUuid()).setPlaying(true);
            PlayerNPC npc = playerRecords.get(record);
            npc.getViewers().addAll(Ruom.getOnlinePlayers());
            npc.addNPCPacket();
            npc.removeNPCTabList();
            npc.setMetadata(EntityMetadata.getEntityCustomNameVisibilityId(), false);
            //noinspection UnstableApiUsage
            npc.setMetadata(EntityMetadata.getEntityCustomNameId(), Optional.of(MinecraftComponentSerializer.get().serialize(Component.text(record.getName()))));

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
                            } else {
                                shouldPlayThisTick = false;
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
                            if (record.getDroppedItem() != null) {
                                npc.setMetadata(EntityMetadata.getDroppedItemMetadataId(), NMSProvider.getNmsItemStack(record.getDroppedItem()));
                            }

                            lastNonNullTicks.put(uuid, record.getRecordTicks().get(0).clone());
                        } else {
                            int entityTickIndex = tickIndex - (record.getStartingTick() + 1);
                            if (entityTickIndex >= record.getRecordTicks().size()) {
                                if (record.getPickedUpBy() != null) {
                                    int collector = getEntityId(record.getPickedUpBy());
                                    if (collector != 0) {
                                        npc.collect(collector, lastNonNullTicks.get(uuid).getItemAmount());
                                    }
                                }
                                replayCache.get(uuid).setPlaying(false);
                                npc.removeNPCPacket();
                            } else {
                                RecordTick tick = record.getRecordTicks().get(entityTickIndex);
                                RecordTick lastNonNullTick = lastNonNullTicks.get(uuid);

                                if (tick.getLocation() != null) {
                                    moveNPC(tick, lastNonNullTick, npc, null, record.getCenter(), shouldPlayThisTick, lowSpeedHelpIndex, playbackControl.getSpeed(), false);
                                }

                                if (!shouldPlayThisTick) continue;

                                Location location = Vector3Utils.toLocation(world,
                                        center.clone().add(Vector3Utils.getTravelDistance(record.getCenter(), lastNonNullTick.getLocation())));

                                if (tick.getVelocity() != null) {
                                    npc.velocity(tick.getVelocity());

                                    if (lastNonNullTick.getLocation() != null) {
                                        if (entityTickIndex != 0) {
                                            if (record.getEntityType().equals(EntityNPCType.ARROW)) {
                                                float soundPitch = (float) (1.1 + (random.nextInt(2) / 10));
                                                for (Player player : npc.getViewers()) {
                                                    player.playSound(location, XSound.ENTITY_ARROW_HIT.parseSound(), playbackControl.getVolume(), soundPitch);
                                                }
                                            }
                                        }
                                    }
                                }
                                if (entityTickIndex == record.getRecordTicks().size() - 1) {
                                    switch(record.getEntityType()) {
                                        case POTION: {
                                            for (Player player : npc.getViewers()) {
                                                player.playSound(location, XSound.ENTITY_SPLASH_POTION_BREAK.parseSound(), playbackControl.getVolume() - 0.3f, 1);
                                                //noinspection deprecation
                                                player.playEffect(location, Effect.POTION_BREAK, NMSProvider.getPotionColor(record.getItem()));
                                            }
                                            break;
                                        }
                                        case SNOWBALL: {
                                            for (Player player : npc.getViewers()) {
                                                player.spawnParticle(Particle.SNOWBALL, location, random.nextInt(7));
                                            }
                                            break;
                                        }
                                    }
                                }
                                if (tick.getItemAmount() != -1) {
                                    ItemStack droppedItem = new ItemStack(record.getDroppedItem().getType());
                                    droppedItem.setItemMeta(record.getDroppedItem().getItemMeta());
                                    droppedItem.setAmount(tick.getItemAmount());
                                    npc.setMetadata(EntityMetadata.getDroppedItemMetadataId(), NMSProvider.getNmsItemStack(droppedItem));
                                    lastNonNullTick.setItemAmount(tick.getItemAmount());
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
                                playerHolograms.get(record.getUuid()).unload();
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
                            Hologram hologram = playerHolograms.get(record.getUuid());
                            ReplayCache cache = replayCache.get(record.getUuid());
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

                                hologram.addViewer(Ruom.getOnlinePlayers().toArray(new Player[0]));
                            } else {
                                lastNonNullTick = lastNonNullTicks.get(record.getUuid());

                                moveNPC(tick, lastNonNullTick, npc, hologram, record.getCenter(), shouldPlayThisTick, lowSpeedHelpIndex, playbackControl.getSpeed(), true);

                                if (tick.getState() != null)
                                    npc.setState(tick.getState());
                                if (tick.getEntityMetadata() != -1)
                                    npc.setMetadata(EntityMetadata.EntityStatus.getMetadataId(), tick.getEntityMetadata());
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

                                if (tick.getHealth() != -1)
                                    lastNonNullTick.setHealth(tick.getHealth());
                                if (tick.getHunger() != -1)
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

                            if (!shouldPlayThisTick) continue;

                            if (tick.getPing() != -1) {
                                hologram.editLine(2, ComponentUtils.parse(HOLOGRAM_LINE_PING.replace(String.valueOf(0), String.valueOf(tick.getPing()))));
                            }
                            //TODO: Hologram CPS

                            Location location = Vector3Utils.toLocation(world,
                                    center.clone().add(Vector3Utils.getTravelDistance(record.getCenter(), lastNonNullTick.getLocation())));

                            if (tick.didSwing())
                                npc.animate(NPCAnimation.SWING_MAIN_ARM);
                            if (tick.tookDamage()) {
                                npc.animate(NPCAnimation.TAKE_DAMAGE);

                                switch (tick.getTakenDamageType()) {
                                    case CRITICAL: {
                                        npc.animate(NPCAnimation.CRITICAL_EFFECT);
                                        for (Player player : npc.getViewers()) {
                                            player.playSound(location, XSound.ENTITY_PLAYER_ATTACK_CRIT.parseSound(), playbackControl.getVolume(), 1);
                                            player.playSound(location, XSound.ENTITY_PLAYER_HURT.parseSound(), playbackControl.getVolume(), 1);
                                        }
                                        break;
                                    }
                                    case SPRINT_ATTACK: {
                                        for (Player player : npc.getViewers()) {
                                            player.playSound(location, XSound.ENTITY_PLAYER_ATTACK_STRONG.parseSound(), playbackControl.getVolume(), 1);
                                            player.playSound(location, XSound.ENTITY_PLAYER_HURT.parseSound(), playbackControl.getVolume(), 1);
                                        }
                                        break;
                                    }
                                    case BURN: {
                                        for (Player player : npc.getViewers()) {
                                            player.playSound(location, XSound.ENTITY_PLAYER_HURT_ON_FIRE.parseSound(), playbackControl.getVolume(), 1);
                                        }
                                        break;
                                    }
                                    case PROJECTILE: {
                                        for (Player player : npc.getViewers()) {
                                            player.playSound(location, XSound.ENTITY_ARROW_SHOOT.parseSound(), playbackControl.getVolume(), 1);
                                            player.playSound(location, XSound.ENTITY_PLAYER_HURT.parseSound(), playbackControl.getVolume(), 1);
                                        }
                                        break;
                                    }
                                    case NORMAL: {
                                        for (Player player : npc.getViewers()) {
                                            player.playSound(location, XSound.ENTITY_PLAYER_HURT.parseSound(), playbackControl.getVolume(), 1);
                                        }
                                        break;
                                    }
                                }
                            }
                            if (tick.threwProjectile()) {
                                for (Player player : npc.getViewers()) {
                                    //Throw sounds are all the same
                                    player.playSound(location, XSound.ENTITY_SPLASH_POTION_THROW.parseSound(), playbackControl.getVolume() - 0.5f, 0.1f);
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
                                        player.playSound(location, XSound.ENTITY_ARROW_SHOOT.parseSound(), playbackControl.getVolume(), soundPitch + (float) (random.nextInt(2) - 1) / 10);
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
                                    player.playSound(location, XSound.ITEM_CROSSBOW_SHOOT.parseSound(), playbackControl.getVolume(), 1f);
                                }
                            }
                            Sound crossbowLoadingSound = null;
                            switch (tick.getCrossbowChargeLevel()) {
                                case 2: {
                                    crossbowLoadingSound = XSound.ITEM_CROSSBOW_LOADING_END.parseSound();
                                    break;
                                }
                                case 1: {
                                    crossbowLoadingSound = XSound.ITEM_CROSSBOW_LOADING_MIDDLE.parseSound();
                                    break;
                                }
                                case 0: {
                                    crossbowLoadingSound = XSound.ITEM_CROSSBOW_LOADING_START.parseSound();
                                    break;
                                }
                            }
                            if (crossbowLoadingSound != null) {
                                for (Player player : npc.getViewers()) {
                                    player.playSound(location, crossbowLoadingSound, playbackControl.getVolume() - 0.3f, 1);
                                }
                            }
                            if (tick.getBodyArrows() >= 0) {
                                npc.setMetadata(EntityMetadata.getBodyArrowsMetadataId(), tick.getBodyArrows());
                            }
                            if (tick.getPotionColor() != -1) {
                                npc.setMetadata(EntityMetadata.getLivingEntityPotionColorMetadataId(), tick.getPotionColor());
                                npc.setMetadata(EntityMetadata.getLivingEntityPotionAmbientMetadataId(), true);
                            }

                            if (tick.getBlockPlaces() != null) {
                                for (Vector3 blockLoc : tick.getBlockPlaces().keySet()) {
                                    Vector3 blockLocFinal = center.clone().add(Vector3Utils.getTravelDistance(record.getCenter(), blockLoc));
                                    Location blockLocation = Vector3Utils.toLocation(world, blockLocFinal);
                                    Material blockMaterial = tick.getBlockPlaces().get(blockLoc);
                                    BlockData blockData = Bukkit.createBlockData(tick.getBlockData().get(blockLoc));
                                    Block block = world.getBlockAt(blockLocation);
                                    if (blockMaterial.toString().contains("_DOOR")) {
                                        placeDoor(blockLocation, blockData);
                                    } else if (blockMaterial.toString().contains("BED")) {
                                        placeBed(blockLocation, blockData);
                                    } else {
                                        block.setType(blockMaterial, true);
                                        block.setBlockData(blockData);
                                    }
                                    for (Player player : npc.getViewers()) {
                                        player.playSound(blockLocation,
                                                BlockSoundUtils.getBlockSound(BlockSoundUtils.SoundType.PLACE, blockMaterial), playbackControl.getVolume(), 0.8f);
                                    }
                                    cache.getBlockDataUsedForPlacing().add(blockLoc);
                                }
                            }

                            if (tick.getBlockBreaks() != null) {
                                for (Vector3 blockLoc : tick.getBlockBreaks().keySet()) {
                                    Vector3 blockLocFinal = center.clone().add(Vector3Utils.getTravelDistance(record.getCenter(), blockLoc));
                                    Location blockLocation = Vector3Utils.toLocation(world, blockLocFinal);
                                    Material blockMaterial = tick.getBlockBreaks().get(blockLoc);
                                    if (blockMaterial.toString().contains("_DOOR")) {
                                        breakDoor(blockLocation);
                                    } else if (blockMaterial.toString().contains("BED")) {
                                        breakBed(blockLocation);
                                    } else {
                                        Block block = world.getBlockAt(blockLocation);
                                        block.setType(Material.AIR, true);
                                    }
                                    for (Player player : npc.getViewers()) {
                                        player.playSound(blockLocation,
                                                BlockSoundUtils.getBlockSound(BlockSoundUtils.SoundType.BREAK, blockMaterial), playbackControl.getVolume(), 0.8f);
                                    }
                                    NMSProvider.sendBlockBreakAnimation(npc.getViewers(), blockLocFinal, -1);
                                    BlockUtils.spawnBlockBreakParticles(blockLocation, tick.getBlockBreaks().get(blockLoc));
                                }
                            }

                            if (tick.getBlockInteractionLocation() != null) {
                                Vector3 blockLocFinal = center.clone().add(Vector3Utils.getTravelDistance(record.getCenter(), tick.getBlockInteractionLocation()));
                                Location blockLocation = Vector3Utils.toLocation(world, blockLocFinal);
                                NMSProvider.sendChestAnimation(npc.getViewers(), blockLocFinal, tick.getBlockInteractionType(), tick.isOpenChestInteraction());

                                float pitch = 0.9f + (random.nextBoolean() ? 0.1f : 0);
                                boolean shouldDelaySound = false;
                                Sound sound;
                                switch (tick.getBlockInteractionType()) {
                                    case TRAPPED_CHEST:
                                    case CHEST: {
                                        sound = tick.isOpenChestInteraction() ? XSound.BLOCK_CHEST_OPEN.parseSound() : XSound.BLOCK_CHEST_CLOSE.parseSound();
                                        if (!tick.isOpenChestInteraction()) shouldDelaySound = true;
                                        break;
                                    }
                                    case ENDER_CHEST: {
                                        sound = tick.isOpenChestInteraction() ? XSound.BLOCK_ENDER_CHEST_OPEN.parseSound() : XSound.BLOCK_ENDER_CHEST_CLOSE.parseSound();
                                        if (!tick.isOpenChestInteraction()) shouldDelaySound = true;
                                        break;
                                    }
                                    default: {
                                        sound = tick.isOpenChestInteraction() ? XSound.BLOCK_SHULKER_BOX_OPEN.parseSound() : XSound.BLOCK_SHULKER_BOX_CLOSE.parseSound();
                                    }
                                }
                                if (shouldDelaySound) {
                                    Ruom.runSync(() -> {
                                        for (Player player : npc.getViewers()) {
                                            player.playSound(blockLocation, sound, playbackControl.getVolume() - 0.3f, pitch);
                                        }
                                    }, 6);
                                } else {
                                    for (Player player : npc.getViewers()) {
                                        player.playSound(blockLocation, sound, playbackControl.getVolume() - 0.3f, pitch);
                                    }
                                }
                            }

                            if (tick.getBlockData() != null) {
                                for (Vector3 blockLoc : tick.getBlockData().keySet()) {
                                    if (cache.getBlockDataUsedForPlacing().contains(blockLoc)) continue;
                                    Vector3 blockLocFinal = center.clone().add(Vector3Utils.getTravelDistance(record.getCenter(), blockLoc));
                                    Location blockLocation = Vector3Utils.toLocation(world, blockLocFinal);
                                    BlockData blockData = Bukkit.createBlockData(tick.getBlockData().get(blockLoc));
                                    world.getBlockAt(blockLocation).setBlockData(blockData);
                                    getBlockDataSound(blockData).ifPresent(soundContainer -> {
                                        soundContainer.withVolume(playbackControl.getVolume() - 0.5f).play(blockLocation, npc.getViewers());
                                    });
                                    if (blockData.getMaterial().toString().contains("BUTTON")) {
                                        scheduleButtonAutoPowerOff(blockLocation.getBlock());
                                    }
                                }
                                cache.getBlockDataUsedForPlacing().clear();
                            }

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
                                        player.playSound(location, XSound.ENTITY_GENERIC_EAT.parseSound(), playbackControl.getVolume(), 1f);
                                    }
                                }
                                cache.getPendingFoodEatSkippedTicks().put(record.getUuid(),
                                        cache.getPendingFoodEatSkippedTicks().get(record.getUuid()) + 1);
                            } else {
                                replayCache.get(record.getUuid()).getPendingFoodEatSkippedTicks().remove(record.getUuid());
                            }
                        }
                    }

                    if (shouldPlayThisTick)
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
                    for (Hologram hologram : playerHolograms.values()) {
                        hologram.unload();
                    }
                    playerHolograms.clear();
                    cancel();
                }
            }
        }.runTaskTimer(Ruom.getPlugin(), 0, 1);

        return playbackControl;
    }

    public void suspend() {
        replayRunnable.cancel();
        for (PlayerRecord record : playerRecords.keySet()) {
            PlayerNPC npc = playerRecords.get(record);
            npc.removeNPCPacket();
        }
    }

    private void moveNPC(RecordTick tick, RecordTick lastNonNullTick, NPC npc, @Nullable Hologram hologram, Vector3 recordCenter, boolean shouldPlayThisTick, int lowSpeedHelpIndex, PlayBackControl.Speed speed, boolean onGround) {
        Vector3 travelDistance = null;
        if (tick.getLocation() != null) {
            Vector3 centerOffSet = Vector3Utils.getTravelDistance(recordCenter, tick.getLocation());
            Vector3 lastPoint = lastNonNullTick.getLocation().clone().add(centerOffSet);
            Vector3 newPoint = tick.getLocation().clone().add(centerOffSet);

            if (!shouldPlayThisTick) {
                travelDistance = getCalculatedLowSpeedTravelDistance(lastNonNullTick, lastPoint, newPoint, centerOffSet, speed, lowSpeedHelpIndex);
            } else {
                travelDistance = Vector3Utils.getTravelDistance(lastPoint, newPoint);
                lastNonNullTick.setLocation(tick.getLocation());
            }
        }

        if (lastNonNullTick.getYaw() == -1 && tick.getYaw() != -1)
            lastNonNullTick.setYaw(tick.getYaw());
        if (lastNonNullTick.getPitch() == -1 && tick.getPitch() != -1)
            lastNonNullTick.setPitch(tick.getPitch());

        float newYaw = tick.getYaw();
        float newPitch = tick.getPitch();
        float lastYaw = lastNonNullTick.getYaw();
        float lastPitch = lastNonNullTick.getPitch();
        float yaw = getCalculatedLookAngle(lastYaw, newYaw, speed, lowSpeedHelpIndex);
        float pitch = getCalculatedLookAngle(lastPitch, newPitch, speed, lowSpeedHelpIndex);
        if (!shouldPlayThisTick) {
            if (speed.equals(PlayBackControl.Speed.x025) && lowSpeedHelpIndex == 3) {
                if (yaw != -1) {
                    lastNonNullTick.setYaw(yaw);
                }
                if (pitch != -1) {
                    lastNonNullTick.setPitch(pitch);
                }
            }
        } else {
            if (yaw != -1)
                lastNonNullTick.setYaw(yaw);
            if (pitch != -1)
                lastNonNullTick.setPitch(pitch);
        }
        if (yaw == -1) {
            yaw = lastYaw;
        }
        if (pitch == -1) {
            pitch = lastPitch;
        }
        //(String.format("yaw: '%s' pitch: '%s' newYaw: '%s' newPich '%s' lastYaw: '%s' lastPitch: '%s'", yaw, pitch, newYaw, newPitch, lastYaw, lastPitch));

        //TravelDistance will be null if player don't move
        if (travelDistance != null && !travelDistance.equals(Vector3.at(0, 0, 0))) {
            //Returns false if distance was more than 8 blocks, Move packet does not support more than 8 blocks.
            if (!npc.moveAndLook(travelDistance.getX(), travelDistance.getY(), travelDistance.getZ(), yaw, pitch, onGround)) {
                Location location = npc.getLocation().add(travelDistance.getX(), travelDistance.getY(), travelDistance.getZ());
                npc.teleport(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch(), true);
            }
            if (hologram != null) {
                hologram.move(travelDistance);
            }
        } else {
            if (!(yaw == lastYaw && pitch == lastPitch))
                npc.look(yaw, pitch);
        }
    }

    private Vector3 getCalculatedLowSpeedTravelDistance(RecordTick lastNonNullTick, Vector3 from, Vector3 to, Vector3 centerOffSet, PlayBackControl.Speed speed, int lowSpeedHelpIndex) {
        Vector3 travelDistance = null;
        Vector3 centerPoint = Vector3Utils.getCenter(from, to);
        if (speed.equals(PlayBackControl.Speed.x050)) {
            travelDistance = Vector3Utils.getTravelDistance(from, centerPoint);
            lastNonNullTick.setLocation(centerPoint.clone().add(-centerOffSet.getX(), -centerOffSet.getY(), -centerOffSet.getZ()));
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
        float angle = -1;

        float centerAngle = (360 + newAngle + (((oldAngle - newAngle + 180 + 360) % 360) - 180) / 2) % 360;
        //float centerAngle = (oldAngle + newAngle) / 2;

        if (speed.equals(PlayBackControl.Speed.x1)) {
            angle = newAngle;
        } else if (speed.equals(PlayBackControl.Speed.x050)) {
            if (newAngle != -1) {
                angle = centerAngle;
            }
        } else {
            switch (lowSpeedHelpIndex) {
                case 1: {
                    if (newAngle != -1)
                        angle = (oldAngle + centerAngle) / 2;
                    break;
                }
                case 2: {
                    if (newAngle != -1)
                        angle = centerAngle;
                    break;
                }
                case 3: {
                    if (newAngle != -1) {
                        angle = (centerAngle + newAngle) / 2;
                    }
                    break;
                }
            }
        }
        return angle;
    }

    private void placeDoor(Location location, BlockData blockData) {
        Block bottomBlock = location.getBlock();
        Block upperBlock = bottomBlock.getRelative(BlockFace.UP);
        BlockData upperBlockData = Bukkit.createBlockData(blockData.getAsString().replace("half=lower", "half=upper"));
        bottomBlock.setType(blockData.getMaterial(), false);
        upperBlock.setType(blockData.getMaterial(), false);
        upperBlock.setBlockData(upperBlockData);
    }

    private void breakDoor(Location location) {
        Block block = location.getBlock();
        BlockData blockData = block.getBlockData().clone();
        block.setType(Material.AIR, false);
        if (blockData.getAsString().contains("half=lower")) {
            block.getRelative(0, 1, 0).setType(Material.AIR, false);
        } else if (blockData.getAsString().contains("half=upper")) {
            block.getRelative(0, -1, 0).setType(Material.AIR, false);
        }
    }

    private void placeBed(Location location, BlockData blockData) {
        Block block = location.getBlock();
        String blockDataString = blockData.getAsString();
        block.setType(blockData.getMaterial(), false);
        block.setBlockData(Bukkit.createBlockData(blockDataString));

        Block otherPartBlock = getOtherBedPart(location, blockData).getBlock();
        otherPartBlock.setType(blockData.getMaterial(), false);
        otherPartBlock.setBlockData(Bukkit.createBlockData(blockDataString.replace("part=foot", "part=head")));
    }

    private Location getOtherBedPart(Location location, BlockData blockData) {
        String blockDataString = blockData.getAsString();
        if (!blockDataString.contains("part=")) {
            throw new IllegalStateException("Given BlockData is not a bed's BlockData.");
        }
        boolean isFootPart = blockDataString.contains("part=foot");

        if (blockDataString.contains("facing=north")) {
            return location.clone().add(0, 0, isFootPart ? -1 : 1);
        } else if (blockDataString.contains("facing=east")) {
            return location.clone().add(isFootPart ? 1 : -1, 0, 0);
        } else if (blockDataString.contains("facing=south")) {
            return location.clone().add(0, 0, isFootPart ? 1 : -1);
        } else if (blockDataString.contains("facing=west")) {
            return location.clone().add(isFootPart ? -1 : 1, 0, 0);
        }

        throw new IllegalStateException("Given BlockData is not a bed's BlockData.");
    }

    private void breakBed(Location location) {
        Block block = location.getBlock();
        BlockData blockData = block.getBlockData();

        block.setType(Material.AIR, false);
        getOtherBedPart(location, blockData).getBlock().setType(Material.AIR, false);
    }

    private Optional<SoundContainer> getBlockDataSound(BlockData blockData) {
        String blockDataString = blockData.getAsString();
        if (blockData.getMaterial().toString().contains("_DOOR")) {
            boolean isIronDoor = blockData.getMaterial().equals(XMaterial.IRON_DOOR.parseMaterial());
            return Optional.of(SoundContainer.soundContainer(blockDataString.contains("open=false") ? isIronDoor ?
                    XSound.BLOCK_IRON_DOOR_CLOSE : XSound.BLOCK_WOODEN_DOOR_CLOSE : isIronDoor ?
                    XSound.BLOCK_IRON_DOOR_OPEN : XSound.BLOCK_WOODEN_DOOR_OPEN));
        } else if (blockData.getMaterial().toString().contains("TRAPDOOR")) {
            boolean isIronTrapDoor = blockData.getMaterial().equals(XMaterial.IRON_TRAPDOOR.parseMaterial());
            return Optional.of(SoundContainer.soundContainer(blockDataString.contains("open=false") ? isIronTrapDoor ?
                    XSound.BLOCK_IRON_TRAPDOOR_CLOSE : XSound.BLOCK_WOODEN_TRAPDOOR_CLOSE : isIronTrapDoor ?
                    XSound.BLOCK_IRON_TRAPDOOR_OPEN : XSound.BLOCK_WOODEN_TRAPDOOR_OPEN));
        } else if (blockData.getMaterial().equals(XMaterial.LEVER.parseMaterial())) {
            return Optional.of(blockDataString.contains("powered=false") ?
                    SoundContainer.soundContainer(XSound.BLOCK_LEVER_CLICK, 1f, 0.4f) :
                    SoundContainer.soundContainer(XSound.BLOCK_LEVER_CLICK, 1f, 0.6f));
        } else if (blockData.getMaterial().toString().contains("BUTTON")) {
            boolean isStoneButton = blockData.getMaterial().toString().contains("STONE");
            return Optional.of(blockDataString.contains("powered=false") ? isStoneButton ?
                    SoundContainer.soundContainer(XSound.BLOCK_STONE_BUTTON_CLICK_OFF, 1f, 0.4f) :
                    SoundContainer.soundContainer(XSound.BLOCK_WOODEN_BUTTON_CLICK_OFF, 1f, 0.4f) : isStoneButton ?
                    SoundContainer.soundContainer(XSound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 0.6f) :
                    SoundContainer.soundContainer(XSound.BLOCK_WOODEN_BUTTON_CLICK_ON, 1f, 0.6f));
        }

        return Optional.empty();
    }

    private void scheduleButtonAutoPowerOff(Block button) {
        Ruom.runSync(() -> {
            if (button.getType().toString().contains("BUTTON")) {
                button.setBlockData(Bukkit.createBlockData(button.getBlockData().getAsString().replace("powered=true", "powered=false")));
                getBlockDataSound(button.getBlockData()).ifPresent(soundContainer -> soundContainer.withVolume(0.5f).play(button.getLocation()));
            }
        }, 25);
    }

    private int getEntityId(UUID uuid) {
        for (PlayerRecord record : playerRecords.keySet()) {
            if (record.getUuid().equals(uuid)) {
                return playerRecords.get(record).getId();
            }
        }
        for (EntityRecord record : entityRecords.keySet()) {
            if (modifiedUuids.get(record.getUuid()).equals(uuid)) {
                return entityRecords.get(record).getId();
            }
        }
        return 0;
    }

}
