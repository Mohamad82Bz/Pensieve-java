package me.mohamad82.pensieve.recording.record;

import com.google.gson.JsonObject;
import me.mohamad82.ruom.npc.NPCType;
import me.mohamad82.ruom.utils.NMSUtils;
import me.mohamad82.ruom.vector.Vector3;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class ProjectileRecord extends EntityRecord {

    private ItemStack projectileItem;

    public ProjectileRecord(UUID uuid, Vector3 center, int startingTick, ItemStack projectileItem) {
        super(RecordType.PROJECTILE, uuid, center, NPCType.POTION, startingTick);
        this.projectileItem = projectileItem;
    }

    protected ProjectileRecord() {

    }

    public ItemStack getProjectileItem() {
        return projectileItem;
    }

    @Override
    public RecordTick createRecordTick() {
        return new ProjectileRecordTick();
    }

    public JsonObject toJson(JsonObject jsonObject) {
        jsonObject.addProperty("projectileitem", NMSUtils.getItemStackNBTJson(projectileItem));

        return super.toJson(jsonObject);
    }

    public ProjectileRecord fromJson(SerializableRecord record, JsonObject jsonObject) {
        ProjectileRecord projectileRecord = (ProjectileRecord) record;

        projectileRecord.projectileItem = NMSUtils.getItemStackFromNBTJson(jsonObject.get("projectileitem").getAsString());

        return (ProjectileRecord) super.fromJson(record, jsonObject);
    }

}
