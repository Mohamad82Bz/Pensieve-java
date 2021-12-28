package me.mohamad82.pensieve.utils;

import me.mohamad82.ruom.vector.Vector3;
import me.mohamad82.ruom.xseries.ReflectionUtils;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Player;
import org.bukkit.projectiles.ProjectileSource;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class Utils {

    private static Class<?> CRAFT_AREA_EFFECT_CLOUD;

    private static Method CRAFT_AREA_EFFECT_CLOUD_GET_SOURCE_METHOD;

    static {
        try {
            CRAFT_AREA_EFFECT_CLOUD = ReflectionUtils.getCraftClass("entity.CraftAreaEffectCloud");
            CRAFT_AREA_EFFECT_CLOUD_GET_SOURCE_METHOD = CRAFT_AREA_EFFECT_CLOUD.getMethod("getSource");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static final Set<Vector3> usedLocations = new HashSet<>();

    public static Vector3 getRandomScaledLocation() {
        Vector3 vectorLocation;
        int arenaDistance = 2000;
        int x = 0;
        int z = 0;
        while (true) {
            boolean randomXZDirection = new Random().nextBoolean();
            if (randomXZDirection) {
                z += arenaDistance;
            } else {
                x += arenaDistance;
            }
            vectorLocation = Vector3.at(x, 100, z);

            if (!usedLocations.contains(vectorLocation)) {
                usedLocations.add(vectorLocation);
                return vectorLocation;
            }
        }
    }

    public static <T> T copy(T object, T newObject, Field[]... allFields) {
        try {
            Set<Field> fields = new HashSet<>();

            Class<?> clazz = object.getClass();
            while (!clazz.equals(Object.class)) {
                fields.addAll(List.of(clazz.getDeclaredFields()));
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

    @Nullable
    public static Player getAreaEffectCloudOwner(AreaEffectCloud areaEffectCloud) {
        try {
            ProjectileSource projectileSource = (ProjectileSource) CRAFT_AREA_EFFECT_CLOUD_GET_SOURCE_METHOD.invoke(areaEffectCloud);
            if (projectileSource == null) return null;
            if (!(projectileSource instanceof Player)) return null;
            return (Player) projectileSource;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void removeUsedLocation(Vector3 location) {
        usedLocations.remove(location);
    }

}
