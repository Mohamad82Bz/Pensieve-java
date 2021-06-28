package me.Mohamad82.Pensieve.record.listeners;

import me.Mohamad82.Pensieve.record.*;
import me.Mohamad82.RUoM.ServerVersion;
import me.Mohamad82.RUoM.Vector3;
import org.bukkit.GameMode;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class RecordListeners implements Listener {

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        for (Recorder recorder : RecordManager.getInstance().getRecorders()) {
            if (recorder.getPlayers().contains(player)) {
                if (recorder.isRunning()) {
                    RecordTick currentTick = recorder.getCurrentTick(player);
                    if (currentTick.getBlockPlaces() == null)
                        currentTick.initializeBlockPlaces();
                    currentTick.getBlockPlaces().put(Vector3.at(event.getBlockPlaced().getLocation().getBlockX(),
                            event.getBlockPlaced().getLocation().getBlockY(), event.getBlockPlaced().getLocation().getBlockZ()),
                            event.getBlockPlaced().getType());
                    currentTick.swing();
                }
            }
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        for (Recorder recorder : RecordManager.getInstance().getRecorders()) {
            if (recorder.getPlayers().contains(player)) {
                if (recorder.isRunning()) {
                    RecordTick currentTick = recorder.getCurrentTick(player);
                    if (currentTick.getBlockBreaks() == null)
                        currentTick.initializeBlockBreaks();
                    currentTick.getBlockBreaks().put(Vector3.at(event.getBlock().getLocation().getBlockX(),
                            event.getBlock().getLocation().getBlockY(), event.getBlock().getLocation().getBlockZ()),
                            event.getBlock().getType());
                    if (player.getGameMode().equals(GameMode.SURVIVAL)) {
                        List<PendingBlockBreak> pendingBlockBreaks = new ArrayList<>();
                        int i = 2;
                        for (Record record : recorder.getRecords()) {
                            if (record.getPlayerUUID().equals(player.getUniqueId())) {
                                while (record.getRecordTicks().get(recorder.getCurrentTickIndex() - i).getPendingBlockBreak() != null) {
                                    pendingBlockBreaks.add(record.getRecordTicks().get(recorder.getCurrentTickIndex() - i).getPendingBlockBreak());
                                    i++;
                                }
                            }
                        }
                        int index = 0;
                        for (int a = 0; a < 10; a++) {
                            for (int j = 0; j < Math.round((float) pendingBlockBreaks.size() / 10); j++) {
                                if (index >= pendingBlockBreaks.size())
                                    pendingBlockBreaks.get(pendingBlockBreaks.size() - 1).getAnimationStages().add(a);
                                else
                                    pendingBlockBreaks.get(index).getAnimationStages().add(a);
                                index++;
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        for (Recorder recorder : RecordManager.getInstance().getRecorders()) {
            if (recorder.getPlayers().contains(player)) {
                if (recorder.isRunning()) {
                    ItemStack item = null;
                    if (player.getInventory().getItem(EquipmentSlot.HAND).getType().isEdible())
                        item = player.getInventory().getItem(EquipmentSlot.HAND);
                    else if (ServerVersion.supports(9) &&
                            player.getInventory().getItem(EquipmentSlot.OFF_HAND).getType().isEdible())
                        item = player.getInventory().getItem(EquipmentSlot.OFF_HAND);

                    RecordTick currentTick = recorder.getCurrentTick(player);

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
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!event.getEntityType().equals(EntityType.PLAYER)) return;
        Player player = (Player) event.getEntity();
        for (Recorder recorder : RecordManager.getInstance().getRecorders()) {
            if (recorder.getPlayers().contains(player)) {
                if (recorder.isRunning()) {
                    RecordTick currentTick = recorder.getCurrentTick(player);
                    currentTick.damage();
                    currentTick.setHealth(player.getHealth() - event.getDamage());
                }
            }
        }
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        Player player = (Player) event.getEntity();
        for (Recorder recorder : RecordManager.getInstance().getRecorders()) {
            if (recorder.getPlayers().contains(player)) {
                if (recorder.isRunning()) {
                    RecordTick currentTick = recorder.getCurrentTick(player);
                    currentTick.setHunger(event.getFoodLevel());
                }
            }
        }
    }

    @EventHandler
    public void onItemConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        for (Recorder recorder : RecordManager.getInstance().getRecorders()) {
            if (recorder.getPlayers().contains(player)) {
                if (recorder.isRunning()) {
                    RecordTick currentTick = recorder.getCurrentTick(player);
                    currentTick.eatFood();
                }
            }
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        for (Recorder recorder : RecordManager.getInstance().getRecorders()) {
            if (recorder.getPlayers().contains(player)) {
                if (recorder.isRunning()) {
                    RecordTick currentTick = recorder.getCurrentTick(player);
                    currentTick.setMessage(event.getMessage());
                }
            }
        }
    }

}
