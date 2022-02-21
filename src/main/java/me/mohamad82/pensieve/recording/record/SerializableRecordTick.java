package me.mohamad82.pensieve.recording.record;

import com.google.gson.JsonObject;

public abstract class SerializableRecordTick {

    public abstract JsonObject toJson(JsonObject jsonObject);

    public abstract SerializableRecordTick fromJson(SerializableRecordTick recordTick, JsonObject jsonObject);

}
