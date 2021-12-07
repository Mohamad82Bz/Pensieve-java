package me.mohamad82.pensieve.replaying;

import me.Mohamad82.RUoM.vector.Vector3;

import java.util.*;

public class ReplayCache {

    private final Map<UUID, Integer> pendingBlockBreakStages = new HashMap<>();
    private final Map<UUID, Vector3> pendingBlockBreakOffSetLocations = new HashMap<>();
    private final Map<UUID, Integer> pendingBlockBreakSkippedParticleSpawns = new HashMap<>();
    private final Map<UUID, Integer> pendingFoodEatSkippedTicks = new HashMap<>();
    private final Set<Vector3> blockDataUsedForPlacing = new HashSet<>();
    private Vector3 centersDistance;
    private boolean playing;

    public Map<UUID, Integer> getPendingBlockBreakStages() {
        return pendingBlockBreakStages;
    }

    public Map<UUID, Vector3> getPendingBlockBreakOffSetLocations() {
        return pendingBlockBreakOffSetLocations;
    }

    public Map<UUID, Integer> getPendingBlockBreakSkippedParticleSpawns() {
        return pendingBlockBreakSkippedParticleSpawns;
    }

    public Map<UUID, Integer> getPendingFoodEatSkippedTicks() {
        return pendingFoodEatSkippedTicks;
    }

    public Set<Vector3> getBlockDataUsedForPlacing() {
        return blockDataUsedForPlacing;
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
