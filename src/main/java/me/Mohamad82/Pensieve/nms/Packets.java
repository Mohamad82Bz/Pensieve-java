package me.Mohamad82.Pensieve.nms;

import com.mojang.datafixers.util.Pair;
import me.Mohamad82.Pensieve.nms.enums.NPCAnimation;
import me.Mohamad82.Pensieve.nms.enums.NPCState;
import me.Mohamad82.RUoM.XSeries.ReflectionUtils;
import me.Mohamad82.RUoM.vector.Vector3;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

public class Packets {

    public static Object getPacketPlayOutPlayerInfo(Object entityPlayer, String action) {
        try {
            Class<?> ENTITY_PLAYER = ReflectionUtils.getNMSClass("server.level", "EntityPlayer");
            Class<?> ENUM_PLAYER_INFO_ACTION = ReflectionUtils.getNMSClass("network.protocol.game", "PacketPlayOutPlayerInfo$EnumPlayerInfoAction");
            Class<?> PACKET_PLAY_OUT_PLAYER_INFO = ReflectionUtils.getNMSClass("network.protocol.game", "PacketPlayOutPlayerInfo");

            Object addPlayerEnum = ENUM_PLAYER_INFO_ACTION.getField(action.toUpperCase()).get(null);
            Object entityPlayerArray = Array.newInstance(ENTITY_PLAYER, 1);
            Constructor<?> packetPlayerInfoConstructor = PACKET_PLAY_OUT_PLAYER_INFO
                    .getConstructor(ENUM_PLAYER_INFO_ACTION,
                            entityPlayerArray.getClass());
            Array.set(entityPlayerArray, 0, entityPlayer);

            return packetPlayerInfoConstructor.newInstance(addPlayerEnum, entityPlayerArray);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error(e);
        }
    }

    public static Object getPacketPlayOutNamedEntitySpawn(Object entityPlayer) {
        try {
            Class<?> PACKET_PLAY_OUT_NAMED_ENTITY_SPAWN = ReflectionUtils.getNMSClass("network.protocol.game", "PacketPlayOutNamedEntitySpawn");
            Class<?> ENTITY_HUMAN = ReflectionUtils.getNMSClass("world.entity.player", "EntityHuman");

            Constructor<?> packetNamedEntitySpawnConstructor = PACKET_PLAY_OUT_NAMED_ENTITY_SPAWN
                    .getConstructor(ENTITY_HUMAN);

            return packetNamedEntitySpawnConstructor.newInstance(entityPlayer);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error(e);
        }
    }

    public static Object getPacketPlayOutEntityHeadRotation(Object entityPlayer, float yaw) {
        try {
            Class<?> PACKET_PLAY_OUT_ENTITY_HEAD_ROTATION = ReflectionUtils.getNMSClass("network.protocol.game", "PacketPlayOutEntityHeadRotation");
            Class<?> ENTITY = ReflectionUtils.getNMSClass("world.entity", "Entity");

            Constructor<?> packetEntityHeadRotationConstructor = PACKET_PLAY_OUT_ENTITY_HEAD_ROTATION
                    .getConstructor(ENTITY, byte.class);

            return packetEntityHeadRotationConstructor.newInstance(entityPlayer, (byte) (yaw * 256 / 360));
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error(e);
        }
    }

    public static Object getPacketPlayOutEntityDestroy(Object entityPlayer) {
        try {
            Class<?> PACKET_PLAY_OUT_ENTITY_DESTROY = ReflectionUtils.getNMSClass("network.protocol.game", "PacketPlayOutEntityDestroy");

            Constructor<?> packetEntityDestroyConstructor = PACKET_PLAY_OUT_ENTITY_DESTROY
                    .getConstructor(int[].class);
            int id = getEntityPlayerId(entityPlayer);
            Object ids = Array.newInstance(int.class, 1);
            Array.set(ids, 0, id);

            //noinspection JavaReflectionInvocation
            return packetEntityDestroyConstructor.newInstance(ids);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error(e);
        }
    }

    public static Object getPacketPlayOutEntityLook(Object entityPlayer, float yaw, float pitch) {
        try {
            Class<?> PACKET_PLAY_OUT_ENTITY_LOOK = ReflectionUtils.getNMSClass("network.protocol.game", "PacketPlayOutEntity$PacketPlayOutEntityLook");

            Constructor<?> packetEntityLookConstructor = PACKET_PLAY_OUT_ENTITY_LOOK
                    .getConstructor(int.class, byte.class, byte.class, boolean.class);
            int id = getEntityPlayerId(entityPlayer);

            return packetEntityLookConstructor.newInstance(id,
                    (byte) (yaw * 256 / 360), (byte) (pitch * 256 / 360), true);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error(e);
        }
    }

    public static Object getPacketPlayOutRelEntityMove(Object entityPlayer, double x, double y, double z) {
        try {
            Class<?> PACKET_PLAY_OUT_REL_ENTITY_MOVE = ReflectionUtils.getNMSClass("network.protocol.game", "PacketPlayOutEntity$PacketPlayOutRelEntityMove");

            Constructor<?> packetRelEntityMoveConstructor = PACKET_PLAY_OUT_REL_ENTITY_MOVE
                    .getConstructor(int.class, short.class, short.class, short.class, boolean.class);
            int id = getEntityPlayerId(entityPlayer);

            return packetRelEntityMoveConstructor.newInstance(id,
                    (short) (x * 4096), (short) (y * 4096), (short) (z * 4096), true);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error(e);
        }
    }

    public static Object getPacketPlayOutRelEntityMoveLook(Object entityPlayer, double x, double y, double z, float yaw, float pitch) {
        try {
            Class<?> PACKET_PLAY_OUT_REL_ENTITY_MOVE_LOOK = ReflectionUtils.getNMSClass("network.protocol.game", "PacketPlayOutEntity$PacketPlayOutRelEntityMoveLook");

            Constructor<?> packetRelEntityMoveLookConstructor = PACKET_PLAY_OUT_REL_ENTITY_MOVE_LOOK
                    .getConstructor(int.class, short.class, short.class, short.class, byte.class, byte.class, boolean.class);
            int id = getEntityPlayerId(entityPlayer);

            return packetRelEntityMoveLookConstructor.newInstance(id,
                    (short) (x * 4096), (short) (y * 4096), (short) (z * 4096),
                    (byte) (yaw * 256 / 360), (byte) (pitch * 256 / 360), true);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error(e);
        }
    }

    public static Object getPacketPlayOutAnimation(Object entityPlayer, NPCAnimation npcAnimation) {
        try {
            Class<?> PACKET_PLAY_OUT_ANIMATION = ReflectionUtils.getNMSClass("network.protocol.game", "PacketPlayOutAnimation");
            Class<?> ENTITY = ReflectionUtils.getNMSClass("world.entity", "Entity");

            Constructor<?> packetAnimationConstructor = PACKET_PLAY_OUT_ANIMATION
                    .getConstructor(ENTITY, int.class);

            return packetAnimationConstructor.newInstance(entityPlayer, npcAnimation.getValue());
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error(e);
        }
    }

    public static Object getPacketPlayOutBlockBreakAnimation(Vector3 location, int stage) {
        try {
            Class<?> BLOCK_POSITION = ReflectionUtils.getNMSClass("core", "BlockPosition");
            Class<?> PACKET_PLAY_OUT_BLOCK_BREAK_ANIMATION = ReflectionUtils.getNMSClass("network.protocol.game", "PacketPlayOutBlockBreakAnimation");

            Constructor<?> blockPositionConsturctor = BLOCK_POSITION
                    .getConstructor(int.class, int.class, int.class);
            Object blockPosition = blockPositionConsturctor.newInstance(location.getBlockX(), location.getBlockY(), location.getBlockZ());

            Constructor<?> packetBlockBreakAnimation = PACKET_PLAY_OUT_BLOCK_BREAK_ANIMATION
                    .getConstructor(int.class, BLOCK_POSITION, int.class);

            return packetBlockBreakAnimation.newInstance(location.getBlockX() + location.getBlockY() + location.getBlockZ(),
                    blockPosition, stage);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error(e);
        }
    }

    public static Object getPacketPlayOutEntityEquipment(Object entityPlayer, EquipmentSlot slot, ItemStack item) {
        try {
            Class<?> ENUM_ITEM_SLOT = ReflectionUtils.getNMSClass("world.entity", "EnumItemSlot");
            Class<?> CRAFT_ITEM_STACK = ReflectionUtils.getCraftClass("inventory.CraftItemStack");
            Class<?> PACKET_PLAY_OUT_ENTITY_EQUIPMENT = ReflectionUtils.getNMSClass("network.protocol.game", "PacketPlayOutEntityEquipment");

            String itemSlot;
            if (slot.equals(EquipmentSlot.HAND))
                itemSlot = "MAINHAND";
            else if (slot.equals(EquipmentSlot.OFF_HAND))
                itemSlot = "OFFHAND";
            else itemSlot = slot.toString().toUpperCase();

            Object enumItemSlot = ENUM_ITEM_SLOT.getField(itemSlot).get(null);

            Object nmsItem = CRAFT_ITEM_STACK.getMethod("asNMSCopy",
                    ItemStack.class).invoke(null, item);

            Pair<Object, Object> pair = new Pair<>(enumItemSlot, nmsItem);
            List<Pair<Object, Object>> pairList = new ArrayList<>();
            pairList.add(pair);

            Constructor<?> packetEntityEquipmentConstructor = PACKET_PLAY_OUT_ENTITY_EQUIPMENT
                    .getConstructor(int.class, List.class);
            int id = getEntityPlayerId(entityPlayer);

            return packetEntityEquipmentConstructor.newInstance(id, pairList);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error(e);
        }
    }

    public static Object getPacketPlayOutEntityMetadata(Object entityPlayer, NPCState npcState) {
        try {
            Class<?> ENTITY_POSE = ReflectionUtils.getNMSClass("world.entity", "EntityPose");
            Class<?> ENTITY = ReflectionUtils.getNMSClass("world.entity", "Entity");
            Class<?> DATAWATCHER = ReflectionUtils.getNMSClass("network.syncher", "DataWatcher");
            Class<?> DATAWATCHER_OBJECT = ReflectionUtils.getNMSClass("network.syncher", "DataWatcherObject");
            Class<?> DATAWATCHER_SERIALIZER = ReflectionUtils.getNMSClass("network.syncher", "DataWatcherSerializer");
            Class<?> DATAWATCHER_REGISTRY = ReflectionUtils.getNMSClass("network.syncher", "DataWatcherRegistry");
            Class<?> PACKET_PLAY_OUT_ENTITY_METADATA = ReflectionUtils.getNMSClass("network.protocol.game", "PacketPlayOutEntityMetadata");

            Object entityPose = ENTITY_POSE.getField(npcState.toString().toUpperCase()).get(null);

            Object dataWatcher = DATAWATCHER
                    .getConstructor(ENTITY).newInstance((Object) null);

            Constructor<?> dataWatcherObjectConstructor = DATAWATCHER_OBJECT
                    .getConstructor(int.class, DATAWATCHER_SERIALIZER);

            dataWatcher.getClass().getMethod("register", DATAWATCHER_OBJECT,
                    Object.class).invoke(dataWatcher,
                    dataWatcherObjectConstructor.newInstance(6, DATAWATCHER_REGISTRY.getField("s").get(null)),
                    entityPose);

            Constructor<?> packetEntityMetadataConstructor = PACKET_PLAY_OUT_ENTITY_METADATA
                    .getConstructor(int.class, DATAWATCHER, boolean.class);
            int id = getEntityPlayerId(entityPlayer);

            return packetEntityMetadataConstructor.newInstance(id, dataWatcher, true);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error(e);
        }
    }

    public static int getEntityPlayerId(Object entityPlayer) throws Exception {
        return (int) entityPlayer.getClass().getMethod("getId").invoke(entityPlayer);
    }

}
