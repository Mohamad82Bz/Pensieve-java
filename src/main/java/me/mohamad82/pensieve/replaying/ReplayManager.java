package me.mohamad82.pensieve.replaying;

import java.util.HashMap;
import java.util.Map;

public class ReplayManager {

    private static ReplayManager instance;

    private final Map<String, Replayer> internalReplayers = new HashMap<>();

    private ReplayManager() {

    }

    public Map<String, Replayer> getInternalReplayers() {
        return internalReplayers;
    }

    public static ReplayManager getInstance() {
        if (instance == null) {
            instance = new ReplayManager();
        }
        return instance;
    }

}
