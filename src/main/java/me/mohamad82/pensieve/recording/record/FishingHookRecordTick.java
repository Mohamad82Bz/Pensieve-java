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
            jsonObject.addProperty("he", hookedEntity.toString());
        }

        return super.toJson(jsonObject);
    }

    public FishingHookRecordTick fromJson(SerializableRecordTick recordTick, JsonObject jsonObject) {
        FishingHookRecordTick tick = (FishingHookRecordTick) recordTick;

        if (jsonObject.has("he")) {
            tick.hookedEntity = UUID.fromString(jsonObject.get("he").getAsString());
        }

        return (FishingHookRecordTick) super.fromJson(tick, jsonObject);
    }

}
