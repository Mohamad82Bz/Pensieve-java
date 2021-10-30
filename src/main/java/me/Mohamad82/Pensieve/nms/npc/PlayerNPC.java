package me.Mohamad82.Pensieve.nms.npc;

import com.mojang.authlib.GameProfile;
import me.Mohamad82.Pensieve.nms.PacketProvider;
import me.Mohamad82.Pensieve.nms.enums.NPCAnimation;
import me.Mohamad82.Pensieve.nms.enums.EntityMetadata;
import me.Mohamad82.RUoM.XSeries.ReflectionUtils;
import me.Mohamad82.RUoM.translators.skin.MinecraftSkin;
import me.Mohamad82.RUoM.utils.ServerVersion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.UUID;

public class PlayerNPC extends NPC {

    private static Class<?> CRAFT_SERVER, CRAFT_WORLD, ENTITY_PLAYER, MINECRAFT_SERVER, PLAYER_INTERACT_MANAGER, WORLD_SERVER, ENTITY, PROFILE;
    private static Constructor<?> ENTITY_PLAYER_CONSTRUCTOR, INTERACT_MANAGER_CONSTRUCTOR;
    private static Method SET_LOCATION_METHOD, GET_SERVER_METHOD, CRAFT_WORLD_GET_HANDLE_METHOD;

    static {
        try {
            {
                CRAFT_SERVER = ReflectionUtils.getCraftClass("CraftServer");
                CRAFT_WORLD = ReflectionUtils.getCraftClass("CraftWorld");
                ENTITY_PLAYER = ReflectionUtils.getNMSClass("server.level", "EntityPlayer");
                MINECRAFT_SERVER = ReflectionUtils.getNMSClass("server", "MinecraftServer");
                PLAYER_INTERACT_MANAGER = ReflectionUtils.getNMSClass("server.level", "PlayerInteractManager");
                WORLD_SERVER = ReflectionUtils.getNMSClass("server.level", "WorldServer");
                ENTITY = ReflectionUtils.getNMSClass("world.entity", "Entity");
                PROFILE = Class.forName("com.mojang.authlib.GameProfile");
            }
            {
                if (ServerVersion.supports(17))
                    ENTITY_PLAYER_CONSTRUCTOR = ENTITY_PLAYER.getConstructor(MINECRAFT_SERVER, WORLD_SERVER, PROFILE);
                else
                    ENTITY_PLAYER_CONSTRUCTOR = ENTITY_PLAYER.getConstructor(MINECRAFT_SERVER, WORLD_SERVER, PROFILE, PLAYER_INTERACT_MANAGER);
                INTERACT_MANAGER_CONSTRUCTOR = PLAYER_INTERACT_MANAGER.getConstructor(WORLD_SERVER);
            }
            {
                SET_LOCATION_METHOD = ENTITY_PLAYER.getMethod("setLocation", double.class, double.class, double.class, float.class, float.class);
                GET_SERVER_METHOD = CRAFT_SERVER.getMethod("getServer");
                CRAFT_WORLD_GET_HANDLE_METHOD = CRAFT_WORLD.getMethod("getHandle");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final float yaw;
    private final Object npc; //EntityPlayer

    public PlayerNPC(String name, Location location, Optional<MinecraftSkin> skin) {
        try {
            Object server = GET_SERVER_METHOD.invoke(Bukkit.getServer());
            Object world = CRAFT_WORLD_GET_HANDLE_METHOD.invoke(location.getWorld());
            GameProfile profile = new GameProfile(UUID.randomUUID(), name);

            npc = ENTITY_PLAYER_CONSTRUCTOR.newInstance(server, world, profile, INTERACT_MANAGER_CONSTRUCTOR.newInstance(world));
            SET_LOCATION_METHOD.invoke(npc, location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());

            this.yaw = location.getYaw();

            initialize(PacketProvider.getEntityId(npc), location);

            if (skin.isPresent())
                skin.get().apply(npc);
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    public void animate(NPCAnimation npcAnimation) {
        Object packetPlayOutAnimation = PacketProvider.getPacketPlayOutAnimation(npc, npcAnimation);

        getViewers().forEach(player -> {
            ReflectionUtils.sendPacket(player,
                    packetPlayOutAnimation);
        });
    }

    public Object getNpc() {
        return npc;
    }

    @Override
    public void addNPCPacket(Player... players) {
        Object packetPlayOutPlayerInfo = PacketProvider.getPacketPlayOutPlayerInfo(npc, "ADD_PLAYER");
        Object packetPlayOutNamedEntitySpawn = PacketProvider.getPacketPlayOutNamedEntitySpawn(npc);
        Object packetPlayOutEntityHeadRotation = PacketProvider.getPacketPlayOutEntityHeadRotation(npc, yaw);
        Object packetPlayOutEntityMetadata = PacketProvider.getPacketPlayOutEntityMetadata(id,
                EntityMetadata.PlayerSkin.getMetadataId(), EntityMetadata.PlayerSkin.getAllBitMasks());

        for (Player player : players) {
            ReflectionUtils.sendPacket(player,
                    packetPlayOutPlayerInfo,
                    packetPlayOutNamedEntitySpawn,
                    packetPlayOutEntityHeadRotation,
                    packetPlayOutEntityMetadata);
        }
    }

    @Override
    public void removeNPCPacket(Player... players) {
        Object packetPlayOutPlayerInfo = PacketProvider.getPacketPlayOutPlayerInfo(npc, "REMOVE_PLAYER");
        Object packetPlayOutEntityDestroy = PacketProvider.getPacketPlayOutEntityDestroy(PacketProvider.getEntityId(npc));

        for (Player player : players) {
            ReflectionUtils.sendPacket(player,
                    packetPlayOutPlayerInfo,
                    packetPlayOutEntityDestroy);
        }
    }

}
