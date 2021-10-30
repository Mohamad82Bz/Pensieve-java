package me.Mohamad82.Pensieve.record;

import me.Mohamad82.RUoM.translators.skin.MinecraftSkin;
import me.Mohamad82.RUoM.translators.skin.SkinBuilder;
import me.Mohamad82.RUoM.vector.Vector3;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * @deprecated Replaced with new Record abstract class.
 */
public class RecordOld {

    private List<RecordTick> recordTicks = new ArrayList<>();
    private Optional<Integer> startingTick = Optional.empty();
    private Optional<MinecraftSkin> skin = Optional.empty();
    private final UUID uuid;
    private String name;
    private Vector3 startLocation;
    private Vector3 center;

    protected RecordOld(Player player) {
        this.uuid = player.getUniqueId();
        this.name = player.getName();

        this.skin = Optional.ofNullable(SkinBuilder.getInstance().getSkin(player));
    }

    protected RecordOld(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    protected RecordOld(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public Vector3 getStartLocation() {
        return startLocation;
    }

    protected void setStartLocation(Vector3 startLocation) {
        this.startLocation = startLocation;
    }

    public Vector3 getCenter() {
        return center;
    }

    protected void setCenter(Vector3 center) {
        this.center = center;
    }

    public List<RecordTick> getRecordTicks() {
        return recordTicks;
    }

    protected void setRecordTicks(List<RecordTick> recordTicks) {
        this.recordTicks = recordTicks;
    }

    public int getTotalTicks() {
        return recordTicks.size();
    }

    public void setStartingTick(int startingTick) {
        this.startingTick = Optional.of(startingTick);
    }

    public Optional<Integer> getStartingTick() {
        return startingTick;
    }

    public void setSkin(MinecraftSkin skin) {
        this.skin = Optional.ofNullable(skin);
    }

    public Optional<MinecraftSkin> getSkin() {
        return skin;
    }

}
