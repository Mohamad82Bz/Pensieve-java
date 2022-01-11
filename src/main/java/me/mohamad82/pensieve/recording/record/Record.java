package me.mohamad82.pensieve.recording.record;

import com.google.common.collect.ImmutableList;
import me.mohamad82.ruom.vector.Vector3;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;

public abstract class Record {

    private final List<RecordTick> recordTicks = new ArrayList<>();
    private final UUID uuid;
    private final Vector3 center;
    private Vector3 startLocation;

    private final Set<String> customDataSet = new HashSet<>();
    private final Map<String, String> customDataMap = new HashMap<>();

    protected Record(UUID uuid, Vector3 center) {
        this.uuid = uuid;
        this.center = center;
    }

    /**
     * Returns the total ticks saved in this record.
     * @return The total tick.
     */
    public int getTotalTicks() {
        return recordTicks.size();
    }

    /**
     * Returns the UUID of this record.
     * @return The UUID.
     */
    public UUID getUuid() {
        return uuid;
    }

    /**
     * Returns the center of this record used to offset a replay.
     * @return The center.
     */
    public Vector3 getCenter() {
        return center;
    }

    /**
     * The location where record is started.
     * @return The location.
     */
    public Vector3 getStartLocation() {
        return startLocation;
    }

    /**
     * Sets the start location of this record  - INTERNAL USE ONLY
     * @param startLocation The start location.
     */
    @ApiStatus.Internal
    public void setStartLocation(Vector3 startLocation) {
        this.startLocation = startLocation;
    }

    @ApiStatus.Internal
    public void addRecordTick(RecordTick recordTick) {
        recordTicks.add(recordTick);
    }

    /**
     * Returns an immutablelist of the recordticks that are currently in this record.
     * @return The ImmutableList of recordticks.
     */
    public ImmutableList<RecordTick> getRecordTicks() {
        return ImmutableList.copyOf(recordTicks);
    }

    /**
     * Returns the custom data set used to store custom data for writing addons, etc.
     * @return The custom data set.
     */
    public Set<String> getCustomDataSet() {
        return customDataSet;
    }

    /**
     * Returns the custom data map used to store custom data for writing addons, etc.
     * @return The custom data map.
     */
    public Map<String, String> getCustomDataMap() {
        return customDataMap;
    }

    /**
     * Constructs a new record tick - INTERNAL USE ONLY
     * @return A new recordtick
     */
    @ApiStatus.Internal
    public abstract RecordTick createRecordTick();

}
