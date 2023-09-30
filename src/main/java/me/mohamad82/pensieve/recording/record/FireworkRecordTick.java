package me.mohamad82.pensieve.recording.record;

import com.google.gson.JsonObject;
import me.mohamad82.pensieve.utils.Utils;

public class FireworkRecordTick extends EntityRecordTick {

    @Override
    public RecordTick copy() {
        return Utils.copy(this, new FireworkRecordTick());
    }

    @Override
    public JsonObject toJson(JsonObject jsonObject) {
        return super.toJson(jsonObject);
    }

    public FireworkRecordTick fromJson(SerializableRecordTick recordTick, JsonObject jsonObject) {
        return (FireworkRecordTick) super.fromJson(recordTick, jsonObject);
    }

}
