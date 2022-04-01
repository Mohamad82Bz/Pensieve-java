package me.mohamad82.pensieve.commands;

import me.mohamad82.pensieve.recording.Recorder;
import me.mohamad82.pensieve.recording.RecorderImpl;
import me.mohamad82.ruom.adventure.AdventureApi;
import me.mohamad82.ruom.adventure.ComponentUtils;
import me.mohamad82.ruom.math.vector.Vector3;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;

public class PensieveCommand implements CommandExecutor {

    private final Map<String, Recorder> recorders = new HashMap<>();

    private final String prefix = "<dark_purple>[<light_purple>Pensieve<dark_purple>] <reset>";

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            AdventureApi.get().console().sendMessage(ComponentUtils.parse(prefix + "<dark_red>Console cannot use this command!"));
            return true;
        }
        Player player = (Player) sender;

        if (args.length == 0) {
            ComponentUtils.send(player, ComponentUtils.parse(""));
        } else {
            switch (args[0].toLowerCase()) {
                case "recorder": {
                    if (args.length == 1) {

                    } else {
                        switch (args[1].toLowerCase()) {
                            case "create": {
                                Optional<String> name = getRecorderNameFromArgs(args);
                                if (name.isPresent()) {
                                    if (recorders.containsKey(name.get())) {
                                        ComponentUtils.send(player, ComponentUtils.parse("<red>This recorder already exists!"));
                                    } else {
                                        final Vector3 location;
                                        if (args.length == 6) {
                                            try {
                                                location = Vector3.at(
                                                        Double.parseDouble(args[3]),
                                                        Double.parseDouble(args[4]),
                                                        Double.parseDouble(args[5])
                                                );
                                            } catch (NumberFormatException e) {
                                                ComponentUtils.send(player, ComponentUtils.parse("<red>Last three arguments must be integers or doubles."));
                                                return true;
                                            }
                                        } else {
                                            location = Vector3.getZero();
                                        }
                                        recorders.put(name.get(), Recorder.recorder(new HashSet<>(), location));
                                    }
                                }
                                break;
                            }
                            case "delete": {
                                Optional<String> name = getRecorderNameFromArgs(args);
                                if (name.isPresent()) {
                                    if (recorders.containsKey(name.get())) {
                                        recorders.remove(name.get());
                                        ComponentUtils.send(player, ComponentUtils.parse("<green>Successfully removed recorder " + name.get()));
                                    } else {
                                        ComponentUtils.send(player, ComponentUtils.parse("<red>Could not find recorder with name: " + name.get()));
                                    }
                                }
                                break;
                            }
                            case "addplayer": {
                                Optional<String> name = getRecorderNameFromArgs(args);
                                if (name.isPresent()) {
                                    if (recorders.containsKey(name.get())) {
                                        ComponentUtils.send(player, ComponentUtils.parse("<red>Could not find recorder with name: " + name.get()));
                                    } else {
                                        
                                    }
                                }
                                break;
                            }
                            case "removeplayer": {

                                break;
                            }
                            case "start": {

                                break;
                            }
                            case "stop": {

                                break;
                            }
                            case "save": {

                                break;
                            }
                        }
                    }
                    break;
                }
                case "replayer": {

                    break;
                }
            }
        }

        return true;
    }

    private Optional<String> getRecorderNameFromArgs(String[] args) {
        if (args.length == 3) {
            return Optional.of(args[2]);
        } else {
            return Optional.empty();
        }
    }

}
