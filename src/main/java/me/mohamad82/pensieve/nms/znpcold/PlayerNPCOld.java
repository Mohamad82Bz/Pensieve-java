package me.mohamad82.pensieve.nms.znpcold;

import com.mojang.authlib.GameProfile;
import me.Mohamad82.RUoM.Ruom;
import me.Mohamad82.RUoM.XSeries.ReflectionUtils;
import me.Mohamad82.RUoM.translators.skin.MinecraftSkin;
import me.Mohamad82.RUoM.utils.ServerVersion;
import me.mohamad82.pensieve.nms.EntityMetadata;
import me.mohamad82.pensieve.nms.PacketUtils;
import me.mohamad82.pensieve.nms.npc.enums.NPCAnimation;
import me.mohamad82.pensieve.nms.znpcold.NPCOld;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.UUID;

public class PlayerNPCOld extends NPCOld {

    private static Class<?> CRAFT_SERVER, CRAFT_WORLD, ENTITY_PLAYER, MINECRAFT_SERVER, PLAYER_INTERACT_MANAGER, WORLD_SERVER;
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
            }
            {
                if (ServerVersion.supports(17))
                    ENTITY_PLAYER_CONSTRUCTOR = ENTITY_PLAYER.getConstructor(MINECRAFT_SERVER, WORLD_SERVER, GameProfile.class);
                else
                    ENTITY_PLAYER_CONSTRUCTOR = ENTITY_PLAYER.getConstructor(MINECRAFT_SERVER, WORLD_SERVER, GameProfile.class, PLAYER_INTERACT_MANAGER);
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

    private final Object npc; //EntityPlayer
    private final GameProfile profile;
    private final float yaw;
    private boolean tabListVisibility = true;

    public PlayerNPCOld(String name, Location location, Optional<MinecraftSkin> skin) {
        try {
            Object server = GET_SERVER_METHOD.invoke(Bukkit.getServer());
            Object world = CRAFT_WORLD_GET_HANDLE_METHOD.invoke(location.getWorld());
            this.profile = new GameProfile(UUID.randomUUID(), name);

            npc = ENTITY_PLAYER_CONSTRUCTOR.newInstance(server, world, profile, INTERACT_MANAGER_CONSTRUCTOR.newInstance(world));
            SET_LOCATION_METHOD.invoke(npc, location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());

            this.yaw = location.getYaw();

            initialize(PacketUtils.getEntityId(npc), location);

            if (skin.isPresent())
                skin.get().apply(npc);
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    public void animate(NPCAnimation npcAnimation) {
        Object packetPlayOutAnimation = PacketUtils.getAnimatePacket(npc, npcAnimation.getValue());

        getViewers().forEach(player -> {
            ReflectionUtils.sendPacket(player,
                    packetPlayOutAnimation);
        });
    }

    public void collect(int collectedEntityId, int amount) {
        collect(collectedEntityId, id, amount);
    }

    public void setTabList(String newName, Player... players) {
        Object packetPlayOutPlayerInfo = PacketUtils.getPacketPlayOutPlayerInfoTabListUpdate(npc, profile, newName);

        for (Player player : players) {
            ReflectionUtils.sendPacket(player,
                    packetPlayOutPlayerInfo);
        }
    }

    public void setTabList(String newName) {
        setTabList(newName, getViewers().toArray(new Player[0]));
    }

    public void addNPCTabList(Player... players) {
        tabListVisibility = true;
        Object packetPlayOutPlayerInfo = PacketUtils.getPlayerInfoPacket(npc, "ADD_PLAYER");

        for (Player player : players) {
            ReflectionUtils.sendPacket(player,
                    packetPlayOutPlayerInfo);
        }
    }

    public void addNPCTabList() {
        addNPCTabList(getViewers().toArray(new Player[0]));
    }

    public void removeNPCTabList(Player... players) {
        tabListVisibility = false;
        Object packetPlayOutPlayerInfo = PacketUtils.getPlayerInfoPacket(npc, "REMOVE_PLAYER");

        Ruom.runSync(() -> {
            for (Player player : players) {
                ReflectionUtils.sendPacket(player,
                        packetPlayOutPlayerInfo);
            }
        }, 3);
    }

    public void removeNPCTabList() {
        removeNPCTabList(getViewers().toArray(new Player[0]));
    }

    public boolean isTabListVisible() {
        return tabListVisibility;
    }

    public Object getNpc() {
        return npc;
    }

    @Override
    public void addNPCPacket(Player... players) {
        Object packetPlayOutPlayerInfo = PacketUtils.getPlayerInfoPacket(npc, "ADD_PLAYER");
        Object packetPlayOutNamedEntitySpawn = PacketUtils.getAddPlayerPacket(npc);
        Object packetPlayOutEntityHeadRotation = PacketUtils.getHeadRotatePacket(npc, yaw);
        Object packetPlayOutEntityMetadata = PacketUtils.getPacketPlayOutEntityMetadata(id,
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
        Object packetPlayOutPlayerInfo = PacketUtils.getPlayerInfoPacket(npc, "REMOVE_PLAYER");
        Object packetPlayOutEntityDestroy = PacketUtils.getRemoveEntitiesPacket(PacketUtils.getEntityId(npc));

        for (Player player : players) {
            ReflectionUtils.sendPacket(player,
                    packetPlayOutPlayerInfo,
                    packetPlayOutEntityDestroy);
        }
    }

}
