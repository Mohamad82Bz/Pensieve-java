package me.mohamad82.pensieve.api.event;

import me.mohamad82.ruom.npc.NPC;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PensieveNPCLookEvent extends Event {

    private final static HandlerList HANDLER_LIST = new HandlerList();

    private final NPC npc;
    private final float yaw;
    private final float pitch;

    public PensieveNPCLookEvent(NPC npc, float yaw, float pitch) {
        this.npc = npc;
        this.yaw = yaw;
        this.pitch = pitch;
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

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

}
