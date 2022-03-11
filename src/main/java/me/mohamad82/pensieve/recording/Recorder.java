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
     */
    void start();

    /**
     * Stops the recorder.
     */
    void stop();

    /**
     * Adds a new entity to the recorder. Please look at the supported entity types before adding.
     * If the plugin does not support your desired entity type, feel free to contant us, and we will try to implement it.
     */
    void safeAddEntity(Entity entity);

    /**
     * Removes an entity (if it exists) from the recorder.
     * Useful tip: Entities will get automatically removed if they were dead.
     */
    void safeRemoveEntity(Entity entity);

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
