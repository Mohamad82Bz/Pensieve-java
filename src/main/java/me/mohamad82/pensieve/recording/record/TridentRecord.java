package me.mohamad82.pensieve.recording.record;

import com.google.gson.JsonObject;
import me.mohamad82.ruom.npc.NPCType;
import me.mohamad82.ruom.vector.Vector3;

import java.util.UUID;

public class TridentRecord extends EntityRecord {

    private byte loyalty;
    private boolean enchanted;
    private UUID pickedBy;

    public TridentRecord(UUID uuid, Vector3 center, int startingTick, byte loyalty, boolean enchanted) {
        super(RecordType.TRIDENT, uuid, center, NPCType.TRIDENT, startingTick);
        this.loyalty = loyalty;
        this.enchanted = enchanted;
    }

    protected TridentRecord() {

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

    public JsonObject toJson(JsonObject jsonObject) {
        jsonObject.addProperty("loyalty", loyalty);
        jsonObject.addProperty("enchanted", enchanted);

        if (pickedBy != null) {
            jsonObject.addProperty("pickedby", pickedBy.toString());
        }

        return super.toJson(jsonObject);
    }

    public TridentRecord fromJson(Record record, JsonObject jsonObject) {
        TridentRecord tridentRecord = (TridentRecord) record;

        tridentRecord.loyalty = jsonObject.get("loyalty").getAsByte();
        tridentRecord.enchanted = jsonObject.get("enchanted").getAsBoolean();
        if (jsonObject.has("pickedby")) {
            tridentRecord.pickedBy = UUID.fromString(jsonObject.get("pickedby").getAsString());
        }

        return (TridentRecord) super.fromJson(record, jsonObject);
    }

}
