package me.mohamad82.pensieve.world;

import me.mohamad82.ruom.utils.ServerVersion;
import org.bukkit.*;

public class WorldManager {

    private static WorldManager instance;
    public static WorldManager getInstance() {
        return instance;
    }

    private WorldCreator worldCreator;
    private final World world;

    private final String name = "pensieve_world";

    public WorldManager() {
        instance = this;

        World bukkitWorld = Bukkit.getWorld(name);
        if (bukkitWorld != null) {
            world = bukkitWorld;
            return;
        }

        worldCreator = new WorldCreator(name);
        if (ServerVersion.supports(13)) {
            worldCreator.generator(new EmptyChunkGenerator());
        } else {
            worldCreator.type(WorldType.FLAT);
            worldCreator.generatorSettings("2;0;1;");
        }
        world = worldCreator.createWorld();

        world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        world.setGameRule(GameRule.DO_FIRE_TICK, false);
        world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        world.setGameRule(GameRule.NATURAL_REGENERATION, false);
        if (ServerVersion.supports(13))
            world.setGameRule(GameRule.DO_INSOMNIA, false);
    }

    public World getWorld() {
        return world;
    }

}