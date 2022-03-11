package me.mohamad82.pensieve.recording.record;

import com.google.gson.JsonObject;
import me.mohamad82.ruom.npc.NPCType;
import me.mohamad82.ruom.math.vector.Vector3;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class ArrowRecord extends EntityRecord {

    private int color;
    private UUID pickedBy;

    public ArrowRecord(UUID uuid, Vector3 center, int startingTick, int color) {
        super(RecordType.ARROW, uuid, center, NPCType.ARROW, startingTick);
        this.color = color;
    }

    protected ArrowRecord() {

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

    public JsonObject toJson(JsonObject jsonObject) {
        jsonObject.addProperty("color", color);

        if (pickedBy != null) {
            jsonObject.addProperty("pickedby", pickedBy.toString());
        }

        return super.toJson(jsonObject);
    }

    public ArrowRecord fromJson(SerializableRecord record, JsonObject jsonObject) {
        ArrowRecord arrowRecord = (ArrowRecord) record;

        arrowRecord.color = jsonObject.get("color").getAsInt();
        if (jsonObject.has("pickedby")) {
            arrowRecord.pickedBy = UUID.fromString(jsonObject.get("pickedby").getAsString());
        }

        return (ArrowRecord) super.fromJson(record, jsonObject);
    }

}
