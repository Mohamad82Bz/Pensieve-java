package me.Mohamad82.Pensieve.record.listeners;

import io.netty.channel.*;
import me.Mohamad82.Pensieve.nms.enums.BlockDirection;
import me.Mohamad82.Pensieve.record.PendingBlockBreak;
import me.Mohamad82.Pensieve.record.RecordManager;
import me.Mohamad82.Pensieve.record.RecordTick;
import me.Mohamad82.Pensieve.record.Recorder;
import me.Mohamad82.RUoM.XSeries.ReflectionUtils;
import me.Mohamad82.RUoM.utils.ServerVersion;
import me.Mohamad82.RUoM.vector.Vector3;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class PacketListener implements Listener {

    Map<Player, PendingBlockBreak> breakingPlayers = new HashMap<>();
    Map<Player, ItemStack> eatingPlayers = new HashMap<>();

    JavaPlugin plugin;

    private static PacketListener instance;
    public static PacketListener getInstance() {
        return instance;
    }

    public PacketListener(JavaPlugin plugin) {
        instance = this;
        this.plugin = plugin;
        start();

        for (Player player : Bukkit.getOnlinePlayers()) {
            try {
                injectPlayer(player);
            } catch (IllegalArgumentException ignore) {}
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        injectPlayer(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        removePlayer(event.getPlayer());
    }

    public void start() {
        new BukkitRunnable() {
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (breakingPlayers.containsKey(player) || eatingPlayers.containsKey(player)) {
                        for (Recorder recorder : RecordManager.getInstance().getRecorders()) {
                            if (recorder.getPlayers().contains(player)) {
                                if (recorder.isRunning()) {
                                    RecordTick currentTick = recorder.getCurrentTick(player);

                                    if (breakingPlayers.containsKey(player))
                                        currentTick.setPendingBlockBreak(breakingPlayers.get(player));
                                    if (eatingPlayers.containsKey(player))
                                        currentTick.setEatingItem(eatingPlayers.get(player));
                                }
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    private void injectPlayer(Player player) {
        ChannelDuplexHandler channelDuplexHandler = new ChannelDuplexHandler() {
            @Override
            public void channelRead(ChannelHandlerContext context, Object packet) throws Exception {
                if (packet.toString().contains("PacketPlayInBlockDig")) {
                    Object digType = packet.getClass().getMethod(ServerVersion.supports(13) ? "d" : "c").invoke(packet);

                    switch (digType.toString()) {
                        case "START_DESTROY_BLOCK":
                            for (Recorder recorder : RecordManager.getInstance().getRecorders()) {
                                if (recorder.getPlayers().contains(player)) {
                                    if (recorder.isRunning()) {
                                        if (player.getGameMode().equals(GameMode.SURVIVAL)) {
                                            RecordTick currentTick = recorder.getCurrentTick(player);
                                            PendingBlockBreak pendingBlockBreak = new PendingBlockBreak();
                                            Object blockPosition = packet.getClass().getMethod(ServerVersion.supports(13) ? "b" : "a").invoke(packet);
                                            int x = (int) blockPosition.getClass().getMethod("getX").invoke(blockPosition);
                                            int y = (int) blockPosition.getClass().getMethod("getY").invoke(blockPosition);
                                            int z = (int) blockPosition.getClass().getMethod("getZ").invoke(blockPosition);
                                            pendingBlockBreak.setLocation(Vector3.at(x, y, z));
                                            pendingBlockBreak.setMaterial(player.getWorld().getBlockAt(x, y, z).getType());
                                            pendingBlockBreak.setBlockDirection(BlockDirection.valueOf(
                                                    packet.getClass().getMethod(ServerVersion.supports(13) ? "c" : "b")
                                                            .invoke(packet).toString().toUpperCase()));
                                            currentTick.setPendingBlockBreak(pendingBlockBreak);
                                            breakingPlayers.put(player, pendingBlockBreak);
                                        }
                                    }
                                }
                            }
                            break;
                        case "ABORT_DESTROY_BLOCK":
                        case "STOP_DESTROY_BLOCK":
                            breakingPlayers.remove(player);
                            break;
                        case "RELEASE_USE_ITEM":
                            eatingPlayers.remove(player);
                            break;
                    }
                }
                super.channelRead(context, packet);
            }

            @Override
            public void write(ChannelHandlerContext context, Object packet, ChannelPromise channelPromise) throws Exception {
                super.write(context, packet, channelPromise);
            }
        };

        try {
            Object playerConnection = ReflectionUtils.getConnection(player);
            ChannelPipeline pipeline = (ChannelPipeline) playerConnection.getClass().getField("networkManager").getType()
                    .getField("channel").getType().getMethod("pipeline")
                    .invoke(ReflectionUtils.getConnection(player).getClass().getField("networkManager").getType().getField("channel")
                    .get(playerConnection.getClass().getField("networkManager").get(playerConnection)));
            pipeline.addBefore("packet_handler", player.getName(), channelDuplexHandler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removePlayer(Player player) {
        try {
            Object playerConnection = ReflectionUtils.getConnection(player);
            Channel channel = (Channel) playerConnection.getClass().getField("networkManager").getType().getField("channel")
                    .get(playerConnection.getClass().getField("networkManager")
                    .get(playerConnection));

            channel.eventLoop().submit(() -> {
                channel.pipeline().remove(player.getName());
                return null;
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
