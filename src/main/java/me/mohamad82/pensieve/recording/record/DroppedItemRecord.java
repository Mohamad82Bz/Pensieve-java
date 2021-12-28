package me.mohamad82.pensieve.recording.record;

import me.mohamad82.ruom.npc.NPCType;
import me.mohamad82.ruom.vector.Vector3;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class DroppedItemRecord extends EntityRecord {

    private final ItemStack item;
    private UUID pickedBy;

    public DroppedItemRecord(UUID uuid, Vector3 center, int startingTick, ItemStack item) {
        super(uuid, center, NPCType.ITEM, startingTick);
        this.item = item;
    }

    public UUID getPickedBy() {
        return pickedBy;
    }

    public void setPickedBy(UUID pickedBy) {
        this.pickedBy = pickedBy;
    }

    public ItemStack getItem() {
        return item;
    }

    @Override
    public RecordTick createRecordTick() {
        return new DroppedItemRecordTick();
    }

}
