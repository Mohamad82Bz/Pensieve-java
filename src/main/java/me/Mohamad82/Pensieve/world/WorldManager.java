package me.Mohamad82.Pensieve.world;

import me.Mohamad82.RUoM.ServerVersion;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;

public class WorldManager {

    private static WorldManager instance;
    public static WorldManager getInstance() {
        return instance;
    }

    WorldCreator worldCreator;
    World world;

    public WorldManager() {
        instance = this;

        worldCreator = new WorldCreator("pensieve_world");
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