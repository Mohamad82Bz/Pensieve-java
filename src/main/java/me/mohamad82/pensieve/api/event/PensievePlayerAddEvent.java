package me.mohamad82.pensieve.api.event;

import me.mohamad82.pensieve.recording.Recorder;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PensievePlayerAddEvent extends Event implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();
    private boolean cancelled;

    private final Recorder recorder;
    private final Player player;

    public PensievePlayerAddEvent(Recorder recorder, Player player) {
        this.recorder = recorder;
        this.player = player;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public Recorder getRecorder() {
        return recorder;
    }

    public Player getPlayer() {
        return player;
    }

}
