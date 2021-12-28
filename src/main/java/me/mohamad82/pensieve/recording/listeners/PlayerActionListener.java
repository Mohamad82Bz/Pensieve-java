package me.mohamad82.pensieve.recording.listeners;

import me.mohamad82.pensieve.recording.PendingBlockBreak;
import me.mohamad82.pensieve.recording.RecordManager;
import me.mohamad82.pensieve.recording.record.PlayerRecordTick;
import me.mohamad82.pensieve.recording.record.RecordTick;
import me.mohamad82.ruom.event.packet.PlayerActionEvent;
import me.mohamad82.ruom.vector.Vector3;
import me.mohamad82.ruom.vector.Vector3Utils;
import org.bukkit.entity.Player;

public class PlayerActionListener extends PlayerActionEvent {

    @Override
    protected void onStartDig(Player player, Vector3 blockPos, Direction direction) {
        RecordTick recordTick = RecordManager.getInstance().getCurrentRecordTick(player);
        if (recordTick == null) return;
        PlayerRecordTick playerRecordTick = (PlayerRecordTick) recordTick;

        final PendingBlockBreak pendingBlockBreak = new PendingBlockBreak(
                blockPos,
                PendingBlockBreak.BlockDirection.valueOf(direction.toString().toUpperCase()),
                Vector3Utils.toLocation(player.getWorld(), blockPos).getBlock().getType()
        );

        playerRecordTick.setPendingBlockBreak(pendingBlockBreak);
        RecordManager.getInstance().getBreakingPlayers().put(player, pendingBlockBreak);
    }

    @Override
    protected void onStopDig(Player player, Vector3 blockPos) {
        RecordManager.getInstance().getBreakingPlayers().remove(player);
    }

    @Override
    protected void onUseItemRelease(Player player) {
        RecordManager.getInstance().getEatingPlayers().remove(player);
    }

    @Override
    protected void onDropAllItems(Player player) {

    }

    @Override
    protected void onDropItem(Player player) {

    }

    @Override
    protected void onSwapItemsWithOffHand(Player player) {

    }

}
