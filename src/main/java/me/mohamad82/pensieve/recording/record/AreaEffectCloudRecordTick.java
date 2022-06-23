package me.mohamad82.pensieve.recording.record;

import com.google.gson.JsonObject;
import me.mohamad82.pensieve.utils.Utils;

public class AreaEffectCloudRecordTick extends EntityRecordTick {

    private float radius;

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public RecordTick copy() {
        return Utils.copy(this, new AreaEffectCloudRecordTick());
    }

    public JsonObject toJson(JsonObject jsonObject) {
        jsonObject.addProperty("r", radius);

        return super.toJson(jsonObject);
    }

    public AreaEffectCloudRecordTick fromJson(SerializableRecordTick recordTick, JsonObject jsonObject) {
        AreaEffectCloudRecordTick tick = (AreaEffectCloudRecordTick) recordTick;

        tick.radius = jsonObject.get("r").getAsFloat();

        return (AreaEffectCloudRecordTick) super.fromJson(tick, jsonObject);
    }

}
