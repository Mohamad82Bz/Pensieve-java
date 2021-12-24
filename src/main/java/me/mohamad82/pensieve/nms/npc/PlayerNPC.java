package me.mohamad82.pensieve.nms.npc;

import com.mojang.authlib.GameProfile;
import me.Mohamad82.RUoM.Ruom;
import me.Mohamad82.RUoM.translators.skin.MinecraftSkin;
import me.Mohamad82.RUoM.utils.ServerVersion;
import me.mohamad82.pensieve.nms.NMSUtils;
import me.mohamad82.pensieve.nms.PacketUtils;
import me.mohamad82.pensieve.nms.accessors.EntityAccessor;
import me.mohamad82.pensieve.nms.accessors.ServerPlayerAccessor;
import me.mohamad82.pensieve.nms.accessors.ServerPlayerGameModeAccessor;
import me.mohamad82.pensieve.utils.Utils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public class PlayerNPC extends NPC {

    private final float yaw;

    protected PlayerNPC(String name, Location location, Optional<MinecraftSkin> skin) {
        this.yaw = location.getYaw();

        Utils.ignoreExcRun(() -> {
            GameProfile profile = new GameProfile(UUID.randomUUID(), name);
            Object entity;
            if (ServerVersion.supports(17)) {
                entity = ServerPlayerAccessor.getConstructor0().newInstance(
                        NMSUtils.getDedicatedServer(),
                        NMSUtils.getServerLevel(location.getWorld()),
                        profile
                );
            } else {
                entity = ServerPlayerAccessor.getConstructor1().newInstance(
                        NMSUtils.getDedicatedServer(),
                        NMSUtils.getServerLevel(location.getWorld()),
                        profile,
                        ServerPlayerGameModeAccessor.getConstructor0().newInstance(NMSUtils.getServerLevel(location.getWorld()))
                );
            }
            EntityAccessor.getMethodSetPos1().invoke(entity, location.getX(), location.getY(), location.getZ());
            initialize(entity);
            if (skin.isPresent())
                skin.get().apply(entity);
        });
    }

    public static PlayerNPC playerNPC(String name, Location location, Optional<MinecraftSkin> skin) {
        return new PlayerNPC(name, location, skin);
    }

    public void collect(int collectedEntityId, int amount) {
        collect(collectedEntityId, id, amount);
    }

    public void setTabList(@Nullable String name) {
        Ruom.runSync(() -> {
            NMSUtils.sendPacket(getViewers(),
                    PacketUtils.getPlayerInfoPacket(entity, "REMOVE_PLAYER"));
            if (name != null) {
                //TODO: modify listName here.
                NMSUtils.sendPacket(getViewers(),
                        PacketUtils.getPlayerInfoPacket(entity, "ADD_PLAYER"));
            }
        }, 1);
    }

    @Override
    protected void addViewer(Player player) {
        NMSUtils.sendPacket(player,
                PacketUtils.getPlayerInfoPacket(entity, "ADD_PLAYER"),
                PacketUtils.getAddPlayerPacket(entity),
                PacketUtils.getHeadRotatePacket(entity, this.yaw));
    }

    @Override
    protected void removeViewer(Player player) {
        NMSUtils.sendPacket(player,
                PacketUtils.getPlayerInfoPacket(entity, "REMOVE_PLAYER"),
                PacketUtils.getRemoveEntitiesPacket(id));
    }

}
