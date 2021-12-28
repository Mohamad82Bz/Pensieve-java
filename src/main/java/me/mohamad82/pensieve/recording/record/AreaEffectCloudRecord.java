package me.mohamad82.pensieve.recording.record;

import me.mohamad82.ruom.npc.NPCType;
import me.mohamad82.ruom.vector.Vector3;

import java.util.UUID;

public class AreaEffectCloudRecord extends EntityRecord {

    private final int color;

    public AreaEffectCloudRecord(UUID uuid, Vector3 center, int startingTick, int color) {
        super(uuid, center, NPCType.AREA_EFFECT_CLOUD, startingTick);
        this.color = color;
    }

    public int getColor() {
        return color;
    }

    @Override
    public RecordTick createRecordTick() {
        return new AreaEffectCloudTick();
    }

}
