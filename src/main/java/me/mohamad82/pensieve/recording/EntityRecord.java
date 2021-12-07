package me.mohamad82.pensieve.recording;

import me.mohamad82.pensieve.nms.npc.enums.EntityNPCType;
import me.Mohamad82.RUoM.vector.Vector3;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class EntityRecord extends Record {

    private final EntityNPCType entityType;
    private final int startingTick;
    private UUID pickedUpBy; //Used for dropped items
    private ItemStack item; //Used for projectiles like splash potions
    private ItemStack droppedItem; //Used for dropped items

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

    public UUID getPickedUpBy() {
        return pickedUpBy;
    }

    public void setPickedUpBy(UUID pickedUpBy) {
        this.pickedUpBy = pickedUpBy;
    }

    public void setItem(ItemStack item) {
        this.item = item;
    }

    public ItemStack getItem() {
        return item;
    }

    public ItemStack getDroppedItem() {
        return droppedItem;
    }

    public void setDroppedItem(ItemStack droppedItem) {
        this.droppedItem = droppedItem;
    }

}
