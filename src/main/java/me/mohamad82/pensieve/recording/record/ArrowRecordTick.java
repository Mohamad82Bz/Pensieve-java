package me.mohamad82.pensieve.recording.record;

import com.google.gson.JsonObject;
import me.mohamad82.pensieve.utils.Utils;

public class ArrowRecordTick extends EntityRecordTick {

    @Override
    public ArrowRecordTick copy() {
        return Utils.copy(this, new ArrowRecordTick());
    }

    @Override
    public JsonObject toJson(JsonObject jsonObject) {
        return super.toJson(jsonObject);
    }

    public ArrowRecordTick fromJson(SerializableRecordTick recordTick, JsonObject jsonObject) {
        return (ArrowRecordTick) super.fromJson(recordTick, jsonObject);
    }

    public ArrowRecordTick fromJson(JsonObject jsonObject) {
        return fromJson(new ArrowRecordTick(), jsonObject);
    }

}
