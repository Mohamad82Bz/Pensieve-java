package me.Mohamad82.Pensieve.test;

import me.Mohamad82.Pensieve.Main;
import me.Mohamad82.Pensieve.record.RecordManager;
import me.Mohamad82.Pensieve.record.Recorder;
import me.Mohamad82.Pensieve.replay.Replay;
import me.Mohamad82.Pensieve.utils.BlockSoundUtils;
import me.Mohamad82.RUoM.Vector3;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.FileNotFoundException;
import java.util.HashSet;

public class TestRecordCommand implements CommandExecutor {

    Recorder recorder;

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;

        if (args[0].equalsIgnoreCase("start")) {
            recorder = new Recorder(Main.getInstance(), new HashSet<>(Bukkit.getOnlinePlayers()), Vector3.at(0, 0, 0));
            recorder.start();
        } else if (args[0].equalsIgnoreCase("stop")) {
            recorder.stop();
            RecordManager.getInstance().writeToFile(Main.getInstance().getDataFolder(), "testRecord", recorder.getRecords());
        } else if (args[0].equalsIgnoreCase("play")) {
            try {
                Replay replay = new Replay(Main.getInstance(), RecordManager.getInstance().getFromFile(Main.getInstance().getDataFolder(), "testRecord"),
                        player.getWorld(), Vector3.at(0, 0, 0));
                replay.start();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else if (args[0].equalsIgnoreCase("break")) {
            player.playSound(new Location(player.getWorld(), 41, 170, 17),
                    BlockSoundUtils.getBlockSound(BlockSoundUtils.SoundType.valueOf(args[0].toUpperCase()), player.getWorld().getBlockAt(41, 170, 17).getType()),
                    1, 1);
        }
        return true;
    }

}
