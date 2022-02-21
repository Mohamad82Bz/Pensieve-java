package me.mohamad82.pensieve.serializer;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.mohamad82.pensieve.recording.RecordContainer;
import me.mohamad82.pensieve.recording.record.*;
import me.mohamad82.pensieve.recording.record.Record;
import me.mohamad82.ruom.utils.GsonUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class PensieveGsonSerializer {

    private static final PensieveGsonSerializer INSTANCE = new PensieveGsonSerializer();

    public static PensieveGsonSerializer get() {
        return INSTANCE;
    }

    private PensieveGsonSerializer() {

    }

    public String serialize(RecordContainer recordContainer, boolean prettyPrinter) {
        Set<Record> records = new HashSet<>();
        records.addAll(recordContainer.getPlayerRecords());
        records.addAll(recordContainer.getEntityRecords());

        JsonObject jsonObject = new JsonObject();
        JsonArray recordArray = new JsonArray();

        for (Record record : records) {
            try {
                Method recordToJsonMethod = record.getType().getRecordClass().getMethod("toJson", JsonObject.class);

                recordArray.add((JsonObject) recordToJsonMethod.invoke(record, new JsonObject()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        jsonObject.add("records", recordArray);

        return prettyPrinter ? GsonUtils.getPrettyPrinter().toJson(jsonObject) : GsonUtils.get().toJson(jsonObject);
    }

    public String serialize(RecordContainer recordContainer) {
        return serialize(recordContainer, false);
    }

    public RecordContainer deserialize(String jsonString) {
        RecordContainer recordContainer = RecordContainer.createEmptyContainer();

        JsonObject jsonObject = GsonUtils.getParser().parse(jsonString).getAsJsonObject();
        JsonArray recordsArray = jsonObject.get("records").getAsJsonArray();

        for (int i = 0; i < recordsArray.size(); i++) {
            JsonObject recordJsonObject = recordsArray.get(i).getAsJsonObject();
            RecordType recordType = RecordType.valueOf(recordJsonObject.get("type").getAsString());
            try {
                Method recordFromJsonMethod = recordType.getRecordClass().getMethod("fromJson", SerializableRecord.class, JsonObject.class);
                Record record = recordType.createEmptyObject();
                recordFromJsonMethod.invoke(record, record, recordJsonObject);

                if (recordType == RecordType.PLAYER) {
                    recordContainer.getPlayerRecords().add((PlayerRecord) record);
                } else {
                    recordContainer.getEntityRecords().add((EntityRecord) record);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return recordContainer;
    }

    public RecordContainer deserialize(File file) {
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            return deserialize(bufferedReader.readLine());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
