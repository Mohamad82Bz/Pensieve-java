package me.mohamad82.pensieve.recording.record;

import com.google.gson.JsonObject;
import me.mohamad82.ruom.npc.NPCType;
import me.mohamad82.ruom.math.vector.Vector3;

import java.util.UUID;

public class AreaEffectCloudRecord extends EntityRecord {

    private int color;

    public AreaEffectCloudRecord(UUID uuid, Vector3 center, int startingTick, int color) {
        super(RecordType.AREA_EFFECT_CLOUD, uuid, center, NPCType.AREA_EFFECT_CLOUD, startingTick);
        this.color = color;
    }

    protected AreaEffectCloudRecord() {

    }

    public int getColor() {
        return color;
    }

    @Override
    public RecordTick createRecordTick() {
        return new AreaEffectCloudRecordTick();
    }

    public JsonObject toJson(JsonObject jsonObject) {
        jsonObject.addProperty("color", color);

        return super.toJson(jsonObject);
    }

    public AreaEffectCloudRecord fromJson(SerializableRecord record, JsonObject jsonObject) {
        AreaEffectCloudRecord areaEffectCloudRecord = (AreaEffectCloudRecord) record;

        areaEffectCloudRecord.color = jsonObject.get("color").getAsInt();

        return (AreaEffectCloudRecord) super.fromJson(record, jsonObject);
    }

}
