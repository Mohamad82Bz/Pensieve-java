package me.mohamad82.pensieve.commands;

import me.mohamad82.pensieve.recording.RecordManager;
import me.mohamad82.pensieve.recording.Recorder;
import me.mohamad82.pensieve.replaying.ReplayManager;
import me.mohamad82.pensieve.replaying.Replayer;
import me.mohamad82.pensieve.serializer.PensieveGsonSerializer;
import me.mohamad82.ruom.Ruom;
import me.mohamad82.ruom.adventure.AdventureApi;
import me.mohamad82.ruom.adventure.ComponentUtils;
import me.mohamad82.ruom.math.vector.Vector3;
import me.mohamad82.ruom.utils.ListUtils;
import me.mohamad82.ruom.utils.PlayerUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;

public class PensieveCommand implements CommandExecutor {

    private final Map<String, Recorder> recorders = RecordManager.getInstance().getInternalRecorders();
    private final Map<String, Replayer> replayers = ReplayManager.getInstance().getInternalReplayers();

    private final String prefix = "<dark_purple>[<light_purple>Pensieve<dark_purple>] <reset>";

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            AdventureApi.get().console().sendMessage(ComponentUtils.parse(prefix + "<dark_red>Console cannot use this command!"));
            return true;
        }
        Player player = (Player) sender;
        if (!player.hasPermission("pensieve.use")) {
            ComponentUtils.send(player, ComponentUtils.parse(prefix + "<dark_red>You don't have permission to use this command!"));
            return true;
        }

        if (args.length == 0) {
            ComponentUtils.send(player, ComponentUtils.parse("Subcommand list here"));
        } else {
            switch (args[0].toLowerCase()) {
                case "recorder": {
                    if (args.length == 1) {
                        //TODO: List of all available arguments (create/delete/addplayer/removeplayer/start/stop/save)
                    } else {
                        switch (args[1].toLowerCase()) {
                            case "create": {
                                Optional<String> name = getNameFromArgs(args);
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
                                                break;
                                            }
                                        } else {
                                            location = Vector3.getZero();
                                        }
                                        recorders.put(name.get(), Recorder.recorder(new HashSet<>(), location));
                                        sendMessage(player, label, "<green>Recorder created!");
                                    }
                                } else {
                                    ComponentUtils.send(player, ComponentUtils.parse("<red>You must specify a name for the recorder!"));
                                }
                                break;
                            }
                            case "delete": {
                                Optional<String> name = getNameFromArgs(args);
                                if (name.isPresent()) {
                                    if (recorders.containsKey(name.get())) {
                                        recorders.get(name.get()).stop();
                                        recorders.remove(name.get());
                                        sendMessage(player, label, "<green>Successfully removed recorder " + name.get());
                                    } else {
                                        ComponentUtils.send(player, ComponentUtils.parse("<red>Could not find recorder with name: " + name.get()));
                                    }
                                } else {
                                    ComponentUtils.send(player, ComponentUtils.parse("<red>You must specify a name for the recorder!"));
                                }
                                break;
                            }
                            case "addplayer": {
                                Optional<String> name = getNameFromArgs(args);
                                if (name.isPresent()) {
                                    if (!recorders.containsKey(name.get())) {
                                        sendMessage(player, label, "<red>Could not find recorder with name: " + name.get());
                                        ComponentUtils.send(player, ComponentUtils.parse("<red>Could not find recorder with name: " + name.get()));
                                    } else {
                                        recorders.get(name.get()).safeAddPlayer(player);
                                        sendMessage(player, label, "<green>Successfully added player to recorder " + name.get());
                                    }
                                } else {
                                    ComponentUtils.send(player, ComponentUtils.parse("<red>You must specify a name for the recorder!"));
                                }
                                break;
                            }
                            case "removeplayer": {
                                Optional<String> name = getNameFromArgs(args);
                                if (name.isPresent()) {
                                    if (!recorders.containsKey(name.get())) {
                                        ComponentUtils.send(player, ComponentUtils.parse("<red>Could not find recorder with name: " + name.get()));
                                    } else {
                                        recorders.get(name.get()).safeRemovePlayer(player);
                                        sendMessage(player, label, "<green>Successfully removed player from recorder " + name.get());
                                    }
                                } else {
                                    ComponentUtils.send(player, ComponentUtils.parse("<red>You must specify a name for the recorder!"));
                                }
                                break;
                            }
                            case "start": {
                                Optional<String> name = getNameFromArgs(args);
                                if (name.isPresent()) {
                                    if (!recorders.containsKey(name.get())) {
                                        ComponentUtils.send(player, ComponentUtils.parse("<red>Could not find recorder with name: " + name.get()));
                                    } else {
                                        recorders.get(name.get()).start();
                                        sendMessage(player, label, "<green>Successfully started recorder " + name.get());
                                    }
                                } else {
                                    ComponentUtils.send(player, ComponentUtils.parse("<red>You must specify a name for the recorder!"));
                                }
                                break;
                            }
                            case "stop": {
                                Optional<String> name = getNameFromArgs(args);
                                if (name.isPresent()) {
                                    if (!recorders.containsKey(name.get())) {
                                        ComponentUtils.send(player, ComponentUtils.parse("<red>Could not find recorder with name: " + name.get()));
                                    } else {
                                        recorders.get(name.get()).stop();
                                        sendMessage(player, label, "<green>Successfully stopped recorder " + name.get());
                                    }
                                } else {
                                    ComponentUtils.send(player, ComponentUtils.parse("<red>You must specify a name for the recorder!"));
                                }
                                break;
                            }
                            case "save": {
                                Optional<String> name = getNameFromArgs(args);
                                if (name.isPresent()) {
                                    if (!recorders.containsKey(name.get())) {
                                        ComponentUtils.send(player, ComponentUtils.parse("<red>Could not find recorder with name: " + name.get()));
                                    } else {
                                        String path;
                                        if (args.length > 3) {
                                            StringBuilder stringBuilder = new StringBuilder();
                                            for (int i = 3; i < args.length; i++) {
                                                stringBuilder.append(args[i]).append(" ");
                                            }
                                            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                                            path = stringBuilder.toString();
                                        } else {
                                            Date date = new Date();
                                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
                                            dateFormat.format(date);
                                            path = dateFormat.format(date) + ";" + name.get();
                                        }
                                        File storageFile = new File(Ruom.getPlugin().getDataFolder(), "storage");
                                        storageFile.mkdir();
                                        File file = new File(storageFile, path);
                                        if (file.exists()) {
                                            ComponentUtils.send(player, ComponentUtils.parse("<red>File already exists: " + path));
                                        } else {
                                            try {
                                                PensieveGsonSerializer.get().serialize(file, recorders.get(name.get()).getRecordContainer());
                                                replayers.remove(name.get());
                                                sendMessage(player, label, "<green>Successfully saved recorder " + name.get());
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                                ComponentUtils.send(player, ComponentUtils.parse("<red>Could not save recorder " + name.get()));
                                            }
                                        }
                                    }
                                } else {
                                    ComponentUtils.send(player, ComponentUtils.parse("<red>You must specify a name for the recorder!"));
                                }
                                break;
                            }
                            case "list": {
                                ComponentUtils.send(player, ComponentUtils.parse("<blue>Recorders: " + recorders.keySet()));
                                for (String recorder : recorders.keySet()) {
                                    ComponentUtils.send(player, ComponentUtils.parse("<yellow>- " + recorder));
                                }
                                break;
                            }
                            default: {
                                if (recorders.containsKey(args[1])) {
                                    Recorder recorder = recorders.get(args[1]);
                                    String status = "";
                                    if (!recorder.isStarted() && !recorder.isStopped()) {
                                        status = "<#CBD18F>Ready";
                                    } else if (recorder.isStarted() && !recorder.isStopped()) {
                                        status = "<#CBD18F>Recording";
                                    } else if (recorder.isStopped()) {
                                        status = "<#CC313D>Stopped";
                                    }
                                    for (String message : ListUtils.toList(
                                            "<#3A6B35>]<strikethrough>                     </strikethrough>»<#CCF381> Recorder <#3A6B35>«<strikethrough>                     </strikethrough>[",
                                            "<#CBD18F>>",
                                            "<#CBD18F>>   <#8A307F>[•] <#E3B448>Name: <#CBD18F> " + args[1],
                                            "<#CBD18F>>   <#8A307F>[•] <#E3B448>Status: " + status,
                                            "<#CBD18F>>",
                                            "<#CBD18F>>   <#8A307F>[•] <#79A7D3>Start           <#8A307F>[•] <#79A7D3>Stop           <#8A307F>[•] <#79A7D3>Save ",
                                            "<#CBD18F>>   <#8A307F>[•] <#79A7D3>Add Player    <#8A307F>[•] <#79A7D3>Remove Player",
                                            "<#CBD18F>>",
                                            "<#3A6B35>]<strikethrough>                                                           </strikethrough>["
                                    )) {
                                        ComponentUtils.send(player, ComponentUtils.parse(message));
                                    }
                                } else {
                                    ComponentUtils.send(player, ComponentUtils.parse("<red>Unknown subcommand! Use <yellow>/pensieve<red> to see the list of available commands."));
                                }
                            }
                        }
                    }
                    break;
                }
                case "replayer": {
                    /*
                    /replayer load <fileName> <name> (x, y, z)
                    /replayer <start/pause/stop> <name>
                    /replayer <skip/rewind> <name> <amount>
                     */
                    if (args.length == 1) {
                        //TODO: List replayer commands
                    } else {
                        switch (args[1].toLowerCase()) {
                            case "load": {
                                Optional<String> fileName = getNameFromArgs(args);
                                if (!fileName.isPresent()) {
                                    ComponentUtils.send(player, ComponentUtils.parse("<red>Invalid file name!"));
                                    break;
                                }
                                Optional<String> name = getNameFromArgs(args, 4);
                                if (!name.isPresent()) {
                                    ComponentUtils.send(player, ComponentUtils.parse("<red>Please specify a name for the replayer!"));
                                    break;
                                }
                                if (replayers.containsKey(name.get())) {
                                    ComponentUtils.send(player, ComponentUtils.parse("<red>Replayer with name " + name.get() + " already exists!"));
                                    break;
                                }
                                Vector3 center = PlayerUtils.getPlayerVector3Location(player);
                                if (args.length == 7) {
                                    try {
                                        center = Vector3.at(
                                                Double.parseDouble(args[3]),
                                                Double.parseDouble(args[4]),
                                                Double.parseDouble(args[5])
                                        );
                                    } catch (NumberFormatException e) {
                                        ComponentUtils.send(player, ComponentUtils.parse("<red>Could not parse coordinates of center position. Last three arguments must be numbers."));
                                        break;
                                    }
                                }
                                if (fileName.isPresent()) {
                                    File storageFile = new File(Ruom.getPlugin().getDataFolder(), "storage");
                                    File file = new File(storageFile, fileName.get());
                                    if (file.exists()) {
                                        try {
                                            Replayer replayer = Replayer.replayer(PensieveGsonSerializer.get().deserialize(file), player.getWorld(), center);
                                            replayers.put(name.get(), replayer);
                                        } catch (IOException e) {
                                            ComponentUtils.send(player, ComponentUtils.parse("<red>Could not load replayer " + fileName.get() + ": " + e.getMessage()));
                                        }
                                    } else {
                                        ComponentUtils.send(player, ComponentUtils.parse("<red>Could not find replayer " + fileName.get() + "!"));
                                    }
                                }
                                break;
                            }
                            case "start": {
                                Optional<String> name = getNameFromArgs(args);
                                if (!name.isPresent()) {
                                    ComponentUtils.send(player, ComponentUtils.parse("<red>Please specify a name for the replayer!"));
                                } else {
                                    if (!replayers.containsKey(name.get())) {
                                        ComponentUtils.send(player, ComponentUtils.parse("<red>Replayer with name " + name.get() + " does not exist!"));
                                    } else {
                                        sendMessage(player, label, "<green>Preparing replayer " + name.get() + "...");
                                        replayers.get(name.get()).prepare().whenComplete((v, err) -> {
                                            if (err != null) {
                                                ComponentUtils.send(player, ComponentUtils.parse("<red>Could not prepare replayer: " + err.getMessage()));
                                            } else {
                                                try {
                                                    replayers.get(name.get()).start();
                                                    sendMessage(player, label, "<green>Replayer " + name.get() + " started!");
                                                } catch (IllegalStateException e) {
                                                    ComponentUtils.send(player, ComponentUtils.parse("<red>Replayer " + name.get() + " is already running!"));
                                                }
                                            }
                                        });
                                    }
                                }
                                break;
                            }
                            case "pause": {
                                Optional<String> name = getNameFromArgs(args);
                                if (!name.isPresent()) {
                                    ComponentUtils.send(player, ComponentUtils.parse("<red>Please specify a name for the replayer!"));
                                } else {
                                    if (!replayers.containsKey(name.get())) {
                                        ComponentUtils.send(player, ComponentUtils.parse("<red>Replayer with name " + name.get() + " does not exist!"));
                                    } else {
                                        if (replayers.get(name.get()).getPlaybackControl().isPause()) {
                                            ComponentUtils.send(player, ComponentUtils.parse("<red>Replayer " + name.get() + " is already paused!"));
                                        } else {
                                            replayers.get(name.get()).getPlaybackControl().setPause(true);
                                            sendMessage(player, label, "<green>Replayer " + name.get() + " paused!");
                                        }
                                    }
                                }
                                break;
                            }
                            case "resume": {
                                Optional<String> name = getNameFromArgs(args);
                                if (!name.isPresent()) {
                                    ComponentUtils.send(player, ComponentUtils.parse("<red>Please specify a name for the replayer!"));
                                } else {
                                    if (!replayers.containsKey(name.get())) {
                                        ComponentUtils.send(player, ComponentUtils.parse("<red>Replayer with name " + name.get() + " does not exist!"));
                                    } else {
                                        if (replayers.get(name.get()).getPlaybackControl().isPause()) {
                                            replayers.get(name.get()).getPlaybackControl().setPause(false);
                                            sendMessage(player, label, "<green>Replayer " + name.get() + " resumed!");
                                        } else {
                                            ComponentUtils.send(player, ComponentUtils.parse("<red>Replayer " + name.get() + " is not paused!"));
                                        }
                                    }
                                }
                                break;
                            }
                            case "stop": {
                                Optional<String> name = getNameFromArgs(args);
                                if (!name.isPresent()) {
                                    ComponentUtils.send(player, ComponentUtils.parse("<red>Please specify a name for the replayer!"));
                                } else {
                                    if (!replayers.containsKey(name.get())) {
                                        ComponentUtils.send(player, ComponentUtils.parse("<red>Replayer with name " + name.get() + " does not exist!"));
                                    } else {
                                        if (replayers.get(name.get()).suspend()) {
                                            sendMessage(player, label, "<green>Replayer " + name.get() + " stopped!");
                                        } else {
                                            ComponentUtils.send(player, ComponentUtils.parse("<red>Replayer " + name.get() + " is already stopped!"));
                                        }
                                    }
                                }
                                break;
                            }
                            case "skip": {
                                Optional<String> name = getNameFromArgs(args);
                                if (!name.isPresent()) {
                                    ComponentUtils.send(player, ComponentUtils.parse("<red>Please specify a name for the replayer!"));
                                } else {
                                    if (!replayers.containsKey(name.get())) {
                                        ComponentUtils.send(player, ComponentUtils.parse("<red>Replayer with name " + name.get() + " does not exist!"));
                                    } else {
                                        if (args.length != 3) {
                                            ComponentUtils.send(player, ComponentUtils.parse("<red>Please specify a number of ticks to skip!"));
                                        } else {
                                            int ticks;
                                            try {
                                                ticks = Integer.parseInt(args[2]);
                                            } catch (NumberFormatException e) {
                                                ComponentUtils.send(player, ComponentUtils.parse("<red>Number of ticks must be an integer!"));
                                                break;
                                            }
                                            replayers.get(name.get()).getPlaybackControl().setProgress(replayers.get(name.get()).getPlaybackControl().getProgress() + ticks);
                                            sendMessage(player, label, "<green>Replayer " + name.get() + " skipped " + ticks + " ticks!");
                                        }
                                    }
                                }
                                break;
                            }
                            case "rewind": {
                                Optional<String> name = getNameFromArgs(args);
                                if (!name.isPresent()) {
                                    ComponentUtils.send(player, ComponentUtils.parse("<red>Please specify a name for the replayer!"));
                                } else {
                                    if (!replayers.containsKey(name.get())) {
                                        ComponentUtils.send(player, ComponentUtils.parse("<red>Replayer with name " + name.get() + " does not exist!"));
                                    } else {
                                        if (args.length != 3) {
                                            ComponentUtils.send(player, ComponentUtils.parse("<red>Please specify a number of ticks to rewind!"));
                                        } else {
                                            int ticks;
                                            try {
                                                ticks = Integer.parseInt(args[2]);
                                            } catch (NumberFormatException e) {
                                                ComponentUtils.send(player, ComponentUtils.parse("<red>Number of ticks must be an integer!"));
                                                break;
                                            }
                                            replayers.get(name.get()).getPlaybackControl().setProgress(replayers.get(name.get()).getPlaybackControl().getProgress() - ticks);
                                            sendMessage(player, label, "<green>Replayer " + name.get() + " rewound " + ticks + " ticks!");
                                        }
                                    }
                                }
                                break;
                            }
                            case "list": {
                                ComponentUtils.send(player, ComponentUtils.parse("<blue>Replayer list:"));
                                for (String name : replayers.keySet()) {
                                    ComponentUtils.send(player, ComponentUtils.parse("<green>- " + name));
                                }
                                break;
                            }
                        }
                    }
                    break;
                }
            }
        }

        return true;
    }

    private Optional<String> getNameFromArgs(String[] args, int arg) {
        if (args.length >= arg) {
            return Optional.of(args[arg - 1]);
        } else {
            return Optional.empty();
        }
    }

    private Optional<String> getNameFromArgs(String[] args) {
        return getNameFromArgs(args, 3);
    }

    private void sendMessage(Player player, String label, String message) {
        if (label.equalsIgnoreCase("pensieve")) {
            ComponentUtils.send(player, ComponentUtils.parse(message));
        }
    }

}
