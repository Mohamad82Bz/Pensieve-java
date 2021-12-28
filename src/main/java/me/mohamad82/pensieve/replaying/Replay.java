package me.mohamad82.pensieve.replaying;

import me.mohamad82.pensieve.recording.record.Record;
import me.mohamad82.pensieve.recording.record.*;
import me.mohamad82.ruom.Ruom;
import me.mohamad82.ruom.hologram.Hologram;
import me.mohamad82.ruom.npc.EntityNPC;
import me.mohamad82.ruom.npc.LivingEntityNPC;
import me.mohamad82.ruom.npc.NPC;
import me.mohamad82.ruom.npc.PlayerNPC;
import me.mohamad82.ruom.npc.entity.AreaEffectCloudNPC;
import me.mohamad82.ruom.npc.entity.ArrowNPC;
import me.mohamad82.ruom.npc.entity.ItemNPC;
import me.mohamad82.ruom.npc.entity.ThrowableProjectileNPC;
import me.mohamad82.ruom.utils.*;
import me.mohamad82.ruom.vector.Vector3;
import me.mohamad82.ruom.vector.Vector3Utils;
import me.mohamad82.ruom.xseries.XMaterial;
import me.mohamad82.ruom.xseries.XSound;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Replay {

    private final Map<PlayerRecord, PlayerNPC> playerRecords = new HashMap<>();
    private final Map<EntityRecord, EntityNPC> entityRecords = new HashMap<>();
    private final Map<UUID, List<RecordTick>> preparedLastNonNullTicks = new HashMap<>();
    private final Map<UUID, ReplayCache> replayCache = new HashMap<>();
    private final Map<UUID, Hologram> playerHolograms = new HashMap<>();
    private final Map<UUID, UUID> modifiedUuids = new HashMap<>();

    private final World world;
    private final Vector3 center;

    private BukkitTask replayRunnable;
    private final Random random = new Random();

    private void prepareLastNonNullTicks() {
        Set<Record> records = new HashSet<>();
        records.addAll(playerRecords.keySet());
        records.addAll(entityRecords.keySet());

        for (Record record : records) {
            List<RecordTick> lastNonNullTicks = new ArrayList<>();
            RecordTick lastNonNullTick = null;
            boolean firstLoop = true;
            for (RecordTick tick : record.getRecordTicks()) {
                if (firstLoop) {
                    lastNonNullTick = tick.copy();
                    firstLoop = false;
                } else {
                    if (tick.getLocation() != null)
                        lastNonNullTick.setLocation(tick.getLocation());
                    if (tick.getYaw() != -1)
                        lastNonNullTick.setYaw(tick.getYaw());
                    if (tick.getPitch() != -1)
                        lastNonNullTick.setPitch(tick.getPitch());
                    if (tick.getVelocity() != null)
                        lastNonNullTick.setVelocity(tick.getVelocity());
                    if (tick.getEffectColor() != -1)
                        lastNonNullTick.setEffectColor(tick.getEffectColor());

                    if (tick instanceof PlayerRecordTick) {
                        PlayerRecordTick playerRecordTick = (PlayerRecordTick) tick;
                        PlayerRecordTick lastNonNullPlayerRecordTick = (PlayerRecordTick) lastNonNullTick;
                        if (playerRecordTick.getPing() != -1)
                            lastNonNullPlayerRecordTick.setPing(playerRecordTick.getPing());
                        if (playerRecordTick.getHealth() != -1)
                            lastNonNullPlayerRecordTick.setHealth(playerRecordTick.getHealth());
                        if (playerRecordTick.getHunger() != -1)
                            lastNonNullPlayerRecordTick.setHunger(playerRecordTick.getHunger());
                        if (playerRecordTick.getPose() != null)
                            lastNonNullPlayerRecordTick.setPose(playerRecordTick.getPose());
                        if (playerRecordTick.getHand() != null)
                            lastNonNullPlayerRecordTick.setHand(playerRecordTick.getHand());
                        if (playerRecordTick.getOffHand() != null)
                            lastNonNullPlayerRecordTick.setOffHand(playerRecordTick.getOffHand());
                        if (playerRecordTick.getHelmet() != null)
                            lastNonNullPlayerRecordTick.setHelmet(playerRecordTick.getHelmet());
                        if (playerRecordTick.getChestplate() != null)
                            lastNonNullPlayerRecordTick.setChestplate(playerRecordTick.getChestplate());
                        if (playerRecordTick.getLeggings() != null)
                            lastNonNullPlayerRecordTick.setLeggings(playerRecordTick.getLeggings());
                        if (playerRecordTick.getBoots() != null)
                            lastNonNullPlayerRecordTick.setBoots(playerRecordTick.getBoots());
                    } else if (tick instanceof DroppedItemRecordTick) {
                        DroppedItemRecordTick droppedItemRecordTick = (DroppedItemRecordTick) tick;
                        DroppedItemRecordTick lastNonNullDroppedItemRecordTick = (DroppedItemRecordTick) lastNonNullTick;
                        if (droppedItemRecordTick.getItemAmount() != -1)
                            lastNonNullDroppedItemRecordTick.setItemAmount(droppedItemRecordTick.getItemAmount());
                    }
                }

                lastNonNullTicks.add(lastNonNullTick.copy());
            }

            preparedLastNonNullTicks.put(record.getUuid(), lastNonNullTicks);
        }
    }

    private void setSpeed(PlayBackControl.Speed speed) {
        for (EntityRecord record : entityRecords.keySet()) {
            if (replayCache.containsKey(record.getUuid()) && replayCache.get(record.getUuid()).isPlaying()) {
                boolean noGravity = speed.equals(PlayBackControl.Speed.x050) || speed.equals(PlayBackControl.Speed.x025);
                entityRecords.get(record).setNoGravity(noGravity);
            }
        }
    }

    public Replay(Set<PlayerRecord> playerRecords, Set<EntityRecord> entityRecords, World world, Vector3 center) {
        this.world = world;
        this.center = Vector3Utils.simplifyToCenter(center);

        for (PlayerRecord record : playerRecords) {
            Vector3 centerOffSetDistance = Vector3Utils.getTravelDistance(record.getCenter(), record.getStartLocation());
            Vector3 centerOffSet = center.clone().add(centerOffSetDistance).add(0.5, 0, 0.5);
            this.playerRecords.put(record, PlayerNPC.playerNPC(
                    record.getName(),
                    Vector3Utils.toLocation(world, centerOffSet),
                    record.getSkin()
            ));
            ReplayCache cache = new ReplayCache();
            replayCache.put(record.getUuid(), cache);
            cache.setCentersDistance(Vector3Utils.getTravelDistance(record.getCenter(), center));

            //TODO: Hologram rework
            playerHolograms.put(record.getUuid(), Hologram.hologram(Collections.emptyList(), Vector3Utils.toLocation(world, Vector3.at(0, 0, 0))));
        }

        for (EntityRecord record : entityRecords) {
            UUID uuid = UUID.randomUUID();
            modifiedUuids.put(record.getUuid(), uuid);
            Location startLocation = Vector3Utils.toLocation(world, center.clone().add(Vector3Utils.getTravelDistance(record.getCenter(), record.getStartLocation())).add(0.5, 0, 0.5));
            EntityNPC entityNPC;

            switch (record.getEntityType()) {
                case ITEM: {
                    entityNPC = ItemNPC.itemNPC(startLocation, ((DroppedItemRecord) record).getItem());
                    break;
                }
                case AREA_EFFECT_CLOUD: {
                    entityNPC = AreaEffectCloudNPC.areaEffectCloudNPC(startLocation);
                    break;
                }
                case POTION: {
                    entityNPC = ThrowableProjectileNPC.throwableProjectileNPC(startLocation, ((ProjectileRecord) record).getProjectileItem());
                    break;
                }
                case ARROW: {
                    entityNPC = ArrowNPC.arrowNPC(startLocation);
                    break;
                }
                default: {
                    entityNPC = null;
                    Ruom.warn("Unsupported entity type was added to a replay: " + record.getEntityType().toString().toLowerCase());
                }
            }
            if (entityNPC != null)
                this.entityRecords.put(record, entityNPC);
        }

        prepareLastNonNullTicks();
    }

    public Replay(Set<PlayerRecord> playerRecords, Set<EntityRecord> entityRecords, World world) {
        this(playerRecords, entityRecords, world, Vector3.at(0.5, 100, 0.5));
    }

    public PlayBackControl start() {
        PlayBackControl playbackControl = new PlayBackControl();
        playbackControl.setSpeed(PlayBackControl.Speed.x1);

        int maxTicks = 0;

        for (PlayerRecord record : playerRecords.keySet()) {
            replayCache.get(record.getUuid()).setPlaying(true);
            PlayerNPC npc = playerRecords.get(record);
            npc.setTabList(null);
            npc.setCustomNameVisible(false);
            npc.addViewers(Ruom.getOnlinePlayers());

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
                            } else {
                                shouldPlayThisTick = false;
                            }
                            break;
                        }
                    }

                    for (EntityRecord record : entityRecords.keySet()) {
                        UUID uuid = modifiedUuids.get(record.getUuid());
                        if (replayCache.containsKey(uuid) && !replayCache.get(uuid).isPlaying()) continue;
                        if (tickIndex < record.getStartingTick()) continue;
                        EntityNPC npc = entityRecords.get(record);
                        if (!replayCache.containsKey(uuid)) {
                            if (shouldPlayThisTick) {
                                ReplayCache cache = new ReplayCache();
                                cache.setPlaying(true);
                                replayCache.put(uuid, cache);

                                if (playbackControl.getSpeed().equals(PlayBackControl.Speed.x050) || playbackControl.getSpeed().equals(PlayBackControl.Speed.x025))
                                    npc.setNoGravity(true);

                                if (record instanceof DroppedItemRecord) {
                                    ((ItemNPC) npc).setItem(((DroppedItemRecord) record).getItem());
                                } else if (record instanceof AreaEffectCloudRecord) {
                                    ((AreaEffectCloudNPC) npc).setColor(((AreaEffectCloudRecord) record).getColor());
                                } else if (record instanceof ProjectileRecord) {
                                    ((ThrowableProjectileNPC) npc).setItem(((ProjectileRecord) record).getProjectileItem());
                                } else if (record instanceof ArrowRecord) {
                                    ((ArrowNPC) npc).setColor(((ArrowRecord) record).getColor());
                                }

                                npc.addViewers(Ruom.getOnlinePlayers());
                                if (!(record instanceof AreaEffectCloudRecord))
                                    npc.setVelocity(preparedLastNonNullTicks.get(record.getUuid()).get(tickIndex - record.getStartingTick()).getVelocity());
                            }
                        } else {
                            int entityTickIndex = tickIndex - (record.getStartingTick());
                            if (entityTickIndex >= record.getRecordTicks().size()) {
                                if (record instanceof DroppedItemRecord) {
                                    DroppedItemRecord droppedItemRecord = (DroppedItemRecord) record;
                                    if (droppedItemRecord.getPickedBy() != null) {
                                        int collector = getEntityId(droppedItemRecord.getPickedBy());
                                        if (collector != 0) {
                                            ((ItemNPC) npc).collect(collector);
                                        }
                                    }
                                }
                                replayCache.get(uuid).setPlaying(false);
                                npc.removeViewers(Ruom.getOnlinePlayers());
                            } else {
                                RecordTick tick = record.getRecordTicks().get(entityTickIndex);
                                RecordTick lastNonNullTick = preparedLastNonNullTicks.get(record.getUuid()).get(entityTickIndex - 1);

                                if (tick instanceof AreaEffectCloudTick) {
                                    ((AreaEffectCloudNPC) npc).setRadius(((AreaEffectCloudTick) tick).getRadius());
                                    continue;
                                }

                                if (tick.getLocation() != null) {
                                    moveNPC(tick, lastNonNullTick, npc, null, record.getCenter(), shouldPlayThisTick, lowSpeedHelpIndex, playbackControl.getSpeed(), false);
                                }

                                if (!shouldPlayThisTick) continue;

                                Location location = Vector3Utils.toLocation(world,
                                        center.clone().add(Vector3Utils.getTravelDistance(record.getCenter(), lastNonNullTick.getLocation())));

                                if (tick.getVelocity() != null) {
                                    npc.setVelocity(tick.getVelocity());

                                    if (lastNonNullTick.getLocation() != null) {
                                        if (entityTickIndex != 0) {
                                            if (record instanceof ArrowRecord) {
                                                float soundPitch = (float) (1.1 + (random.nextInt(2) / 10));
                                                for (Player player : npc.getViewers()) {
                                                    player.playSound(location, XSound.ENTITY_ARROW_HIT.parseSound(), playbackControl.getVolume(), soundPitch);
                                                }
                                            }
                                        }
                                    }
                                }
                                if (entityTickIndex == record.getRecordTicks().size() - 1) {
                                    if (record instanceof ProjectileRecord) {
                                        ProjectileRecord projectileRecord = (ProjectileRecord) record;

                                        if (ListUtils.toList(XMaterial.SPLASH_POTION.parseMaterial(), XMaterial.LINGERING_POTION.parseMaterial()).contains(projectileRecord.getProjectileItem().getType())) {
                                            for (Player player : npc.getViewers()) {
                                                player.playSound(location, XSound.ENTITY_SPLASH_POTION_BREAK.parseSound(), playbackControl.getVolume() - 0.3f, 1);
                                                //noinspection deprecation
                                                player.playEffect(location, Effect.POTION_BREAK, NMSUtils.getPotionColor(projectileRecord.getProjectileItem()));
                                            }
                                        } else if (projectileRecord.getProjectileItem().getType().equals(XMaterial.SNOWBALL.parseMaterial())) {
                                            for (Player player : npc.getViewers()) {
                                                player.spawnParticle(Particle.SNOWBALL, location, random.nextInt(7));
                                            }
                                        }
                                    }
                                }

                                if (tick instanceof DroppedItemRecordTick) {
                                    int itemAmount = ((DroppedItemRecordTick) tick).getItemAmount();
                                    if (itemAmount > 0) {
                                        ItemStack originalDroppedItem = ((DroppedItemRecord) record).getItem();
                                        ItemStack droppedItem = new ItemStack(originalDroppedItem.getType());
                                        droppedItem.setItemMeta(originalDroppedItem.getItemMeta());
                                        droppedItem.setAmount(itemAmount);
                                        ((ItemNPC) npc).setItem(droppedItem);
                                        ((DroppedItemRecordTick) lastNonNullTick).setItemAmount(itemAmount);
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
                                npc.removeViewers(Ruom.getOnlinePlayers());
                                playerHolograms.get(record.getUuid()).unload();
                                replayCache.get(record.getUuid()).setPlaying(false);
                                finishedRecords++;
                                if (finishedRecords == totalRecords) {
                                    entityRecords.forEach((entityRecord, entityNPC) -> {
                                        entityNPC.removeViewers(Ruom.getOnlinePlayers());
                                    });
                                    cancel();
                                    return;
                                }
                                continue;
                            }
                            Hologram hologram = playerHolograms.get(record.getUuid());
                            ReplayCache cache = replayCache.get(record.getUuid());
                            PlayerRecordTick tick = (PlayerRecordTick) record.getRecordTicks().get(tickIndex);
                            PlayerRecordTick lastNonNullTick;
                            PlayerRecordTick nextTick;
                            try {
                                nextTick = (PlayerRecordTick) record.getRecordTicks().get(tickIndex + 1);
                            } catch (IndexOutOfBoundsException e) {
                                //It's the last tickIndex
                                nextTick = null;
                            }

                            if (tickIndex == 0) {
                                npc.setPose(tick.getPose());
                                npc.setSprinting(tick.wasSprinting());
                                npc.setGlowing(tick.wasGlowing());
                                npc.setInvisible(tick.wasInvisible());
                                npc.setOnFire(tick.wasBurning());

                                npc.setEquipment(NPC.EquipmentSlot.MAINHAND, tick.getHand());
                                npc.setEquipment(NPC.EquipmentSlot.OFFHAND, tick.getOffHand());
                                npc.setEquipment(NPC.EquipmentSlot.HEAD, tick.getHelmet());
                                npc.setEquipment(NPC.EquipmentSlot.CHEST, tick.getChestplate());
                                npc.setEquipment(NPC.EquipmentSlot.LEGS, tick.getLeggings());
                                npc.setEquipment(NPC.EquipmentSlot.FEET, tick.getBoots());

                                lastNonNullTick = (PlayerRecordTick) preparedLastNonNullTicks.get(record.getUuid()).get(tickIndex);

                                hologram.addViewers(Ruom.getOnlinePlayers());
                            } else {
                                lastNonNullTick = (PlayerRecordTick) preparedLastNonNullTicks.get(record.getUuid()).get(tickIndex - 1);

                                moveNPC(tick, lastNonNullTick, npc, hologram, record.getCenter(), shouldPlayThisTick, lowSpeedHelpIndex, playbackControl.getSpeed(), true);

                                if (tick.wasCrouching())
                                    npc.setPose(NPC.Pose.CROUCHING);
                                else if (tick.wasGliding())
                                    npc.setPose(NPC.Pose.SWIMMING);
                                else if (tick.wasSwimming())
                                    npc.setPose(NPC.Pose.SWIMMING);
                                else
                                    npc.setPose(NPC.Pose.STANDING);

                                npc.setSprinting(tick.wasSprinting());
                                npc.setInvisible(tick.wasInvisible());
                                npc.setOnFire(tick.wasBurning());
                                npc.setGlowing(tick.wasGlowing());

                                if (tick.getHand() != null)
                                    npc.setEquipment(NPC.EquipmentSlot.MAINHAND, tick.getHand());
                                if (tick.getOffHand() != null)
                                    npc.setEquipment(NPC.EquipmentSlot.OFFHAND, tick.getOffHand());
                                if (tick.getHelmet() != null)
                                    npc.setEquipment(NPC.EquipmentSlot.HEAD, tick.getHelmet());
                                if (tick.getChestplate() != null)
                                    npc.setEquipment(NPC.EquipmentSlot.CHEST, tick.getChestplate());
                                if (tick.getLeggings() != null)
                                    npc.setEquipment(NPC.EquipmentSlot.LEGS, tick.getLeggings());
                                if (tick.getBoots() != null)
                                    npc.setEquipment(NPC.EquipmentSlot.FEET, tick.getBoots());
                            }

                            if (!shouldPlayThisTick) continue;

                            if (tick.getPing() != -1) {
                                //TODO Holograms re-impl
                                //hologram.editLine(2, ComponentUtils.parse(HOLOGRAM_LINE_PING.replace(String.valueOf(0), String.valueOf(tick.getPing()))));
                            }
                            //TODO: Hologram CPS

                            Location location = Vector3Utils.toLocation(world,
                                    center.clone().add(Vector3Utils.getTravelDistance(record.getCenter(), lastNonNullTick.getLocation())));

                            if (tick.didSwing())
                                npc.animate(NPC.Animation.SWING_MAIN_ARM);
                            if (tick.getTakenDamageType() != null) {
                                npc.animate(NPC.Animation.TAKE_DAMAGE);

                                switch (tick.getTakenDamageType()) {
                                    case CRITICAL: {
                                        npc.animate(NPC.Animation.CRITICAL_EFFECT);
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
                            if (tick.thrownProjectile()) {
                                for (Player player : npc.getViewers()) {
                                    //Throw sounds are all the same
                                    player.playSound(location, XSound.ENTITY_SPLASH_POTION_THROW.parseSound(), playbackControl.getVolume() - 0.5f, 0.1f);
                                }
                            }
                            if (tick.getUseItemInteractionHand() > 0) {
                                switch (tick.getUseItemInteractionHand()) {
                                    case 1: {
                                        npc.startUsingItem(LivingEntityNPC.InteractionHand.MAIN_HAND);
                                        break;
                                    }
                                    case 2: {
                                        npc.startUsingItem(LivingEntityNPC.InteractionHand.OFF_HAND);
                                        break;
                                    }
                                    case 3: {
                                        npc.stopUsingItem();
                                        if (!tick.drawnCrossbow()) {
                                            float soundPitch;
                                            if (tick.getUsedItemTime() < 300) {
                                                soundPitch = 0.8f;
                                            } else if (tick.getUsedItemTime() < 500) {
                                                soundPitch = 0.9f;
                                            } else if (tick.getUsedItemTime() < 750) {
                                                soundPitch = 1.0f;
                                            } else if (tick.getUsedItemTime() < 1000) {
                                                soundPitch = 1.1f;
                                            } else {
                                                soundPitch = 1.2f;
                                            }
                                            for (Player player : npc.getViewers()) {
                                                player.playSound(location, XSound.ENTITY_ARROW_SHOOT.parseSound(), playbackControl.getVolume(), soundPitch + (float) (random.nextInt(2) - 1) / 10);
                                            }
                                        }
                                        break;
                                    }
                                }
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
                                npc.setArrowsOnBody(tick.getBodyArrows());
                            }
                            if (tick.getEffectColor() != -1) {
                                npc.setEffectColor(tick.getEffectColor());
                                npc.setEffectsAsAmbients(false);
                            }

                            if (tick.getBlockPlaces() != null) {
                                for (Vector3 blockLoc : tick.getBlockPlaces().keySet()) {
                                    Vector3 blockLocFinal = center.clone().add(Vector3Utils.getTravelDistance(record.getCenter(), blockLoc));
                                    Location blockLocation = Vector3Utils.toLocation(world, blockLocFinal);
                                    Material blockMaterial = tick.getBlockPlaces().get(blockLoc);
                                    BlockData blockData = Bukkit.createBlockData(tick.getBlockData().get(blockLoc));
                                    Block block = world.getBlockAt(blockLocation);
                                    SoundContainer soundContainer = SoundContainer.soundContainer(blockMaterial.createBlockData().getSoundGroup().getPlaceSound()).withVolume(playbackControl.getVolume()).withPitch(0.8f);
                                    if (blockMaterial.toString().contains("_DOOR")) {
                                        BlockUtils.placeDoor(blockLocation, blockData);
                                    } else if (blockMaterial.toString().contains("BED")) {
                                        BlockUtils.placeBed(blockLocation, blockData);
                                    } else {
                                        block.setType(blockMaterial, true);
                                        block.setBlockData(blockData);
                                    }
                                    soundContainer.play(npc.getViewers());
                                    cache.getBlockDataUsedForPlacing().add(blockLoc);
                                }
                            }

                            if (tick.getBlockBreaks() != null) {
                                for (Vector3 blockLoc : tick.getBlockBreaks().keySet()) {
                                    Vector3 blockLocFinal = center.clone().add(Vector3Utils.getTravelDistance(record.getCenter(), blockLoc));
                                    Location blockLocation = Vector3Utils.toLocation(world, blockLocFinal);
                                    Material blockMaterial = tick.getBlockBreaks().get(blockLoc);
                                    SoundContainer soundContainer = SoundContainer.soundContainer(blockMaterial.createBlockData().getSoundGroup().getBreakSound()).withVolume(playbackControl.getVolume()).withPitch(0.8f);
                                    if (blockMaterial.toString().contains("_DOOR")) {
                                        BlockUtils.breakDoor(blockLocation);
                                    } else if (blockMaterial.toString().contains("BED")) {
                                        BlockUtils.breakBed(blockLocation);
                                    } else {
                                        Block block = world.getBlockAt(blockLocation);
                                        block.setType(Material.AIR, true);
                                    }
                                    soundContainer.play(npc.getViewers());
                                    NMSUtils.sendBlockDestruction(npc.getViewers(), blockLocFinal, -1);
                                    BlockUtils.spawnBlockBreakParticles(blockLocation, tick.getBlockBreaks().get(blockLoc));
                                }
                            }

                            if (tick.getBlockInteractionLocation() != null) {
                                Vector3 blockLocFinal = center.clone().add(Vector3Utils.getTravelDistance(record.getCenter(), tick.getBlockInteractionLocation()));
                                Location blockLocation = Vector3Utils.toLocation(world, blockLocFinal);
                                NMSUtils.sendChestAnimation(npc.getViewers(), blockLocFinal, tick.getBlockInteractionType(), tick.didOpenChestInteraction());

                                float pitch = 0.9f + (random.nextBoolean() ? 0.1f : 0);
                                boolean shouldDelaySound = false;
                                Sound sound;
                                switch (tick.getBlockInteractionType()) {
                                    case TRAPPED_CHEST:
                                    case CHEST: {
                                        sound = tick.didOpenChestInteraction() ? XSound.BLOCK_CHEST_OPEN.parseSound() : XSound.BLOCK_CHEST_CLOSE.parseSound();
                                        if (!tick.didOpenChestInteraction()) shouldDelaySound = true;
                                        break;
                                    }
                                    case ENDER_CHEST: {
                                        sound = tick.didOpenChestInteraction() ? XSound.BLOCK_ENDER_CHEST_OPEN.parseSound() : XSound.BLOCK_ENDER_CHEST_CLOSE.parseSound();
                                        if (!tick.didOpenChestInteraction()) shouldDelaySound = true;
                                        break;
                                    }
                                    default: {
                                        sound = tick.didOpenChestInteraction() ? XSound.BLOCK_SHULKER_BOX_OPEN.parseSound() : XSound.BLOCK_SHULKER_BOX_CLOSE.parseSound();
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
                                    BlockUtils.getBlockDataSound(blockData).ifPresent(soundContainer -> {
                                        soundContainer.withVolume(playbackControl.getVolume() - 0.5f).play(blockLocation, npc.getViewers());
                                    });
                                    if (blockData.getMaterial().toString().contains("BUTTON")) {
                                        scheduleButtonAutoPowerOff(blockLocation.getBlock());
                                    }
                                }
                                cache.getBlockDataUsedForPlacing().clear();
                            }

                            if (tick.getPendingBlockBreak() != null) {
                                npc.animate(NPC.Animation.SWING_MAIN_ARM);
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
                        playerRecords.get(record).removeViewers(Ruom.getOnlinePlayers());
                    }
                    for (EntityRecord record : entityRecords.keySet()) {
                        entityRecords.get(record).removeViewers(Ruom.getOnlinePlayers());
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
            npc.removeViewers(Ruom.getOnlinePlayers());
        }
    }

    private void moveNPC(RecordTick tick, RecordTick lastNonNullTick, NPC npc, @Nullable Hologram hologram, Vector3 recordCenter, boolean shouldPlayThisTick, int lowSpeedHelpIndex, PlayBackControl.Speed speed, boolean onGround) {
        float newYaw = tick.getYaw();
        float newPitch = tick.getPitch();
        float lastYaw = lastNonNullTick.getYaw();
        float lastPitch = lastNonNullTick.getPitch();

        float yaw;
        float pitch;

        if (shouldPlayThisTick) {
            yaw = newYaw;
            pitch = newPitch;

        } else {
            yaw = getCalculatedLookAngle(lastYaw, newYaw, speed, lowSpeedHelpIndex);
            pitch = getCalculatedLookAngle(lastPitch, newPitch, speed, lowSpeedHelpIndex);
        }

        if (yaw == -1) {
            yaw = lastYaw;
        }
        if (pitch == -1) {
            pitch = lastPitch;
        }

        if (tick.getLocation() != null) {
            Vector3 centerOffSet = Vector3Utils.getTravelDistance(recordCenter, tick.getLocation());
            Vector3 lastPoint = lastNonNullTick.getLocation().clone().add(centerOffSet);
            Vector3 newPoint = tick.getLocation().clone().add(centerOffSet);

            Vector3 travelDistance = getCalculatedTravelDistance(lastPoint, newPoint, centerOffSet, speed, lowSpeedHelpIndex);

            if (!travelDistance.equals(Vector3.at(0, 0, 0))) {
                if (!npc.moveAndLook(travelDistance, yaw, pitch)) {
                    npc.teleport(center.clone().add(centerOffSet), yaw, pitch);
                }
                if (hologram != null) {
                    hologram.move(travelDistance);
                }
            }
        } else {
            if (!(yaw == lastYaw && pitch == lastPitch))
                npc.look(yaw, pitch);
        }
    }

    private Vector3 getCalculatedTravelDistance(Vector3 from, Vector3 to, Vector3 centerOffSet, PlayBackControl.Speed speed, int lowSpeedHelpIndex) {
        Vector3 travelDistance = null;
        Vector3 centerPoint = Vector3Utils.getCenter(from, to);

        switch (speed) {
            case x1: {
                travelDistance = Vector3Utils.getTravelDistance(from, to);
                break;
            }
            case x050: {
                if (lowSpeedHelpIndex == 0) {
                    travelDistance = Vector3Utils.getTravelDistance(centerPoint, to);
                } else {
                    travelDistance = Vector3Utils.getTravelDistance(from, centerPoint);
                }
                break;
            }
            case x025: {
                switch (lowSpeedHelpIndex) {
                    case 0: {
                        Vector3 centerOfCenterPoint = Vector3Utils.getCenter(centerPoint, to);
                        travelDistance = Vector3Utils.getTravelDistance(centerOfCenterPoint, to);
                        break;
                    }
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
                        //lastNonNullTick.setLocation(centerOfCenterPoint.clone().add(-centerOffSet.getX(), -centerOffSet.getY(), -centerOffSet.getZ()));
                        break;
                    }
                }
                break;
            }
        }

        return travelDistance;
    }

    private float getCalculatedLookAngle(float oldAngle, float newAngle, PlayBackControl.Speed speed, int lowSpeedHelpIndex) {
        float angle = -1;

        float centerAngle = MathUtils.getCenterAngle(oldAngle, newAngle);

        if (speed.equals(PlayBackControl.Speed.x050)) {
            if (newAngle != -1) {
                angle = centerAngle;
            }
        } else {
            switch (lowSpeedHelpIndex) {
                case 1: {
                    if (newAngle != -1)
                        angle = MathUtils.getCenterAngle(oldAngle, centerAngle);
                    break;
                }
                case 2: {
                    if (newAngle != -1)
                        angle = centerAngle;
                    break;
                }
                case 3: {
                    if (newAngle != -1) {
                        angle = MathUtils.getCenterAngle(centerAngle, newAngle);
                    }
                    break;
                }
            }
        }
        return angle;
    }

    private void scheduleButtonAutoPowerOff(Block button) {
        Ruom.runSync(() -> {
            if (button.getType().toString().contains("BUTTON")) {
                button.setBlockData(Bukkit.createBlockData(button.getBlockData().getAsString().replace("powered=true", "powered=false")));
                BlockUtils.getBlockDataSound(button.getBlockData()).ifPresent(soundContainer -> soundContainer.withVolume(0.5f).play(button.getLocation()));
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
