package me.mohamad82.pensieve.recording.record;

import me.mohamad82.pensieve.utils.Utils;

import java.util.UUID;

public class FishingHookRecordTick extends EntityRecordTick {

    private UUID hookedEntityLocation;

    public UUID getHookedEntity() {
        return hookedEntityLocation;
    }

    public void setHookedEntity(UUID hookedEntityLocation) {
        this.hookedEntityLocation = hookedEntityLocation;
    }

    @Override
    public RecordTick copy() {
        return Utils.copy(this, new FishingHookRecordTick());
    }

}
