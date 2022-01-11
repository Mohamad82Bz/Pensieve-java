package me.mohamad82.pensieve.recording.record;

import me.mohamad82.pensieve.utils.Utils;

public class FireworkRecordTick extends EntityRecordTick {

    @Override
    public RecordTick copy() {
        return Utils.copy(this, new FireworkRecordTick());
    }

}
