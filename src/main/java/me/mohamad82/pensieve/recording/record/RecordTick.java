package me.mohamad82.pensieve.recording.record;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.mohamad82.ruom.math.vector.Vector3;
import me.mohamad82.ruom.math.vector.Vector3Utils;

import java.util.HashMap;
import java.util.Map;

public abstract class RecordTick extends SerializableRecordTick {

    private Map<String, String> customDataMap = new HashMap<>();

    private Vector3 location;
    private Vector3 velocity;

    private float yaw = -1;
    private float pitch = -1;

    private int effectColor = -1;

    protected RecordTick() {

    }

    public Map<String, String> getCustomDataMap() {
        return customDataMap;
    }

    public Vector3 getLocation() {
        return location;
    }

    public void setLocation(Vector3 location) {
        this.location = location;
    }

    public Vector3 getVelocity() {
        return velocity;
    }

    public void setVelocity(Vector3 velocity) {
        this.velocity = velocity;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public int getEffectColor() {
        return effectColor;
    }

    public void setEffectColor(int effectColor) {
        this.effectColor = effectColor;
    }

    public abstract RecordTick copy();

    public JsonObject toJson(JsonObject jsonObject) {
        if (!customDataMap.isEmpty()) {
            JsonObject customDataMapJson = new JsonObject();
            for (Map.Entry<String, String> entry : customDataMap.entrySet()) {
                customDataMapJson.addProperty(entry.getKey(), entry.getValue());
            }
            jsonObject.add("customdatamap", customDataMapJson);
        }
        if (location != null)
            jsonObject.addProperty("l", location.toString());
        if (velocity != null)
            jsonObject.addProperty("v", velocity.toString());
        if (yaw != -1)
            jsonObject.addProperty("y", yaw);
        if (pitch != -1)
            jsonObject.addProperty("p", pitch);
        if (effectColor != -1)
            jsonObject.addProperty("e", effectColor);

        return jsonObject;
    }

    public RecordTick fromJson(SerializableRecordTick serializableRecordTick, JsonObject jsonObject) {
        RecordTick tick = (RecordTick) serializableRecordTick;
        if (jsonObject.has("customdatamap")) {
            JsonObject customDataMapJson = jsonObject.get("customdatamap").getAsJsonObject();
            Map<String, String> customDataMap = new HashMap<>();
            for (Map.Entry<String, JsonElement> entry : customDataMapJson.entrySet()) {
                customDataMap.put(entry.getKey(), entry.getValue().getAsString());
            }
            tick.customDataMap = customDataMap;
        }
        if (jsonObject.has("l")) {
            tick.location = Vector3Utils.toVector3(jsonObject.get("l").getAsString());
        }
        if (jsonObject.has("v")) {
            tick.velocity = Vector3Utils.toVector3(jsonObject.get("v").getAsString());
        }
        if (jsonObject.has("y")) {
            tick.yaw = jsonObject.get("y").getAsFloat();
        }
        if (jsonObject.has("p")) {
            tick.pitch = jsonObject.get("p").getAsFloat();
        }
        if (jsonObject.has("e")) {
            tick.effectColor = jsonObject.get("e").getAsInt();
        }

        return tick;
    }
    
}
