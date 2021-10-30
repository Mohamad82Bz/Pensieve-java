package me.Mohamad82.Pensieve.record.listeners;

import me.Mohamad82.Pensieve.record.*;
import me.Mohamad82.Pensieve.record.enums.DamageType;
import me.Mohamad82.RUoM.XSeries.XMaterial;
import me.Mohamad82.RUoM.utils.ServerVersion;
import me.Mohamad82.RUoM.vector.Vector3;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CrossbowMeta;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

public class RecordListeners implements Listener {

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();

        RecordTick currentTick = RecordManager.getInstance().getCurrentRecordTick(player);
        if (currentTick == null) return;

        if (currentTick.getBlockPlaces() == null)
            currentTick.initializeBlockPlaces();
        currentTick.getBlockPlaces().put(Vector3.at(event.getBlockPlaced().getLocation().getBlockX(),
                        event.getBlockPlaced().getLocation().getBlockY(), event.getBlockPlaced().getLocation().getBlockZ()),
                event.getBlockPlaced().getType());
        currentTick.swing();
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        RecordTick currentTick = RecordManager.getInstance().getCurrentRecordTick(player);
        if (currentTick == null) return;

        if (currentTick.getBlockBreaks() == null)
            currentTick.initializeBlockBreaks();
        currentTick.getBlockBreaks().put(
                Vector3.at(
                        block.getLocation().getBlockX(),
                        block.getLocation().getBlockY(),
                        block.getLocation().getBlockZ()),
                block.getType());
        //Pending Block Break (Block break animations)
        if (player.getGameMode().equals(GameMode.SURVIVAL)) {
            Recorder recorder = RecordManager.getInstance().getPlayerRecorder(player);
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

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        RecordTick currentTick = RecordManager.getInstance().getCurrentRecordTick(player);
        if (currentTick == null) return;

        ItemStack item = null;
        if (player.getInventory().getItem(EquipmentSlot.HAND).getType().isEdible())
            item = player.getInventory().getItem(EquipmentSlot.HAND);
        else if (ServerVersion.supports(9) &&
                player.getInventory().getItem(EquipmentSlot.OFF_HAND).getType().isEdible())
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
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {
        if (!event.getEntityType().equals(EntityType.PLAYER)) return;
        Player victim = (Player) event.getEntity();

        RecordTick currentTick = RecordManager.getInstance().getCurrentRecordTick(victim);
        if (currentTick == null) return;

        if (!event.getDamager().getType().equals(EntityType.PLAYER)) {
            //Hit by a mob
            currentTick.damage(DamageType.NORMAL);
        } else {
            Player damager = (Player) event.getDamager();
            if (isCritical(damager)) {
                currentTick.damage(DamageType.CRITICAL);
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!event.getEntityType().equals(EntityType.PLAYER)) return;
        Player player = (Player) event.getEntity();

        RecordTick currentTick = RecordManager.getInstance().getCurrentRecordTick(player);
        if (currentTick == null) return;

        currentTick.damage(DamageType.NORMAL);
        currentTick.setHealth(player.getHealth() - event.getDamage());
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        Player player = (Player) event.getEntity();

        RecordTick currentTick = RecordManager.getInstance().getCurrentRecordTick(player);
        if (currentTick == null) return;

        currentTick.setHunger(event.getFoodLevel());
    }

    @EventHandler
    public void onItemConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();

        RecordTick currentTick = RecordManager.getInstance().getCurrentRecordTick(player);
        if (currentTick == null)
            return;

        currentTick.eatFood();
    }

    @EventHandler
    public void onMainHandChange(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();

        RecordTick currentTick = RecordManager.getInstance().getCurrentRecordTick(player);
        if (currentTick == null) return;

        PacketListener.getInstance().eatingPlayers.remove(player);
    }

    @EventHandler
    public void onSwapHand(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();

        RecordTick currentTick = RecordManager.getInstance().getCurrentRecordTick(player);
        if (currentTick == null) return;

        PacketListener.getInstance().eatingPlayers.remove(player);
    }

    @EventHandler
    public void onDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();

        RecordTick currentTick = RecordManager.getInstance().getCurrentRecordTick(player);
        if (currentTick == null) return;

        if (player.getInventory().getItem(EquipmentSlot.HAND).getAmount() == 0) {
            PacketListener.getInstance().eatingPlayers.remove(player);
        }
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        Projectile projectile = event.getEntity();
        if (!(projectile.getShooter() instanceof Player)) return;
        Player player = (Player) projectile.getShooter();

        Recorder recorder = RecordManager.getInstance().getPlayerRecorder(player);
        if (recorder == null) return;
        RecordTick currentTick = recorder.getCurrentTick(player);
        if (currentTick == null) return;

        if (projectile instanceof ThrownPotion) {
            currentTick.throwPotion();
        }

        recorder.getEntities().add(projectile);

        if (ServerVersion.supports(14) && projectile instanceof Arrow) {
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

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        Projectile projectile = event.getEntity();
        if (!(projectile.getShooter() instanceof Player)) return;
        if (event.getHitEntity() == null) return;
        Player player = (Player) projectile.getShooter();

        Recorder recorder = RecordManager.getInstance().getPlayerRecorder(player);
        if (recorder == null) return;

        recorder.getEntities().remove(projectile);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        RecordTick currentTick = RecordManager.getInstance().getCurrentRecordTick(player);
        if (currentTick == null) return;

        currentTick.setMessage(event.getMessage());
    }

    @SuppressWarnings("deprecation")
    private boolean isCritical(Player damager) {
        return
                damager.getFallDistance() > 0.0F &&
                        !damager.isOnGround() &&
                        !damager.isInsideVehicle() &&
                        !damager.hasPotionEffect(PotionEffectType.BLINDNESS) &&
                        damager.getLocation().getBlock().getType() != Material.LADDER &&
                        damager.getLocation().getBlock().getType() != Material.VINE;
    }

}
