package me.mohamad82.pensieve.recording.record;

import com.google.gson.JsonObject;
import me.mohamad82.pensieve.utils.Utils;

public class ProjectileRecordTick extends EntityRecordTick {

    @Override
    public ProjectileRecordTick copy() {
        return Utils.copy(this, new ProjectileRecordTick());
    }

    @Override
    public JsonObject toJson(JsonObject jsonObject) {
        return super.toJson(jsonObject);
    }

    public ProjectileRecordTick fromJson(SerializableRecordTick recordTick, JsonObject jsonObject) {
        return (ProjectileRecordTick) super.fromJson(recordTick, jsonObject);
    }

    public ProjectileRecordTick fromJson(JsonObject jsonObject) {
        return fromJson(new ProjectileRecordTick(), jsonObject);
    }

}
