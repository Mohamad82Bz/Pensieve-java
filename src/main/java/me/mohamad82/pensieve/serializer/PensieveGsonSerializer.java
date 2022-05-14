package me.mohamad82.pensieve.serializer;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.mohamad82.pensieve.recording.RecordContainer;
import me.mohamad82.pensieve.recording.record.Record;
import me.mohamad82.pensieve.recording.record.*;
import me.mohamad82.ruom.utils.GsonUtils;

import java.io.*;
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

    public String serialize(RecordContainer recordContainer) {
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

        return GsonUtils.get().toJson(jsonObject);
    }

    /**
     * Serializes and saves a RecordContainer to a file. Note that the file will be re-created.
     * @param file The file that the RecordContainer is going to be saved in.
     * @param recordContainer The RecordContainer
     * @throws IllegalArgumentException If the given file was a directory (folder).
     * @throws IOException If the operation fails for file-related issues.
     */
    public void serialize(File file, RecordContainer recordContainer) throws IOException {
        if (file.isDirectory()) {
            throw new IllegalArgumentException("Given file is a directory (folder).");
        }
        file.createNewFile();
        FileWriter writer = new FileWriter(file);
        writer.write(serialize(recordContainer));
        writer.flush();
        writer.close();
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

    public RecordContainer deserialize(File file) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        return deserialize(bufferedReader.readLine());
    }

}
