package me.mohamad82.pensieve.recording.listeners;

import me.Mohamad82.RUoM.Ruom;
import me.Mohamad82.RUoM.XSeries.XMaterial;
import me.Mohamad82.RUoM.utils.ListUtils;
import me.Mohamad82.RUoM.utils.ServerVersion;
import me.Mohamad82.RUoM.vector.Vector3;
import me.Mohamad82.RUoM.vector.Vector3Utils;
import me.mohamad82.pensieve.recording.*;
import me.mohamad82.pensieve.recording.enums.DamageType;
import me.mohamad82.pensieve.recording.record.EntityRecord;
import me.mohamad82.pensieve.recording.record.PlayerRecord;
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
import org.bukkit.inventory.meta.CrossbowMeta;
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

        RecordTick currentTick = RecordManager.getInstance().getCurrentRecordTick(player);
        if (currentTick == null) return;

        if (currentTick.getBlockPlaces() == null)
            currentTick.initializeBlockPlaces();
        if (currentTick.getBlockData() == null)
            currentTick.initializeBlockData();

        Vector3 location = Vector3Utils.simplifyToBlock(Vector3Utils.toVector3(block.getLocation()));
        currentTick.getBlockPlaces().put(location, block.getType());
        currentTick.getBlockData().put(location, block.getState().getBlockData().getAsString());
        currentTick.swing();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBreak(BlockBreakEvent event) {
        if (event.isCancelled()) return;
        Player player = event.getPlayer();
        Block block = event.getBlock();

        Recorder recorder = RecordManager.getInstance().getPlayerRecorder(player);
        if (recorder == null) return;
        RecordTick currentTick = recorder.getCurrentTick(player);
        if (currentTick == null) return;

        if (currentTick.getBlockBreaks() == null)
            currentTick.initializeBlockBreaks();
        currentTick.getBlockBreaks().put(Vector3Utils.toVector3(block.getLocation()), block.getType());

        //Pending Block Break (Block break animations)
        if (player.getGameMode().equals(GameMode.SURVIVAL)) {
            List<PendingBlockBreak> pendingBlockBreaks = new ArrayList<>();
            int i = 2;
            for (PlayerRecord record : recorder.getPlayerRecords()) {
                if (record.getUuid().equals(player.getUniqueId())) {
                    while (record.getRecordTicks().get(recorder.getCurrentTickIndex() - i).getPendingBlockBreak() != null) {
                        pendingBlockBreaks.add(record.getRecordTicks().get(recorder.getCurrentTickIndex() - i).getPendingBlockBreak());
                        i++;
                    }
                }
            }
            //Some weird calculations for the break animation stages
            int index = 0;
            for (int a = 0; a < 10; a++) {
                for (int j = 0; j < Math.ceil((float) pendingBlockBreaks.size() / 10); j++) {
                    if (index >= pendingBlockBreaks.size())
                        pendingBlockBreaks.get(pendingBlockBreaks.size() - 1).getAnimationStages().add(a);
                    else
                        pendingBlockBreaks.get(index).getAnimationStages().add(a);
                    index++;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSpawn(EntitySpawnEvent event) {
        if (event.isCancelled()) return;
        Entity entity = event.getEntity();
        if (!entity.getType().equals(EntityType.DROPPED_ITEM)) return;

        Player player = null;
        for (Entity nearbyEntity : entity.getLocation().getWorld().getNearbyEntities(entity.getLocation(), 8, 8, 8)) {
            if (nearbyEntity.getType().equals(EntityType.PLAYER)) {
                Player nearbyPlayer = (Player) nearbyEntity;
                RecordTick nearbyPlayerCurrentTick = RecordManager.getInstance().getCurrentRecordTick(nearbyPlayer);
                if (nearbyPlayerCurrentTick != null) {
                    if (nearbyPlayerCurrentTick.getBlockBreaks() != null) {
                        for (Vector3 blockLocation : nearbyPlayerCurrentTick.getBlockBreaks().keySet()) {
                            Vector3 entityLocation = Vector3Utils.toVector3(entity.getLocation());
                            if (blockLocation.equals(Vector3.at(Math.floor(entityLocation.getX()), Math.floor(entityLocation.getY()), Math.floor(entityLocation.getZ())))) {
                                player = nearbyPlayer;
                            }
                        }
                    }
                }
            }
        }
        if (player == null) return;
        Recorder recorder = RecordManager.getInstance().getPlayerRecorder(player);

        recorder.getEntities().add(entity);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDrop(PlayerDropItemEvent event) {
        if (event.isCancelled()) return;
        Item droppedItem = event.getItemDrop();

        Player player = Bukkit.getPlayer(droppedItem.getThrower());
        if (player == null) return;

        Recorder recorder = RecordManager.getInstance().getPlayerRecorder(player);
        if (recorder == null) return;

        recorder.getEntities().add(droppedItem);
        recorder.getCurrentTick(player).swing();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPickup(EntityPickupItemEvent event) {
        if (event.isCancelled()) return;
        Item droppedItem = event.getItem();
        LivingEntity entity = event.getEntity();

        Recorder recorder = RecordManager.getInstance().getEntityRecorder(droppedItem);
        if (recorder == null) return;
        EntityRecord entityRecord = recorder.getEntityRecord(droppedItem);
        if (entityRecord == null) return;
        boolean pickerHasRecorder = false;

        if (entity.getType().equals(EntityType.PLAYER)) {
            if (RecordManager.getInstance().getPlayerRecorder((Player) entity) != null)
                pickerHasRecorder = true;
        } else {
            if (RecordManager.getInstance().getEntityRecorder(entity) != null)
                pickerHasRecorder = true;
        }

        if (pickerHasRecorder)
            entityRecord.setPickedUpBy(event.getEntity().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMerge(ItemMergeEvent event) {
        if (event.isCancelled()) return;
        Item entity = event.getEntity();
        Item target = event.getTarget();

        RecordTick currentTick = RecordManager.getInstance().getCurrentRecordTick(target);
        if (currentTick == null) return;

        int amount = target.getItemStack().getAmount() + entity.getItemStack().getAmount();
        currentTick.setItemAmount(amount);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        RecordTick currentTick = RecordManager.getInstance().getCurrentRecordTick(player);
        if (currentTick == null) return;

        ItemStack item = null;
        if (player.getInventory().getItem(EquipmentSlot.HAND).getType().isEdible())
            item = player.getInventory().getItem(EquipmentSlot.HAND);
        else if (ServerVersion.supports(9) && player.getInventory().getItem(EquipmentSlot.OFF_HAND).getType().isEdible())
            item = player.getInventory().getItem(EquipmentSlot.OFF_HAND);

        if (item != null && (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) || event.getAction().equals(Action.RIGHT_CLICK_AIR))) {
            if (!(PacketListener.getInstance().eatingPlayers.containsKey(player))) {
                currentTick.setEatingItem(item);
                PacketListener.getInstance().eatingPlayers.put(player, item);
            }
        }
        if (event.getAction().equals(Action.LEFT_CLICK_AIR) || event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
            currentTick.swing();
        }
        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && block.getType().isInteractable()) {
            currentTick.swing();
            Ruom.runSync(() -> {
                if (currentTick.getBlockData() == null)
                    currentTick.initializeBlockData();
                if (!(block.getType().toString().contains("BUTTON") && buttonInteractionCooldowns.contains(block.getLocation()))) {
                    currentTick.getBlockData().put(Vector3Utils.toVector3(block.getLocation()), block.getBlockData().getAsString());
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
            if (block.getType().equals(XMaterial.FURNACE.parseMaterial()) ||
                    (ServerVersion.supports(14) && (block.getType().equals(XMaterial.BLAST_FURNACE.parseMaterial()) || block.getType().equals(XMaterial.SMOKER.parseMaterial())))) {
                furnaceInteractions.put(player.getUniqueId(), block.getLocation());
            } else if (block.getType().equals(XMaterial.CHEST.parseMaterial()) ||
                    block.getType().equals(XMaterial.TRAPPED_CHEST.parseMaterial()) ||
                    block.getType().equals(XMaterial.ENDER_CHEST.parseMaterial()) ||
                    block.getType().toString().contains("SHULKER_BOX")) {
                chestInteractionRunnables.put(player.getUniqueId(), new Runnable() {
                    @Override
                    public void run() {
                        Vector3 interactionLocation = Vector3Utils.toVector3(block.getLocation());
                        currentTick.setBlockInteractionLocation(interactionLocation);
                        currentTick.setBlockInteractionType(block.getType());
                        currentTick.setOpenChestInteraction(true);

                        RecordTick lastNonNullTick = RecordManager.getInstance().getPlayerRecorder(player).getLastNonNullTick(player.getUniqueId());
                        lastNonNullTick.setBlockInteractionLocation(interactionLocation);
                        lastNonNullTick.setBlockInteractionType(block.getType());
                        lastNonNullTick.setOpenChestInteraction(true);
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

        Recorder recorder = RecordManager.getInstance().getPlayerRecorder(player);
        if (recorder == null) return;
        RecordTick lastNonNullTick = recorder.getLastNonNullTick(player.getUniqueId());
        if (lastNonNullTick == null) return;
        RecordTick currentTick = recorder.getCurrentTick(player);

        if (furnaceInteractions.containsKey(player.getUniqueId())) {
            if (event.getInventory().getType().equals(InventoryType.FURNACE) ||
                    (ServerVersion.supports(14) && (event.getInventory().getType().equals(InventoryType.BLAST_FURNACE)) || event.getInventory().getType().equals(InventoryType.SMOKER))) {
                Block block = furnaceInteractions.get(player.getUniqueId()).getBlock();
                if (block.getType().equals(XMaterial.FURNACE.parseMaterial()) ||
                        (ServerVersion.supports(14) && (block.getType().equals(XMaterial.BLAST_FURNACE.parseMaterial()) || block.getType().equals(XMaterial.SMOKER.parseMaterial())))) {
                    if (currentTick.getBlockData() == null)
                        currentTick.initializeBlockData();
                    currentTick.getBlockData().put(Vector3Utils.toVector3(block.getLocation()), block.getBlockData().getAsString());
                }
            }
            furnaceInteractions.remove(player.getUniqueId());
        }
        if (lastNonNullTick.isOpenChestInteraction()) {
            currentTick.setBlockInteractionLocation(lastNonNullTick.getBlockInteractionLocation());
            currentTick.setBlockInteractionType(lastNonNullTick.getBlockInteractionType());
            currentTick.setOpenChestInteraction(false);

            lastNonNullTick.setOpenChestInteraction(false);
            lastNonNullTick.setBlockInteractionLocation(null);
            lastNonNullTick.setBlockInteractionType(null);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onHit(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) return;
        if (!event.getEntityType().equals(EntityType.PLAYER)) return;
        Player victim = (Player) event.getEntity();

        RecordTick currentTick = RecordManager.getInstance().getCurrentRecordTick(victim);
        if (currentTick == null) return;

        if (!event.getDamager().getType().equals(EntityType.PLAYER)) {
            //Hit by a mob
            currentTick.damage(DamageType.NORMAL);
            //Hit by an arrow
            if (event.getDamager().getType().toString().contains("ARROW")) {
                currentTick.damage(DamageType.PROJECTILE);
                currentTick.setBodyArrows(victim.getArrowsInBody() + 1);
                RecordManager.getInstance().getPlayerRecorder(victim).getLastNonNullTick(victim.getUniqueId()).setBodyArrows(victim.getArrowsInBody() + 1);
            }
        } else {
            Player damager = (Player) event.getDamager();
            if (isCritical(damager)) {
                currentTick.damage(DamageType.CRITICAL);
            } else if (isSprintAttack(damager)) {
                currentTick.damage(DamageType.SPRINT_ATTACK);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDamage(EntityDamageEvent event) {
        if (event.isCancelled()) return;
        if (!event.getEntityType().equals(EntityType.PLAYER)) return;
        Player player = (Player) event.getEntity();

        RecordTick currentTick = RecordManager.getInstance().getCurrentRecordTick(player);
        if (currentTick == null) return;

        if (event.getCause().equals(EntityDamageEvent.DamageCause.LAVA) ||
                event.getCause().equals(EntityDamageEvent.DamageCause.FIRE) ||
                event.getCause().equals(EntityDamageEvent.DamageCause.FIRE_TICK) ||
                event.getCause().equals(EntityDamageEvent.DamageCause.HOT_FLOOR)) {
            currentTick.damage(DamageType.BURN);
        } else {
            currentTick.damage(DamageType.NORMAL);
        }
        currentTick.setHealth(player.getHealth() - event.getDamage());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (event.isCancelled()) return;
        Player player = (Player) event.getEntity();

        RecordTick currentTick = RecordManager.getInstance().getCurrentRecordTick(player);
        if (currentTick == null) return;

        currentTick.setHunger(event.getFoodLevel());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onItemConsume(PlayerItemConsumeEvent event) {
        if (event.isCancelled()) return;
        Player player = event.getPlayer();

        RecordTick currentTick = RecordManager.getInstance().getCurrentRecordTick(player);
        if (currentTick == null)
            return;

        currentTick.eatFood();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMainHandChange(PlayerItemHeldEvent event) {
        if (event.isCancelled()) return;
        Player player = event.getPlayer();

        RecordTick currentTick = RecordManager.getInstance().getCurrentRecordTick(player);
        if (currentTick == null) return;

        PacketListener.getInstance().eatingPlayers.remove(player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSwapHand(PlayerSwapHandItemsEvent event) {
        if (event.isCancelled()) return;
        Player player = event.getPlayer();

        RecordTick currentTick = RecordManager.getInstance().getCurrentRecordTick(player);
        if (currentTick == null) return;

        PacketListener.getInstance().eatingPlayers.remove(player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDropItem(PlayerDropItemEvent event) {
        if (event.isCancelled()) return;
        Player player = event.getPlayer();

        RecordTick currentTick = RecordManager.getInstance().getCurrentRecordTick(player);
        if (currentTick == null) return;

        if (player.getInventory().getItem(EquipmentSlot.HAND).getAmount() == 0) {
            PacketListener.getInstance().eatingPlayers.remove(player);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (event.isCancelled()) return;
        Projectile projectile = event.getEntity();
        if (!(projectile.getShooter() instanceof Player)) return;
        Player player = (Player) projectile.getShooter();

        Recorder recorder = RecordManager.getInstance().getPlayerRecorder(player);
        if (recorder == null) return;
        RecordTick currentTick = recorder.getCurrentTick(player);
        if (currentTick == null) return;

        if (projectile instanceof ThrownPotion || projectile instanceof ThrownExpBottle || projectile instanceof Snowball || projectile instanceof EnderPearl || projectile instanceof Egg) {
            currentTick.throwProjectile();
        }

        recorder.getEntities().add(projectile);

        if (ServerVersion.supports(14) && projectile instanceof AbstractArrow) {
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
                        if (handItem.getType().isEdible())
                            hasInteractableItemOnMainHand = true;
                        else if (handItem.getType().equals(XMaterial.BOW.parseMaterial()))
                            hasInteractableItemOnMainHand = true;
                    }
                }
                if (!hasInteractableItemOnMainHand) {
                    CrossbowMeta crossbowMeta = (CrossbowMeta) crossbowItem.getItemMeta();
                    if (crossbowMeta.hasChargedProjectiles()) {
                        boolean hasFireworksOnCrossbow = false;
                        for (ItemStack crossbowProjectile : crossbowMeta.getChargedProjectiles()) {
                            if (crossbowProjectile.getType().equals(XMaterial.FIREWORK_ROCKET.parseMaterial()))
                                hasFireworksOnCrossbow = true;
                        }
                        if (!hasFireworksOnCrossbow) {
                            currentTick.shootCrossbow();

                            ItemStack crossbowCopy = crossbowItem.clone();
                            CrossbowMeta crossbowCopyMeta = (CrossbowMeta) crossbowCopy.getItemMeta();
                            crossbowCopyMeta.setChargedProjectiles(new ArrayList<>());
                            crossbowCopy.setItemMeta(crossbowCopyMeta);
                            if (hasCrossbowOnOffHand) {
                                currentTick.setOffHand(crossbowCopy);
                            } else {
                                currentTick.setHand(crossbowCopy);
                            }
                        }
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

        Recorder recorder = RecordManager.getInstance().getPlayerRecorder(player);
        if (recorder == null) return;

        recorder.getEntities().remove(projectile);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChat(AsyncPlayerChatEvent event) {
        if (event.isCancelled()) return;
        Player player = event.getPlayer();

        RecordTick currentTick = RecordManager.getInstance().getCurrentRecordTick(player);
        if (currentTick == null) return;

        currentTick.setMessage(event.getMessage());
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
