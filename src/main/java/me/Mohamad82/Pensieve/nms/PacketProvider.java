package me.Mohamad82.Pensieve.nms;

import com.mojang.datafixers.util.Pair;
import me.Mohamad82.Pensieve.nms.enums.NPCAnimation;
import me.Mohamad82.Pensieve.nms.enums.NPCState;
import me.Mohamad82.RUoM.XSeries.ReflectionUtils;
import me.Mohamad82.RUoM.utils.ServerVersion;
import me.Mohamad82.RUoM.vector.Vector3;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PacketProvider {

    private static Class<?> ENTITY_PLAYER, ENTITY_HUMAN, ENTITY_POSE, ENTITY, ENTITY_TYPES, ENUM_PLAYER_INFO_ACTION, ENUM_ITEM_SLOT, BLOCK_POSITION, CRAFT_ITEM_STACK,
            PACKET_PLAY_OUT_PLAYER_INFO, PACKET_PLAY_OUT_NAMED_ENTITY_SPAWN, PACKET_PLAY_OUT_ENTITY_HEAD_ROTATION, PACKET_PLAY_OUT_ENTITY_DESTROY,
            PACKET_PLAY_OUT_ENTITY_LOOK, PACKET_PLAY_OUT_REL_ENTITY_MOVE, PACKET_PLAY_OUT_REL_ENTITY_MOVE_LOOK, PACKET_PLAY_OUT_ANIMATION,
            PACKET_PLAY_OUT_BLOCK_BREAK_ANIMATION, PACKET_PLAY_OUT_ENTITY_METADATA, PACKET_PLAY_OUT_ENTITY_EQUIPMENT, PACKET_PLAY_OUT_ENTITY_TELEPORT,
            PACKET_PLAY_OUT_SPAWN_ENTITY, PACKET_PLAY_OUT_ENTITY_VELOCITY, VEC_3D, DATAWATCHER, DATAWATCHER_OBJECT, DATAWATCHER_SERIALIZER, DATAWATCHER_REGISTRY,
            CRAFT_PLAYER;
    private static Constructor<?> PACKET_PLAYER_INFO_CONSTRUCTOR, PACKET_NAMED_ENTITY_SPAWN_CONSTRUCTOR, PACKET_ENTITY_HEAD_ROTATION_CONSTRUCTOR,
            PACKET_ENTITY_HEAD_ROTATION_EMPTY_CONSTRUCTOR, PACKET_ENTITY_DESTROY_CONSTRUCTOR, PACKET_ENTITY_LOOK_CONSTRUCTOR, PACKET_REL_ENTITY_MOVE_CONSTRUCTOR,
            PACKET_REL_ENTITY_MOVE_LOOK_CONSTRUCTOR, PACKET_ANIMATION_CONSTRUCTOR, PACKET_BLOCK_BREAK_ANIMATION_CONSTRUCTOR, PACKET_ENTITY_EQUIPMENT_CONSTRUCTOR,
            PACKET_ENTITY_METADATA_CONSTRUCTOR, PACKET_ENTITY_TELEPORT_CONSTRUCTOR, PACKET_ENTITY_TELEPORT_EMPTY_CONSTRUCTOR,
            PACKET_PLAY_OUT_SPAWN_ENTITY_CONSTRUCTOR, PACKET_PLAY_OUT_ENTITY_VELOCITY_CONSTRUCTOR,VEC_3D_CONSTRUCTOR, DATAWATCHER_CONSTRUCTOR,
            DATAWATCHER_OBJECT_CONSTRUCTOR, BLOCK_POSITION_CONSTRUCTOR;
    private static Method CRAFT_ITEM_STACK_AS_NMS_COPY_METHOD, DATAWATCHER_REGISTER_METHOD, ENTITY_PLAYER_GETID_METHOD, ENTITY_SET_LOCATION_METHOD,
            ENTITY_TYPES_GET_METHOD, CRAFT_PLAYER_GET_HANDLE_METHOD;
    private static Field ENTITY_ON_GROUND_FIELD, PACKET_PLAY_OUT_ENTITY_HEAD_ROTATION_ID_FIELD, PACKET_PLAY_OUT_ENTITY_HEAD_ROTATION_BYTE_FIELD,
            PACKET_PLAY_OUT_ENTITY_TELEPORT_ID_FIELD, PACKET_PLAY_OUT_ENTITY_TELEPORT_X_FIELD, PACKET_PLAY_OUT_ENTITY_TELEPORT_Y_FIELD,
            PACKET_PLAY_OUT_ENTITY_TELEPORT_Z_FIELD, PACKET_PLAY_OUT_ENTITY_TELEPORT_YAW_FIELD, PACKET_PLAY_OUT_ENTITY_TELEPORT_PITCH_FIELD,
            PACKET_PLAY_OUT_ENTITY_TELEPORT_ON_GROUND_FIELD, DATAWATCHER_REGISTRY_BYTE, DATAWATCHER_REGISTRY_INTEGER, DATAWATCHER_REGISTRY_FLOAT,
            DATAWATCHER_REGISTRY_STRING, DATAWATCHER_REGISTRY_ICHATBASECOMPONENT, DATAWATCHER_REGISTRY_ITEMSTACK, DATAWATCHER_REGISTRY_BOOLEAN,
            DATAWATCHER_REGISTRY_PARTICLEPARAM, DATAWATCHER_REGISTRY_VECTOR3F;

    static {
        try {
            {
                ENTITY_PLAYER = ReflectionUtils.getNMSClass("server.level", "EntityPlayer");
                ENTITY_HUMAN = ReflectionUtils.getNMSClass("world.entity.player", "EntityHuman");
                ENTITY_POSE = ReflectionUtils.getNMSClass("world.entity", "EntityPose");
                ENTITY = ReflectionUtils.getNMSClass("world.entity", "Entity");
                ENTITY_TYPES = ReflectionUtils.getNMSClass("world.entity", "EntityTypes");
                ENUM_PLAYER_INFO_ACTION = ReflectionUtils.getNMSClass("network.protocol.game", "PacketPlayOutPlayerInfo$EnumPlayerInfoAction");
                ENUM_ITEM_SLOT = ReflectionUtils.getNMSClass("world.entity", "EnumItemSlot");
                BLOCK_POSITION = ReflectionUtils.getNMSClass("core", "BlockPosition");
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
                VEC_3D = ReflectionUtils.getNMSClass("world.phys", "Vec3D");
                DATAWATCHER = ReflectionUtils.getNMSClass("network.syncher", "DataWatcher");
                DATAWATCHER_OBJECT = ReflectionUtils.getNMSClass("network.syncher", "DataWatcherObject");
                DATAWATCHER_SERIALIZER = ReflectionUtils.getNMSClass("network.syncher", "DataWatcherSerializer");
                DATAWATCHER_REGISTRY = ReflectionUtils.getNMSClass("network.syncher", "DataWatcherRegistry");
                CRAFT_PLAYER = ReflectionUtils.getCraftClass("entity.CraftPlayer");
            }
            {
                PACKET_PLAYER_INFO_CONSTRUCTOR = PACKET_PLAY_OUT_PLAYER_INFO.getConstructor(ENUM_PLAYER_INFO_ACTION, Array.newInstance(ENTITY_PLAYER, 1).getClass());
                PACKET_NAMED_ENTITY_SPAWN_CONSTRUCTOR = PACKET_PLAY_OUT_NAMED_ENTITY_SPAWN.getConstructor(ENTITY_HUMAN);
                PACKET_ENTITY_HEAD_ROTATION_CONSTRUCTOR = PACKET_PLAY_OUT_ENTITY_HEAD_ROTATION.getConstructor(ENTITY, byte.class);
                PACKET_ENTITY_HEAD_ROTATION_EMPTY_CONSTRUCTOR = PACKET_PLAY_OUT_ENTITY_HEAD_ROTATION.getConstructor();
                PACKET_ENTITY_DESTROY_CONSTRUCTOR = PACKET_PLAY_OUT_ENTITY_DESTROY.getConstructor(int[].class);
                PACKET_ENTITY_LOOK_CONSTRUCTOR = PACKET_PLAY_OUT_ENTITY_LOOK.getConstructor(int.class, byte.class, byte.class, boolean.class);
                PACKET_REL_ENTITY_MOVE_CONSTRUCTOR = PACKET_PLAY_OUT_REL_ENTITY_MOVE.getConstructor(int.class, short.class, short.class, short.class, boolean.class);
                PACKET_REL_ENTITY_MOVE_LOOK_CONSTRUCTOR = PACKET_PLAY_OUT_REL_ENTITY_MOVE_LOOK.getConstructor(int.class, short.class, short.class, short.class, byte.class, byte.class, boolean.class);
                PACKET_ANIMATION_CONSTRUCTOR = PACKET_PLAY_OUT_ANIMATION.getConstructor(ENTITY, int.class);
                PACKET_BLOCK_BREAK_ANIMATION_CONSTRUCTOR = PACKET_PLAY_OUT_BLOCK_BREAK_ANIMATION.getConstructor(int.class, BLOCK_POSITION, int.class);
                PACKET_ENTITY_EQUIPMENT_CONSTRUCTOR = PACKET_PLAY_OUT_ENTITY_EQUIPMENT.getConstructor(int.class, List.class);
                PACKET_ENTITY_METADATA_CONSTRUCTOR = PACKET_PLAY_OUT_ENTITY_METADATA.getConstructor(int.class, DATAWATCHER, boolean.class);
                PACKET_ENTITY_TELEPORT_CONSTRUCTOR = PACKET_PLAY_OUT_ENTITY_TELEPORT.getConstructor(ENTITY);
                PACKET_ENTITY_TELEPORT_EMPTY_CONSTRUCTOR = PACKET_PLAY_OUT_ENTITY_TELEPORT.getConstructor();
                if (ServerVersion.supports(13)) {
                    PACKET_PLAY_OUT_SPAWN_ENTITY_CONSTRUCTOR = PACKET_PLAY_OUT_SPAWN_ENTITY.getConstructor(int.class, UUID.class, double.class, double.class, double.class, float.class, float.class, ENTITY_TYPES, int.class, VEC_3D);
                    VEC_3D_CONSTRUCTOR = VEC_3D.getConstructor(double.class, double.class, double.class);
                }
                if (ServerVersion.supports(14)) {
                    PACKET_PLAY_OUT_ENTITY_VELOCITY_CONSTRUCTOR = PACKET_PLAY_OUT_ENTITY_VELOCITY.getConstructor(int.class, VEC_3D);
                } else {
                    PACKET_PLAY_OUT_ENTITY_VELOCITY_CONSTRUCTOR = PACKET_PLAY_OUT_ENTITY_VELOCITY.getConstructor(int.class, double.class, double.class, double.class);
                }
                DATAWATCHER_CONSTRUCTOR = DATAWATCHER.getConstructor(ENTITY);
                DATAWATCHER_OBJECT_CONSTRUCTOR = DATAWATCHER_OBJECT.getConstructor(int.class, DATAWATCHER_SERIALIZER);
                BLOCK_POSITION_CONSTRUCTOR = BLOCK_POSITION.getConstructor(int.class, int.class, int.class);
            }
            {
                CRAFT_ITEM_STACK_AS_NMS_COPY_METHOD = CRAFT_ITEM_STACK.getMethod("asNMSCopy", ItemStack.class);
                DATAWATCHER_REGISTER_METHOD = DATAWATCHER.getMethod("register", DATAWATCHER_OBJECT, Object.class);
                ENTITY_PLAYER_GETID_METHOD = ENTITY.getMethod("getId");
                ENTITY_SET_LOCATION_METHOD = ENTITY.getMethod("setLocation", double.class, double.class, double.class, float.class, float.class);
                CRAFT_PLAYER_GET_HANDLE_METHOD = CRAFT_PLAYER.getMethod("getHandle");
            }
            {
                ENTITY_ON_GROUND_FIELD  = ENTITY.getField("onGround");
                PACKET_PLAY_OUT_ENTITY_HEAD_ROTATION_ID_FIELD = PACKET_PLAY_OUT_ENTITY_HEAD_ROTATION.getDeclaredField("a");
                PACKET_PLAY_OUT_ENTITY_HEAD_ROTATION_BYTE_FIELD = PACKET_PLAY_OUT_ENTITY_HEAD_ROTATION.getDeclaredField("b");
                PACKET_PLAY_OUT_ENTITY_TELEPORT_ID_FIELD = PACKET_PLAY_OUT_ENTITY_TELEPORT.getDeclaredField("a");
                PACKET_PLAY_OUT_ENTITY_TELEPORT_X_FIELD = PACKET_PLAY_OUT_ENTITY_TELEPORT.getDeclaredField("b");
                PACKET_PLAY_OUT_ENTITY_TELEPORT_Y_FIELD = PACKET_PLAY_OUT_ENTITY_TELEPORT.getDeclaredField("c");
                PACKET_PLAY_OUT_ENTITY_TELEPORT_Z_FIELD = PACKET_PLAY_OUT_ENTITY_TELEPORT.getDeclaredField("d");
                PACKET_PLAY_OUT_ENTITY_TELEPORT_YAW_FIELD = PACKET_PLAY_OUT_ENTITY_TELEPORT.getDeclaredField("e");
                PACKET_PLAY_OUT_ENTITY_TELEPORT_PITCH_FIELD = PACKET_PLAY_OUT_ENTITY_TELEPORT.getDeclaredField("f");
                PACKET_PLAY_OUT_ENTITY_TELEPORT_ON_GROUND_FIELD = PACKET_PLAY_OUT_ENTITY_TELEPORT.getDeclaredField("g");
                PACKET_PLAY_OUT_ENTITY_HEAD_ROTATION_ID_FIELD.setAccessible(true);
                PACKET_PLAY_OUT_ENTITY_HEAD_ROTATION_BYTE_FIELD.setAccessible(true);
                PACKET_PLAY_OUT_ENTITY_TELEPORT_ID_FIELD.setAccessible(true);
                PACKET_PLAY_OUT_ENTITY_TELEPORT_X_FIELD.setAccessible(true);
                PACKET_PLAY_OUT_ENTITY_TELEPORT_Y_FIELD.setAccessible(true);
                PACKET_PLAY_OUT_ENTITY_TELEPORT_Z_FIELD.setAccessible(true);
                PACKET_PLAY_OUT_ENTITY_TELEPORT_YAW_FIELD.setAccessible(true);
                PACKET_PLAY_OUT_ENTITY_TELEPORT_PITCH_FIELD.setAccessible(true);
                PACKET_PLAY_OUT_ENTITY_TELEPORT_ON_GROUND_FIELD.setAccessible(true);
                DATAWATCHER_REGISTRY_BYTE = DATAWATCHER_REGISTRY.getField("a");
                DATAWATCHER_REGISTRY_INTEGER = DATAWATCHER_REGISTRY.getField("b");
                DATAWATCHER_REGISTRY_FLOAT = DATAWATCHER_REGISTRY.getField("c");
                DATAWATCHER_REGISTRY_STRING = DATAWATCHER_REGISTRY.getField("d");
                DATAWATCHER_REGISTRY_ICHATBASECOMPONENT = DATAWATCHER_REGISTRY.getField("e");
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

    public static Object getPacketPlayOutPlayerInfo(Object entityPlayer, String action) {
        try {
            Object addPlayerEnum = ENUM_PLAYER_INFO_ACTION.getField(action.toUpperCase()).get(null);
            Object entityPlayerArray = Array.newInstance(ENTITY_PLAYER, 1);
            Array.set(entityPlayerArray, 0, entityPlayer);

            return PACKET_PLAYER_INFO_CONSTRUCTOR.newInstance(addPlayerEnum, entityPlayerArray);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error(e);
        }
    }

    public static Object getPacketPlayOutNamedEntitySpawn(Object entityPlayer) {
        try {
            return PACKET_NAMED_ENTITY_SPAWN_CONSTRUCTOR.newInstance(entityPlayer);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error(e);
        }
    }

    public static Object getPacketPlayOutSpawnEntity(UUID uuid, Location location, String entityTypeName) {
        try {
            if (ServerVersion.supports(13)) {
                Object entityType = ENTITY_TYPES.getField(entityTypeName).get(null);
                Object vec3d = VEC_3D_CONSTRUCTOR.newInstance(0d, 0d, 0d);

                return PACKET_PLAY_OUT_SPAWN_ENTITY_CONSTRUCTOR.newInstance(
                        uuid.hashCode(), uuid,
                        location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch(),
                        entityType, 0, vec3d
                );
            } if (ServerVersion.supports(9)) {

            } else {

            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error(e);
        }
        //TODO: 1.12 and lower comtatibility. And remove this next line.
        return null;
    }

    public static Object getPacketPlayOutEntityHeadRotation(int id, float yaw) {
        try {
            Object packetEntityHeadRotation = PACKET_ENTITY_HEAD_ROTATION_EMPTY_CONSTRUCTOR.newInstance();
            PACKET_PLAY_OUT_ENTITY_HEAD_ROTATION_ID_FIELD.set(packetEntityHeadRotation, id);
            PACKET_PLAY_OUT_ENTITY_HEAD_ROTATION_BYTE_FIELD.set(packetEntityHeadRotation, getAngle(yaw));

            return packetEntityHeadRotation;
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error(e);
        }
    }

    public static Object getPacketPlayOutEntityHeadRotation(Object entity, float yaw) {
        try {
            return PACKET_ENTITY_HEAD_ROTATION_CONSTRUCTOR.newInstance(entity, getAngle(yaw));
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error(e);
        }
    }

    public static Object getPacketPlayOutEntityDestroy(int id) {
        try {
            Object ids = Array.newInstance(int.class, 1);
            Array.set(ids, 0, id);

            return PACKET_ENTITY_DESTROY_CONSTRUCTOR.newInstance(ids);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error(e);
        }
    }

    public static Object getPacketPlayOutEntityLook(int id, float yaw, float pitch) {
        try {
            return PACKET_ENTITY_LOOK_CONSTRUCTOR.newInstance(id, getAngle(yaw), getAngle(pitch), true);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error(e);
        }
    }

    public static Object getPacketPlayOutRelEntityMove(int id, double x, double y, double z) {
        try {
            return PACKET_REL_ENTITY_MOVE_CONSTRUCTOR.newInstance(id,
                    (short) (x * 4096), (short) (y * 4096), (short) (z * 4096), true);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error(e);
        }
    }

    public static Object getPacketPlayOutRelEntityMoveLook(int id, double x, double y, double z, float yaw, float pitch, boolean onGround) {
        try {
            return PACKET_REL_ENTITY_MOVE_LOOK_CONSTRUCTOR.newInstance(id,
                    (short) (x * 4096), (short) (y * 4096), (short) (z * 4096),
                    getAngle(yaw), getAngle(pitch), onGround);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error(e);
        }
    }

    public static Object getPacketPlayOutEntityTeleport(Object entity, double x, double y, double z, float yaw, float pitch, boolean onGround) {
        try {
            ENTITY_ON_GROUND_FIELD.set(entity, onGround);
            ENTITY_SET_LOCATION_METHOD.invoke(entity, x, y, z, yaw, pitch);
            return PACKET_ENTITY_TELEPORT_CONSTRUCTOR.newInstance(entity);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error(e);
        }
    }

    public static Object getPacketPlayOutEntityTeleport(int id, double x, double y, double z, float yaw, float pitch, boolean onGround) {
        try {
            Object packetPlytOutEntityTeleport = PACKET_ENTITY_TELEPORT_EMPTY_CONSTRUCTOR.newInstance();
            PACKET_PLAY_OUT_ENTITY_TELEPORT_ID_FIELD.set(packetPlytOutEntityTeleport, id);
            PACKET_PLAY_OUT_ENTITY_TELEPORT_X_FIELD.set(packetPlytOutEntityTeleport, x);
            PACKET_PLAY_OUT_ENTITY_TELEPORT_Y_FIELD.set(packetPlytOutEntityTeleport, y);
            PACKET_PLAY_OUT_ENTITY_TELEPORT_Z_FIELD.set(packetPlytOutEntityTeleport, z);
            PACKET_PLAY_OUT_ENTITY_TELEPORT_YAW_FIELD.set(packetPlytOutEntityTeleport, getAngle(yaw));
            PACKET_PLAY_OUT_ENTITY_TELEPORT_PITCH_FIELD.set(packetPlytOutEntityTeleport, getAngle(pitch));
            PACKET_PLAY_OUT_ENTITY_TELEPORT_ON_GROUND_FIELD.set(packetPlytOutEntityTeleport, onGround);
            return packetPlytOutEntityTeleport;
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error(e);
        }
    }

    public static Object getPacketPlayOutEntityVelocity(int id, double x, double y, double z) {
        try {
            if (ServerVersion.supports(14)) {
                return PACKET_PLAY_OUT_ENTITY_VELOCITY_CONSTRUCTOR.newInstance(id, VEC_3D_CONSTRUCTOR.newInstance(x, y, z));
            } else {
                return PACKET_PLAY_OUT_ENTITY_VELOCITY_CONSTRUCTOR.newInstance(id, x, y, z);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error(e);
        }
    }

    public static Object getPacketPlayOutAnimation(Object entityPlayer, NPCAnimation npcAnimation) {
        try {
            return PACKET_ANIMATION_CONSTRUCTOR.newInstance(entityPlayer, npcAnimation.getValue());
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error(e);
        }
    }

    public static Object getPacketPlayOutBlockBreakAnimation(Vector3 location, int stage) {
        try {
            Object blockPosition = BLOCK_POSITION_CONSTRUCTOR.newInstance(location.getBlockX(), location.getBlockY(), location.getBlockZ());

            return PACKET_BLOCK_BREAK_ANIMATION_CONSTRUCTOR.newInstance(location.getBlockX() + location.getBlockY() + location.getBlockZ(),
                    blockPosition, stage);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error(e);
        }
    }

    public static Object getPacketPlayOutEntityEquipment(int id, EquipmentSlot slot, ItemStack item) {
        try {
            String itemSlot;
            if (slot.equals(EquipmentSlot.HAND))
                itemSlot = "MAINHAND";
            else if (slot.equals(EquipmentSlot.OFF_HAND))
                itemSlot = "OFFHAND";
            else itemSlot = slot.toString().toUpperCase();

            Object enumItemSlot = ENUM_ITEM_SLOT.getField(itemSlot).get(null);
            Object nmsItem = CRAFT_ITEM_STACK_AS_NMS_COPY_METHOD.invoke(null, item);

            Pair<Object, Object> pair = new Pair<>(enumItemSlot, nmsItem);
            List<Pair<Object, Object>> pairList = new ArrayList<>();
            pairList.add(pair);

            return PACKET_ENTITY_EQUIPMENT_CONSTRUCTOR.newInstance(id, pairList);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error(e);
        }
    }

    public static Object getPacketPlayOutEntityMetadata(int id, NPCState npcState) {
        try {
            Object entityPose = ENTITY_POSE.getField(npcState.toString().toUpperCase()).get(null);
            Object dataWatcher = DATAWATCHER_CONSTRUCTOR.newInstance((Object) null);
            DATAWATCHER_REGISTER_METHOD.invoke(dataWatcher, DATAWATCHER_OBJECT_CONSTRUCTOR.newInstance(6, DATAWATCHER_REGISTRY.getField("s").get(null)), entityPose);

            return PACKET_ENTITY_METADATA_CONSTRUCTOR.newInstance(id, dataWatcher, true);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error(e);
        }
    }

    public static Object getPacketPlayOutEntityMetadata(int id, int metadataId, Object value) {
        try {
            Object dataWatcher = DATAWATCHER_CONSTRUCTOR.newInstance((Object) null);
            Object dataWatcherRegistry = getDataWatcherRegistry(value);
            DATAWATCHER_REGISTER_METHOD.invoke(dataWatcher, DATAWATCHER_OBJECT_CONSTRUCTOR.newInstance(metadataId, dataWatcherRegistry), value);

            return PACKET_ENTITY_METADATA_CONSTRUCTOR.newInstance(id, dataWatcher, true);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error(e);
        }
    }

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

    public static Object getNmsPlayer(Player player) {
        try {
            return CRAFT_PLAYER_GET_HANDLE_METHOD.invoke(player);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static int getEntityId(Object entity) {
        try {
            return (int) ENTITY_PLAYER_GETID_METHOD.invoke(entity);
        } catch (Exception e) {
            e.printStackTrace();
            return UUID.randomUUID().hashCode();
        }
    }

    private static byte getAngle(float yawOrPitch) {
        return (byte) (yawOrPitch * 256 / 360);
    }

}
