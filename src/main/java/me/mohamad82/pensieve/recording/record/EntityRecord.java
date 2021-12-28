package me.mohamad82.pensieve.recording.record;

import me.mohamad82.ruom.npc.NPCType;
import me.mohamad82.ruom.vector.Vector3;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public abstract class EntityRecord extends Record {

    private final NPCType entityType;
    private final int startingTick;

    protected EntityRecord(UUID uuid, Vector3 center, NPCType entityType, int startingTick) {
        super(uuid, center);
        this.entityType = entityType;
        this.startingTick = startingTick;
    }

    public NPCType getEntityType() {
        return entityType;
    }

    public int getStartingTick() {
        return startingTick;
    }

}
