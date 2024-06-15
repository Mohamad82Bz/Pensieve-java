package me.mohamad82.pensieve.api.event;

import me.mohamad82.pensieve.recording.Recorder;
import me.mohamad82.pensieve.recording.record.RawRecord;
import me.mohamad82.pensieve.recording.record.RawRecordTick;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PensieveRecorderRawTickEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final Recorder recorder;
    private final int tickIndex;
    private final RawRecord record;
    private final RawRecordTick recordTick;

    public PensieveRecorderRawTickEvent(Recorder recorder, int tickIndex, RawRecord record, RawRecordTick recordTick) {
        this.recorder = recorder;
        this.tickIndex = tickIndex;
        this.record = record;
        this.recordTick = recordTick;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public Recorder getRecorder() {
        return recorder;
    }

    public int getTickIndex() {
        return tickIndex;
    }

    public RawRecord getRecord() {
        return record;
    }

    public RawRecordTick getRecordTick() {
        return recordTick;
    }

}
