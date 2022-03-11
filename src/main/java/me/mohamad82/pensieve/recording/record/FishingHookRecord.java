package me.mohamad82.pensieve.recording.record;

import com.google.gson.JsonObject;
import me.mohamad82.ruom.npc.NPCType;
import me.mohamad82.ruom.math.vector.Vector3;

import java.util.UUID;

public class FishingHookRecord extends EntityRecord {

    private UUID owner;

    public FishingHookRecord(UUID uuid, Vector3 center, int startingTick, UUID owner) {
        super(RecordType.FISHING_HOOK, uuid, center, NPCType.FISHING_BOBBER, startingTick);
        this.owner = owner;
    }

    protected FishingHookRecord() {

    }

    public UUID getOwner() {
        return owner;
    }

    @Override
    public RecordTick createRecordTick() {
        return new FishingHookRecordTick();
    }

    public JsonObject toJson(JsonObject jsonObject) {
        jsonObject.addProperty("owner", owner.toString());

        return super.toJson(jsonObject);
    }

    public FishingHookRecord fromJson(SerializableRecord record, JsonObject jsonObject) {
        FishingHookRecord fishingHookRecord = (FishingHookRecord) record;

        fishingHookRecord.owner = UUID.fromString(jsonObject.get("owner").getAsString());

        return (FishingHookRecord) super.fromJson(record, jsonObject);
    }

}
