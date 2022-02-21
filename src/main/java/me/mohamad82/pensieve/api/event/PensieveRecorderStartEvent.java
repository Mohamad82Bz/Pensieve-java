package me.mohamad82.pensieve.api.event;

import me.mohamad82.pensieve.recording.Recorder;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PensieveRecorderStartEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final Recorder recorder;

    public PensieveRecorderStartEvent(Recorder recorder) {
        this.recorder = recorder;
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

}