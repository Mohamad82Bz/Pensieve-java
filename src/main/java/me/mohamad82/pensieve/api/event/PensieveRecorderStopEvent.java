package me.mohamad82.pensieve.api.event;

import me.mohamad82.pensieve.recording.Recorder;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PensieveRecorderStopEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final Recorder recorder;

    public PensieveRecorderStopEvent(Recorder recorder) {
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
