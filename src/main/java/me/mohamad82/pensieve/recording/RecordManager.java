package me.mohamad82.pensieve.recording;

import me.mohamad82.pensieve.recording.record.EntityRecordTick;
import me.mohamad82.pensieve.recording.record.PlayerRecordTick;
import me.mohamad82.ruom.Ruom;
import me.mohamad82.ruom.utils.ServerVersion;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class RecordManager {

    private final Set<RecorderImpl> recorders = new HashSet<>();

    private final Map<Player, PendingBlockBreak> breakingPlayers = new HashMap<>();
    private final Map<Player, ItemStack> eatingPlayers = new HashMap<>();

    private static RecordManager instance;
    public static RecordManager getInstance() {
        return instance;
    }

    public RecordManager() {
        instance = this;

        Ruom.runAsync(() -> {
            for (Player player : Ruom.getOnlinePlayers()) {
                if (breakingPlayers.containsKey(player) || eatingPlayers.containsKey(player)) {
                    Collection<PlayerRecordTick> collection = getCurrentRecordTick(player);
                    if (breakingPlayers.containsKey(player)) {
                        collection.forEach(playerRecordTick -> playerRecordTick.setPendingBlockBreak(breakingPlayers.get(player)));
                        breakingPlayers.get(player).timeSpent++;
                    }
                    if (eatingPlayers.containsKey(player)) {
                        ItemStack foodItem = eatingPlayers.get(player);
                        if (foodItem.getAmount() == 0) {
                            eatingPlayers.remove(player);
                        } else {
                            if (!player.getInventory().getItem(EquipmentSlot.HAND).getType().isEdible() &&
                                    (ServerVersion.supports(9) && !player.getInventory().getItem(EquipmentSlot.OFF_HAND).getType().isEdible())) {
                                eatingPlayers.remove(player);
                            } else {
                                collection.forEach(playerRecordTick -> playerRecordTick.setEatingMaterial(foodItem.getType()));
                            }
                        }
                    }
                }
            }
        }, 0, 1);
    }

    public Collection<PlayerRecordTick> getCurrentRecordTick(Player player) {
        Collection<PlayerRecordTick> collection = new HashSet<>();
        for (RecorderImpl recorder : recorders) {
            if (recorder.getPlayers().contains(player)) {
                if (recorder.isRunning()) {
                    collection.add(recorder.getCurrentTick(player));
                }
            }
        }
        return collection;
    }

    public Collection<EntityRecordTick> getCurrentRecordTick(Entity entity) {
        Collection<EntityRecordTick> collection = new HashSet<>();
        for (RecorderImpl recorder : recorders) {
            if (recorder.getEntities().contains(entity)) {
                if (recorder.isRunning()) {
                    collection.add(recorder.getCurrentTick(entity));
                }
            }
        }
        return collection;
    }

    public Collection<RecorderImpl> getPlayerRecorder(Player player) {
        Collection<RecorderImpl> collection = new HashSet<>();
        for (RecorderImpl recorder : recorders) {
            if (recorder.getPlayers().contains(player))
                collection.add(recorder);
        }
        return collection;
    }

    public Collection<RecorderImpl> getEntityRecorder(Entity entity) {
        Collection<RecorderImpl> collection = new HashSet<>();
        for (RecorderImpl recorder : recorders) {
            if (recorder.getEntities().contains(entity))
                collection.add(recorder);
        }
        return collection;
    }

    public Set<RecorderImpl> getRecorders() {
        return recorders;
    }

    public Map<Player, PendingBlockBreak> getBreakingPlayers() {
        return breakingPlayers;
    }

    public Map<Player, ItemStack> getEatingPlayers() {
        return eatingPlayers;
    }

}
