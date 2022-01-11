package me.mohamad82.pensieve.test;

import me.mohamad82.pensieve.recording.Recorder;
import me.mohamad82.pensieve.replaying.Replayer;
import me.mohamad82.ruom.Ruom;
import me.mohamad82.ruom.npc.entity.FishingHookNPC;
import me.mohamad82.ruom.utils.NMSUtils;
import me.mohamad82.ruom.utils.PacketUtils;
import me.mohamad82.ruom.vector.Vector3;
import me.mohamad82.ruom.vector.Vector3Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TestRecordCommand implements CommandExecutor {

    Recorder recorder;

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;

        switch (args[0].toLowerCase()) {
            case "hook": {
                FishingHookNPC npc = FishingHookNPC.fishingHookNPC(player.getLocation().clone().add(0, -1, 0), player.getEntityId());
                npc.addViewers(Ruom.getOnlinePlayers());
                npc.setHookedEntity(Bukkit.getPlayerExact("xii69").getEntityId() + 1);
                break;
            }
            case "start": {
                recorder = Recorder.recorder(Ruom.getOnlinePlayers(), Vector3.at(0, 0, 0));
                recorder.start();
                break;
            }
            case "stop": {
                recorder.stop();
                break;
            }
            case "play": {
                Replayer replayer = Replayer.replayer(recorder.getRecordContainer(), player.getWorld(), Vector3.at(0, 0, 0));
                replayer.start();
                break;
            }
        }
        return true;
    }

}
