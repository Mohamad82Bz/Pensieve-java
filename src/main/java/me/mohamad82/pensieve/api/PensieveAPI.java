package me.mohamad82.pensieve.api;

import me.mohamad82.pensieve.recording.RecordContainer;
import me.mohamad82.pensieve.recording.Recorder;
import me.mohamad82.pensieve.serializer.exception.PensieveSerializationException;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class PensieveAPI {

    /**
     * Returns all the recorders that this player is in.
     * Will return an empty set if the player wasn't in any recorder.
     * @param player The player.
     * @return A set of recorders.
     */
    public static Set<Recorder> getPlayerRecorders(Player player) {
        return null;
    }

    /**
     * Returns all the recorders that this NON-PLAYER entity is in.
     * Will return an empty set if the entity wasn't in any recorder.
     * @param entity The non-player entity.
     * @return A set of recorders.
     */
    public static Set<Recorder> getEntityRecorders(Entity entity) {
        return null;
    }

    /**
     * Returns a RecordContainer from a pensieve file.
     * @param file The pensieve formatted file.
     * @return The loaded RecordContainer.
     * @throws PensieveSerializationException If an error happened during deserialization.
     */
    public static RecordContainer getRecordContainerFromFile(File file) throws PensieveSerializationException {
        return null;
    }

    /**
     * Saves a RecordContainer to a file.
     * @param recordContainer The RecordContainer.
     * @param file The target file.
     * @return A CompletableFuture that will be completed once serialization was done. Serialization is async.
     * @throws PensieveSerializationException If an error happened during serialization.
     */
    public static CompletableFuture<Void> saveRecordContainerToFile(RecordContainer recordContainer, File file) throws PensieveSerializationException {
        return null;
    }

}
