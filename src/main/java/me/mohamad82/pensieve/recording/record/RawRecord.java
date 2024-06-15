package me.mohamad82.pensieve.recording.record;

import com.google.gson.JsonObject;
import me.mohamad82.ruom.math.vector.Vector3;

import java.util.UUID;

public class RawRecord extends Record {

    public RawRecord(UUID uuid, Vector3 center, int startingTick) {
        super(RecordType.RAW, uuid, center, startingTick);
    }

    protected RawRecord() {

    }

    @Override
    public RecordTick createRecordTick() {
        return new RawRecordTick();
    }

    public JsonObject toJson(JsonObject jsonObject) {
        return super.toJson(jsonObject);
    }

    public RawRecord fromJson(SerializableRecord record, JsonObject jsonObject) {
        RawRecord entityRecord = (RawRecord) record;

        return (RawRecord) super.fromJson(record, jsonObject);
    }

}
