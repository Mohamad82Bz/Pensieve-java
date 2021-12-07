package me.mohamad82.pensieve.nms.npc;

import me.mohamad82.pensieve.nms.PacketProvider;
import me.mohamad82.pensieve.nms.npc.enums.EntityNPCType;
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

        initialize(uuid.hashCode(), location.clone());
    }

    public void collect(int collectorEntityId, int amount) {
        collect(id, collectorEntityId, amount);
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
