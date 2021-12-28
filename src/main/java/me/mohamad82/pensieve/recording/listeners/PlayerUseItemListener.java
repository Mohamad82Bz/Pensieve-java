package me.mohamad82.pensieve.recording.listeners;

import me.mohamad82.pensieve.recording.RecordManager;
import me.mohamad82.pensieve.recording.record.PlayerRecordTick;
import me.mohamad82.pensieve.recording.record.RecordTick;
import me.mohamad82.ruom.event.PlayerUseItemEvent;
import me.mohamad82.ruom.utils.PlayerUtils;
import me.mohamad82.ruom.utils.ServerVersion;
import me.mohamad82.ruom.xseries.XMaterial;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PlayerUseItemListener extends PlayerUseItemEvent {

    @Override
    protected void onStartUseItem(Player player, ItemStack itemStack, boolean isMainHand) {
        RecordTick recordTick = RecordManager.getInstance().getCurrentRecordTick(player);
        if (recordTick == null) return;
        PlayerRecordTick playerRecordTick = (PlayerRecordTick) recordTick;

        playerRecordTick.useItemInteraction((byte) (isMainHand ? 1 : 2));
    }

    @Override
    protected void onStopUseItem(Player player, ItemStack itemStack, float holdTime) {
        RecordTick recordTick = RecordManager.getInstance().getCurrentRecordTick(player);
        if (recordTick == null) return;
        PlayerRecordTick playerRecordTick = (PlayerRecordTick) recordTick;

        playerRecordTick.useItemInteraction((byte) 3);
        playerRecordTick.setUsedItemTime(Math.round(holdTime));
        if (ServerVersion.supports(14) && PlayerUtils.hasItemInHand(player, XMaterial.CROSSBOW.parseMaterial())) {
            playerRecordTick.drawCrossbow();
        }
    }

}
