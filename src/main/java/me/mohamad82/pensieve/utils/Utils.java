package me.mohamad82.pensieve.utils;

import com.google.common.collect.Lists;
import me.mohamad82.ruom.utils.ServerVersion;
import me.mohamad82.ruom.xseries.XMaterial;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class Utils {

    public static <T> T copy(T object, T newObject) {
        try {
            Set<Field> fields = new HashSet<>();

            Class<?> clazz = object.getClass();
            while (!clazz.equals(Object.class)) {
                fields.addAll(Lists.newArrayList(clazz.getDeclaredFields()));
                clazz = clazz.getSuperclass();
            }

            for (Field field : fields) {
                field.setAccessible(true);
                Object fieldObject = field.get(object);
                if (fieldObject == null) continue;
                if (fieldObject instanceof Collection) {
                    //noinspection JavaReflectionInvocation
                    fieldObject = fieldObject.getClass().getConstructor(Collection.class).newInstance(fieldObject);
                } else {
                    try {
                        fieldObject = fieldObject.getClass().getDeclaredMethod("clone").invoke(fieldObject);
                    } catch (NoSuchMethodException ignored) {}
                }
                field.set(newObject, fieldObject);
            }
            return newObject;
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    public static boolean isDrinkableItem(Material material) {
        return material.equals(XMaterial.POTION.parseMaterial()) || material.equals(XMaterial.MILK_BUCKET.parseMaterial());
    }

    @SuppressWarnings("deprecation")
    public static boolean hasEdibleItemInHand(Player player) {
        if (ServerVersion.supports(9)) {
            if (player.getInventory().getItemInMainHand().getType().isEdible() || isDrinkableItem(player.getInventory().getItemInMainHand().getType())) {
                return true;
            } else return player.getInventory().getItemInOffHand().getType().isEdible() || isDrinkableItem(player.getInventory().getItemInOffHand().getType());
        } else {
            return player.getInventory().getItemInHand().getType().isEdible() || isDrinkableItem(player.getInventory().getItemInHand().getType());
        }
    }

}
