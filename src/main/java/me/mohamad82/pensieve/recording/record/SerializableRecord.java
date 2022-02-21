package me.mohamad82.pensieve.recording.record;

import com.google.gson.JsonObject;

public abstract class SerializableRecord {

    public abstract JsonObject toJson(JsonObject jsonObject);

    public abstract SerializableRecord fromJson(SerializableRecord record, JsonObject jsonObject);

}
