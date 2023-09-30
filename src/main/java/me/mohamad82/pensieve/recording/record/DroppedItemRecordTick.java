package me.mohamad82.pensieve.recording.record;

import com.google.gson.JsonObject;
import me.mohamad82.pensieve.utils.Utils;

public class DroppedItemRecordTick extends EntityRecordTick {

    private int itemAmount = -1;

    public int getItemAmount() {
        return itemAmount;
    }

    public void setItemAmount(int itemAmount) {
        this.itemAmount = itemAmount;
    }

    @Override
    public DroppedItemRecordTick copy() {
        return Utils.copy(this, new DroppedItemRecordTick());
    }

    @Override
    public JsonObject toJson(JsonObject jsonObject) {
        if (itemAmount != -1) {
            jsonObject.addProperty("ia", itemAmount);
        }

        return super.toJson(jsonObject);
    }

    public DroppedItemRecordTick fromJson(SerializableRecordTick recordTick, JsonObject jsonObject) {
        DroppedItemRecordTick tick = (DroppedItemRecordTick) recordTick;

        if (jsonObject.has("ia")) {
            tick.itemAmount = jsonObject.get("ia").getAsInt();
        }

        return (DroppedItemRecordTick) super.fromJson(tick, jsonObject);
    }

}
