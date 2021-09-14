package me.Mohamad82.Pensieve.replay;

public class ReplayManager {

    private static ReplayManager instance;
    public static ReplayManager getInstance() {
        return instance;
    }

    public ReplayManager() {
        instance = this;
    }

}
