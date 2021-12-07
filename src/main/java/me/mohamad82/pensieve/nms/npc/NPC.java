package me.mohamad82.pensieve.nms.npc;

import me.mohamad82.pensieve.nms.PacketProvider;
import me.mohamad82.pensieve.nms.npc.enums.NPCState;
import me.Mohamad82.RUoM.XSeries.ReflectionUtils;
import me.Mohamad82.RUoM.vector.Vector3;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public abstract class NPC {

    private final Set<Player> viewers = new HashSet<>();

    protected int id;
    protected Location location;

    protected NPC() {

    }

    public void initialize(int id, Location location) {
        this.id = id;
        this.location = location;
    }

    public abstract void addNPCPacket(Player... players);

    public void addNPCPacket() {
        addNPCPacket(viewers.toArray(new Player[0]));
    }

    public abstract void removeNPCPacket(Player... players);

    public void removeNPCPacket() {
        removeNPCPacket(viewers.toArray(new Player[0]));
    }

    public void look(float yaw, float pitch) {
        Object packetPlayOutEntityHeadRotation = PacketProvider.getPacketPlayOutEntityHeadRotation(id, yaw);
        Object packetPlayOutEntityLook = PacketProvider.getPacketPlayOutEntityLook(id, yaw, pitch);

        if (location != null) {
            location.setYaw(yaw);
            location.setPitch(pitch);
        }

        viewers.forEach(player -> {
            ReflectionUtils.sendPacket(player,
                    packetPlayOutEntityHeadRotation,
                    packetPlayOutEntityLook);
        });
    }

    public boolean move(double x, double y, double z) {
        if (x > 8 || y > 8 || z > 8) {
            return false;
        }
        Object packetPlayOutRelEntityMove = PacketProvider.getPacketPlayOutRelEntityMove(id, x, y, z);

        if (location != null) {
            location.add(x, y, z);
        }

        viewers.forEach(player -> {
            ReflectionUtils.sendPacket(player,
                    packetPlayOutRelEntityMove);
        });
        return true;
    }

    public boolean move(Vector3 vector) {
        return move(vector.getX(), vector.getY(), vector.getZ());
    }

    public boolean moveAndLook(double x, double y, double z, float yaw, float pitch, boolean onGround) {
        if (x > 8 || y > 8 || z > 8)
            return false;

        Object packetPlayOutEntityHeadRotation = PacketProvider.getPacketPlayOutEntityHeadRotation(id, yaw);
        Object packetPlayOutRelEntityMoveLook = PacketProvider.getPacketPlayOutRelEntityMoveLook(id, x, y, z, yaw, pitch, onGround);

        if (location != null) {
            location.add(x, y, z);
            location.setYaw(yaw);
            location.setPitch(pitch);
        }

        viewers.forEach(player -> {
            ReflectionUtils.sendPacket(player,
                    packetPlayOutEntityHeadRotation,
                    packetPlayOutRelEntityMoveLook);
        });
        return true;
    }

    public boolean moveAndLook(Vector3 vector, float yaw, float pitch, boolean onGround) {
        return moveAndLook(vector.getX(), vector.getY(), vector.getZ(), yaw, pitch, onGround);
    }

    public void teleport(double x, double y, double z, float yaw, float pitch, boolean onGround, Player... players) {
        Object packetPlayOutEntityTeleport = PacketProvider.getPacketPlayOutEntityTeleport(id, x, y, z, yaw, pitch, onGround);

        if (location != null) {
            location = new Location(location.getWorld(), x, y, z, yaw, pitch);
        }

        for (Player player : players) {
            ReflectionUtils.sendPacket(player,
                    packetPlayOutEntityTeleport);
        }
    }

    public void teleport(double x, double y, double z, float yaw, float pitch, boolean onGround) {
        teleport(x, y, z, yaw, pitch, onGround, viewers.toArray(new Player[0]));
    }

    public void velocity(double x, double y, double z) {
        Object packetPlayOutEntityVelocity = PacketProvider.getPacketPlayOutEntityVelocity(id, x, y, z);

        viewers.forEach(player -> {
            ReflectionUtils.sendPacket(player,
                    packetPlayOutEntityVelocity);
        });
    }

    public void velocity(Vector3 vector) {
        velocity(vector.getX(), vector.getY(), vector.getZ());
    }

    public void setEquipment(EquipmentSlot slot, ItemStack item) {
        Object packetPlayOutEntityEquipment = PacketProvider.getPacketPlayOutEntityEquipment(id, slot, item);

        viewers.forEach(player -> {
            ReflectionUtils.sendPacket(player,
                    packetPlayOutEntityEquipment);
        });
    }

    protected void collect(int collectedEntityId, int collectorEntityId, int amount) {
        Object packetPlayOutCollect = PacketProvider.getPacketPlayOutCollect(collectedEntityId, collectorEntityId, amount);

        viewers.forEach(player -> {
            ReflectionUtils.sendPacket(player,
                    packetPlayOutCollect);
        });
    }

    public void setState(NPCState npcState) {
        Object packetPlayOutEntityMetadata = PacketProvider.getPacketPlayOutEntityMetadata(id, npcState);

        viewers.forEach(player -> {
            ReflectionUtils.sendPacket(player,
                    packetPlayOutEntityMetadata);
        });
    }

    public void setMetadata(int metadataId, Object value, Player... viewers) {
        Object packetPlayOutEntityMetadata = PacketProvider.getPacketPlayOutEntityMetadata(id, metadataId, value);

        for (Player player : viewers) {
            ReflectionUtils.sendPacket(player, packetPlayOutEntityMetadata);
        }
    }

    public void setMetadata(int metadataId, Object value) {
        setMetadata(metadataId, value, getViewers().toArray(new Player[0]));
    }

    public int getId() {
        return id;
    }

    public Location getLocation() {
        return location.clone();
    }

    public boolean addViewers(Set<Player> players) {
        return viewers.addAll(players);
    }

    public boolean addViewers(Player... players) {
        return viewers.addAll(Arrays.asList(players));
    }

    public boolean addViewer(Player player) {
        return viewers.add(player);
    }

    public boolean removeViewer(Player player) {
        removeNPCPacket(player);
        return viewers.remove(player);
    }

    public Set<Player> getViewers() {
        return viewers;
    }

}
