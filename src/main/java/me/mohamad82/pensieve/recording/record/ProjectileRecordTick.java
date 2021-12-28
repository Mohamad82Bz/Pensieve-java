package me.mohamad82.pensieve.recording.record;

import me.mohamad82.pensieve.utils.Utils;

public class ProjectileRecordTick extends EntityRecordTick {

    @Override
    public ProjectileRecordTick copy() {
        return Utils.copy(this, new ProjectileRecordTick());
    }

}
