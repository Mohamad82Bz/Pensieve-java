package me.mohamad82.pensieve.recording.record;

import me.mohamad82.ruom.npc.NPCType;
import me.mohamad82.ruom.vector.Vector3;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class ArrowRecord extends EntityRecord {

    private final int color;
    private UUID pickedBy;

    public ArrowRecord(UUID uuid, Vector3 center, int startingTick, int color) {
        super(uuid, center, NPCType.ARROW, startingTick);
        this.color = color;
    }

    public int getColor() {
        return color;
    }

    public UUID getPickedBy() {
        return pickedBy;
    }

    public void setPickedBy(UUID pickedBy) {
        this.pickedBy = pickedBy;
    }

    @Override
    public RecordTick createRecordTick() {
        return new ArrowRecordTick();
    }

}
