package me.mohamad82.pensieve.test;

import me.mohamad82.pensieve.recording.Recorder;
import me.mohamad82.pensieve.replaying.Replay;
import me.mohamad82.ruom.Ruom;
import me.mohamad82.ruom.vector.Vector3;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TestRecordCommand implements CommandExecutor {

    Recorder recorder;

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;

        switch (args[0].toLowerCase()) {
            case "start": {
                recorder = new Recorder(Ruom.getOnlinePlayers(), Vector3.at(0, 0, 0));
                recorder.start();
                break;
            }
            case "stop": {
                recorder.stop();
                break;
            }
            case "play": {
                Replay replay = new Replay(recorder.getPlayerRecords(), recorder.getEntityRecords(), player.getWorld(), Vector3.at(0, 0, 0));
                replay.start();
                break;
            }
        }
        return true;
    }

}
