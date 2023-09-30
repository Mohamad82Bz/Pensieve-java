package me.mohamad82.pensieve.recording.record;

import com.google.gson.JsonObject;
import me.mohamad82.pensieve.utils.Utils;

public class TridentRecordTick extends EntityRecordTick {

    private boolean attachedBlock;
    private boolean returning;

    public boolean hasAttachedBlock() {
        return attachedBlock;
    }

    public void setAttachedBlock() {
        this.attachedBlock = true;
    }

    public boolean isReturning() {
        return returning;
    }

    public void setReturning() {
        this.returning = true;
    }

    @Override
    public RecordTick copy() {
        return Utils.copy(this, new TridentRecordTick());
    }

    @Override
    public JsonObject toJson(JsonObject jsonObject) {
        if (attachedBlock) {
            jsonObject.addProperty("attachedblock", true);
        }
        if (returning) {
            jsonObject.addProperty("returning", true);
        }

        return super.toJson(jsonObject);
    }

    public TridentRecordTick fromJson(SerializableRecordTick recordTick, JsonObject jsonObject) {
        TridentRecordTick tick = (TridentRecordTick) recordTick;

        if (jsonObject.has("attachedblock")) {
            tick.attachedBlock = jsonObject.get("attachedblock").getAsBoolean();
        }
        if (jsonObject.has("returning")) {
            tick.returning = jsonObject.get("returning").getAsBoolean();
        }

        return (TridentRecordTick) super.fromJson(recordTick, jsonObject);
    }

}
