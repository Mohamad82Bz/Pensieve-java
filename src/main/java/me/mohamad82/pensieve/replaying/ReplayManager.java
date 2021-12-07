package me.mohamad82.pensieve.replaying;

public class ReplayManager {

    private static ReplayManager instance;
    public static ReplayManager getInstance() {
        return instance;
    }

    public ReplayManager() {
        instance = this;
    }

}
