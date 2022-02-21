package me.mohamad82.pensieve.recording;

import me.mohamad82.pensieve.recording.record.EntityRecord;
import me.mohamad82.pensieve.recording.record.PlayerRecord;
import me.mohamad82.ruom.vector.Vector3;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashSet;
import java.util.Set;

public class RecordContainer {

    private final Set<PlayerRecord> playerRecords;
    private final Set<EntityRecord> entityRecords;

    RecordContainer(Set<PlayerRecord> playerRecords, Set<EntityRecord> entityRecords) {
        this.playerRecords = playerRecords;
        this.entityRecords = entityRecords;
    }

    RecordContainer() {
        this.playerRecords = new HashSet<>();
        this.entityRecords = new HashSet<>();
    }

    @ApiStatus.Internal
    public static RecordContainer createEmptyContainer() {
        return new RecordContainer();
    }

    public Set<PlayerRecord> getPlayerRecords() {
        return playerRecords;
    }

    public Set<EntityRecord> getEntityRecords() {
        return entityRecords;
    }

}
