package me.mohamad82.pensieve.replaying;

import me.mohamad82.pensieve.recording.RecordContainer;
import me.mohamad82.ruom.math.vector.Vector3;
import org.bukkit.World;

import java.util.concurrent.CompletableFuture;

public interface Replayer {

    /**
     * Constructs a replayer.
     * @param recordContainer The RecordContainer that can be obtained by a recorder.
     * @param world The world that the replay will be played on.
     * @param center Center of the replayer used to offset the replay location.
     * @return The constructed replayer.
     */
    static Replayer replayer(RecordContainer recordContainer, World world, Vector3 center) {
        return new ReplayerImpl(recordContainer, world, center);
    }

    /**
     * Calculates and caches the necessary stuff async.
     * <br>
     * This method can take upto few seconds depending on how long the given ReplayContainer is.
     * @return A completablefuture that completes once the operation is done.
     */
    CompletableFuture<Void> prepare();

    /**
     * Starts the replayer.
     * @throws IllegalStateException If the replay is not prepared using prepare method
     * @return A PlayBackControl used to control the replay, such as modifiying speed, volume, progress, etc.
     */
    PlayBackControl start();

    /**
     * Suspends the replayer.
     */
    void suspend();

}
