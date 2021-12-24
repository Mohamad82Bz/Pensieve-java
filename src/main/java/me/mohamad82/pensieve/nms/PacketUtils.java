package me.mohamad82.pensieve.nms;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Pair;
import me.Mohamad82.RUoM.XSeries.ReflectionUtils;
import me.Mohamad82.RUoM.adventureapi.ComponentUtils;
import me.Mohamad82.RUoM.adventureapi.adventure.platform.bukkit.MinecraftComponentSerializer;
import me.Mohamad82.RUoM.utils.ServerVersion;
import me.Mohamad82.RUoM.vector.Vector3;
import me.mohamad82.pensieve.nms.accessors.*;
import me.mohamad82.pensieve.nms.npc.NPC;
import me.mohamad82.pensieve.nms.npc.enums.NPCState;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PacketUtils {

    private static Class<?> ENTITY_PLAYER, ENTITY_HUMAN, ENTITY_POSE, ENTITY, ENTITY_TYPES, ENUM_PLAYER_INFO_ACTION, PLAYER_INFO_DATA,
            BLOCK_POSITION, BLOCK, CRAFT_ITEM_STACK, PACKET_PLAY_OUT_PLAYER_INFO, PACKET_PLAY_OUT_NAMED_ENTITY_SPAWN, PACKET_PLAY_OUT_ENTITY_HEAD_ROTATION,
            PACKET_PLAY_OUT_ENTITY_DESTROY, PACKET_PLAY_OUT_ENTITY_LOOK, PACKET_PLAY_OUT_REL_ENTITY_MOVE, PACKET_PLAY_OUT_REL_ENTITY_MOVE_LOOK,
            PACKET_PLAY_OUT_ANIMATION, PACKET_PLAY_OUT_BLOCK_BREAK_ANIMATION, PACKET_PLAY_OUT_ENTITY_METADATA, PACKET_PLAY_OUT_ENTITY_EQUIPMENT,
            PACKET_PLAY_OUT_ENTITY_TELEPORT, PACKET_PLAY_OUT_SPAWN_ENTITY, PACKET_PLAY_OUT_ENTITY_VELOCITY, PACKET_PLAY_OUT_COLLECT, PACKET_PLAY_OUT_BLOCK_ACTION,
            PACKET_PLAY_OUT_MOUNT, VEC_3D, DATAWATCHER, DATAWATCHER_OBJECT, DATAWATCHER_SERIALIZER, DATAWATCHER_REGISTRY, ENUM_GAMEMODE, ICHAT_BASE_COMPONENT;

    private static Constructor<?> PACKET_PLAY_OUT_PLAYER_INFO_EMPTY_CONSTRUCTOR, PLAYER_INFO_DATA_CONSTRUCTOR, PACKET_PLAY_OUT_ENTITY_HEAD_ROTATION_EMPTY_CONSTRUCTOR,
            PACKET_PLAY_OUT_BLOCK_BREAK_ANIMATION_CONSTRUCTOR, PACKET_PLAY_OUT_ENTITY_METADATA_CONSTRUCTOR,
            PACKET_PLAY_OUT_ENTITY_TELEPORT_EMPTY_CONSTRUCTOR, PACKET_PLAY_OUT_SPAWN_ENTITY_CONSTRUCTOR, PACKET_PLAY_OUT_MOUNT_EMPTY_CONSTRUCTOR, VEC_3D_CONSTRUCTOR,
            DATAWATCHER_CONSTRUCTOR, DATAWATCHER_OBJECT_CONSTRUCTOR, BLOCK_POSITION_CONSTRUCTOR;

    private static Method DATAWATCHER_REGISTER_METHOD, ENTITY_PLAYER_GETID_METHOD;

    private static Field PACKET_PLAY_OUT_PLAYER_INFO_A_FIELD, PACKET_PLAY_OUT_PLAYER_INFO_B_FIELD,
            PACKET_PLAY_OUT_ENTITY_HEAD_ROTATION_ID_FIELD, PACKET_PLAY_OUT_ENTITY_HEAD_ROTATION_BYTE_FIELD, PACKET_PLAY_OUT_ENTITY_TELEPORT_ID_FIELD,
            PACKET_PLAY_OUT_ENTITY_TELEPORT_X_FIELD, PACKET_PLAY_OUT_ENTITY_TELEPORT_Y_FIELD, PACKET_PLAY_OUT_ENTITY_TELEPORT_Z_FIELD,
            PACKET_PLAY_OUT_ENTITY_TELEPORT_YAW_FIELD, PACKET_PLAY_OUT_ENTITY_TELEPORT_PITCH_FIELD, PACKET_PLAY_OUT_ENTITY_TELEPORT_ON_GROUND_FIELD,
            PACKET_PLAY_OUT_MOUNT_ENTITY_ID_FIELD, PACKET_PLAY_OUT_MOUNT_PASSENGERS_INT_ARRAY_FIELD,
            DATAWATCHER_REGISTRY_BYTE, DATAWATCHER_REGISTRY_INTEGER, DATAWATCHER_REGISTRY_FLOAT, DATAWATCHER_REGISTRY_STRING,
            DATAWATCHER_REGISTRY_ICHATBASECOMPONENT, DATA_WATCHER_REGISTRY_OPT_ICHAT_BASE_COMPONENT, DATAWATCHER_REGISTRY_ITEMSTACK, DATAWATCHER_REGISTRY_BOOLEAN, DATAWATCHER_REGISTRY_PARTICLEPARAM,
            DATAWATCHER_REGISTRY_VECTOR3F;

    static {
        try {
            {
                ENTITY_PLAYER = ReflectionUtils.getNMSClass("server.level", "EntityPlayer");
                ENTITY_HUMAN = ReflectionUtils.getNMSClass("world.entity.player", "EntityHuman");
                ENTITY_POSE = ReflectionUtils.getNMSClass("world.entity", "EntityPose");
                ENTITY = ReflectionUtils.getNMSClass("world.entity", "Entity");
                ENTITY_TYPES = ReflectionUtils.getNMSClass("world.entity", "EntityTypes");
                ENUM_PLAYER_INFO_ACTION = ReflectionUtils.getNMSClass("network.protocol.game", "PacketPlayOutPlayerInfo$EnumPlayerInfoAction");
                PLAYER_INFO_DATA = ReflectionUtils.getNMSClass("network.protocol.game", "PacketPlayOutPlayerInfo$PlayerInfoData");
                BLOCK_POSITION = ReflectionUtils.getNMSClass("core", "BlockPosition");
                BLOCK = ReflectionUtils.getNMSClass("world.level.block", "Block");
                CRAFT_ITEM_STACK = ReflectionUtils.getCraftClass("inventory.CraftItemStack");
                PACKET_PLAY_OUT_PLAYER_INFO = ReflectionUtils.getNMSClass("network.protocol.game", "PacketPlayOutPlayerInfo");
                PACKET_PLAY_OUT_NAMED_ENTITY_SPAWN = ReflectionUtils.getNMSClass("network.protocol.game", "PacketPlayOutNamedEntitySpawn");
                PACKET_PLAY_OUT_ENTITY_HEAD_ROTATION = ReflectionUtils.getNMSClass("network.protocol.game", "PacketPlayOutEntityHeadRotation");
                PACKET_PLAY_OUT_ENTITY_DESTROY = ReflectionUtils.getNMSClass("network.protocol.game", "PacketPlayOutEntityDestroy");
                PACKET_PLAY_OUT_ENTITY_LOOK = ReflectionUtils.getNMSClass("network.protocol.game", "PacketPlayOutEntity$PacketPlayOutEntityLook");
                PACKET_PLAY_OUT_REL_ENTITY_MOVE = ReflectionUtils.getNMSClass("network.protocol.game", "PacketPlayOutEntity$PacketPlayOutRelEntityMove");
                PACKET_PLAY_OUT_REL_ENTITY_MOVE_LOOK = ReflectionUtils.getNMSClass("network.protocol.game", "PacketPlayOutEntity$PacketPlayOutRelEntityMoveLook");
                PACKET_PLAY_OUT_ANIMATION = ReflectionUtils.getNMSClass("network.protocol.game", "PacketPlayOutAnimation");
                PACKET_PLAY_OUT_BLOCK_BREAK_ANIMATION = ReflectionUtils.getNMSClass("network.protocol.game", "PacketPlayOutBlockBreakAnimation");
                PACKET_PLAY_OUT_ENTITY_METADATA = ReflectionUtils.getNMSClass("network.protocol.game", "PacketPlayOutEntityMetadata");
                PACKET_PLAY_OUT_ENTITY_EQUIPMENT = ReflectionUtils.getNMSClass("network.protocol.game", "PacketPlayOutEntityEquipment");
                PACKET_PLAY_OUT_ENTITY_TELEPORT = ReflectionUtils.getNMSClass("network.protocol.game", "PacketPlayOutEntityTeleport");
                PACKET_PLAY_OUT_SPAWN_ENTITY = ReflectionUtils.getNMSClass("network.protocol.game", "PacketPlayOutSpawnEntity");
                PACKET_PLAY_OUT_ENTITY_VELOCITY = ReflectionUtils.getNMSClass("network.protocol.game", "PacketPlayOutEntityVelocity");
                PACKET_PLAY_OUT_COLLECT = ReflectionUtils.getNMSClass("network.protocol.game", "PacketPlayOutCollect");
                PACKET_PLAY_OUT_BLOCK_ACTION = ReflectionUtils.getNMSClass("network.protocol.game", "PacketPlayOutBlockAction");
                PACKET_PLAY_OUT_MOUNT = ReflectionUtils.getNMSClass("network.protocol.game", "PacketPlayOutMount");
                VEC_3D = ReflectionUtils.getNMSClass("world.phys", "Vec3D");
                DATAWATCHER = ReflectionUtils.getNMSClass("network.syncher", "DataWatcher");
                DATAWATCHER_OBJECT = ReflectionUtils.getNMSClass("network.syncher", "DataWatcherObject");
                DATAWATCHER_SERIALIZER = ReflectionUtils.getNMSClass("network.syncher", "DataWatcherSerializer");
                DATAWATCHER_REGISTRY = ReflectionUtils.getNMSClass("network.syncher", "DataWatcherRegistry");
                ENUM_GAMEMODE = ReflectionUtils.getNMSClass("world.level", ServerVersion.supports(9) ? "EnumGamemode" : "WorldSettings$EnumGamemode");
                ICHAT_BASE_COMPONENT = ReflectionUtils.getNMSClass("network.chat", "IChatBaseComponent");
            }
            {
                PACKET_PLAY_OUT_PLAYER_INFO_EMPTY_CONSTRUCTOR = PACKET_PLAY_OUT_PLAYER_INFO.getConstructor();
                PLAYER_INFO_DATA_CONSTRUCTOR = PLAYER_INFO_DATA.getConstructor(PACKET_PLAY_OUT_PLAYER_INFO, GameProfile.class, int.class, ENUM_GAMEMODE, ICHAT_BASE_COMPONENT);
                PACKET_PLAY_OUT_ENTITY_HEAD_ROTATION_EMPTY_CONSTRUCTOR = PACKET_PLAY_OUT_ENTITY_HEAD_ROTATION.getConstructor();
                PACKET_PLAY_OUT_BLOCK_BREAK_ANIMATION_CONSTRUCTOR = PACKET_PLAY_OUT_BLOCK_BREAK_ANIMATION.getConstructor(int.class, BLOCK_POSITION, int.class);
                PACKET_PLAY_OUT_ENTITY_METADATA_CONSTRUCTOR = PACKET_PLAY_OUT_ENTITY_METADATA.getConstructor(int.class, DATAWATCHER, boolean.class);
                PACKET_PLAY_OUT_ENTITY_TELEPORT_EMPTY_CONSTRUCTOR = PACKET_PLAY_OUT_ENTITY_TELEPORT.getConstructor();
                if (ServerVersion.supports(13)) {
                    PACKET_PLAY_OUT_SPAWN_ENTITY_CONSTRUCTOR = PACKET_PLAY_OUT_SPAWN_ENTITY.getConstructor(int.class, UUID.class, double.class, double.class, double.class, float.class, float.class, ENTITY_TYPES, int.class, VEC_3D);
                    VEC_3D_CONSTRUCTOR = VEC_3D.getConstructor(double.class, double.class, double.class);
                }
                PACKET_PLAY_OUT_MOUNT_EMPTY_CONSTRUCTOR = PACKET_PLAY_OUT_MOUNT.getConstructor();
                DATAWATCHER_CONSTRUCTOR = DATAWATCHER.getConstructor(ENTITY);
                DATAWATCHER_OBJECT_CONSTRUCTOR = DATAWATCHER_OBJECT.getConstructor(int.class, DATAWATCHER_SERIALIZER);
                BLOCK_POSITION_CONSTRUCTOR = BLOCK_POSITION.getConstructor(int.class, int.class, int.class);
            }
            {
                DATAWATCHER_REGISTER_METHOD = DATAWATCHER.getMethod("register", DATAWATCHER_OBJECT, Object.class);
                ENTITY_PLAYER_GETID_METHOD = ENTITY.getMethod("getId");
            }
            {
                PACKET_PLAY_OUT_PLAYER_INFO_A_FIELD = PACKET_PLAY_OUT_PLAYER_INFO.getDeclaredField("a");
                PACKET_PLAY_OUT_PLAYER_INFO_B_FIELD = PACKET_PLAY_OUT_PLAYER_INFO.getDeclaredField("b");
                PACKET_PLAY_OUT_ENTITY_HEAD_ROTATION_ID_FIELD = PACKET_PLAY_OUT_ENTITY_HEAD_ROTATION.getDeclaredField("a");
                PACKET_PLAY_OUT_ENTITY_HEAD_ROTATION_BYTE_FIELD = PACKET_PLAY_OUT_ENTITY_HEAD_ROTATION.getDeclaredField("b");
                PACKET_PLAY_OUT_ENTITY_TELEPORT_ID_FIELD = PACKET_PLAY_OUT_ENTITY_TELEPORT.getDeclaredField("a");
                PACKET_PLAY_OUT_ENTITY_TELEPORT_X_FIELD = PACKET_PLAY_OUT_ENTITY_TELEPORT.getDeclaredField("b");
                PACKET_PLAY_OUT_ENTITY_TELEPORT_Y_FIELD = PACKET_PLAY_OUT_ENTITY_TELEPORT.getDeclaredField("c");
                PACKET_PLAY_OUT_ENTITY_TELEPORT_Z_FIELD = PACKET_PLAY_OUT_ENTITY_TELEPORT.getDeclaredField("d");
                PACKET_PLAY_OUT_ENTITY_TELEPORT_YAW_FIELD = PACKET_PLAY_OUT_ENTITY_TELEPORT.getDeclaredField("e");
                PACKET_PLAY_OUT_ENTITY_TELEPORT_PITCH_FIELD = PACKET_PLAY_OUT_ENTITY_TELEPORT.getDeclaredField("f");
                PACKET_PLAY_OUT_ENTITY_TELEPORT_ON_GROUND_FIELD = PACKET_PLAY_OUT_ENTITY_TELEPORT.getDeclaredField("g");
                PACKET_PLAY_OUT_MOUNT_ENTITY_ID_FIELD = PACKET_PLAY_OUT_MOUNT.getDeclaredField("a");
                PACKET_PLAY_OUT_MOUNT_PASSENGERS_INT_ARRAY_FIELD = PACKET_PLAY_OUT_MOUNT.getDeclaredField("b");
                PACKET_PLAY_OUT_PLAYER_INFO_A_FIELD.setAccessible(true);
                PACKET_PLAY_OUT_PLAYER_INFO_B_FIELD.setAccessible(true);
                PACKET_PLAY_OUT_ENTITY_HEAD_ROTATION_ID_FIELD.setAccessible(true);
                PACKET_PLAY_OUT_ENTITY_HEAD_ROTATION_BYTE_FIELD.setAccessible(true);
                PACKET_PLAY_OUT_ENTITY_TELEPORT_ID_FIELD.setAccessible(true);
                PACKET_PLAY_OUT_ENTITY_TELEPORT_X_FIELD.setAccessible(true);
                PACKET_PLAY_OUT_ENTITY_TELEPORT_Y_FIELD.setAccessible(true);
                PACKET_PLAY_OUT_ENTITY_TELEPORT_Z_FIELD.setAccessible(true);
                PACKET_PLAY_OUT_ENTITY_TELEPORT_YAW_FIELD.setAccessible(true);
                PACKET_PLAY_OUT_ENTITY_TELEPORT_PITCH_FIELD.setAccessible(true);
                PACKET_PLAY_OUT_ENTITY_TELEPORT_ON_GROUND_FIELD.setAccessible(true);
                PACKET_PLAY_OUT_MOUNT_ENTITY_ID_FIELD.setAccessible(true);
                PACKET_PLAY_OUT_MOUNT_PASSENGERS_INT_ARRAY_FIELD.setAccessible(true);
                DATAWATCHER_REGISTRY_BYTE = DATAWATCHER_REGISTRY.getField("a");
                DATAWATCHER_REGISTRY_INTEGER = DATAWATCHER_REGISTRY.getField("b");
                DATAWATCHER_REGISTRY_FLOAT = DATAWATCHER_REGISTRY.getField("c");
                DATAWATCHER_REGISTRY_STRING = DATAWATCHER_REGISTRY.getField("d");
                DATAWATCHER_REGISTRY_ICHATBASECOMPONENT = DATAWATCHER_REGISTRY.getField("e");
                DATA_WATCHER_REGISTRY_OPT_ICHAT_BASE_COMPONENT = DATAWATCHER_REGISTRY.getField(ServerVersion.supports(13) ? "f" : "g");
                DATAWATCHER_REGISTRY_ITEMSTACK = DATAWATCHER_REGISTRY.getField(ServerVersion.supports(13) ? "g" : "f");
                DATAWATCHER_REGISTRY_BOOLEAN = DATAWATCHER_REGISTRY.getField(ServerVersion.supports(13) ? "i" : "h");
                if (ServerVersion.supports(13))
                    DATAWATCHER_REGISTRY_PARTICLEPARAM = DATAWATCHER_REGISTRY.getField("j");
                DATAWATCHER_REGISTRY_VECTOR3F = DATAWATCHER_REGISTRY.getField(ServerVersion.supports(13) ? "k" : "i");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Deprecated
    public static Object getPacketPlayOutEntityMetadata(int id, NPCState npcState) {
        try {
            Object entityPose = ENTITY_POSE.getField(npcState.toString().toUpperCase()).get(null);
            Object dataWatcher = DATAWATCHER_CONSTRUCTOR.newInstance((Object) null);
            DATAWATCHER_REGISTER_METHOD.invoke(dataWatcher, DATAWATCHER_OBJECT_CONSTRUCTOR.newInstance(6, DATAWATCHER_REGISTRY.getField("s").get(null)), entityPose);

            return PACKET_PLAY_OUT_ENTITY_METADATA_CONSTRUCTOR.newInstance(id, dataWatcher, true);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error(e);
        }
    }

    @Deprecated
    public static Object getPacketPlayOutEntityMetadata(int id, int metadataId, Object value) {
        try {
            Object dataWatcher = DATAWATCHER_CONSTRUCTOR.newInstance((Object) null);
            Object dataWatcherRegistry = getDataWatcherRegistry(value);
            DATAWATCHER_REGISTER_METHOD.invoke(dataWatcher, DATAWATCHER_OBJECT_CONSTRUCTOR.newInstance(metadataId, dataWatcherRegistry), value);

            return PACKET_PLAY_OUT_ENTITY_METADATA_CONSTRUCTOR.newInstance(id, dataWatcher, true);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error(e);
        }
    }

    @Deprecated
    public static Object getPacketPlayOutBlockBreakAnimation(Vector3 location, int stage) {
        //TODO: WRONG PACKET
        try {
            Object blockPosition = BLOCK_POSITION_CONSTRUCTOR.newInstance(location.getBlockX(), location.getBlockY(), location.getBlockZ());

            return PACKET_PLAY_OUT_BLOCK_BREAK_ANIMATION_CONSTRUCTOR.newInstance(location.getBlockX() + location.getBlockY() + location.getBlockZ(),
                    blockPosition, stage);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error(e);
        }
    }

    @Deprecated
    public static Object getPacketPlayOutEntityTeleport(int id, double x, double y, double z, float yaw, float pitch, boolean onGround) {
        try {
            Object packetPlytOutEntityTeleport = PACKET_PLAY_OUT_ENTITY_TELEPORT_EMPTY_CONSTRUCTOR.newInstance();
            PACKET_PLAY_OUT_ENTITY_TELEPORT_ID_FIELD.set(packetPlytOutEntityTeleport, id);
            PACKET_PLAY_OUT_ENTITY_TELEPORT_X_FIELD.set(packetPlytOutEntityTeleport, x);
            PACKET_PLAY_OUT_ENTITY_TELEPORT_Y_FIELD.set(packetPlytOutEntityTeleport, y);
            PACKET_PLAY_OUT_ENTITY_TELEPORT_Z_FIELD.set(packetPlytOutEntityTeleport, z);
            PACKET_PLAY_OUT_ENTITY_TELEPORT_YAW_FIELD.set(packetPlytOutEntityTeleport, NMSUtils.getAngle(yaw));
            PACKET_PLAY_OUT_ENTITY_TELEPORT_PITCH_FIELD.set(packetPlytOutEntityTeleport, NMSUtils.getAngle(pitch));
            PACKET_PLAY_OUT_ENTITY_TELEPORT_ON_GROUND_FIELD.set(packetPlytOutEntityTeleport, onGround);
            return packetPlytOutEntityTeleport;
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error(e);
        }
    }

    @Deprecated
    public static Object getPacketPlayOutEntityHeadRotation(int id, float yaw) {
        try {
            Object packetEntityHeadRotation = PACKET_PLAY_OUT_ENTITY_HEAD_ROTATION_EMPTY_CONSTRUCTOR.newInstance();
            PACKET_PLAY_OUT_ENTITY_HEAD_ROTATION_ID_FIELD.set(packetEntityHeadRotation, id);
            PACKET_PLAY_OUT_ENTITY_HEAD_ROTATION_BYTE_FIELD.set(packetEntityHeadRotation, NMSUtils.getAngle(yaw));

            return packetEntityHeadRotation;
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error(e);
        }
    }

    @Deprecated
    public static Object getPacketPlayOutMount(int id, int... passengerIds) {
        try {
            Object packet = PACKET_PLAY_OUT_MOUNT_EMPTY_CONSTRUCTOR.newInstance();

            PACKET_PLAY_OUT_MOUNT_ENTITY_ID_FIELD.set(packet, id);
            PACKET_PLAY_OUT_MOUNT_PASSENGERS_INT_ARRAY_FIELD.set(packet, passengerIds);

            return packet;
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error(e);
        }
    }

    @Deprecated
    @SuppressWarnings("unchecked")
    public static Object getPacketPlayOutPlayerInfoTabListUpdate(Object entityPlayer, GameProfile profile, String newName) {
        try {
            Object packet = PACKET_PLAY_OUT_PLAYER_INFO_EMPTY_CONSTRUCTOR.newInstance();
            Object data = PLAYER_INFO_DATA_CONSTRUCTOR.newInstance(packet, profile, 0, ENUM_GAMEMODE.getField("NOT_SET").get(null), MinecraftComponentSerializer.get().serialize(ComponentUtils.parse(newName)));
            List<Object> players = (List<Object>) PACKET_PLAY_OUT_PLAYER_INFO_B_FIELD.get(packet);
            players.add(data);
            PACKET_PLAY_OUT_PLAYER_INFO_A_FIELD.set(packet, ENUM_PLAYER_INFO_ACTION.getField("ADD_PLAYER").get(null));
            PACKET_PLAY_OUT_PLAYER_INFO_B_FIELD.set(packet, players);

            return packet;
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error(e);
        }
    }

    public static Object getPlayerInfoPacket(Object serverPlayer, String action) {
        try {
            Object playerInfoAction = ClientboundPlayerInfoPacket_i_ActionAccessor.getType().getField(action.toUpperCase()).get(null);
            Object serverPlayerArray = Array.newInstance(ServerPlayerAccessor.getType(), 1);
            Array.set(serverPlayerArray, 0, serverPlayer);

            return ClientboundPlayerInfoPacketAccessor.getConstructor0().newInstance(playerInfoAction, serverPlayerArray);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error(e);
        }
    }

    public static Object getAddPlayerPacket(Object player) {
        try {
            return ClientboundAddPlayerPacketAccessor.getConstructor0().newInstance(player);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error(e);
        }
    }

    public static Object getAddEntityPacket(UUID uuid, Location location, String entityTypeName) {
        try {
            if (ServerVersion.supports(13)) {
                Object entityType = ENTITY_TYPES.getField(entityTypeName).get(null);
                Object vec3d = VEC_3D_CONSTRUCTOR.newInstance(0d, 0d, 0d);

                return PACKET_PLAY_OUT_SPAWN_ENTITY_CONSTRUCTOR.newInstance(
                        uuid.hashCode(), uuid,
                        location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch(),
                        entityType, 0, vec3d
                );
            } else if (ServerVersion.supports(9)) {

            } else {

            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error(e);
        }
        //TODO: 1.12 and lower comtatibility. And remove this next line.
        return null;
    }

    public static Object getAddEntityPacket(Object entity, int data) {
        try {
            return ClientboundAddEntityPacketAccessor.getConstructor1().newInstance(entity, data);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error(e);
        }
    }

    public static Object getAddEntityPacket(Object entity) {
        return getAddEntityPacket(entity, 0);
    }

    public static Object getHeadRotatePacket(Object entity, float yaw) {
        try {
            return ClientboundRotateHeadPacketAccessor.getConstructor0().newInstance(entity, NMSUtils.getAngle(yaw));
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error(e);
        }
    }

    public static Object getRemoveEntitiesPacket(int... ids) {
        try {
            Object idArray = Array.newInstance(int.class, ids.length);
            for (int i = 0; i < ids.length; i++) {
                Array.set(idArray, i, ids[i]);
            }

            return ClientboundRemoveEntitiesPacketAccessor.getConstructor0().newInstance(idArray);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error(e);
        }
    }

    public static Object getEntityRotPacket(int id, float yaw, float pitch) {
        try {
            return ClientboundMoveEntityPacket_i_RotAccessor.getConstructor0().newInstance(id, NMSUtils.getAngle(yaw), NMSUtils.getAngle(pitch), true);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error(e);
        }
    }

    public static Object getEntityPosPacket(int id, double x, double y, double z) {
        try {
            return ClientboundMoveEntityPacket_i_PosAccessor.getConstructor0().newInstance(id,
                    (short) (x * 4096), (short) (y * 4096), (short) (z * 4096), true);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error(e);
        }
    }

    public static Object getEntityPosRotPacket(int id, double x, double y, double z, float yaw, float pitch, boolean onGround) {
        try {
            return ClientboundMoveEntityPacket_i_PosRotAccessor.getConstructor0().newInstance(id,
                    (short) (x * 4096), (short) (y * 4096), (short) (z * 4096),
                    NMSUtils.getAngle(yaw), NMSUtils.getAngle(pitch), onGround);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error(e);
        }
    }

    public static Object getTeleportEntityPacket(Object entity) {
        try {
            return ClientboundTeleportEntityPacketAccessor.getConstructor0().newInstance(entity);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error(e);
        }
    }

    public static Object getEntityVelocityPacket(int id, double x, double y, double z) {
        try {
            if (ServerVersion.supports(14)) {
                return ClientboundSetEntityMotionPacketAccessor.getConstructor0().newInstance(id, Vec3Accessor.getConstructor0().newInstance(x, y, z));
            } else {
                return ClientboundSetEntityMotionPacketAccessor.getConstructor1().newInstance(id, x, y, z);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error(e);
        }
    }

    public static Object getAnimatePacket(Object entity, int action) {
        try {
            return ClientboundAnimatePacketAccessor.getConstructor0().newInstance(entity, action);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error(e);
        }
    }

    public static Object getBlockDestructionPacket(Vector3 location, int stage) {
        try {
            return ClientboundBlockDestructionPacketAccessor.getConstructor0().newInstance(
                    BlockPosAccessor.getConstructor0().newInstance(location.getBlockX(), location.getBlockY(), location.getBlockZ()),
                    stage
            );
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error(e);
        }
    }

    public static Object getEntityEquipmentPacket(int id, NPC.EquipmentSlot slot, ItemStack item) {
        try {
            Object nmsItem = NMSUtils.getNmsItemStack(item);
            Pair<Object, Object> pair = new Pair<>(slot.getNmsSlot(), nmsItem);
            List<Pair<Object, Object>> pairList = new ArrayList<>();
            pairList.add(pair);

            return ClientboundSetEquipmentPacketAccessor.getConstructor0().newInstance(id, pairList);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error(e);
        }
    }

    public static Object getCollectItemPacket(int id, int collectorId, int itemAmount) {
        try {
            if (ServerVersion.supports(9)) {
                return ClientboundTakeItemEntityPacketAccessor.getConstructor0().newInstance(id, collectorId, itemAmount);
            } else {
                return ClientboundTakeItemEntityPacketAccessor.getConstructor1().newInstance(id, collectorId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error(e);
        }
    }

    public static Object getBlockEventPacket(Vector3 location, Material blockMaterial, int actionId, int actionParam) {
        try {
            return ClientboundBlockEventPacketAccessor.getConstructor0().newInstance(
                    BlockPosAccessor.getConstructor0().newInstance(location.getBlockX(), location.getBlockY(), location.getBlockZ()),
                    BlocksAccessor.getType().getField(blockMaterial.toString().toUpperCase()).get(null),
                    actionId, actionParam
            );
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error(e);
        }
    }

    public static Object getEntityPassengersPacket(Object entity, int... passengerIds) {
        try {
            Object packet = ClientboundSetPassengersPacketAccessor.getConstructor0().newInstance(entity);
            ClientboundSetPassengersPacketAccessor.getFieldPassengers().set(packet, passengerIds);

            return packet;
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error(e);
        }
    }

    public static Object getEntityDataPacket(Object entity) {
        try {
            return ClientboundSetEntityDataPacketAccessor.getConstructor0().newInstance(
                    EntityAccessor.getMethodGetId1().invoke(entity),
                    EntityAccessor.getMethodGetEntityData1().invoke(entity),
                    true
            );
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error(e);
        }
    }

    public static Object getEntityDataPacket(int id, int metadataId, Object value) {
        try {
            Object entityData = SynchedEntityDataAccessor.getConstructor0().newInstance((Object) null);
            Object entityDataSerializer = getEntityDataSerializer(value);
            SynchedEntityDataAccessor.getMethodDefine1().invoke(entityData, EntityDataSerializerAccessor.getMethodCreateAccessor1().invoke(entityDataSerializer, metadataId), value);

            return ClientboundSetEntityDataPacketAccessor.getConstructor0().newInstance(id, entityData, true);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error(e);
        }
    }

    public static Object getEntityDataSerializer(Object object) {
        try {
            switch (object.getClass().getSimpleName()) {
                case "Byte":
                    return EntityDataSerializersAccessor.getFieldBYTE().get(null);
                case "Integer":
                    return EntityDataSerializersAccessor.getFieldINT().get(null);
                case "Float":
                    return EntityDataSerializersAccessor.getFieldFLOAT().get(null);
                case "String":
                    return EntityDataSerializersAccessor.getFieldSTRING().get(null);
                case "Optional":
                    return EntityDataSerializersAccessor.getFieldOPTIONAL_COMPONENT().get(null);
                case "ItemStack":
                    return EntityDataSerializersAccessor.getFieldITEM_STACK().get(null);
                case "Boolean":
                    return EntityDataSerializersAccessor.getFieldBOOLEAN().get(null);
                default: {
                    if (object.getClass().equals(ComponentAccessor.getType())) {
                        return EntityDataSerializersAccessor.getFieldCOMPONENT();
                    } else if (object.getClass().equals(PoseAccessor.getType())) {
                        return EntityDataSerializersAccessor.getFieldPOSE();
                    } else {
                        return null;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error(e);
        }
    }

    @Deprecated
    public static Object getDataWatcherRegistry(Object object) {
        try {
            switch (object.getClass().getSimpleName()) {
                case "Byte":
                    return DATAWATCHER_REGISTRY_BYTE.get(null);
                case "Integer":
                    return DATAWATCHER_REGISTRY_INTEGER.get(null);
                case "Float":
                    return DATAWATCHER_REGISTRY_FLOAT.get(null);
                case "String":
                    return DATAWATCHER_REGISTRY_STRING.get(null);
                case "IChatBaseComponent":
                    return DATAWATCHER_REGISTRY_ICHATBASECOMPONENT.get(null);
                case "Optional":
                    return DATA_WATCHER_REGISTRY_OPT_ICHAT_BASE_COMPONENT.get(null);
                case "ItemStack":
                    return DATAWATCHER_REGISTRY_ITEMSTACK.get(null);
                case "Boolean":
                    return DATAWATCHER_REGISTRY_BOOLEAN.get(null);
                case "ParticleParam":
                    return DATAWATCHER_REGISTRY_PARTICLEPARAM.get(null);
                case "Vector3f":
                    return DATAWATCHER_REGISTRY_VECTOR3F.get(null);
                default:
                    return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error(e);
        }
    }

    @Deprecated
    public static int getEntityId(Object entity) {
        try {
            return (int) ENTITY_PLAYER_GETID_METHOD.invoke(entity);
        } catch (Exception e) {
            e.printStackTrace();
            return UUID.randomUUID().hashCode();
        }
    }

}