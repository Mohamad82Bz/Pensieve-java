package me.Mohamad82.Pensieve.nms;

import com.mojang.authlib.GameProfile;
import me.Mohamad82.Pensieve.nms.enums.NPCAnimation;
import me.Mohamad82.Pensieve.nms.enums.NPCState;
import me.Mohamad82.RUoM.XSeries.ReflectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class NPC {

    private final Set<Player> players = new HashSet<>();
    private final float yaw;
    private final Object npc;

    public NPC(String playerName, Location location) {
        try {
            Object server = ReflectionUtils.getCraftClass("CraftServer").getMethod("getServer").invoke(Bukkit.getServer());
            Object world = ReflectionUtils.getCraftClass("CraftWorld").getMethod("getHandle").invoke(location.getWorld());
            GameProfile profile = new GameProfile(UUID.randomUUID(), ChatColor.YELLOW + playerName);

            Constructor<?> entityPlayerConstructor = ReflectionUtils.getNMSClass("EntityPlayer")
                    .getConstructor(ReflectionUtils.getNMSClass("MinecraftServer"), ReflectionUtils.getNMSClass("WorldServer"),
                            profile.getClass(), ReflectionUtils.getNMSClass("PlayerInteractManager"));
            Constructor<?> interactManagerConstructor = ReflectionUtils.getNMSClass("PlayerInteractManager")
                    .getConstructor(ReflectionUtils.getNMSClass("WorldServer"));

            npc = entityPlayerConstructor.newInstance(server, world, profile, interactManagerConstructor.newInstance(world));
            npc.getClass().getMethod("setLocation", double.class, double.class, double.class, float.class, float.class).invoke(npc,
                    location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());

            this.yaw = location.getYaw();
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    public void addNPCPacket() {
        Object packetPlayOutPlayerInfo = Packets.getPacketPlayOutPlayerInfo(npc, "ADD_PLAYER");
        Object packetPlayOutNamedEntitySpawn = Packets.getPacketPlayOutNamedEntitySpawn(npc);
        Object packetPlayOutEntityHeadRotation = Packets.getPacketPlayOutEntityHeadRotation(npc, yaw);

        for (Player player : players) {
            ReflectionUtils.sendPacket(player,
                    packetPlayOutPlayerInfo,
                    packetPlayOutNamedEntitySpawn,
                    packetPlayOutEntityHeadRotation);
        }
    }

    public void addNPCPacket(Player player) {
        ReflectionUtils.sendPacket(player,
                Packets.getPacketPlayOutPlayerInfo(npc, "ADD_PLAYER"),
                Packets.getPacketPlayOutNamedEntitySpawn(npc),
                Packets.getPacketPlayOutEntityHeadRotation(npc, yaw));
    }

    public void removeNPCPacket() {
        Object packetPlayOutPlayerInfo = Packets.getPacketPlayOutPlayerInfo(npc, "REMOVE_PLAYER");
        Object packetPlayOutEntityDestroy = Packets.getPacketPlayOutEntityDestroy(npc);

        for (Player player : players) {
            ReflectionUtils.sendPacket(player,
                    packetPlayOutPlayerInfo,
                    packetPlayOutEntityDestroy);
        }
    }

    public void removeNPCPacket(Player player) {
        ReflectionUtils.sendPacket(player,
                Packets.getPacketPlayOutPlayerInfo(npc, "REMOVE_PLAYER"),
                Packets.getPacketPlayOutEntityDestroy(npc));
    }

    public void look(float yaw, float pitch) {
        Object packetPlayOutEntityHeadRotation = Packets.getPacketPlayOutEntityHeadRotation(npc, yaw);
        Object packetPlayOutEntityLook = Packets.getPacketPlayOutEntityLook(npc, yaw, pitch);

        for (Player player : players) {
            ReflectionUtils.sendPacket(player,
                    packetPlayOutEntityHeadRotation,
                    packetPlayOutEntityLook);
        }
    }

    public void move(double x, double y, double z) {
        Object packetPlayOutRelEntityMove = Packets.getPacketPlayOutRelEntityMove(npc, x, y, z);

        for (Player player : players) {
            ReflectionUtils.sendPacket(player,
                    packetPlayOutRelEntityMove);
        }
    }

    public void moveAndLook(double x, double y, double z, float yaw, float pitch) {
        Object packetPlayOutEntityHeadRotation = Packets.getPacketPlayOutEntityHeadRotation(npc, yaw);
        Object packetPlayOutRelEntityMoveLook = Packets.getPacketPlayOutRelEntityMoveLook(npc, x, y, z, yaw, pitch);

        for (Player player : players) {
            ReflectionUtils.sendPacket(player,
                    packetPlayOutEntityHeadRotation,
                    packetPlayOutRelEntityMoveLook);
        }
    }

    public void animate(NPCAnimation npcAnimation) {
        Object packetPlayOutAnimation = Packets.getPacketPlayOutAnimation(npc, npcAnimation);

        for (Player player : players) {
            ReflectionUtils.sendPacket(player,
                    packetPlayOutAnimation);
        }
    }

    public void setEquipment(EquipmentSlot slot, ItemStack item) {
        Object packetPlayOutEntityEquipment = Packets.getPacketPlayOutEntityEquipment(npc, slot, item);

        for (Player player : players) {
            ReflectionUtils.sendPacket(player,
                    packetPlayOutEntityEquipment);
        }
    }

    public void setState(NPCState npcState) {
        Object packetPlayOutEntityMetadata = Packets.getPacketPlayOutEntityMetadata(npc, npcState);

        for (Player player : players) {
            ReflectionUtils.sendPacket(player,
                    packetPlayOutEntityMetadata);
        }
    }

    public boolean addViewer(Player player) {
        addNPCPacket(player);
        return players.add(player);
    }

    public boolean removeViewer(Player player) {
        removeNPCPacket(player);
        return players.remove(player);
    }

    public Set<Player> getViewers() {
        return players;
    }

    public Object getEntityPlayer() {
        return npc;
    }

}
