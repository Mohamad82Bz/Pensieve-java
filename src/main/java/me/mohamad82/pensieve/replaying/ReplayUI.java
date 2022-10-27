package me.mohamad82.pensieve.replaying;

import me.mohamad82.ruom.Ruom;
import me.mohamad82.ruom.string.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class ReplayUI implements Listener {

    private final BossBar bossbar;
    private boolean stopped;

    public ReplayUI(Replayer replayer) {
        Ruom.registerListener(this);
        PlayBackControl playbackControl = replayer.getPlaybackControl();
        bossbar = Bukkit.createBossBar(
                "",
                BarColor.PURPLE,
                BarStyle.SOLID
        );
        bossbar.setVisible(true);
        new BukkitRunnable() {
            public void run() {
                if (stopped || replayer.isStopped()) {
                    bossbar.setVisible(false);
                    for (Player player : bossbar.getPlayers()) {
                        bossbar.removePlayer(player);
                    }
                    cancel();
                    return;
                }
                String speed;
                switch (playbackControl.getSpeed()) {
                    case x1: speed = ">"; break;
                    case x2: speed = ">>"; break;
                    case x5: speed = ">>>"; break;
                    case x050: speed = "<"; break;
                    case x025: speed = "<<"; break;
                    default: speed = "?"; break;
                }
                bossbar.setTitle(
                        StringUtils.colorize(
                                String.format("&d• %s  &b%s  &a%s / %s &d•", playbackControl.isPause() ? "&cPaused" : "&aPlaying", speed, playbackControl.getProgressFormatted(), playbackControl.getMaxProgressFormatted())
                        )
                );
                bossbar.setProgress((double) playbackControl.getProgress() / playbackControl.getMaxProgress());
            }
        }.runTaskTimerAsynchronously(Ruom.getPlugin(), 0, 1);
    }

    public void addPlayer(Player player) {
        bossbar.addPlayer(player);
    }

    public void removePlayer(Player player) {
        bossbar.removePlayer(player);
    }

    public void stop() {
        stopped = true;
    }

    @EventHandler
    public void onPluginDisable(PluginDisableEvent event) {
        if (event.getPlugin().equals(Ruom.getPlugin())) {
            stop();
        }
    }

}
