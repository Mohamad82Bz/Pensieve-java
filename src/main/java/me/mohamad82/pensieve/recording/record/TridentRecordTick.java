package me.mohamad82.pensieve.recording.record;

import me.mohamad82.pensieve.utils.Utils;

public class TridentRecordTick extends EntityRecordTick {

    private boolean attachedBlock;
    private boolean returning;

    public boolean hasAttachedBlock() {
        return attachedBlock;
    }

    public void setAttachedBlock() {
        this.attachedBlock = true;
    }

    public void setReturning(boolean returning) {
        this.returning = returning;
    }

    public boolean isReturning() {
        return returning;
    }

    public void setReturning() {
        this.returning = true;
    }

    @Override
    public RecordTick copy() {
        return Utils.copy(this, new TridentRecordTick());
    }

}
