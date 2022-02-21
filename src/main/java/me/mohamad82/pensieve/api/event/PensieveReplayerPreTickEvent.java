package me.mohamad82.pensieve.api.event;

import me.mohamad82.pensieve.replaying.PlayBackControl;
import me.mohamad82.pensieve.replaying.Replayer;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PensieveReplayerPreTickEvent extends Event implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();
    private boolean cancelled;

    private final Replayer replayer;
    private final PlayBackControl playBackControl;
    private final int tickIndex;

    public PensieveReplayerPreTickEvent(Replayer replayer, PlayBackControl playBackControl, int tickIndex) {
        this.replayer = replayer;
        this.playBackControl = playBackControl;
        this.tickIndex = tickIndex;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public Replayer getReplayer() {
        return replayer;
    }

    public PlayBackControl getPlayBackControl() {
        return playBackControl;
    }

    public int getTickIndex() {
        return tickIndex;
    }

}
