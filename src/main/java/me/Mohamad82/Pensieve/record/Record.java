package me.Mohamad82.Pensieve.record;

import com.google.common.collect.ImmutableList;
import me.Mohamad82.RUoM.vector.Vector3;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class Record {

    private List<RecordTick> recordTicks = new ArrayList<>();
    private final UUID uuid;
    private final Vector3 center;
    private Vector3 startLocation;

    protected Record(UUID uuid, Vector3 center) {
        this.uuid = uuid;
        this.center = center;
    }

    protected void setRecordTicks(List<RecordTick> recordTicks) {
        this.recordTicks = recordTicks;
    }

    public int getTotalTicks() {
        return recordTicks.size();
    }

    public UUID getUuid() {
        return uuid;
    }

    public Vector3 getCenter() {
        return center;
    }

    public Vector3 getStartLocation() {
        return startLocation;
    }

    protected void setStartLocation(Vector3 startLocation) {
        this.startLocation = startLocation;
    }

    public void addRecordTick(RecordTick recordTick) {
        recordTicks.add(recordTick);
    }

    public ImmutableList<RecordTick> getRecordTicks() {
        return ImmutableList.copyOf(recordTicks);
    }

}
