package me.mohamad82.pensieve.recording.record;

import me.mohamad82.ruom.npc.NPCType;
import me.mohamad82.ruom.vector.Vector3;

import java.util.UUID;

public class FishingHookRecord extends EntityRecord {

    private final UUID owner;

    public FishingHookRecord(UUID uuid, Vector3 center, int startingTick, UUID owner) {
        super(uuid, center, NPCType.FISHING_BOBBER, startingTick);
        this.owner = owner;
    }

    public UUID getOwner() {
        return owner;
    }

    @Override
    public RecordTick createRecordTick() {
        return new FishingHookRecordTick();
    }

}
