package me.mohamad82.pensieve.api.event;

import me.mohamad82.pensieve.recording.Recorder;
import me.mohamad82.pensieve.recording.record.RecordTick;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PensieveRecorderPlayerTickEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final Recorder recorder;
    private final Player player;
    private final int tickIndex;
    private final RecordTick recordTick;

    public PensieveRecorderPlayerTickEvent(Recorder recorder, Player player, int tickIndex, RecordTick recordTick) {
        this.recorder = recorder;
        this.player = player;
        this.tickIndex = tickIndex;
        this.recordTick = recordTick;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    public Recorder getRecorder() {
        return recorder;
    }

    public Player getPlayer() {
        return player;
    }

    public int getTickIndex() {
        return tickIndex;
    }

    public RecordTick getRecordTick() {
        return recordTick;
    }

}
