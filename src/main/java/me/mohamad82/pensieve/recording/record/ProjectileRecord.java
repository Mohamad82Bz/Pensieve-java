package me.mohamad82.pensieve.recording.record;

import me.mohamad82.ruom.npc.NPCType;
import me.mohamad82.ruom.vector.Vector3;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class ProjectileRecord extends EntityRecord {

    private final ItemStack projectileItem;

    public ProjectileRecord(UUID uuid, Vector3 center, int startingTick, ItemStack projectileItem) {
        super(uuid, center, NPCType.POTION, startingTick);
        this.projectileItem = projectileItem;
    }

    public ItemStack getProjectileItem() {
        return projectileItem;
    }

    @Override
    public RecordTick createRecordTick() {
        return new ProjectileRecordTick();
    }

}
