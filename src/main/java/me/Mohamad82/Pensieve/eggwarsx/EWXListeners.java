package me.Mohamad82.Pensieve.eggwarsx;

import me.Mohamad82.Pensieve.Main;
import me.Mohamad82.Pensieve.data.DataManager;
import me.Mohamad82.Pensieve.record.RecordManager;
import me.Mohamad82.Pensieve.record.Recorder;
import me.Mohamad82.RUoM.Vector3;
import me.wazup.eggwars.events.EWArenaStartEvent;
import me.wazup.eggwars.events.EWArenaStopEvent;
import me.wazup.eggwars.events.EWPlayerLeaveArenaEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.File;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

public class EWXListeners implements Listener {

    @EventHandler
    public void onArenaStart(EWArenaStartEvent event) {
        List<Player> players = event.getPlayers();
        Recorder recorder = new Recorder(Main.getInstance(), new HashSet<>(players), Vector3.at(0, 100, 0));
        recorder.start();
    }

    @EventHandler
    public void onArenaStop(EWArenaStopEvent event) {
        List<Player> players = event.getPlayers();
        Recorder finishingRecorder = null;
        for (Recorder recorder : RecordManager.getInstance().getRecorders()) {
            for (Player player : players) {
                if (recorder.getPlayers().contains(player)) {
                    finishingRecorder = recorder;
                    break;
                }
            }
        }
        if (finishingRecorder == null) {
            Main.getInstance().getLogger().severe("An Eggwars game finished but couldn't find the recorder," +
                    " Something is may be wrong! Recorders should be stopped otherwise it will fill the server's memory." +
                    " If you don't know why this is happening, please contact the developer.");
            return;
        }
        finishingRecorder.stop();

        Date date = new Date();
        Recorder finalFinishingRecorder = finishingRecorder;
        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), new Runnable() {
            @Override
            public void run() {
                for (Player player : players) {
                    File folder = DataManager.getInstance().createFolder(player.getUniqueId().toString());
                    String fileName = DataManager.dateFormat.format(date) + "-" +
                            DataManager.getInstance().getFilesCountInFolderInSameDate(player.getUniqueId().toString(), date);
                    RecordManager.getInstance().writeToFile(folder, fileName, finalFinishingRecorder.getRecords());
                }
            }
        });
    }

    @EventHandler
    public void onArenaLeave(EWPlayerLeaveArenaEvent event) {
        Player player = event.getPlayer();
        for (Recorder recorder : RecordManager.getInstance().getRecorders()) {
            recorder.getPlayers().remove(player);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        for (Recorder recorder : RecordManager.getInstance().getRecorders()) {
            recorder.getPlayers().remove(player);
            if (recorder.getPlayers().isEmpty()) {
                recorder.stop();
            }
        }
    }

}
