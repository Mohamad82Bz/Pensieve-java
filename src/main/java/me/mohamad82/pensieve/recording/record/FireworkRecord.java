package me.mohamad82.pensieve.recording.record;

import me.mohamad82.ruom.npc.NPCType;
import me.mohamad82.ruom.vector.Vector3;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class FireworkRecord extends EntityRecord {

    private final ItemStack fireworkItem;
    private final boolean shotAtAngle;

    public FireworkRecord(UUID uuid, Vector3 center, int startingTick, ItemStack fireworkItem, boolean shotAtAngle) {
        super(uuid, center, NPCType.FIREWORK_ROCKET, startingTick);
        this.fireworkItem = fireworkItem;
        this.shotAtAngle = shotAtAngle;
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

}
