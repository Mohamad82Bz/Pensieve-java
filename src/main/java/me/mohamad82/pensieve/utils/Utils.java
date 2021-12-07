package me.mohamad82.pensieve.utils;

import me.Mohamad82.RUoM.vector.Vector3;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

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

    public static void removeUsedLocation(Vector3 location) {
        usedLocations.remove(location);
    }

}
