package me.mohamad82.pensieve.recording.record;

import me.mohamad82.pensieve.utils.Utils;

public class AreaEffectCloudTick extends EntityRecordTick {

    private float radius;

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    @Override
    public RecordTick copy() {
        return Utils.copy(this, new AreaEffectCloudTick());
    }

}
