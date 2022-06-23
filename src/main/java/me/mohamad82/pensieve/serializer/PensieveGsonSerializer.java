package me.mohamad82.pensieve.serializer;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.mohamad82.pensieve.recording.RecordContainer;
import me.mohamad82.pensieve.recording.record.Record;
import me.mohamad82.pensieve.recording.record.*;
import me.mohamad82.ruom.Ruom;
import me.mohamad82.ruom.utils.GsonUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

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
    public void serialize(File file, RecordContainer recordContainer, boolean compress) throws IOException {
        if (file.isDirectory()) {
            throw new IllegalArgumentException("Given file is a directory (folder).");
        }
        file.createNewFile();

        if (compress) {
            FileUtils.writeByteArrayToFile(file, compress(serialize(recordContainer)));
        } else {
            FileUtils.writeStringToFile(file, serialize(recordContainer), StandardCharsets.UTF_8);
        }
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

    public RecordContainer deserialize(File file, boolean compressed) throws IOException {
        if (compressed) {
            return deserialize(decompress(FileUtils.readFileToByteArray(file)));
        } else {
            return deserialize(FileUtils.readFileToString(file, StandardCharsets.UTF_8));
        }
    }

    private byte[] compress(String json) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream);
        gzipOutputStream.write(json.getBytes(StandardCharsets.UTF_8));
        gzipOutputStream.close();
        return byteArrayOutputStream.toByteArray();
    }

    private String decompress(byte[] bytes) throws IOException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        GZIPInputStream gzipInputStream = new GZIPInputStream(byteArrayInputStream);
        return IOUtils.toString(gzipInputStream, StandardCharsets.UTF_8);
    }

}
