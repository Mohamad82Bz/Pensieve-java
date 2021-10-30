package me.Mohamad82.Pensieve.nms.npc;

import me.Mohamad82.Pensieve.nms.PacketProvider;
import me.Mohamad82.Pensieve.nms.enums.EntityNPCType;
import me.Mohamad82.RUoM.XSeries.ReflectionUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;

public class EntityNPC extends NPC {

    private final UUID uuid;
    private final EntityNPCType entityType;

    public EntityNPC(UUID uuid, Location location, EntityNPCType entityType) {
        this.uuid = uuid;
        this.entityType = entityType;

        initialize(uuid.hashCode(), location);
    }

    @Override
    public void addNPCPacket(Player... players) {
        Object packetPlayOutSpawnEntity = PacketProvider.getPacketPlayOutSpawnEntity(uuid, location, entityType.toString().toUpperCase());

        for (Player player : players) {
            ReflectionUtils.sendPacket(player,
                    packetPlayOutSpawnEntity);
        }
    }

    @Override
    public void removeNPCPacket(Player... players) {
        Object packetPlayOutEntityDestroy = PacketProvider.getPacketPlayOutEntityDestroy(id);

        for (Player player : players) {
            ReflectionUtils.sendPacket(player,
                    packetPlayOutEntityDestroy);
        }
    }

}
