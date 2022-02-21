package me.mohamad82.pensieve.recording.record;

import com.google.gson.JsonObject;
import me.mohamad82.pensieve.utils.Utils;

import java.util.UUID;

public class FishingHookRecordTick extends EntityRecordTick {

    private UUID hookedEntity;

    public UUID getHookedEntity() {
        return hookedEntity;
    }

    public void setHookedEntity(UUID hookedEntityLocation) {
        this.hookedEntity = hookedEntityLocation;
    }

    @Override
    public RecordTick copy() {
        return Utils.copy(this, new FishingHookRecordTick());
    }

    @Override
    public JsonObject toJson(JsonObject jsonObject) {
        if (hookedEntity != null) {
            jsonObject.addProperty("hookedentity", hookedEntity.toString());
        }

        return super.toJson(jsonObject);
    }

    public FishingHookRecordTick fromJson(SerializableRecordTick recordTick, JsonObject jsonObject) {
        FishingHookRecordTick tick = (FishingHookRecordTick) recordTick;

        if (jsonObject.has("hooked1ntity")) {
            tick.hookedEntity = UUID.fromString(jsonObject.get("hookedentity").getAsString());
        }

        return (FishingHookRecordTick) super.fromJson(tick, jsonObject);
    }

    public FishingHookRecordTick fromJson(JsonObject jsonObject) {
        return fromJson(new FishingHookRecordTick(), jsonObject);
    }

}
