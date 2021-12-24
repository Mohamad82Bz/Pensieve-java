package me.mohamad82.pensieve.nms.znpcold;

import me.mohamad82.pensieve.nms.PacketUtils;
import me.Mohamad82.RUoM.XSeries.ReflectionUtils;
import me.mohamad82.pensieve.nms.npc.NPCType;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;

public class EntityNPCOld extends NPCOld {

    private final UUID uuid;
    private final NPCType entityType;

    public EntityNPCOld(UUID uuid, Location location, NPCType entityType) {
        this.uuid = uuid;
        this.entityType = entityType;

        initialize(uuid.hashCode(), location.clone());
    }

    public void collect(int collectorEntityId, int amount) {
        collect(id, collectorEntityId, amount);
    }

    @Override
    public void addNPCPacket(Player... players) {
        Object packetPlayOutSpawnEntity = PacketUtils.getAddEntityPacket(uuid, location, entityType.toString().toUpperCase());

        for (Player player : players) {
            ReflectionUtils.sendPacket(player,
                    packetPlayOutSpawnEntity);
        }
    }

    @Override
    public void removeNPCPacket(Player... players) {
        Object packetPlayOutEntityDestroy = PacketUtils.getRemoveEntitiesPacket(id);

        for (Player player : players) {
            ReflectionUtils.sendPacket(player,
                    packetPlayOutEntityDestroy);
        }
    }

}
