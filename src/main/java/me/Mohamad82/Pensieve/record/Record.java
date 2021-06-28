package me.Mohamad82.Pensieve.record;

import me.Mohamad82.Pensieve.replay.ReplayCache;
import me.Mohamad82.RUoM.Vector3;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Record {

    private ReplayCache recordCache;
    private final UUID playerUUID;
    private Vector3 startLocation;
    private Vector3 center;
    private List<RecordTick> recordTicks = new ArrayList<>();

    public Record(UUID playerUUID) {
        this.playerUUID = playerUUID;
    }

    public ReplayCache getRecordCache() {
        return recordCache;
    }

    public void setRecordCache(ReplayCache recordCache) {
        this.recordCache = recordCache;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public Vector3 getStartLocation() {
        return startLocation;
    }

    protected void setStartLocation(Vector3 startLocation) {
        this.startLocation = startLocation;
    }

    public Vector3 getCenter() {
        return center;
    }

    protected void setCenter(Vector3 center) {
        this.center = center;
    }

    public List<RecordTick> getRecordTicks() {
        return recordTicks;
    }

    protected void setRecordTicks(List<RecordTick> recordTicks) {
        this.recordTicks = recordTicks;
    }

    public int getTotalTicks() {
        return recordTicks.size();
    }

}
