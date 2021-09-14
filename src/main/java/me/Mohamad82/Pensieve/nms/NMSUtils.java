package me.Mohamad82.Pensieve.nms;

import me.Mohamad82.RUoM.XSeries.ReflectionUtils;
import me.Mohamad82.RUoM.vector.Vector3;
import org.bukkit.entity.Player;

import java.util.Set;

public class NMSUtils {

    public static void sendBlockBreakAnimation(Set<Player> players, Vector3 location, int stage) {
        Object packetPlayOutBlockBreakAnimation = Packets.getPacketPlayOutBlockBreakAnimation(location, stage);

        for (Player player : players) {
            ReflectionUtils.sendPacket(player,
                    packetPlayOutBlockBreakAnimation);
        }
    }

}
