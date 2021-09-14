package me.Mohamad82.Pensieve.replay;

import me.Mohamad82.RUoM.vector.Vector3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ReplayCache {

    private final Map<UUID, Integer> pendingBlockBreakStages = new HashMap<>();
    private final Map<UUID, Vector3> pendingBlockBreakOffSetLocations = new HashMap<>();
    private final Map<UUID, Integer> pendingFoodEatSkippedTicks = new HashMap<>();
    private Vector3 centersDistance;
    private boolean playing;

    public Map<UUID, Integer> getPendingBlockBreakStages() {
        return pendingBlockBreakStages;
    }

    public Map<UUID, Vector3> getPendingBlockBreakOffSetLocations() {
        return pendingBlockBreakOffSetLocations;
    }

    public Map<UUID, Integer> getPendingFoodEatSkippedTicks() {
        return pendingFoodEatSkippedTicks;
    }

    public Vector3 getCentersDistance() {
        return centersDistance;
    }

    public void setCentersDistance(Vector3 centersDistance) {
        this.centersDistance = centersDistance;
    }

    public boolean isPlaying() {
        return playing;
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

}
