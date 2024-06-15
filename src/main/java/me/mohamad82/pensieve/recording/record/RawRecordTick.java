package me.mohamad82.pensieve.recording.record;

import me.mohamad82.pensieve.utils.Utils;

public class RawRecordTick extends RecordTick {

    @Override
    public RecordTick copy() {
        return Utils.copy(this, new RawRecordTick());
    }

}
