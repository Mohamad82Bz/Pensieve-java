package me.mohamad82.pensieve.recording;

import me.mohamad82.ruom.math.vector.Vector3;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;

public interface Recorder {

    /**
     * Constructs a new recorder with a set of players and a center.
     * Center is used to offset the locations when constructing a replay.
     * @param players Players that recorder should record.
     * @param center Center of the recorder.
     * @return The constructed recorder.
     */
    static Recorder recorder(Set<Player> players, Vector3 center) {
        return new RecorderImpl(players, center);
    }

    /**
     * Constructs a new recorder with a single player.
     * Center will be the player's location itself.
     * @param player The player that the recorder should record.
     * @return The constructed recorder.
     */
    static Recorder recorder(Player player) {
        return new RecorderImpl(player);
    }

    /**
     * Starts the recorder.
     * @return True if the recorder was successfully started. (If the recorder was already started, it will return false)
     */
    boolean start();

    /**
     * Checks if the recorder is currently recording.
     * @return True if the recorder is recording.
     */
    boolean isStarted();

    /**
     * Stops the recorder.
     * @return True if the recorder was successfully stopped. (If the recorder was already stopped, it will return false)
     */
    boolean stop();

    /**
     * Checks if the recorder is stopped.
     * @return True if the recorder is stopped.
     */
    boolean isStopped();

    /**
     * Adds a new entity to the recorder. Please look at the supported entity types before adding.
     * If the plugin does not support your desired entity type, feel free to contant us, and we will try to implement it.
     * @param entity The entity to add.
     */
    boolean safeAddEntity(Entity entity);

    /**
     * Removes an entity (if it exists) from the recorder.
     * Useful tip: Entities will get automatically removed if they were dead.
     * @param entity The entity to remove.
     */
    boolean safeRemoveEntity(Entity entity);

    /**
     * Returns if the recorder contained the given entity.
     * @param entity The entity to check.
     * @return True if the recorder contained the entity.
     */
    boolean containsEntity(Entity entity);

    /**
     * Returns if the recorder contained the given entity.
     * @param uuid The UUID of the entity to check.
     * @return True if the recorder contained the entity.
     */
    boolean containsEntity(UUID uuid);

    /**
     * Adds a player to the recorder.
     * @param player The player to add.
     */
    boolean safeAddPlayer(Player player);

    /**
     * Removes a player (if it exists) from the recorder.
     * @param player The player to remove.
     */
    boolean safeRemovePlayer(Player player);

    /**
     * Returns if the recorder contained the given player.
     * @param player The player to check.
     * @return True if the recorder contained the player.
     */
    boolean containsPlayer(Player player);

    /**
     * Returns if the recorder contained the given player.
     * @param uuid The UUID of the player to check.
     * @return True if the recorder contained the player.
     */
    boolean containsPlayer(UUID uuid);

    /**
     * Returns if the recorder contained the given player.
     * @param name The name of the player to check.
     * @return True if the recorder contained the player.
     */
    boolean containsPlayer(String name);

    /**
     * Returns the center of the recorder.
     * @return The center.
     */
    Vector3 getCenter();

    /**
     * Returns the record container that can be used to construct a Replayer.
     * Will return null if recorder wasn't stopped.
     * @return The record container.
     */
    @Nullable
    RecordContainer getRecordContainer();

    /**
     * Returns the unique uuid of this recorder instance.
     * @return The unique uuid.
     */
    UUID getRecorderUUID();

}
