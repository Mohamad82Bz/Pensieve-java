package me.mohamad82.pensieve.recording.record;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import me.mohamad82.ruom.Ruom;
import me.mohamad82.ruom.math.vector.Vector3;
import me.mohamad82.ruom.math.vector.Vector3Utils;
import me.mohamad82.ruom.utils.GsonUtils;

import java.lang.reflect.Method;
import java.util.*;

public abstract class Record extends SerializableRecord {

    private RecordType type;
    private List<RecordTick> recordTicks = new ArrayList<>();
    private UUID uuid;
    private Vector3 center;
    private Vector3 startLocation;
    private int startingTick;
    private int length;

    private final Map<String, String> customDataMap = new HashMap<>();

    protected Record(RecordType type, UUID uuid, Vector3 center, int startingTick) {
        this.type = type;
        this.uuid = uuid;
        this.center = center;
        this.startingTick = startingTick;
    }

    protected Record() {

    }

    public RecordType getType() {
        return type;
    }

    public int getTotalTicks() {
        return recordTicks.size();
    }

    public UUID getUuid() {
        return uuid;
    }

    public Vector3 getCenter() {
        return center;
    }

    public Vector3 getStartLocation() {
        return startLocation;
    }

    public int getStartingTick() {
        return startingTick;
    }

    public int getLength() {
        return length;
    }

    public void setStartLocation(Vector3 startLocation) {
        this.startLocation = startLocation;
    }

    public void addRecordTick(RecordTick recordTick) {
        recordTicks.add(recordTick);
    }

    public ImmutableList<RecordTick> getRecordTicks() {
        return ImmutableList.copyOf(recordTicks);
    }

    public Map<String, String> getCustomDataMap() {
        return customDataMap;
    }

    public abstract RecordTick createRecordTick();

    public JsonObject toJson(JsonObject jsonObject) {
        jsonObject.addProperty("type", type.toString());
        jsonObject.addProperty("uuid", uuid.toString());
        jsonObject.addProperty("center", center.toString());
        jsonObject.addProperty("startlocation", startLocation.toString());
        jsonObject.addProperty("startingtick", startingTick);
        jsonObject.addProperty("length", recordTicks.size());

        if (!customDataMap.isEmpty()) {
            JsonObject customDataMapJson = new JsonObject();
            for (Map.Entry<String, String> entry : customDataMap.entrySet()) {
                customDataMapJson.addProperty(entry.getKey(), entry.getValue());
            }
            jsonObject.add("customdatamap", customDataMapJson);
        }

        try {
            Method recordTickToJsonMethod = type.getRecordTickClass().getMethod("toJson", JsonObject.class);
            JsonObject ticksJsonObject = new JsonObject();
            int tickIndex = 0;
            for (RecordTick tick : recordTicks) {
                JsonObject recordTickJsonObject = (JsonObject) recordTickToJsonMethod.invoke(tick, new JsonObject());
                if (recordTickJsonObject.size() != 0) {
                    ticksJsonObject.add(String.valueOf(tickIndex), recordTickJsonObject);
                }
                tickIndex++;
            }
            jsonObject.add("ticks", ticksJsonObject);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return jsonObject;
    }

    public Record fromJson(SerializableRecord serializableRecord, JsonObject jsonObject) {
        Record record = (Record) serializableRecord;

        record.type = RecordType.valueOf(jsonObject.get("type").getAsString());
        record.uuid = UUID.fromString(jsonObject.get("uuid").getAsString());
        record.center = Vector3Utils.toVector3(jsonObject.get("center").getAsString());
        record.startLocation = Vector3Utils.toVector3(jsonObject.get("startlocation").getAsString());
        record.startingTick = jsonObject.get("startingtick").getAsInt();
        record.length = jsonObject.get("length").getAsInt();

        try {
            Method recordTickFromJsonMethod = record.getType().getRecordTickClass().getMethod("fromJson", SerializableRecordTick.class, JsonObject.class);
            Object tickObject;
            JsonObject ticksJsonObject = jsonObject.get("ticks").getAsJsonObject();
            int tickIndex = 0;
            boolean hasNext = ticksJsonObject.has(String.valueOf(tickIndex));
            List<RecordTick> recordTicks = new ArrayList<>();
            while (hasNext) {
                tickObject = record.getType().getRecordTickClass().getConstructor().newInstance();
                if (ticksJsonObject.has(String.valueOf(tickIndex))) {
                    JsonObject tickJsonObject = ticksJsonObject.get(String.valueOf(tickIndex)).getAsJsonObject();
                    recordTicks.add((RecordTick) recordTickFromJsonMethod.invoke(tickObject, tickObject, tickJsonObject));
                } else {
                    recordTicks.add((RecordTick) tickObject);
                }

                hasNext = ticksJsonObject.has(String.valueOf(++tickIndex)) || tickIndex < record.length;
            }
            record.recordTicks = recordTicks;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return record;
    }

}
