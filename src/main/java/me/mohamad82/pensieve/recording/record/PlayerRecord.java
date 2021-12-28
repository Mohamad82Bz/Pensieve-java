package me.mohamad82.pensieve.recording.record;

import me.mohamad82.ruom.translators.skin.MinecraftSkin;
import me.mohamad82.ruom.translators.skin.SkinBuilder;
import me.mohamad82.ruom.vector.Vector3;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

public class PlayerRecord extends Record {

    private Optional<MinecraftSkin> skin = Optional.empty();
    private final String name;

    public PlayerRecord(Player player, Vector3 center) {
        super(player.getUniqueId(), center);
        this.name = player.getName();
        this.skin = Optional.ofNullable(SkinBuilder.getInstance().getSkin(player));
    }

    public PlayerRecord(UUID uuid, String name, Vector3 center) {
        super(uuid, center);
        this.name = name;
    }

    public Optional<MinecraftSkin> getSkin() {
        return skin;
    }

    public void setSkin(MinecraftSkin skin) {
        this.skin = Optional.ofNullable(skin);
    }

    public String getName() {
        return name;
    }

    @Override
    public RecordTick createRecordTick() {
        return new PlayerRecordTick();
    }

}
