package me.mohamad82.pensieve.recording.record;

import me.mohamad82.ruom.npc.NPCType;
import me.mohamad82.ruom.vector.Vector3;

import java.util.UUID;

public class TridentRecord extends EntityRecord {

    private final byte loyalty;
    private final boolean enchanted;
    private UUID pickedBy;

    public TridentRecord(UUID uuid, Vector3 center, int startingTick, byte loyalty, boolean enchanted) {
        super(uuid, center, NPCType.TRIDENT, startingTick);
        this.loyalty = loyalty;
        this.enchanted = enchanted;
    }

    public byte getLoyalty() {
        return loyalty;
    }

    public boolean isEnchanted() {
        return enchanted;
    }

    public UUID getPickedBy() {
        return pickedBy;
    }

    public void setPickedBy(UUID pickedBy) {
        this.pickedBy = pickedBy;
    }

    @Override
    public RecordTick createRecordTick() {
        return new TridentRecordTick();
    }

}
