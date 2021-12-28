package me.mohamad82.pensieve.recording.record;

import me.mohamad82.pensieve.utils.Utils;

public class ArrowRecordTick extends EntityRecordTick {

    @Override
    public ArrowRecordTick copy() {
        return Utils.copy(this, new ArrowRecordTick());
    }

}
