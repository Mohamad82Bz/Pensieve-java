package me.mohamad82.pensieve.nms.npc;

import me.mohamad82.pensieve.nms.NMSUtils;
import me.mohamad82.pensieve.nms.PacketUtils;
import me.mohamad82.pensieve.nms.accessors.EntityAccessor;
import me.mohamad82.pensieve.utils.Utils;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public abstract class EntityNPC extends NPC {

    public static Object createEntityObject(Class<?> accessor, Object... parameters) {
        try {
            return accessor.getMethod("getConstructor0").invoke(null, parameters);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private final NPCType npcType;

    protected EntityNPC(Object entity, Location location, NPCType npcType) {
        initialize(entity);
        this.npcType = npcType;

        Utils.ignoreExcRun(() -> {
            EntityAccessor.getMethodSetPos1().invoke(entity, location.getX(), location.getY(), location.getZ());
            EntityAccessor.getMethodSetRot1().invoke(entity, location.getYaw(), location.getPitch());
            initialize(entity);
        });
    }

    public NPCType getType() {
        return npcType;
    }

    @Override
    protected void addViewer(Player player) {
        NMSUtils.sendPacket(player,
                PacketUtils.getAddEntityPacket(entity),
                PacketUtils.getEntityDataPacket(entity));
    }

    @Override
    protected void removeViewer(Player player) {
        NMSUtils.sendPacket(player,
                PacketUtils.getRemoveEntitiesPacket(id));
    }

}
