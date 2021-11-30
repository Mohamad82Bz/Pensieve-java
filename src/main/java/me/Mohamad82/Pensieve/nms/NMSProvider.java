package me.Mohamad82.Pensieve.nms;

import me.Mohamad82.RUoM.XSeries.ReflectionUtils;
import me.Mohamad82.RUoM.utils.ServerVersion;
import me.Mohamad82.RUoM.vector.Vector3;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Set;

public class NMSProvider {

    private static Class<?> ITEM_STACK, POTION_UTIL, NBT_TAG_COMPOUND, MOJANGSON_PARSER, CRAFT_ITEM_STACK, CRAFT_CHAT_MESSAGE, CRAFT_PLAYER;

    private static Constructor<?> ITEM_STACK_CONSTRUCTOR, NBT_TAG_COMPOUND_CONSTRUCTOR;

    private static Method ITEM_STACK_SAVE_METHOD, ITEM_STACK_A_METHOD, ITEM_STCAK_CREATE_STACK_METHOD, MOJANGSON_PARSER_PARSE_METHOD, CRAFT_ITEM_STACK_AS_NMS_COPY,
            CRAFT_ITEM_STACK_AS_BUKKIT_COPY, POTION_UTIL_GET_COLOR_METHOD, CRAFT_CHAT_MESSAGE_FROM_STRING_METHOD, NBT_TAG_COMPOUND_TO_STRING_METHOD,
            CRAFT_PLAYER_GET_HANDLE_METHOD;

    static {
        try {
            {
                ITEM_STACK = ReflectionUtils.getNMSClass("world.item", "ItemStack");
                POTION_UTIL = ReflectionUtils.getNMSClass("world.item.alchemy", "PotionUtil");
                MOJANGSON_PARSER = ReflectionUtils.getNMSClass("nbt", "MojangsonParser");
                NBT_TAG_COMPOUND = ReflectionUtils.getNMSClass("nbt", "NBTTagCompound");
                CRAFT_ITEM_STACK = ReflectionUtils.getCraftClass("inventory.CraftItemStack");
                CRAFT_CHAT_MESSAGE = ReflectionUtils.getCraftClass("util.CraftChatMessage");
                CRAFT_PLAYER = ReflectionUtils.getCraftClass("entity.CraftPlayer");
            }
            {
                if (!ServerVersion.supports(13))
                    ITEM_STACK_CONSTRUCTOR = ITEM_STACK.getConstructor(NBT_TAG_COMPOUND);
                NBT_TAG_COMPOUND_CONSTRUCTOR = NBT_TAG_COMPOUND.getConstructor();
            }
            {
                ITEM_STACK_SAVE_METHOD = ITEM_STACK.getMethod("save", NBT_TAG_COMPOUND);
                if (ServerVersion.supports(13))
                    ITEM_STACK_A_METHOD = ITEM_STACK.getMethod("a", NBT_TAG_COMPOUND);
                if (!ServerVersion.supports(9))
                    ITEM_STCAK_CREATE_STACK_METHOD = ITEM_STACK.getMethod("createStack");
                MOJANGSON_PARSER_PARSE_METHOD = MOJANGSON_PARSER.getMethod("parse", String.class);
                CRAFT_ITEM_STACK_AS_NMS_COPY = CRAFT_ITEM_STACK.getMethod("asNMSCopy", ItemStack.class);
                CRAFT_ITEM_STACK_AS_BUKKIT_COPY = CRAFT_ITEM_STACK.getMethod("asBukkitCopy", ITEM_STACK);
                POTION_UTIL_GET_COLOR_METHOD = POTION_UTIL.getMethod("c", ITEM_STACK);
                CRAFT_CHAT_MESSAGE_FROM_STRING_METHOD = CRAFT_CHAT_MESSAGE.getMethod("fromString", String.class);
                NBT_TAG_COMPOUND_TO_STRING_METHOD = NBT_TAG_COMPOUND.getMethod("toString");
                CRAFT_PLAYER_GET_HANDLE_METHOD = CRAFT_PLAYER.getMethod("getHandle");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Object getNmsItemStack(ItemStack item) {
        try {
            return CRAFT_ITEM_STACK_AS_NMS_COPY.invoke(null, item);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error(e);
        }
    }

    public static ItemStack getBukkitItemStack(Object nmsItem) {
        try {
            return (ItemStack) CRAFT_ITEM_STACK_AS_BUKKIT_COPY.invoke(null, nmsItem);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error(e);
        }
    }

    public static String getItemStackNBTJson(ItemStack item) {
        try {
            Object nmsItem = getNmsItemStack(item);
            Object nbtTagCompound = ITEM_STACK_SAVE_METHOD.invoke(nmsItem, NBT_TAG_COMPOUND_CONSTRUCTOR.newInstance());

            return (String) NBT_TAG_COMPOUND_TO_STRING_METHOD.invoke(nbtTagCompound);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error(e);
        }
    }

    public static ItemStack getItemStackFromNBTJson(String nbtJson) {
        try {
            Object nbtTagCompound = MOJANGSON_PARSER_PARSE_METHOD.invoke(null, nbtJson);
            Object nmsItem;
            if (ServerVersion.supports(13)) {
                nmsItem = ITEM_STACK_A_METHOD.invoke(null, nbtTagCompound);
            } else if (ServerVersion.supports(9)) {
                nmsItem = ITEM_STACK_CONSTRUCTOR.newInstance(nbtTagCompound);
            } else {
                nmsItem = ITEM_STCAK_CREATE_STACK_METHOD.invoke(null, nbtTagCompound);
            }

            return getBukkitItemStack(nmsItem);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error(e);
        }
    }

    public static int getPotionColor(ItemStack potion) {
        try {
            return (int) POTION_UTIL_GET_COLOR_METHOD.invoke(null, getNmsItemStack(potion));
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error(e);
        }
    }

    public static Object[] getIChatBaseComponent(String string) {
        try {
            return (Object[]) CRAFT_CHAT_MESSAGE_FROM_STRING_METHOD.invoke(null, string);
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

    public static void sendBlockBreakAnimation(Set<Player> viewers, Vector3 location, int stage) {
        Object packetPlayOutBlockBreakAnimation = PacketProvider.getPacketPlayOutBlockBreakAnimation(location, stage);

        for (Player player : viewers) {
            ReflectionUtils.sendPacket(player,
                    packetPlayOutBlockBreakAnimation);
        }
    }

    public static void setPassengers(Set<Player> viewers, int entityId, int... passengers) {
        Object packetPlayOutMount = PacketProvider.getPacketPlayOutMount(entityId, passengers);

        for (Player player : viewers) {
            ReflectionUtils.sendPacket(player,
                    packetPlayOutMount);
        }
    }

    /**
     * Sends a PacketPlayOutBlockAction to show a chest opening or closing animation.
     * @param viewers The players that are going to see this packet
     * @param blockLocation The block's location
     * @param blockMaterial The block's material. It can be chest, trapped_chest, ender_chest or shulker_box. Other values will be ignored
     * @param open Declear that chest should get opened or closed
     */
    public static void sendChestAnimation(Set<Player> viewers, Vector3 blockLocation, Material blockMaterial, boolean open) {
        Object packetPlayOutBlockAction = PacketProvider.getPacketPlayOutBlockAction(blockLocation, blockMaterial, 1, open ? 1 : 0);

        for (Player player : viewers) {
            ReflectionUtils.sendPacket(player,
                    packetPlayOutBlockAction);
        }
    }

}
