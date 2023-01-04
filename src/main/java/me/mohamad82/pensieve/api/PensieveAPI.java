package me.mohamad82.pensieve.api;

import me.mohamad82.pensieve.recording.RecordManager;
import me.mohamad82.pensieve.recording.Recorder;
import me.mohamad82.pensieve.serializer.PensieveGsonSerializer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class PensieveAPI {

    /**
     * Returns all the recorders that this player is in.
     * Will return an empty set if the player wasn't in any recorder.
     * @param player The player.
     * @return A set of recorders.
     */
    public static Collection<Recorder> getPlayerRecorders(Player player) {
        return new HashSet<>(RecordManager.getInstance().getPlayerRecorder(player));
    }

    /**
     * Returns all the recorders that this NON-PLAYER entity is in.
     * Will return an empty set if the entity wasn't in any recorder.
     * @param entity The non-player entity.
     * @return A set of recorders.
     */
    public static Set<Recorder> getEntityRecorders(Entity entity) {
        return new HashSet<>(RecordManager.getInstance().getEntityRecorder(entity));
    }

    /**
     * Returns the gson serializer used to save records and load replays.
     * @return The gson serializer
     */
    public static PensieveGsonSerializer getSerializer() {
        return PensieveGsonSerializer.get();
    }

}
