package me.Mohamad82.Pensieve.nms;

import me.Mohamad82.RUoM.XSeries.ReflectionUtils;
import me.Mohamad82.RUoM.vector.Vector3;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;
import java.util.Set;

public class NMSProvider {

    private static Class<?> ITEM_STACK, POTION_UTIL, CRAFT_ITEM_STACK;

    private static Method CRAFT_ITEM_STACK_AS_NMS_COPY, POTION_UTIL_GET_COLOR_METHOD;

    static {
        try {
            {
                ITEM_STACK = ReflectionUtils.getNMSClass("world.item", "ItemStack");
                POTION_UTIL = ReflectionUtils.getNMSClass("world.item.alchemy", "PotionUtil");
                CRAFT_ITEM_STACK = ReflectionUtils.getCraftClass("inventory.CraftItemStack");
            }
            {
                CRAFT_ITEM_STACK_AS_NMS_COPY = CRAFT_ITEM_STACK.getMethod("asNMSCopy", ItemStack.class);
                POTION_UTIL_GET_COLOR_METHOD = POTION_UTIL.getMethod("c", ITEM_STACK);
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

    public static int getPotionColor(ItemStack potion) {
        try {
            return (int) POTION_UTIL_GET_COLOR_METHOD.invoke(null, getNmsItemStack(potion));
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error(e);
        }
    }

    public static void sendBlockBreakAnimation(Set<Player> players, Vector3 location, int stage) {
        Object packetPlayOutBlockBreakAnimation = PacketProvider.getPacketPlayOutBlockBreakAnimation(location, stage);

        for (Player player : players) {
            ReflectionUtils.sendPacket(player,
                    packetPlayOutBlockBreakAnimation);
        }
    }

}
