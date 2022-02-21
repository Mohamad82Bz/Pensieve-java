package me.mohamad82.pensieve.recording.record;

import com.google.gson.JsonObject;

public abstract class EntityRecordTick extends RecordTick {

    @Override
    public JsonObject toJson(JsonObject jsonObject) {
        return super.toJson(jsonObject);
    }

    public EntityRecordTick fromJson(SerializableRecordTick recordTick, JsonObject jsonObject) {
        return (EntityRecordTick) super.fromJson(recordTick, jsonObject);
    }

}
