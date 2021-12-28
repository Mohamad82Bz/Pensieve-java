package me.mohamad82.pensieve.recording.record;

import me.mohamad82.pensieve.utils.Utils;

public class DroppedItemRecordTick extends EntityRecordTick {

    private int itemAmount = -1;

    public int getItemAmount() {
        return itemAmount;
    }

    public void setItemAmount(int itemAmount) {
        this.itemAmount = itemAmount;
    }

    @Override
    public DroppedItemRecordTick copy() {
        return Utils.copy(this, new DroppedItemRecordTick());
    }

}
