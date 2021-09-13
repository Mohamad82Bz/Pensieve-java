package me.Mohamad82.Pensieve.gamemodes.eggwarsx;

import me.Mohamad82.Pensieve.Main;

import java.io.File;

public class EWXManager {

    private static EWXManager instance;
    public static EWXManager getInstance() {
        return instance;
    }

    public EWXManager() {
        instance = this;
    }

    public File getArenaSchematic(String arenaName) {
        return new File(Main.getInstance().getDataFolder().getParentFile() + "/Eggwars/arenas/" + arenaName, "save.schem");
    }

}
