package me.mohamad82.pensieve.recording;

import me.mohamad82.pensieve.recording.record.PlayerRecordTick;
import me.mohamad82.pensieve.recording.record.RecordTick;
import me.mohamad82.ruom.Ruom;
import me.mohamad82.ruom.utils.ServerVersion;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RecordManager {

    private final Set<Player> recordingPlayers = new HashSet<>();
    private final Set<Recorder> recorders = new HashSet<>();

    private final Map<Player, PendingBlockBreak> breakingPlayers = new HashMap<>();
    private final Map<Player, ItemStack> eatingPlayers = new HashMap<>();

    private static RecordManager instance;
    public static RecordManager getInstance() {
        return instance;
    }

    public RecordManager() {
        instance = this;

        new BukkitRunnable() {
            public void run() {
                for (Player player : Ruom.getOnlinePlayers()) {
                    if (breakingPlayers.containsKey(player) || eatingPlayers.containsKey(player)) {
                        RecordTick currentTick = RecordManager.getInstance().getCurrentRecordTick(player);
                        if (currentTick == null) return;
                        PlayerRecordTick playerRecordTick = (PlayerRecordTick) currentTick;

                        if (breakingPlayers.containsKey(player)) {
                            playerRecordTick.setPendingBlockBreak(breakingPlayers.get(player));
                        }
                        if (eatingPlayers.containsKey(player)) {
                            ItemStack foodItem = eatingPlayers.get(player);
                            if (foodItem.getAmount() == 0) {
                                eatingPlayers.remove(player);
                            } else {
                                if (!(player.getInventory().getItem(EquipmentSlot.HAND).getType().isEdible()) &&
                                        (ServerVersion.supports(9) &&
                                                !player.getInventory().getItem(EquipmentSlot.OFF_HAND).getType().isEdible())) {
                                    eatingPlayers.remove(player);
                                } else {
                                    playerRecordTick.setEatingItem(foodItem);
                                }
                            }
                        }
                    }
                }
            }
        }.runTaskTimerAsynchronously(Ruom.getPlugin(), 0, 1);
    }

    public @Nullable RecordTick getCurrentRecordTick(Player player) {
        for (Recorder recorder : recorders) {
            if (recorder.getPlayers().contains(player)) {
                if (recorder.isRunning()) {
                    return recorder.getCurrentTick(player);
                }
            }
        }
        return null;
    }

    public @Nullable RecordTick getCurrentRecordTick(Entity entity) {
        for (Recorder recorder : recorders) {
            if (recorder.getEntities().contains(entity)) {
                if (recorder.isRunning()) {
                    return recorder.getCurrentTick(entity);
                }
            }
        }
        return null;
    }

    public @Nullable Recorder getPlayerRecorder(Player player) {
        for (Recorder recorder : recorders) {
            if (recorder.getPlayers().contains(player))
                return recorder;
        }
        return null;
    }

    public @Nullable Recorder getEntityRecorder(Entity entity) {
        for (Recorder recorder : recorders) {
            if (recorder.getEntities().contains(entity))
                return recorder;
        }
        return null;
    }

    public Set<Player> getRecordingPlayers() {
        return recordingPlayers;
    }

    public Set<Recorder> getRecorders() {
        return recorders;
    }

    public Map<Player, PendingBlockBreak> getBreakingPlayers() {
        return breakingPlayers;
    }

    public Map<Player, ItemStack> getEatingPlayers() {
        return eatingPlayers;
    }

}
