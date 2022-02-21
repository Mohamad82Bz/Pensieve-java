package me.mohamad82.pensieve.recording.record;

import com.google.gson.JsonObject;
import me.mohamad82.ruom.translators.skin.MinecraftSkin;
import me.mohamad82.ruom.translators.skin.SkinBuilder;
import me.mohamad82.ruom.vector.Vector3;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

public class PlayerRecord extends Record {

    private Optional<MinecraftSkin> skin = Optional.empty();
    private String name;

    public PlayerRecord(Player player, Vector3 center) {
        super(RecordType.PLAYER, player.getUniqueId(), center);
        this.name = player.getName();
        this.skin = Optional.ofNullable(SkinBuilder.getInstance().getSkin(player));
    }

    protected PlayerRecord() {

    }

    public PlayerRecord(UUID uuid, String name, Vector3 center) {
        super(RecordType.PLAYER, uuid, center);
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

    public JsonObject toJson(JsonObject jsonObject) {
        if (skin.isPresent()) {
            jsonObject.addProperty("skin_texture", skin.get().getTexture());
            jsonObject.addProperty("skin_signature", skin.get().getSignature());
        }
        jsonObject.addProperty("name", name);

        return super.toJson(jsonObject);
    }

    public PlayerRecord fromJson(SerializableRecord record, JsonObject jsonObject) {
        PlayerRecord playerRecord = (PlayerRecord) record;

        if (jsonObject.has("skin_texture")) {
            playerRecord.skin = Optional.of(new MinecraftSkin(jsonObject.get("skin_texture").getAsString(), jsonObject.get("skin_signature").getAsString()));
        }
        playerRecord.name = jsonObject.get("name").getAsString();

        return (PlayerRecord) super.fromJson(record, jsonObject);
    }

}
