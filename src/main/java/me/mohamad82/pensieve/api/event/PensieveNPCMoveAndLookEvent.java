package me.mohamad82.pensieve.api.event;

import me.mohamad82.ruom.npc.NPC;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PensieveNPCMoveAndLookEvent extends Event {

    private final static HandlerList HANDLER_LIST = new HandlerList();

    private final NPC npc;
    private final Location from;
    private final Location to;

    public PensieveNPCMoveAndLookEvent(NPC npc, Location from, Location to) {
        this.npc = npc;
        this.from = from;
        this.to = to;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    public NPC getNpc() {
        return npc;
    }

    public Location getFrom() {
        return from;
    }

    public Location getTo() {
        return to;
    }

}
