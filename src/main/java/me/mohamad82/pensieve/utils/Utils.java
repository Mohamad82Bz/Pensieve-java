package me.mohamad82.pensieve.utils;

import com.google.common.collect.Lists;
import me.mohamad82.ruom.math.vector.Vector3;

import java.lang.reflect.Field;
import java.util.*;

public class Utils {

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

    public static void removeUsedLocation(Vector3 location) {
        usedLocations.remove(location);
    }

}
