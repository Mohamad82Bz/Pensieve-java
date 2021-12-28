package me.mohamad82.pensieve.recording.record;

import me.mohamad82.ruom.vector.Vector3;

import java.lang.reflect.Field;
import java.util.Collection;

public abstract class RecordTick {

    private Vector3 location;
    private Vector3 velocity;

    private float yaw = -1;
    private float pitch = -1;

    private int effectColor = -1;

    protected RecordTick() {

    }

    public Vector3 getLocation() {
        return location;
    }

    public void setLocation(Vector3 location) {
        this.location = location;
    }

    public Vector3 getVelocity() {
        return velocity;
    }

    public void setVelocity(Vector3 velocity) {
        this.velocity = velocity;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public int getEffectColor() {
        return effectColor;
    }

    public void setEffectColor(int effectColor) {
        this.effectColor = effectColor;
    }

    public abstract RecordTick copy();
    
}
