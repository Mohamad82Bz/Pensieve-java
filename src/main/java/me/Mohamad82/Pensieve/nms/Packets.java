package me.Mohamad82.Pensieve.nms;

import com.mojang.datafixers.util.Pair;
import me.Mohamad82.Pensieve.nms.enums.NPCAnimation;
import me.Mohamad82.Pensieve.nms.enums.NPCState;
import me.Mohamad82.RUoM.Vector3;
import me.Mohamad82.RUoM.XSeries.ReflectionUtils;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

public class Packets {

    public static Object getPacketPlayOutPlayerInfo(Object entityPlayer, String action) {
        try {
            Object addPlayerEnum = ReflectionUtils.getNMSClass("PacketPlayOutPlayerInfo$EnumPlayerInfoAction")
                    .getField(action.toUpperCase()).get(null);
            Object entityPlayerArray = Array.newInstance(ReflectionUtils.getNMSClass("EntityPlayer"), 1);
            Constructor<?> packetPlayerInfoConstructor = ReflectionUtils.getNMSClass("PacketPlayOutPlayerInfo")
                    .getConstructor(ReflectionUtils.getNMSClass("PacketPlayOutPlayerInfo$EnumPlayerInfoAction"),
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
            Constructor<?> packetNamedEntitySpawnConstructor = ReflectionUtils.getNMSClass("PacketPlayOutNamedEntitySpawn")
                    .getConstructor(ReflectionUtils.getNMSClass("EntityHuman"));
            return packetNamedEntitySpawnConstructor.newInstance(entityPlayer);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error(e);
        }
    }

    public static Object getPacketPlayOutEntityHeadRotation(Object entityPlayer, float yaw) {
        try {
            Constructor<?> packetEntityHeadRotationConstructor = ReflectionUtils.getNMSClass("PacketPlayOutEntityHeadRotation")
                    .getConstructor(ReflectionUtils.getNMSClass("Entity"), byte.class);

            return packetEntityHeadRotationConstructor.newInstance(entityPlayer, (byte) (yaw * 256 / 360));
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error(e);
        }
    }

    public static Object getPacketPlayOutEntityDestroy(Object entityPlayer) {
        try {
            Constructor<?> packetEntityDestroyConstructor = ReflectionUtils.getNMSClass("PacketPlayOutEntityDestroy")
                    .getConstructor(int[].class);
            int id = (int) entityPlayer.getClass().getMethod("getId").invoke(entityPlayer);
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
            Constructor<?> packetEntityLookConstructor = ReflectionUtils.getNMSClass("PacketPlayOutEntity$PacketPlayOutEntityLook")
                    .getConstructor(int.class, byte.class, byte.class, boolean.class);
            int id = (int) entityPlayer.getClass().getMethod("getId").invoke(entityPlayer);

            return packetEntityLookConstructor.newInstance(id,
                    (byte) (yaw * 256 / 360), (byte) (pitch * 256 / 360), true);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error(e);
        }
    }

    public static Object getPacketPlayOutRelEntityMove(Object entityPlayer, double x, double y, double z) {
        try {
            Constructor<?> packetRelEntityMoveConstructor = ReflectionUtils.getNMSClass("PacketPlayOutEntity$PacketPlayOutRelEntityMove")
                    .getConstructor(int.class, short.class, short.class, short.class, boolean.class);
            int id = (int) entityPlayer.getClass().getMethod("getId").invoke(entityPlayer);

            return packetRelEntityMoveConstructor.newInstance(id,
                    (short) (x * 4096), (short) (y * 4096), (short) (z * 4096), true);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error(e);
        }
    }

    public static Object getPacketPlayOutRelEntityMoveLook(Object entityPlayer, double x, double y, double z, float yaw, float pitch) {
        try {
            Constructor<?> packetRelEntityMoveLookConstructor = ReflectionUtils.getNMSClass("PacketPlayOutEntity$PacketPlayOutRelEntityMoveLook")
                    .getConstructor(int.class, short.class, short.class, short.class, byte.class, byte.class, boolean.class);
            int id = (int) entityPlayer.getClass().getMethod("getId").invoke(entityPlayer);

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
            Constructor<?> packetAnimationConstructor = ReflectionUtils.getNMSClass("PacketPlayOutAnimation")
                    .getConstructor(ReflectionUtils.getNMSClass("Entity"), int.class);

            return packetAnimationConstructor.newInstance(entityPlayer, npcAnimation.getValue());
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error(e);
        }
    }

    public static Object getPacketPlayOutBlockBreakAnimation(Vector3 location, int stage) {
        try {
            Constructor<?> blockPositionConsturctor = ReflectionUtils.getNMSClass("BlockPosition")
                    .getConstructor(int.class, int.class, int.class);
            Object blockPosition = blockPositionConsturctor.newInstance(location.getBlockX(), location.getBlockY(), location.getBlockZ());

            Constructor<?> packetBlockBreakAnimation = ReflectionUtils.getNMSClass("PacketPlayOutBlockBreakAnimation")
                    .getConstructor(int.class, ReflectionUtils.getNMSClass("BlockPosition"), int.class);

            return packetBlockBreakAnimation.newInstance(location.getBlockX() + location.getBlockY() + location.getBlockZ(),
                    blockPosition, stage);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error(e);
        }
    }

    public static Object getPacketPlayOutEntityEquipment(Object entityPlayer, EquipmentSlot slot, ItemStack item) {
        try {
            String itemSlot;
            if (slot.equals(EquipmentSlot.HAND))
                itemSlot = "MAINHAND";
            else if (slot.equals(EquipmentSlot.OFF_HAND))
                itemSlot = "OFFHAND";
            else itemSlot = slot.toString().toUpperCase();

            Object enumItemSlot = ReflectionUtils.getNMSClass("EnumItemSlot").getField(itemSlot).get(null);

            Object nmsItem = ReflectionUtils.getCraftClass("inventory.CraftItemStack").getMethod("asNMSCopy",
                    ItemStack.class).invoke(null, item);

            Pair<Object, Object> pair = new Pair<>(enumItemSlot, nmsItem);
            List<Pair<Object, Object>> pairList = new ArrayList<>();
            pairList.add(pair);

            Constructor<?> packetEntityEquipmentConstructor = ReflectionUtils.getNMSClass("PacketPlayOutEntityEquipment")
                    .getConstructor(int.class, List.class);
            int id = (int) entityPlayer.getClass().getMethod("getId").invoke(entityPlayer);

            return packetEntityEquipmentConstructor.newInstance(id, pairList);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error(e);
        }
    }

    public static Object getPacketPlayOutEntityMetadata(Object entityPlayer, NPCState npcState) {
        try {
            Object entityPose = ReflectionUtils.getNMSClass("EntityPose").getField(npcState.toString().toUpperCase()).get(null);

            Object dataWatcher = ReflectionUtils.getNMSClass("DataWatcher")
                    .getConstructor(ReflectionUtils.getNMSClass("Entity")).newInstance((Object) null);

            Constructor<?> dataWatcherObjectConstructor = ReflectionUtils.getNMSClass("DataWatcherObject")
                    .getConstructor(int.class, ReflectionUtils.getNMSClass("DataWatcherSerializer"));

            dataWatcher.getClass().getMethod("register", ReflectionUtils.getNMSClass("DataWatcherObject"),
                    Object.class).invoke(dataWatcher,
                    dataWatcherObjectConstructor.newInstance(6, ReflectionUtils.getNMSClass("DataWatcherRegistry").getField("s").get(null)),
                    entityPose);

            Constructor<?> packetEntityMetadataConstructor = ReflectionUtils.getNMSClass("PacketPlayOutEntityMetadata")
                    .getConstructor(int.class, ReflectionUtils.getNMSClass("DataWatcher"), boolean.class);
            int id = (int) entityPlayer.getClass().getMethod("getId").invoke(entityPlayer);

            return packetEntityMetadataConstructor.newInstance(id, dataWatcher, true);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error(e);
        }
    }

}
