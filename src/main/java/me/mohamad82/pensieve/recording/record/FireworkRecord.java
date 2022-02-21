package me.mohamad82.pensieve.recording.record;

import com.google.gson.JsonObject;
import me.mohamad82.ruom.npc.NPCType;
import me.mohamad82.ruom.utils.NMSUtils;
import me.mohamad82.ruom.vector.Vector3;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class FireworkRecord extends EntityRecord {

    private ItemStack fireworkItem;
    private boolean shotAtAngle;

    public FireworkRecord(UUID uuid, Vector3 center, int startingTick, ItemStack fireworkItem, boolean shotAtAngle) {
        super(RecordType.FIREWORK, uuid, center, NPCType.FIREWORK_ROCKET, startingTick);
        this.fireworkItem = fireworkItem;
        this.shotAtAngle = shotAtAngle;
    }

    protected FireworkRecord() {

    }

    public ItemStack getFireworkItem() {
        return fireworkItem;
    }

    public boolean isShotAtAngle() {
        return shotAtAngle;
    }

    @Override
    public FireworkRecordTick createRecordTick() {
        return new FireworkRecordTick();
    }

    public JsonObject toJson(JsonObject jsonObject) {
        jsonObject.addProperty("fireworkitem", NMSUtils.getItemStackNBTJson(fireworkItem));
        jsonObject.addProperty("shotatangle", shotAtAngle);

        return super.toJson(jsonObject);
    }

    public FireworkRecord fromJson(SerializableRecord record, JsonObject jsonObject) {
        FireworkRecord fireworkRecord = (FireworkRecord) record;

        fireworkRecord.fireworkItem = NMSUtils.getItemStackFromNBTJson(jsonObject.get("fireworkitem").getAsString());
        fireworkRecord.shotAtAngle = jsonObject.get("shotatangle").getAsBoolean();

        return (FireworkRecord) super.fromJson(record, jsonObject);
    }

}
