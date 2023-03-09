package me.mohamad82.pensieve.recording.listeners;

import me.mohamad82.pensieve.recording.RecordManager;
import me.mohamad82.pensieve.recording.record.PlayerRecordTick;
import me.mohamad82.pensieve.utils.Utils;
import me.mohamad82.ruom.Ruom;
import me.mohamad82.ruom.event.PlayerUseItemEvent;
import me.mohamad82.ruom.utils.ServerVersion;
import me.mohamad82.ruom.utils.item.CrossbowUtils;
import me.mohamad82.ruom.xseries.XMaterial;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class PlayerUseItemListener extends PlayerUseItemEvent {

    private final Set<UUID> eatingPlayers = new HashSet<>();
    private final Map<UUID, ItemStack> crossbowUsers = new HashMap<>();
    private final Map<UUID, Integer> crossbowHoldTimes = new HashMap<>();
    private final Map<UUID, Integer> crossbowCurrentSound = new HashMap<>();

    public PlayerUseItemListener() {
        Ruom.runAsync(() -> {
            for (Player player : Ruom.getOnlinePlayers()) {
                if (crossbowHoldTimes.containsKey(player.getUniqueId()) && crossbowUsers.containsKey(player.getUniqueId())) {
                    Collection<PlayerRecordTick> collection = RecordManager.getInstance().getCurrentRecordTick(player);
                    if (collection.isEmpty()) return;

                    int holdTime = crossbowHoldTimes.get(player.getUniqueId()) + 1;
                    int currentSound = crossbowCurrentSound.get(player.getUniqueId());
                    crossbowHoldTimes.put(player.getUniqueId(), holdTime);
                    float drawn = CrossbowUtils.getPowerForTime(holdTime, crossbowUsers.get(player.getUniqueId()));

                    if (drawn > 0.5F) {
                        if (currentSound == 1) continue;
                        collection.forEach(playerRecordTick -> playerRecordTick.setCrossbowChargeLevel(1));
                        crossbowCurrentSound.put(player.getUniqueId(), 1);
                    } else if (drawn > 0.2F) {
                        if (currentSound == 0) continue;
                        collection.forEach(playerRecordTick -> playerRecordTick.setCrossbowChargeLevel(0));
                        crossbowCurrentSound.put(player.getUniqueId(), 0);
                    }
                }
            }
        }, 0, 1);
    }

    @Override
    protected void onStartUseItem(Player player, ItemStack itemStack, boolean isMainHand) {
        Collection<PlayerRecordTick> collection = RecordManager.getInstance().getCurrentRecordTick(player);
        if (collection.isEmpty()) return;

        if (itemStack.getType().isEdible() || Utils.isDrinkableItem(itemStack.getType())) {
            collection.forEach(playerRecordTick -> playerRecordTick.setEatingMaterial(itemStack.getType()));
            RecordManager.getInstance().getEatingPlayers().put(player, itemStack);
            eatingPlayers.add(player.getUniqueId());
        } else {
            collection.forEach(playerRecordTick -> playerRecordTick.useItemInteraction((byte) (isMainHand ? 1 : 2)));
            if (!crossbowUsers.containsKey(player.getUniqueId()) && ServerVersion.supports(14) && itemStack.getType() == XMaterial.CROSSBOW.parseMaterial()) {
                crossbowUsers.put(player.getUniqueId(), itemStack);
                crossbowHoldTimes.put(player.getUniqueId(), 1);
                crossbowCurrentSound.put(player.getUniqueId(), -1);
            }
        }
    }

    @Override
    protected void onStopUseItem(Player player, ItemStack itemStack, int holdTime) {
        if (eatingPlayers.contains(player.getUniqueId())) {
            RecordManager.getInstance().getEatingPlayers().remove(player);
            eatingPlayers.remove(player.getUniqueId());
            return;
        }
        Collection<PlayerRecordTick> collection = RecordManager.getInstance().getCurrentRecordTick(player);
        if (collection.isEmpty()) return;

        collection.forEach(playerRecordTick -> {
            playerRecordTick.useItemInteraction((byte) 3);
            playerRecordTick.setUsedItemTime(holdTime);
        });

        if (crossbowUsers.containsKey(player.getUniqueId())) {
            float drawn = CrossbowUtils.getPowerForTime(holdTime, crossbowUsers.get(player.getUniqueId()));
            collection.forEach(playerRecordTick -> {
                if (drawn > 1.0F) {
                    playerRecordTick.setCrossbowChargeLevel(2);
                }
                playerRecordTick.setCrossbowChargeLevel(2);
                playerRecordTick.drawCrossbow();
            });
            crossbowUsers.remove(player.getUniqueId());
            crossbowHoldTimes.remove(player.getUniqueId());
            crossbowCurrentSound.remove(player.getUniqueId());
        }
    }

}
