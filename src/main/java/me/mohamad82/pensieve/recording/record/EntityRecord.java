package me.mohamad82.pensieve.recording.record;

import com.google.gson.JsonObject;
import me.mohamad82.ruom.npc.NPCType;
import me.mohamad82.ruom.vector.Vector3;

import java.util.UUID;

public abstract class EntityRecord extends Record {

    private NPCType entityType;
    private int startingTick;

    public EntityRecord(RecordType type, UUID uuid, Vector3 center, NPCType entityType, int startingTick) {
        super(type, uuid, center);
        this.entityType = entityType;
        this.startingTick = startingTick;
    }

    protected EntityRecord() {

    }

    public NPCType getEntityType() {
        return entityType;
    }

    public int getStartingTick() {
        return startingTick;
    }

    public JsonObject toJson(JsonObject jsonObject) {
        jsonObject.addProperty("entitytype", entityType.toString());
        jsonObject.addProperty("startingtick", startingTick);

        return super.toJson(jsonObject);
    }

    public EntityRecord fromJson(SerializableRecord record, JsonObject jsonObject) {
        EntityRecord entityRecord = (EntityRecord) record;

        entityRecord.entityType = NPCType.valueOf(jsonObject.get("entitytype").getAsString());
        entityRecord.startingTick = jsonObject.get("startingtick").getAsInt();

        return (EntityRecord) super.fromJson(record, jsonObject);
    }

}
