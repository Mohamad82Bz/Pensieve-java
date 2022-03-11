package me.mohamad82.pensieve.replaying;

import me.mohamad82.pensieve.api.event.PensieveNPCLookEvent;
import me.mohamad82.pensieve.api.event.PensieveNPCMoveAndLookEvent;
import me.mohamad82.pensieve.recording.PendingBlockBreak;
import me.mohamad82.pensieve.recording.RecordContainer;
import me.mohamad82.pensieve.recording.record.Record;
import me.mohamad82.pensieve.recording.record.*;
import me.mohamad82.ruom.Ruom;
import me.mohamad82.ruom.adventure.ComponentUtils;
import me.mohamad82.ruom.math.MathUtils;
import me.mohamad82.ruom.math.vector.Vector3UtilsBukkit;
import me.mohamad82.ruom.npc.EntityNPC;
import me.mohamad82.ruom.npc.LivingEntityNPC;
import me.mohamad82.ruom.npc.NPC;
import me.mohamad82.ruom.npc.PlayerNPC;
import me.mohamad82.ruom.npc.entity.*;
import me.mohamad82.ruom.utils.*;
import me.mohamad82.ruom.math.vector.Vector3;
import me.mohamad82.ruom.math.vector.Vector3Utils;
import me.mohamad82.ruom.xseries.XMaterial;
import me.mohamad82.ruom.xseries.XSound;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class ReplayerImpl implements Replayer {

    private final Map<PlayerRecord, PlayerNPC> playerRecords = new HashMap<>();
    private final Map<EntityRecord, EntityNPC> entityRecords = new HashMap<>();
    private final Map<UUID, List<RecordTick>> preparedLastNonNullTicks = new HashMap<>();
    private final Map<UUID, UUID> modifiedUuids = new HashMap<>();
    private final Map<Integer, Set<BlockChange>> blockChanges = new HashMap<>();
    private final Map<Integer, Map<UUID, TickAdditions>> tickAdditions = new HashMap<>();
    private final Set<UUID> playedEntities = new HashSet<>();
    private final Set<UUID> finishedEntities = new HashSet<>();

    private final World world;
    private final Vector3 center;
    private boolean prepared;

    private BukkitTask replayRunnable;
    private final Random random = new Random();
    private final PlayBackControl playbackControl = new PlayBackControl();

    ReplayerImpl(RecordContainer recordContainer, World world, Vector3 center) {
        this.world = world;
        this.center = Vector3Utils.simplifyToCenter(center);

        for (PlayerRecord record : recordContainer.getPlayerRecords()) {
            Vector3 centerOffSetDistance = Vector3Utils.getTravelDistance(record.getCenter(), record.getStartLocation());
            Vector3 centerOffSet = center.clone().add(centerOffSetDistance).add(0.5, 0, 0.5);
            this.playerRecords.put(record, PlayerNPC.playerNPC(
                    record.getName(),
                    Vector3UtilsBukkit.toLocation(world, centerOffSet),
                    record.getSkin()
            ));
        }

        for (EntityRecord record : recordContainer.getEntityRecords()) {
            UUID uuid = UUID.randomUUID();
            modifiedUuids.put(record.getUuid(), uuid);
            Location startLocation = Vector3UtilsBukkit.toLocation(world, center.clone().add(Vector3Utils.getTravelDistance(record.getCenter(), record.getStartLocation())).add(0.5, 0, 0.5));
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
                case TRIDENT: {
                    if (!ServerVersion.supports(13)) continue;
                    byte loyalty = ((TridentRecord) record).getLoyalty();
                    boolean enchanted = ((TridentRecord) record).isEnchanted();
                    entityNPC = TridentNPC.tridentNPC(startLocation, loyalty, enchanted);
                    entityNPC.setNoGravity(true);
                    break;
                }
                case FISHING_BOBBER: {
                    int ownerEntityId = getEntityId(((FishingHookRecord) record).getOwner());
                    if (ownerEntityId != 0) {
                        entityNPC = FishingHookNPC.fishingHookNPC(startLocation, ownerEntityId);
                    } else {
                        entityNPC = null;
                    }
                    break;
                }
                case FIREWORK_ROCKET: {
                    entityNPC = FireworkNPC.fireworkNPC(startLocation, Optional.of(((FireworkRecord) record).getFireworkItem()));
                    ((FireworkNPC) entityNPC).setShotAtAngle(((FireworkRecord) record).isShotAtAngle());
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
    }

    @Override
    public CompletableFuture<Void> prepare() {
        return CompletableFuture.runAsync(() -> {
            Set<Record> records = new HashSet<>();
            records.addAll(playerRecords.keySet());
            records.addAll(entityRecords.keySet());

            for (Record record : records) {
                List<RecordTick> lastNonNullTicks = new ArrayList<>();
                RecordTick lastNonNullTick = null;
                boolean firstLoop = true;
                int tickIndex = 0;

                final Map<UUID, Integer> pendingBlockBreakStages = new HashMap<>();
                final Map<UUID, Integer> pendingBlockBreakSkippedParticles = new HashMap<>();
                final Map<UUID, Integer> pendingFoodEatSkippedTicks = new HashMap<>();
                PendingBlockBreak previousPendingBlockBreak = null;

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
                            TickAdditions tickAdditions = new TickAdditions();

                            if (playerRecordTick.getPendingBlockBreak() != null) {
                                PendingBlockBreak pendingBlockBreak = playerRecordTick.getPendingBlockBreak();
                                if (previousPendingBlockBreak != null && !pendingBlockBreak.getUuid().equals(previousPendingBlockBreak.getUuid())) {
                                    pendingBlockBreakStages.remove(record.getUuid());
                                    pendingBlockBreakSkippedParticles.remove(record.getUuid());
                                }

                                Vector3 location = center.clone().add(Vector3Utils.getTravelDistance(record.getCenter(), pendingBlockBreak.getLocation()));
                                if (!pendingBlockBreakSkippedParticles.containsKey(record.getUuid())) {
                                    pendingBlockBreakSkippedParticles.put(record.getUuid(), 0);
                                }
                                int skippedTick = pendingBlockBreakSkippedParticles.get(record.getUuid()) + 1;
                                pendingBlockBreakSkippedParticles.put(record.getUuid(), skippedTick);
                                if (skippedTick % 2 == 0) {
                                    tickAdditions.setBlockBreakParticle(location);
                                    tickAdditions.swing();
                                }

                                if (!pendingBlockBreakStages.containsKey(record.getUuid())) {
                                    pendingBlockBreakStages.put(record.getUuid(), 0);
                                }
                                int stage = pendingBlockBreakStages.get(record.getUuid());
                                tickAdditions.setBlockDestructionStage(new AbstractMap.SimpleEntry<>(location, stage));
                                pendingBlockBreakStages.put(record.getUuid(), stage + 1);

                                previousPendingBlockBreak = pendingBlockBreak;
                            } else {
                                pendingBlockBreakStages.remove(record.getUuid());
                                pendingBlockBreakSkippedParticles.remove(record.getUuid());
                            }

                            if (playerRecordTick.getEatingMaterial() != null) {
                                if (!pendingFoodEatSkippedTicks.containsKey(record.getUuid())) {
                                    pendingFoodEatSkippedTicks.put(record.getUuid(), 0);
                                }
                                int skippedFoodTicks = pendingFoodEatSkippedTicks.get(record.getUuid());
                                if (skippedFoodTicks % 7 == 0) {
                                    tickAdditions.setFoodEatParticle(true);
                                }
                                if (skippedFoodTicks % 4 == 0) {
                                    tickAdditions.setFoodEatSound(true);
                                }
                                pendingFoodEatSkippedTicks.put(record.getUuid(), skippedFoodTicks + 1);
                            } else {
                                pendingFoodEatSkippedTicks.remove(record.getUuid());
                            }

                            if (!this.tickAdditions.containsKey(tickIndex)) {
                                this.tickAdditions.put(tickIndex, new HashMap<>());
                            }
                            this.tickAdditions.get(tickIndex).put(record.getUuid(), tickAdditions);

                            if (playerRecordTick.getBlockBreaks() != null) {
                                if (!blockChanges.containsKey(tickIndex))
                                    blockChanges.put(tickIndex, new HashSet<>());

                                for (Map.Entry<Vector3, BlockData> entry : playerRecordTick.getBlockBreaks().entrySet()) {
                                    Vector3 location = center.clone().add(Vector3Utils.getTravelDistance(record.getCenter(), entry.getKey()));
                                    blockChanges.get(tickIndex).add(BlockChange.breakChange(Vector3UtilsBukkit.toLocation(world, location), entry.getValue()));
                                }
                            }
                            if (playerRecordTick.getBlockPlaces() != null) {
                                if (!blockChanges.containsKey(tickIndex))
                                    blockChanges.put(tickIndex, new HashSet<>());

                                for (Map.Entry<Vector3, BlockData> entry : playerRecordTick.getBlockPlaces().entrySet()) {
                                    Vector3 location = center.clone().add(Vector3Utils.getTravelDistance(record.getCenter(), entry.getKey()));
                                    blockChanges.get(tickIndex).add(BlockChange.placeChange(Vector3UtilsBukkit.toLocation(world, location), entry.getValue()));
                                }
                            }
                            if (playerRecordTick.getBlockData() != null) {
                                if (!blockChanges.containsKey(tickIndex))
                                    blockChanges.put(tickIndex, new HashSet<>());

                                for (Map.Entry<Vector3, BlockData> entry : playerRecordTick.getBlockData().entrySet()) {
                                    Vector3 location = center.clone().add(Vector3Utils.getTravelDistance(record.getCenter(), entry.getKey()));
                                    blockChanges.get(tickIndex).add(BlockChange.blockChange(Vector3UtilsBukkit.toLocation(world, location), entry.getValue()));
                                }
                            }

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

                    tickIndex++;
                    lastNonNullTicks.add(lastNonNullTick.copy());
                }

                preparedLastNonNullTicks.put(record.getUuid(), lastNonNullTicks);
            }
            prepared = true;
        });
    }

    public PlayBackControl start() {
        if (!prepared) {
            throw new IllegalStateException("Cannot start a non-prepared replay.");
        }
        int maxTicks = 0;
        for (PlayerRecord record : playerRecords.keySet()) {
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
            boolean progressChanged;
            int finishedRecords = 0;
            int stackRun = 0;
            final int totalRecords = playerRecords.size();
            PlayBackControl.Speed speed = playbackControl.getSpeed();
            public void run() {
                try {
                    for (Player player : Ruom.getOnlinePlayers()) {
                        NMSUtils.sendActionBar(player, ComponentUtils.parse("<gradient:red:dark_red>Playing tick " + tickIndex + " / " + playbackControl.getMaxProgress()));
                    }
                    if (playbackControl.getProgress() + 1 == tickIndex) {
                        playbackControl.addProgress(1);
                    } else if (tickIndex != 0) {
                        //Progress changed manually
                        progressChanged = true;
                        if (shouldPlayThisTick) {
                            applyProgress(tickIndex, playbackControl.getProgress());
                            tickIndex = playbackControl.getProgress();
                        }
                    }
                    if (playbackControl.isPause()) {
                        return;
                    }
                    if (speed != playbackControl.getSpeed()) {
                        //Speed changed manually
                        if (lowSpeedHelpIndex == 0 && stackRun == 0) {
                            speed = playbackControl.getSpeed();
                        }
                    }
                    switch (speed) {
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
                        default: {
                            shouldPlayThisTick = true;
                            lowSpeedHelpIndex = 0;
                        }
                    }

                    for (EntityRecord record : entityRecords.keySet()) {
                        UUID uuid = modifiedUuids.get(record.getUuid());
                        if (playedEntities.contains(uuid) && finishedEntities.contains(uuid)) continue;
                        if (tickIndex < record.getStartingTick()) continue;
                        EntityNPC npc = entityRecords.get(record);
                        if (!playedEntities.contains(uuid) && !finishedEntities.contains(uuid)) {
                            if (shouldPlayThisTick) {
                                playedEntities.add(uuid);

                                if (speed == PlayBackControl.Speed.x050 || speed == PlayBackControl.Speed.x025)
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
                                /*
                                    End of the record
                                 */
                                if (record instanceof DroppedItemRecord) {
                                    DroppedItemRecord droppedItemRecord = (DroppedItemRecord) record;
                                    if (droppedItemRecord.getPickedBy() != null) {
                                        int collector = getEntityId(droppedItemRecord.getPickedBy());
                                        if (collector != 0) {
                                            ((ItemNPC) npc).collect(collector);
                                        }
                                    }
                                } else if (record instanceof ArrowRecord) {
                                    ArrowRecord arrowRecord = (ArrowRecord) record;
                                    if (arrowRecord.getPickedBy() != null) {
                                        int collector = getEntityId(arrowRecord.getPickedBy());
                                        if (collector != 0) {
                                            ((ArrowNPC) npc).collect(collector);
                                        }
                                    }
                                } else if (record instanceof TridentRecord) {
                                    TridentRecord tridentRecord = (TridentRecord) record;
                                    if (tridentRecord.getPickedBy() != null) {
                                        int collector = getEntityId(tridentRecord.getPickedBy());
                                        if (collector != 0) {
                                            ((TridentNPC) npc).collect(collector);
                                        }
                                    }
                                } else if (record instanceof FireworkRecord) {
                                    if (((FireworkNPC) npc).hasExplosion()) {
                                        ((FireworkNPC) npc).explode();
                                    }
                                }
                                finishedEntities.add(uuid);
                                npc.removeViewers(Ruom.getOnlinePlayers());
                            } else {
                                RecordTick tick = record.getRecordTicks().get(entityTickIndex);
                                RecordTick lastNonNullTick = preparedLastNonNullTicks.get(record.getUuid()).get(entityTickIndex - 1);

                                if (tick instanceof AreaEffectCloudRecordTick) {
                                    ((AreaEffectCloudNPC) npc).setRadius(((AreaEffectCloudRecordTick) tick).getRadius());
                                    continue;
                                }

                                if (tick.getLocation() != null) {
                                    moveNPC(tick, lastNonNullTick, npc, record.getCenter(), shouldPlayThisTick, lowSpeedHelpIndex, speed, false);
                                }

                                if (!shouldPlayThisTick) continue;

                                Location location = Vector3UtilsBukkit.toLocation(world,
                                        center.clone().add(Vector3Utils.getTravelDistance(record.getCenter(), lastNonNullTick.getLocation())));

                                if (tick.getVelocity() != null) {
                                    npc.setVelocity(tick.getVelocity());

                                    if (lastNonNullTick.getLocation() != null) {
                                        if (entityTickIndex != 0) {
                                            float soundPitch = (float) (1.1 + (random.nextInt(2) / 10));
                                            if (record instanceof ArrowRecord) {
                                                SoundContainer.soundContainer(XSound.ENTITY_ARROW_HIT).withVolume(playbackControl.getVolume()).withPitch(soundPitch).play(location, npc.getViewers());
                                            } else if (record instanceof TridentRecord) {
                                                SoundContainer.soundContainer(XSound.ITEM_TRIDENT_HIT_GROUND).withVolume(playbackControl.getVolume()).withPitch(soundPitch + 0.2f).play(location, npc.getViewers());
                                            }
                                        }
                                    }
                                }
                                if (entityTickIndex == record.getRecordTicks().size() - 1) {
                                    if (record instanceof ProjectileRecord) {
                                        ProjectileRecord projectileRecord = (ProjectileRecord) record;

                                        if (ListUtils.toList(XMaterial.SPLASH_POTION.parseMaterial(), XMaterial.LINGERING_POTION.parseMaterial()).contains(projectileRecord.getProjectileItem().getType())) {
                                            for (Player player : npc.getViewers()) {
                                                //noinspection deprecation
                                                player.playEffect(location, Effect.POTION_BREAK, NMSUtils.getPotionColor(projectileRecord.getProjectileItem()));
                                            }
                                            SoundContainer.soundContainer(XSound.ENTITY_SPLASH_POTION_BREAK).withVolume(playbackControl.getVolume() - 0.3f).play(location, npc.getViewers());
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
                                } else if (tick instanceof TridentRecordTick && ServerVersion.supports(13)) {
                                    if (((TridentRecordTick) tick).hasAttachedBlock()) {
                                        SoundContainer.soundContainer(XSound.ITEM_TRIDENT_HIT).withVolume(playbackControl.getVolume()).play(location, npc.getViewers());
                                    }
                                    if (((TridentRecordTick) tick).isReturning()) {
                                        SoundContainer.soundContainer(XSound.ITEM_TRIDENT_RETURN).withVolume(playbackControl.getVolume()).play(location, npc.getViewers());
                                    }
                                } else if (tick instanceof FishingHookRecordTick) {
                                    if (((FishingHookRecordTick) tick).getHookedEntity() != null) {
                                        int hookedEntityId = getEntityId(((FishingHookRecordTick) tick).getHookedEntity());
                                        if (hookedEntityId != 0) {
                                            ((FishingHookNPC) npc).setHookedEntity(hookedEntityId + 1);
                                        }
                                    }
                                }
                            }
                        }
                    }

                    for (PlayerRecord record : playerRecords.keySet()) {
                        if (!finishedEntities.contains(record.getUuid())) {
                            PlayerNPC npc = playerRecords.get(record);
                            if (tickIndex == record.getTotalTicks()) {
                                //This record is finished
                                npc.removeViewers(Ruom.getOnlinePlayers());
                                finishedEntities.add(record.getUuid());
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
                            PlayerRecordTick tick = (PlayerRecordTick) record.getRecordTicks().get(tickIndex);
                            PlayerRecordTick lastNonNullTick;

                            if (tickIndex == 0) {
                                npc.setPose(tick.getPose());
                                npc.setSprinting(tick.wasSprinting());
                                npc.setGlowing(tick.wasGlowing());
                                npc.setInvisible(tick.wasInvisible());
                                npc.setOnFire(tick.wasBurning());

                                setAllEquipments(npc, tick);

                                lastNonNullTick = (PlayerRecordTick) preparedLastNonNullTicks.get(record.getUuid()).get(tickIndex);
                            } else {
                                lastNonNullTick = (PlayerRecordTick) preparedLastNonNullTicks.get(record.getUuid()).get(tickIndex - 1);

                                moveNPC(tick, lastNonNullTick, npc, record.getCenter(), shouldPlayThisTick, lowSpeedHelpIndex, speed, true);

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

                                setAllEquipments(npc, tick);
                            }

                            if (!shouldPlayThisTick) continue;

                            Location location = Vector3UtilsBukkit.toLocation(world,
                                    center.clone().add(Vector3Utils.getTravelDistance(record.getCenter(), lastNonNullTick.getLocation())));

                            if (tick.didSwing()) {
                                npc.animate(NPC.Animation.SWING_MAIN_ARM);
                            }
                            if (tick.getTakenDamageType() != null) {
                                npc.animate(NPC.Animation.TAKE_DAMAGE);
                                SoundContainer hurtSound = SoundContainer.soundContainer(XSound.ENTITY_PLAYER_HURT).withVolume(playbackControl.getVolume());

                                switch (tick.getTakenDamageType()) {
                                    case CRITICAL: {
                                        npc.animate(NPC.Animation.CRITICAL_EFFECT);
                                        SoundContainer.soundContainer(XSound.ENTITY_PLAYER_ATTACK_CRIT).withVolume(playbackControl.getVolume()).play(location, npc.getViewers());
                                        hurtSound.play(location, npc.getViewers());
                                        break;
                                    }
                                    case SPRINT_ATTACK: {
                                        SoundContainer.soundContainer(XSound.ENTITY_PLAYER_ATTACK_STRONG).withVolume(playbackControl.getVolume()).play(location, npc.getViewers());
                                        hurtSound.play(location, npc.getViewers());
                                        break;
                                    }
                                    case BURN: {
                                        SoundContainer.soundContainer(XSound.ENTITY_PLAYER_HURT_ON_FIRE).withVolume(playbackControl.getVolume()).play(location, npc.getViewers());
                                        break;
                                    }
                                    case PROJECTILE: {
                                        SoundContainer.soundContainer(XSound.ENTITY_ARROW_HIT).withVolume(playbackControl.getVolume()).play(location, npc.getViewers());
                                        hurtSound.play(location, npc.getViewers());
                                        break;
                                    }
                                    case NORMAL: {
                                        hurtSound.play(location, npc.getViewers());
                                        break;
                                    }
                                }
                            }
                            if (tick.thrownProjectile() && tick.getUsedItemTime() == -1 && !tick.shotCrossbow()) {
                                //Throw sounds are all the same
                                SoundContainer.soundContainer(XSound.ENTITY_SPLASH_POTION_THROW).withVolume(playbackControl.getVolume()).withPitch(0.1f).play(location, npc.getViewers());
                            }
                            if (tick.getUseItemInteractionHand() > 0) {
                                switch (tick.getUseItemInteractionHand()) {
                                    case 1: {
                                        npc.startUsingItem(LivingEntityNPC.InteractionHand.MAIN_HAND);
                                        SoundContainer.soundContainer(XSound.ITEM_ARMOR_EQUIP_GENERIC).withVolume(playbackControl.getVolume()).play(location, npc.getViewers());
                                        break;
                                    }
                                    case 2: {
                                        npc.startUsingItem(LivingEntityNPC.InteractionHand.OFF_HAND);
                                        SoundContainer.soundContainer(XSound.ITEM_ARMOR_EQUIP_GENERIC).withVolume(playbackControl.getVolume()).play(location, npc.getViewers());
                                        break;
                                    }
                                    case 3: {
                                        npc.stopUsingItem();
                                        if (!tick.drawnCrossbow()) {
                                            if (tick.thrownProjectile()) {
                                                if (tick.thrownTrident()) {
                                                    SoundContainer.soundContainer(XSound.ITEM_TRIDENT_THROW).withVolume(playbackControl.getVolume()).play(location, npc.getViewers());
                                                } else {
                                                    float f = tick.getUsedItemTime() / 20.0F;
                                                    f = (f * f + f * 2.0F) / 3.0F;
                                                    f = Math.min(f, 1F);
                                                    if (f > 0.1D && tick.thrownProjectile()) {
                                                        SoundContainer.soundContainer(XSound.ENTITY_ARROW_SHOOT).withVolume(playbackControl.getVolume()).withPitch(1.0F / (random.nextFloat() * 0.4F + 1.2F) + f * 0.5F).play(location, npc.getViewers());
                                                    }
                                                }
                                            }
                                        }
                                        break;
                                    }
                                }
                            }
                            if (tick.ateFood()) {
                                SoundContainer.soundContainer(XSound.ENTITY_PLAYER_BURP).withVolume(playbackControl.getVolume()).play(location, npc.getViewers());
                            }
                            if (tick.shotCrossbow()) {
                                SoundContainer.soundContainer(XSound.ITEM_CROSSBOW_SHOOT).withVolume(playbackControl.getVolume()).play(location, npc.getViewers());
                            }
                            if (tick.thrownFishingRod()) {
                                SoundContainer.soundContainer(XSound.ENTITY_FISHING_BOBBER_THROW).withPitch(0.1f).withVolume(playbackControl.getVolume() - 0.3f).play(location, npc.getViewers());
                            }
                            if (tick.retrievedFishingRod()) {
                                SoundContainer.soundContainer(XSound.ENTITY_FISHING_BOBBER_RETRIEVE).withPitch(0.4f + (random.nextBoolean() ? 0.1f : 0)).withVolume(playbackControl.getVolume() - 0.3f).play(location, npc.getViewers());
                            }
                            if (tick.thrownFirework()) {
                                SoundContainer.soundContainer(XSound.ENTITY_FIREWORK_ROCKET_LAUNCH).withVolume(playbackControl.getVolume()).play(location, npc.getViewers());
                            }
                            XSound crossbowLoadingSound = null;
                            switch (tick.getCrossbowChargeLevel()) {
                                case 2: {
                                    crossbowLoadingSound = XSound.ITEM_CROSSBOW_LOADING_END;
                                    break;
                                }
                                case 1: {
                                    crossbowLoadingSound = XSound.ITEM_CROSSBOW_LOADING_MIDDLE;
                                    break;
                                }
                                case 0: {
                                    crossbowLoadingSound = XSound.ITEM_CROSSBOW_LOADING_START;
                                    break;
                                }
                            }
                            if (crossbowLoadingSound != null) {
                                SoundContainer.soundContainer(crossbowLoadingSound).withVolume(playbackControl.getVolume() - 0.3f).play(location, npc.getViewers());
                            }
                            if (tick.getBodyArrows() >= 0) {
                                npc.setArrowsOnBody(tick.getBodyArrows());
                            }
                            if (tick.getEffectColor() != -1) {
                                npc.setEffectColor(tick.getEffectColor());
                                npc.setEffectsAsAmbients(false);
                            }

                            if (tickIndex != 0) {
                                TickAdditions tickAddition = tickAdditions.get(tickIndex).get(record.getUuid());

                                if (tick.getPendingBlockBreak() != null) {
                                    PendingBlockBreak pendingBlockBreak = tick.getPendingBlockBreak();

                                    if (tickAddition.didSwing()) {
                                        npc.animate(NPC.Animation.SWING_MAIN_ARM);
                                    }
                                    if (tickAddition.getBlockBreakParticle() != null) {
                                        pendingBlockBreak.spawnParticle(world, tickAddition.getBlockBreakParticle());
                                    }
                                    if (tickAddition.getBlockDestructionStage() != null) {
                                        Map.Entry<Vector3, Integer> entry = tickAddition.getBlockDestructionStage();
                                        pendingBlockBreak.animateBlockBreak(npc.getViewers(), entry.getValue(), entry.getKey());
                                    }
                                }

                                if (tick.getEatingMaterial() != null) {
                                    if (tickAddition.isFoodEatParticle()) {
                                        Location locationYawFixed = location.clone();
                                        locationYawFixed.setYaw(lastNonNullTick.getYaw());
                                        PlayerUtils.spawnFoodEatParticles(locationYawFixed, tick.getEatingMaterial());
                                    }
                                    if (tickAddition.isFoodEatSound()) {
                                        SoundContainer.soundContainer(XSound.ENTITY_GENERIC_EAT).withVolume(playbackControl.getVolume()).withPitch(1f).play(location, npc.getViewers());
                                    }
                                }
                            }

                            if (blockChanges.containsKey(tickIndex)) {
                                for (BlockChange blockChange : blockChanges.get(tickIndex)) {
                                    blockChange.apply();

                                    SoundContainer soundContainer = null;
                                    switch (blockChange.getChangeType()) {
                                        case PLACE: {
                                            soundContainer = SoundContainer.soundContainer(blockChange.getAfterBlockData().getSoundGroup().getPlaceSound()).withVolume(playbackControl.getVolume()).withPitch(0.8f);
                                            break;
                                        }
                                        case BREAK: {
                                            soundContainer = SoundContainer.soundContainer(blockChange.getBeforeBlockData().getSoundGroup().getBreakSound()).withVolume(playbackControl.getVolume()).withPitch(0.8f);
                                            NMSUtils.sendBlockDestruction(npc.getViewers(), Vector3UtilsBukkit.toVector3(blockChange.getLocation()), -1);
                                            BlockUtils.spawnBlockBreakParticles(blockChange.getLocation(), blockChange.getBeforeBlockData().getMaterial());
                                            break;
                                        }
                                        case MODIFY: {
                                            Optional<SoundContainer> blockDataSound = BlockUtils.getBlockDataSound(blockChange.getAfterBlockData());
                                            if (blockDataSound.isPresent())
                                                soundContainer = blockDataSound.get();
                                            if (blockChange.getAfterBlockData().getMaterial().toString().contains("BUTTON")) {
                                                scheduleButtonAutoPowerOff(blockChange.getLocation().getBlock());
                                            }
                                            break;
                                        }
                                    }
                                    if (soundContainer != null) {
                                        soundContainer.play(blockChange.getLocation(), npc.getViewers());
                                    }
                                }
                            }

                            if (tick.getBlockInteractionLocation() != null) {
                                Vector3 blockLocFinal = center.clone().add(Vector3Utils.getTravelDistance(record.getCenter(), tick.getBlockInteractionLocation()));
                                Location blockLocation = Vector3UtilsBukkit.toLocation(world, blockLocFinal);
                                NMSUtils.sendChestAnimation(npc.getViewers(), blockLocFinal, tick.getBlockInteractionType(), tick.didOpenChestInteraction());

                                float pitch = 0.9f + (random.nextBoolean() ? 0.1f : 0);
                                boolean shouldDelaySound = false;
                                XSound interactionSound;
                                switch (tick.getBlockInteractionType()) {
                                    case TRAPPED_CHEST:
                                    case CHEST: {
                                        interactionSound = tick.didOpenChestInteraction() ? XSound.BLOCK_CHEST_OPEN : XSound.BLOCK_CHEST_CLOSE;
                                        if (!tick.didOpenChestInteraction()) shouldDelaySound = true;
                                        break;
                                    }
                                    case ENDER_CHEST: {
                                        interactionSound = tick.didOpenChestInteraction() ? XSound.BLOCK_ENDER_CHEST_OPEN : XSound.BLOCK_ENDER_CHEST_CLOSE;
                                        if (!tick.didOpenChestInteraction()) shouldDelaySound = true;
                                        break;
                                    }
                                    default: {
                                        interactionSound = tick.didOpenChestInteraction() ? XSound.BLOCK_SHULKER_BOX_OPEN : XSound.BLOCK_SHULKER_BOX_CLOSE;
                                    }
                                }
                                if (shouldDelaySound) {
                                    Ruom.runSync(() -> {
                                        SoundContainer.soundContainer(interactionSound).withVolume(playbackControl.getVolume() - 0.3f).withPitch(pitch).play(blockLocation, npc.getViewers());
                                    }, 6);
                                } else {
                                    SoundContainer.soundContainer(interactionSound).withVolume(playbackControl.getVolume() - 0.3f).withPitch(pitch).play(blockLocation, npc.getViewers());
                                }
                            }
                        }
                    }

                    if (shouldPlayThisTick)
                        tickIndex++;

                    if (!isCancelled()) {
                        if (speed == PlayBackControl.Speed.x2) {
                            if (stackRun == 0) {
                                stackRun++;
                                run();
                            } else if (stackRun == 1) {
                                stackRun--;
                            }
                        } else if (speed == PlayBackControl.Speed.x5) {
                            if (stackRun < 5) {
                                stackRun++;
                                run();
                            } else if (stackRun == 5) {
                                stackRun = 0;
                            }
                        }
                    }
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

    public void applyProgress(int currentTick, int newTick) {
        if (newTick > currentTick) {
            for (int i = currentTick; i < newTick; i++) {
                if (blockChanges.containsKey(i)) {
                    for (BlockChange blockChange : blockChanges.get(i)) {
                        blockChange.apply();
                    }
                }
            }
            for (EntityRecord entityRecord : entityRecords.keySet()) {
                NPC npc = entityRecords.get(entityRecord);
                UUID uuid = modifiedUuids.get(entityRecord.getUuid());
                if ((newTick - (entityRecord.getStartingTick())) >= entityRecord.getRecordTicks().size()) {
                    npc.removeViewers(Ruom.getOnlinePlayers());
                    finishedEntities.add(uuid);
                } else {
                    if (!finishedEntities.contains(uuid)) {
                        if (newTick - entityRecord.getStartingTick() - 1 >= 0) {
                            if (!playedEntities.contains(uuid)) {
                                playedEntities.add(uuid);
                                npc.addViewers(Ruom.getOnlinePlayers());
                            }
                            RecordTick lastNonNullTick = preparedLastNonNullTicks.get(entityRecord.getUuid()).get(newTick - entityRecord.getStartingTick() - 1);
                            Vector3 location = center.clone().add(Vector3Utils.getTravelDistance(entityRecord.getCenter(), lastNonNullTick.getLocation()));
                            npc.teleport(location, lastNonNullTick.getYaw(), lastNonNullTick.getPitch());
                            npc.setVelocity(lastNonNullTick.getVelocity());

                            playedEntities.add(uuid);
                        } else {
                            npc.teleport(entityRecord.getStartLocation());
                        }
                    }
                }
            }
            for (PlayerRecord playerRecord : playerRecords.keySet()) {
                PlayerNPC npc = playerRecords.get(playerRecord);
                npc.stopUsingItem();
                if (newTick > playerRecord.getTotalTicks()) {
                    npc.removeViewers(Ruom.getOnlinePlayers());
                } else {
                    PlayerRecordTick lastNonNullTick = (PlayerRecordTick) preparedLastNonNullTicks.get(playerRecord.getUuid()).get(newTick - 1);
                    setAllEquipments(npc, lastNonNullTick);
                    Vector3 location = center.clone().add(Vector3Utils.getTravelDistance(playerRecord.getCenter(), lastNonNullTick.getLocation()));
                    npc.teleport(location, lastNonNullTick.getYaw(), lastNonNullTick.getPitch());
                }
            }
        } else {
            for (int i = currentTick; i > newTick; i--) {
                if (blockChanges.containsKey(i)) {
                    for (BlockChange blockChange : blockChanges.get(i)) {
                        blockChange.rollback();
                    }
                }
            }
            for (EntityRecord entityRecord : entityRecords.keySet()) {
                Ruom.log(entityRecord.getType().toString());
                NPC npc = entityRecords.get(entityRecord);
                UUID uuid = modifiedUuids.get(entityRecord.getUuid());
                if (newTick < entityRecord.getStartingTick()) {
                    npc.removeViewers(Ruom.getOnlinePlayers());
                    npc.teleport(entityRecord.getStartLocation(), 0, 0);
                    finishedEntities.remove(uuid);
                    playedEntities.remove(uuid);
                } else {
                    boolean nowDone = newTick - entityRecord.getStartingTick() >= entityRecord.getRecordTicks().size();
                    boolean wasDone = currentTick - entityRecord.getStartingTick() >= entityRecord.getRecordTicks().size();

                    if (newTick - entityRecord.getStartingTick() - 1 >= 0) {
                        if (wasDone && !nowDone) {
                            npc.addViewers(Ruom.getOnlinePlayers());
                            playedEntities.add(uuid);
                            finishedEntities.remove(uuid);
                        }
                        if (newTick - entityRecord.getStartingTick() - 1 < entityRecord.getRecordTicks().size()) {
                            RecordTick lastNonNullTick = preparedLastNonNullTicks.get(entityRecord.getUuid()).get(newTick - entityRecord.getStartingTick() - 1);
                            Vector3 location = center.clone().add(Vector3Utils.getTravelDistance(entityRecord.getCenter(), lastNonNullTick.getLocation()));
                            npc.teleport(location, lastNonNullTick.getYaw(), lastNonNullTick.getPitch());
                            npc.setVelocity(lastNonNullTick.getVelocity());
                        }
                    } else {
                        playedEntities.remove(uuid);
                        finishedEntities.remove(uuid);
                    }
                }
            }
            for (PlayerRecord playerRecord : playerRecords.keySet()) {
                Ruom.log("Player Record");
                NPC npc = playerRecords.get(playerRecord);
                PlayerRecordTick lastNonNullTick = (PlayerRecordTick) preparedLastNonNullTicks.get(playerRecord.getUuid()).get(newTick - 1);
                setAllEquipments(npc, lastNonNullTick);
                Vector3 location = center.clone().add(Vector3Utils.getTravelDistance(playerRecord.getCenter(), lastNonNullTick.getLocation()));
                npc.teleport(location, lastNonNullTick.getYaw(), lastNonNullTick.getPitch());
            }
        }
    }

    private void moveNPC(RecordTick tick, RecordTick lastNonNullTick, NPC npc, Vector3 recordCenter, boolean shouldPlayThisTick, int lowSpeedHelpIndex, PlayBackControl.Speed speed, boolean onGround) {
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
            Vector3 travelDistance = getCalculatedTravelDistance(lastPoint, newPoint, speed, lowSpeedHelpIndex);

            PensieveNPCMoveAndLookEvent moveAndLookEvent = new PensieveNPCMoveAndLookEvent(npc, Vector3UtilsBukkit.toLocation(world, lastPoint), Vector3UtilsBukkit.toLocation(world, newPoint));
            Ruom.getServer().getPluginManager().callEvent(moveAndLookEvent);

            if (!travelDistance.equals(Vector3.at(0, 0, 0))) {
                if (!npc.moveAndLook(travelDistance, yaw, pitch)) {
                    Vector3 location = center.clone().add(Vector3Utils.getTravelDistance(recordCenter, tick.getLocation()));
                    npc.teleport(location, yaw, pitch);
                }
            }
        } else {
            if (!(yaw == lastYaw && pitch == lastPitch)) {
                PensieveNPCLookEvent lookEvent = new PensieveNPCLookEvent(npc, yaw, pitch);
                Ruom.getServer().getPluginManager().callEvent(lookEvent);
                npc.look(yaw, pitch);
            }
        }
    }

    private Vector3 getCalculatedTravelDistance(Vector3 from, Vector3 to, PlayBackControl.Speed speed, int lowSpeedHelpIndex) {
        Vector3 travelDistance = null;
        Vector3 centerPoint = Vector3Utils.getCenter(from, to);

        switch (speed) {
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
                        break;
                    }
                }
                break;
            }
            default: {
                travelDistance = Vector3Utils.getTravelDistance(from, to);
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

    private void setAllEquipments(NPC npc, PlayerRecordTick tick) {
        if (tick.getHand() != null)
            npc.setEquipment(NPC.EquipmentSlot.MAINHAND, tick.getHand());
        if (tick.getOffHand() != null && ServerVersion.supports(9))
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
