package me.mohamad82.pensieve.commands;

import me.mohamad82.pensieve.recording.RecordManager;
import me.mohamad82.pensieve.replaying.PlayBackControl;
import me.mohamad82.pensieve.replaying.ReplayManager;
import me.mohamad82.ruom.Ruom;
import me.mohamad82.ruom.utils.ListUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.HumanEntity;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class PensieveTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1) {
            return getTabComplete(args[0], ListUtils.toList("recorder", "replayer"));
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("recorder")) {
                return getTabComplete(args[1], ListUtils.toList("create", "delete", "addplayer", "removeplayer", "start", "stop", "save", "list"));
            } else if (args[0].equalsIgnoreCase("replayer")) {
                return getTabComplete(args[1], ListUtils.toList("load", "start", "pause", "resume", "stop", "skip", "rewind", "speed", "list", "delete"));
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("recorder")) {
                switch (args[1].toLowerCase()) {
                    case "create":
                        return ListUtils.toList("<name>");
                    case "delete":
                    case "addplayer":
                    case "removeplayer":
                    case "start":
                    case "stop":
                    case "save":
                        return getTabComplete(args[2], RecordManager.getInstance().getInternalRecorders().keySet());
                }
            } else if (args[0].equalsIgnoreCase("replayer")) {
                switch (args[1].toLowerCase()) {
                    case "load":
                        return getTabComplete(args[2], Arrays.stream(new File(Ruom.getPlugin().getDataFolder() + File.separator + "storage").listFiles()).map(File::getName).collect(Collectors.toList()));
                    case "start":
                    case "stop":
                    case "pause":
                    case "resume":
                    case "skip":
                    case "rewind":
                    case "speed":
                    case "delete":
                        return getTabComplete(args[2], ReplayManager.getInstance().getInternalReplayers().keySet());
                }
            }
        } else if (args.length == 4) {
            if (args[0].equalsIgnoreCase("recorder")) {
                switch (args[1].toLowerCase()) {
                    case "removeplayer":
                    case "addplayer":
                        return getTabComplete(args[3], Ruom.getOnlinePlayers().stream().map(HumanEntity::getName).collect(Collectors.toList()));
                    case "save":
                        return ListUtils.toList("<file>");
                }
            } else if (args[0].equalsIgnoreCase("replayer")) {
                switch (args[1].toLowerCase()) {
                    case "load":
                        return ListUtils.toList("<name>");
                    case "skip":
                    case "rewind":
                        return ListUtils.toList("<time in ticks (20 ticks = 1 second)>");
                    case "speed":
                        return Arrays.stream(PlayBackControl.Speed.values()).map(Enum::name).collect(Collectors.toList());
                }
            }
        } else if (args.length == 5) {
            if (args[0].equalsIgnoreCase("replayer")) {
                if (args[1].equalsIgnoreCase("load")) {
                    return ListUtils.toList("(x) (y) (z)");
                }
            }
        } else if (args.length == 6) {
            if (args[0].equalsIgnoreCase("replayer")) {
                if (args[1].equalsIgnoreCase("load")) {
                    return ListUtils.toList("(y) (z)");
                }
            }
        } else if (args.length == 7) {
            if (args[0].equalsIgnoreCase("replayer")) {
                if (args[1].equalsIgnoreCase("load")) {
                    return ListUtils.toList("(z)");
                }
            }
        }
        return Collections.emptyList();
    }

    public List<String> getTabComplete(String arg, Collection<String> list) {
        List<String> newList = new ArrayList<>();
        for (String a : list) {
            if (a.toLowerCase().startsWith(arg.toLowerCase())) {
                newList.add(a);
            }
        }
        return newList;
    }

}
