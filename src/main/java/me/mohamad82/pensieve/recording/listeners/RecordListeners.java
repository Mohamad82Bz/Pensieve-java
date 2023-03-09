package me.mohamad82.pensieve.recording.listeners;

import me.mohamad82.pensieve.recording.PendingBlockBreak;
import me.mohamad82.pensieve.recording.RecordManager;
import me.mohamad82.pensieve.recording.RecorderImpl;
import me.mohamad82.pensieve.recording.enums.DamageType;
import me.mohamad82.pensieve.recording.record.*;
import me.mohamad82.ruom.Ruom;
import me.mohamad82.ruom.math.vector.Vector3;
import me.mohamad82.ruom.math.vector.Vector3Utils;
import me.mohamad82.ruom.math.vector.Vector3UtilsBukkit;
import me.mohamad82.ruom.utils.ListUtils;
import me.mohamad82.ruom.utils.NMSUtils;
import me.mohamad82.ruom.utils.ServerVersion;
import me.mohamad82.ruom.utils.item.CrossbowUtils;
import me.mohamad82.ruom.world.wrappedblock.WrappedBlockUtils;
import me.mohamad82.ruom.xseries.XBlock;
import me.mohamad82.ruom.xseries.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class RecordListeners implements Listener {

    private final Map<UUID, Runnable> chestInteractionRunnables = new HashMap<>();
    private final Map<UUID, Location> furnaceInteractions = new HashMap<>();
    private final Set<Location> buttonInteractionCooldowns = new HashSet<>();

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlace(BlockPlaceEvent event) {
        if (event.isCancelled()) return;
        Player player = event.getPlayer();
        Block block = event.getBlockPlaced();

        Collection<PlayerRecordTick> collection = RecordManager.getInstance().getCurrentRecordTick(player);
        if (collection.isEmpty()) return;

        Vector3 location = Vector3Utils.simplifyToInt(Vector3UtilsBukkit.toVector3(block.getLocation()));

        collection.forEach(playerRecordTick -> {
            if (playerRecordTick.getBlockPlaces() == null)
                playerRecordTick.initializeBlockPlaces();
            playerRecordTick.getBlockPlaces().put(location, WrappedBlockUtils.create(block));
            playerRecordTick.swing();
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBreak(BlockBreakEvent event) {
        if (event.isCancelled()) return;
        Player player = event.getPlayer();
        Block block = event.getBlock();

        Collection<RecorderImpl> collection = RecordManager.getInstance().getPlayerRecorder(player);
        if (collection.isEmpty()) return;

        collection.forEach(recorder -> {
            PlayerRecordTick playerRecordTick = recorder.getCurrentTick(player);
            if (playerRecordTick.getBlockBreaks() == null)
                playerRecordTick.initializeBlockBreaks();
            playerRecordTick.getBlockBreaks().put(Vector3UtilsBukkit.toVector3(block.getLocation()), WrappedBlockUtils.create(block));
        });

        //Pending Block Break (Block break animations)
        if (player.getGameMode().equals(GameMode.SURVIVAL)) {
            PendingBlockBreak pendingBlockBreak = RecordManager.getInstance().getBreakingPlayers().get(player);
            if (pendingBlockBreak == null) return;
            List<Integer> stages = new ArrayList<>();
            int i = 1;
            if (pendingBlockBreak.timeSpent > 3) {
                while (stages.size() <= pendingBlockBreak.timeSpent) {
                    for (int a = 1; a <= Math.round((float) pendingBlockBreak.timeSpent / 9); a++) {
                        stages.add(i);
                    }
                    if (i < 9)
                        i++;
                }
            }
            pendingBlockBreak.setAnimationStages(stages);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSpawn(EntitySpawnEvent event) {
        if (event.isCancelled()) return;
        Entity entity = event.getEntity();

        switch (entity.getType()) {
            case DROPPED_ITEM: {
                Player player = null;
                for (Entity nearbyEntity : entity.getLocation().getWorld().getNearbyEntities(entity.getLocation(), 8, 8, 8)) {
                    if (nearbyEntity.getType().equals(EntityType.PLAYER)) {
                        Player nearbyPlayer = (Player) nearbyEntity;
                        Collection<PlayerRecordTick> collection = RecordManager.getInstance().getCurrentRecordTick(nearbyPlayer);
                        if (collection.size() != 0) {
                            PlayerRecordTick playerRecordTick = collection.iterator().next();
                            if (playerRecordTick.getBlockBreaks() != null) {
                                for (Vector3 blockLocation : playerRecordTick.getBlockBreaks().keySet()) {
                                    Vector3 entityLocation = Vector3UtilsBukkit.toVector3(entity.getLocation());
                                    if (blockLocation.equals(Vector3.at(Math.floor(entityLocation.getX()), Math.floor(entityLocation.getY()), Math.floor(entityLocation.getZ())))) {
                                        player = nearbyPlayer;
                                    }
                                }
                            }
                        }
                    }
                }
                if (player == null) return;
                RecordManager.getInstance().getPlayerRecorder(player).forEach(recorder -> recorder.getEntities().add(entity));
                break;
            }
            case AREA_EFFECT_CLOUD: {
                if (((AreaEffectCloud) entity).getSource() instanceof Player) {
                    Player player = (Player) ((AreaEffectCloud) entity).getSource();
                    RecordManager.getInstance().getPlayerRecorder(player).forEach(recorder -> recorder.getEntities().add(entity));
                }
                break;
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDrop(PlayerDropItemEvent event) {
        if (event.isCancelled()) return;
        Item droppedItem = event.getItemDrop();

        Player player = Bukkit.getPlayer(droppedItem.getThrower());
        if (player == null) return;

        RecordManager.getInstance().getPlayerRecorder(player).forEach(recorder -> {
            recorder.getEntities().add(droppedItem);
            recorder.getCurrentTick(player).swing();
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPickup(EntityPickupItemEvent event) {
        if (event.isCancelled()) return;
        Item droppedItem = event.getItem();
        LivingEntity entity = event.getEntity();

        RecordManager.getInstance().getEntityRecorder(droppedItem).forEach(recorder -> {
            DroppedItemRecord droppedItemRecord = (DroppedItemRecord) recorder.getEntityRecord(droppedItem);
            if (droppedItemRecord == null) return;

            boolean pickerHasRecorder = false;

            if (entity.getType().equals(EntityType.PLAYER)) {
                if (RecordManager.getInstance().getPlayerRecorder((Player) entity).iterator().hasNext())
                    pickerHasRecorder = true;
            } else {
                if (RecordManager.getInstance().getEntityRecorder(entity).iterator().hasNext())
                    pickerHasRecorder = true;
            }

            if (pickerHasRecorder)
                droppedItemRecord.setPickedBy(event.getEntity().getUniqueId());
        });
    }

    @EventHandler
    public void onPickupArrow(PlayerPickupArrowEvent event) {
        Player player = event.getPlayer();
        Projectile arrow = event.getArrow();

        RecordManager.getInstance().getEntityRecorder(arrow).forEach(recorder -> {
            EntityRecord record = recorder.getEntityRecord(arrow);
            if (record == null) return;

            switch (arrow.getType()) {
                case ARROW: {
                    ((ArrowRecord) record).setPickedBy(player.getUniqueId());
                    break;
                }
                case TRIDENT: {
                    ((TridentRecord) record).setPickedBy(player.getUniqueId());
                    break;
                }
            }
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMerge(ItemMergeEvent event) {
        if (event.isCancelled()) return;
        Item entity = event.getEntity();
        Item target = event.getTarget();

        Collection<EntityRecordTick> collection = RecordManager.getInstance().getCurrentRecordTick(target);
        if (collection.isEmpty()) return;

        collection.forEach(entityRecordTick -> {
            DroppedItemRecordTick droppedItemRecordTick = (DroppedItemRecordTick) entityRecordTick;
            int amount = target.getItemStack().getAmount() + entity.getItemStack().getAmount();
            droppedItemRecordTick.setItemAmount(amount);
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        Collection<PlayerRecordTick> collection = RecordManager.getInstance().getCurrentRecordTick(player);
        if (collection.isEmpty()) return;

        if (event.getAction().equals(Action.LEFT_CLICK_AIR) || event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
            collection.forEach(PlayerRecordTick::swing);
        }
        if (ServerVersion.supports(13) && event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && block.getType().isInteractable()) {
            collection.forEach(PlayerRecordTick::swing);
            Ruom.runSync(() -> {
                collection.forEach(playerRecordTick -> {
                    if (playerRecordTick.getBlockData() == null) {
                        playerRecordTick.initializeBlockData();
                    }
                });
                if (!(block.getType().toString().contains("BUTTON") && buttonInteractionCooldowns.contains(block.getLocation()))) {
                    collection.forEach(playerRecordTick -> playerRecordTick.getBlockData().put(Vector3UtilsBukkit.toVector3(block.getLocation()),
                            WrappedBlockUtils.DOORS.contains(XMaterial.matchXMaterial(block.getType())) ? Bukkit.createBlockData(block.getBlockData().getAsString().replace("half=upper", "half=lower")) : block.getBlockData()));
                    if (block.getType().toString().contains("BUTTON")) {
                        buttonInteractionCooldowns.add(block.getLocation());
                        Ruom.runSync(() -> {
                            buttonInteractionCooldowns.remove(block.getLocation());
                        }, 25);
                    }
                }
            }, 1);
        }
        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            if (ServerVersion.supports(13) && block.getType().equals(XMaterial.FURNACE.parseMaterial()) ||
                    (ServerVersion.supports(14) && (block.getType().equals(XMaterial.BLAST_FURNACE.parseMaterial()) || block.getType().equals(XMaterial.SMOKER.parseMaterial())))) {
                furnaceInteractions.put(player.getUniqueId(), block.getLocation());
            } else if (XBlock.isContainer(block)) {
                chestInteractionRunnables.put(player.getUniqueId(), new Runnable() {
                    @Override
                    public void run() {
                        Vector3 interactionLocation = Vector3UtilsBukkit.toVector3(block.getLocation());
                        collection.forEach(playerRecordTick -> {
                            playerRecordTick.setBlockInteractionLocation(interactionLocation);
                            playerRecordTick.setBlockInteractionType(block.getType());
                            playerRecordTick.setOpenChestInteraction(true);
                        });

                        RecordManager.getInstance().getPlayerRecorder(player).forEach(recorder -> {
                            PlayerRecordTick lastNonNullTick = (PlayerRecordTick) recorder.getLastNonNullTick(player.getUniqueId());
                            lastNonNullTick.setBlockInteractionLocation(interactionLocation);
                            lastNonNullTick.setBlockInteractionType(block.getType());
                            lastNonNullTick.setOpenChestInteraction(true);
                        });
                    }
                });
                Ruom.runSync(() -> {
                    chestInteractionRunnables.remove(player.getUniqueId());
                }, 2);
            }
        }
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        Player player = (Player) event.getPlayer();

        if (event.getInventory().getType().equals(InventoryType.CHEST) || event.getInventory().getType().equals(InventoryType.ENDER_CHEST) || event.getInventory().getType().equals(InventoryType.SHULKER_BOX)) {
            if (ListUtils.toList("Chest", "Large Chest", "Ender Chest", "Shulker Box").contains(event.getView().getTitle())) {
                if (chestInteractionRunnables.containsKey(player.getUniqueId())) {
                    chestInteractionRunnables.get(player.getUniqueId()).run();
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();

        Collection<RecorderImpl> collection = RecordManager.getInstance().getPlayerRecorder(player);
        if (collection.isEmpty()) return;

        if (ServerVersion.supports(13) && furnaceInteractions.containsKey(player.getUniqueId())) {
            if (event.getInventory().getType().equals(InventoryType.FURNACE) || (ServerVersion.supports(14) && (event.getInventory().getType().equals(InventoryType.BLAST_FURNACE)) || event.getInventory().getType().equals(InventoryType.SMOKER))) {
                Block block = furnaceInteractions.get(player.getUniqueId()).getBlock();
                if (block.getType().equals(XMaterial.FURNACE.parseMaterial()) || (ServerVersion.supports(14) && (block.getType().equals(XMaterial.BLAST_FURNACE.parseMaterial()) || block.getType().equals(XMaterial.SMOKER.parseMaterial())))) {
                    collection.forEach(recorder -> {
                        PlayerRecordTick playerRecordTick = recorder.getCurrentTick(player);
                        if (playerRecordTick.getBlockData() == null) {
                            playerRecordTick.initializeBlockData();
                        }
                        playerRecordTick.getBlockData().put(Vector3UtilsBukkit.toVector3(block.getLocation()), block.getBlockData());
                    });
                }
            }
            furnaceInteractions.remove(player.getUniqueId());
        }
        collection.forEach(recorder -> {
            PlayerRecordTick playerRecordTick = recorder.getCurrentTick(player);
            PlayerRecordTick lastNonNullTick = (PlayerRecordTick) recorder.getLastNonNullTick(player.getUniqueId());

            if (lastNonNullTick.didOpenChestInteraction()) {
                playerRecordTick.setBlockInteractionLocation(lastNonNullTick.getBlockInteractionLocation());
                playerRecordTick.setBlockInteractionType(lastNonNullTick.getBlockInteractionType());
                playerRecordTick.setOpenChestInteraction(false);

                lastNonNullTick.setOpenChestInteraction(false);
                lastNonNullTick.setBlockInteractionLocation(null);
                lastNonNullTick.setBlockInteractionType(null);
            }
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onHit(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) return;
        if (!event.getEntityType().equals(EntityType.PLAYER)) return;
        Player victim = (Player) event.getEntity();

        Collection<PlayerRecordTick> collection = RecordManager.getInstance().getCurrentRecordTick(victim);
        if (collection.isEmpty()) return;

        collection.forEach(playerRecordTick -> {
            if (!event.getDamager().getType().equals(EntityType.PLAYER)) {
                //Hit by a mob
                playerRecordTick.damage(DamageType.NORMAL);
                //Hit by an arrow
                if (event.getDamager().getType().toString().contains("ARROW")) {
                    playerRecordTick.damage(DamageType.PROJECTILE);
                    playerRecordTick.setBodyArrows(NMSUtils.getBodyArrows(victim) + 1);
                    RecordManager.getInstance().getPlayerRecorder(victim).forEach(recorder -> {
                        ((PlayerRecordTick) recorder.getLastNonNullTick(victim.getUniqueId())).setBodyArrows(NMSUtils.getBodyArrows(victim) + 1);
                    });
                }
            } else if (ServerVersion.supports(9)) {
                Player damager = (Player) event.getDamager();
                if (isCritical(damager)) {
                    playerRecordTick.damage(DamageType.CRITICAL);
                } else if (isSprintAttack(damager)) {
                    playerRecordTick.damage(DamageType.SPRINT_ATTACK);
                }
            }
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDamage(EntityDamageEvent event) {
        if (event.isCancelled()) return;
        if (!event.getEntityType().equals(EntityType.PLAYER)) return;
        Player player = (Player) event.getEntity();

        Collection<PlayerRecordTick> collection = RecordManager.getInstance().getCurrentRecordTick(player);
        if (collection.isEmpty()) return;

        collection.forEach(playerRecordTick -> {
            if (event.getCause().equals(EntityDamageEvent.DamageCause.LAVA) ||
                    event.getCause().equals(EntityDamageEvent.DamageCause.FIRE) ||
                    event.getCause().equals(EntityDamageEvent.DamageCause.FIRE_TICK) ||
                    event.getCause().equals(EntityDamageEvent.DamageCause.HOT_FLOOR)) {
                playerRecordTick.damage(DamageType.BURN);
            } else {
                if (playerRecordTick.getTakenDamageType() == null) {
                    playerRecordTick.damage(DamageType.NORMAL);
                }
            }
            playerRecordTick.setHealth(player.getHealth() - event.getDamage());
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (event.isCancelled()) return;
        Player player = (Player) event.getEntity();

        Collection<PlayerRecordTick> collection = RecordManager.getInstance().getCurrentRecordTick(player);
        if (collection.isEmpty()) return;

        collection.forEach(playerRecordTick -> playerRecordTick.setHunger(event.getFoodLevel()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onItemConsume(PlayerItemConsumeEvent event) {
        if (event.isCancelled()) return;
        Player player = event.getPlayer();

        Collection<PlayerRecordTick> collection = RecordManager.getInstance().getCurrentRecordTick(player);
        if (collection.isEmpty()) return;

        collection.forEach(PlayerRecordTick::eatFood);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMainHandChange(PlayerItemHeldEvent event) {
        if (event.isCancelled()) return;
        Player player = event.getPlayer();

        Collection<PlayerRecordTick> collection = RecordManager.getInstance().getCurrentRecordTick(player);
        if (collection.isEmpty()) return;

        RecordManager.getInstance().getEatingPlayers().remove(player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSwapHand(PlayerSwapHandItemsEvent event) {
        if (event.isCancelled()) return;
        Player player = event.getPlayer();

        Collection<PlayerRecordTick> collection = RecordManager.getInstance().getCurrentRecordTick(player);
        if (collection.isEmpty()) return;

        RecordManager.getInstance().getEatingPlayers().remove(player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDropItem(PlayerDropItemEvent event) {
        if (event.isCancelled()) return;
        Player player = event.getPlayer();

        Collection<PlayerRecordTick> collection = RecordManager.getInstance().getCurrentRecordTick(player);
        if (collection.isEmpty()) return;

        if (player.getInventory().getItem(EquipmentSlot.HAND).getAmount() == 0) {
            RecordManager.getInstance().getEatingPlayers().remove(player);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onFish(PlayerFishEvent event) {
        if (event.isCancelled()) return;
        Player player = event.getPlayer();

        if (event.getState() == PlayerFishEvent.State.REEL_IN || event.getState() == PlayerFishEvent.State.IN_GROUND || event.getState() == PlayerFishEvent.State.CAUGHT_ENTITY) {
            Collection<PlayerRecordTick> collection = RecordManager.getInstance().getCurrentRecordTick(player);
            if (collection.isEmpty()) return;

            collection.forEach(PlayerRecordTick::retrieveFishingRod);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (event.isCancelled()) return;
        Projectile projectile = event.getEntity();
        if (!(projectile.getShooter() instanceof Player)) return;
        Player player = (Player) projectile.getShooter();

        Collection<RecorderImpl> collection = RecordManager.getInstance().getPlayerRecorder(player);

        collection.forEach(recorder -> {
            PlayerRecordTick playerRecordTick = recorder.getCurrentTick(player);

            if (projectile instanceof Arrow || projectile instanceof SpectralArrow || projectile instanceof ThrownPotion || projectile instanceof ThrownExpBottle || projectile instanceof Snowball || projectile instanceof EnderPearl || projectile instanceof Egg) {
                playerRecordTick.throwProjectile();
                recorder.getEntities().add(projectile);
            }
            if (ServerVersion.supports(13) && projectile instanceof Trident) {
                playerRecordTick.throwTrident();
                recorder.getEntities().add(projectile);
            }
            if (projectile instanceof FishHook) {
                playerRecordTick.throwFishingRod();
                recorder.getEntities().add(projectile);
            }
            if (projectile instanceof Firework) {
                playerRecordTick.throwFirework();
                playerRecordTick.swing();
                recorder.getEntities().add(projectile);
            }
        });

        if (ServerVersion.supports(14) && (projectile instanceof Arrow || projectile instanceof SpectralArrow)) {
            ItemStack crossbowItem = null;
            boolean hasCrossbowOnOffHand = false;
            boolean hasInteractableItemOnMainHand = false;
            if (player.getInventory().getItem(EquipmentSlot.HAND).getType().equals(XMaterial.CROSSBOW.parseMaterial())) {
                crossbowItem = player.getInventory().getItem(EquipmentSlot.HAND);
            } else if (player.getInventory().getItem(EquipmentSlot.OFF_HAND).getType().equals(XMaterial.CROSSBOW.parseMaterial())) {
                crossbowItem = player.getInventory().getItem(EquipmentSlot.OFF_HAND);
                hasCrossbowOnOffHand = true;
            }
            if (crossbowItem != null) {
                if (hasCrossbowOnOffHand) {
                    ItemStack handItem = player.getInventory().getItem(EquipmentSlot.HAND);
                    if (handItem != null) {
                        if (handItem.getType().isInteractable()) {
                            hasInteractableItemOnMainHand = true;
                        }
                    }
                }
                if (!hasInteractableItemOnMainHand) {
                    collection.forEach(recorder -> recorder.getCurrentTick(player).shootCrossbow());

                    ItemStack chargedCrossbow = crossbowItem.clone();
                    CrossbowUtils.setCharged(chargedCrossbow, true);

                    if (hasCrossbowOnOffHand) {
                        collection.forEach(recorder -> recorder.getCurrentTick(player).setOffHand(chargedCrossbow));
                    } else {
                        collection.forEach(recorder -> recorder.getCurrentTick(player).setHand(chargedCrossbow));
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onProjectileHit(ProjectileHitEvent event) {
        if (event.isCancelled()) return;
        Projectile projectile = event.getEntity();
        if (!(projectile.getShooter() instanceof Player)) return;
        if (event.getHitEntity() == null) return;
        Player player = (Player) projectile.getShooter();

        Collection<RecorderImpl> collection = RecordManager.getInstance().getPlayerRecorder(player);
        if (collection.isEmpty()) return;

        collection.forEach(recorder -> {
            if (!(projectile instanceof FishHook)) {
                recorder.getEntities().remove(projectile);
            }
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Collection<RecorderImpl> collection = RecordManager.getInstance().getPlayerRecorder(player);
        if (collection.isEmpty()) return;

        collection.forEach(recorder -> {
            recorder.safeRemovePlayer(player);
        });
    }

    @SuppressWarnings("deprecation")
    private boolean isCritical(Player player) {
        return player.getFallDistance() > 0.0f &&
                !player.isOnGround() &&
                !player.isInsideVehicle() &&
                !player.hasPotionEffect(PotionEffectType.BLINDNESS) &&
                player.getLocation().getBlock().getType() != Material.LADDER &&
                player.getLocation().getBlock().getType() != Material.VINE;
    }

    private boolean isSprintAttack(Player player) {
        return player.isSprinting();
    }

}
