package me.mohamad82.pensieve.recording.record;

import com.google.gson.JsonObject;
import me.mohamad82.ruom.npc.NPCType;
import me.mohamad82.ruom.utils.NMSUtils;
import me.mohamad82.ruom.math.vector.Vector3;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class DroppedItemRecord extends EntityRecord {

    private ItemStack item;
    private UUID pickedBy;

    public DroppedItemRecord(UUID uuid, Vector3 center, int startingTick, ItemStack item) {
        super(RecordType.DROPPED_ITEM, uuid, center, NPCType.ITEM, startingTick);
        this.item = item;
    }

    protected DroppedItemRecord() {

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

    public JsonObject toJson(JsonObject jsonObject) {
        jsonObject.addProperty("item", NMSUtils.getItemStackNBTJson(item));

        if (pickedBy != null) {
            jsonObject.addProperty("pickedby", pickedBy.toString());
        }

        return super.toJson(jsonObject);
    }

    public DroppedItemRecord fromJson(SerializableRecord record, JsonObject jsonObject) {
        DroppedItemRecord droppedItemRecord = (DroppedItemRecord) record;

        droppedItemRecord.item = NMSUtils.getItemStackFromNBTJson(jsonObject.get("item").getAsString());
        if (jsonObject.has("pickedby")) {
            droppedItemRecord.pickedBy = UUID.fromString(jsonObject.get("pickedby").getAsString());
        }

        return (DroppedItemRecord) super.fromJson(record, jsonObject);
    }

}
