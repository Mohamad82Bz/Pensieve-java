package me.mohamad82.pensieve.hooks;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.mohamad82.pensieve.recording.RecordManager;
import me.mohamad82.pensieve.recording.Recorder;
import me.mohamad82.pensieve.replaying.ReplayManager;
import me.mohamad82.ruom.Ruom;
import org.bukkit.OfflinePlayer;

public class PlaceholderAPIHook extends PlaceholderExpansion {

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String getAuthor() {
        return Ruom.getPlugin().getDescription().getAuthors().toString();
    }

    @Override
    public String getIdentifier() {
        return "pensieve";
    }

    @Override
    public String getVersion() {
        return Ruom.getPlugin().getDescription().getVersion();
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (params.startsWith("recorder_exist_")) {
            String recorderName = params.replace("recorder_exist_", "");
            return RecordManager.getInstance().getInternalRecorders().containsKey(recorderName) ? "true" : "false";
        } else if (params.startsWith("recorder_") && params.contains("_contain_player_")) {
            String recorderName = params.replace("recorder_", "");
            String playerName = params.replace("recorder_" + recorderName + "_contain_player_", "");
            if (recorderName.equalsIgnoreCase("any")) {
                boolean contains = false;
                for (Recorder recorder : RecordManager.getInstance().getInternalRecorders().values()) {
                    if (recorder.containsPlayer(playerName)) {
                        contains = true;
                    }
                }
                return contains ? "true" : "false";
            } else {
                if (RecordManager.getInstance().getInternalRecorders().containsKey(recorderName)) {
                    Recorder recorder = RecordManager.getInstance().getInternalRecorders().get(recorderName);
                    return recorder.containsPlayer(playerName) ? "true" : "false";
                } else {
                    return "false";
                }
            }
        } else if (params.startsWith("replayer_exist_")) {
            String replayerName = params.replace("replayer_exist_", "");
            return ReplayManager.getInstance().getInternalReplayers().containsKey(replayerName) ? "true" : "false";
        } else if (params.startsWith("replayer_progress_")) {
            String replayerName = params.replace("replayer_progress_", "");
            if (ReplayManager.getInstance().getInternalReplayers().containsKey(replayerName)) {
                return ReplayManager.getInstance().getInternalReplayers().get(replayerName).getPlaybackControl().getProgress() + "";
            } else {
                return "null";
            }
        } else if (params.startsWith("replayer_formatted_progress_")) {
            String replayerName = params.replace("replayer_formatted_progress_", "");
            if (ReplayManager.getInstance().getInternalReplayers().containsKey(replayerName)) {
                return ReplayManager.getInstance().getInternalReplayers().get(replayerName).getPlaybackControl().getProgressFormatted();
            } else {
                return "null";
            }
        } else if (params.startsWith("replayer_max_progress_")) {
            String replayerName = params.replace("replayer_max_progress_", "");
            if (ReplayManager.getInstance().getInternalReplayers().containsKey(replayerName)) {
                return ReplayManager.getInstance().getInternalReplayers().get(replayerName).getPlaybackControl().getMaxProgress() + "";
            } else {
                return "null";
            }
        } else if (params.startsWith("replayer_speed_")) {
            String replayerName = params.replace("replayer_speed_", "");
            if (ReplayManager.getInstance().getInternalReplayers().containsKey(replayerName)) {
                return ReplayManager.getInstance().getInternalReplayers().get(replayerName).getPlaybackControl().getSpeed() + "";
            } else {
                return "null";
            }
        }

        return null;
    }

}
