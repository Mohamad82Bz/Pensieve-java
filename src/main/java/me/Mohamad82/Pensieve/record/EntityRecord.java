package me.Mohamad82.Pensieve.record;

import me.Mohamad82.Pensieve.nms.enums.EntityNPCType;
import me.Mohamad82.RUoM.vector.Vector3;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class EntityRecord extends Record {

    private final EntityNPCType entityType;
    private final int startingTick;
    private ItemStack item; //Used for projectiles like splash potions

    public EntityRecord(UUID uuid, Vector3 center, EntityNPCType entityType, int startingTick) {
        super(uuid, center);
        this.entityType = entityType;
        this.startingTick = startingTick;
    }

    public EntityNPCType getEntityType() {
        return entityType;
    }

    public int getStartingTick() {
        return startingTick;
    }

    public void setItem(ItemStack item) {
        this.item = item;
    }

    public ItemStack getItem() {
        return item;
    }

}
