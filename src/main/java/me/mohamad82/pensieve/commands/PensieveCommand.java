package me.mohamad82.pensieve.commands;

import me.mohamad82.pensieve.recording.RecordManager;
import me.mohamad82.pensieve.recording.Recorder;
import me.mohamad82.pensieve.recording.RecorderImpl;
import me.mohamad82.pensieve.replaying.*;
import me.mohamad82.pensieve.serializer.PensieveGsonSerializer;
import me.mohamad82.ruom.Ruom;
import me.mohamad82.ruom.adventure.AdventureApi;
import me.mohamad82.ruom.adventure.ComponentUtils;
import me.mohamad82.ruom.adventure.text.Component;
import me.mohamad82.ruom.math.vector.Vector3;
import me.mohamad82.ruom.utils.ListUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class PensieveCommand implements CommandExecutor {

    private final Map<String, Recorder> recorders = RecordManager.getInstance().getInternalRecorders();
    private final Map<String, Replayer> replayers = ReplayManager.getInstance().getInternalReplayers();

    private final boolean compress = false; //TODO: Turn this back on

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

        boolean doNotSendPanel = false;
        if (label.equalsIgnoreCase("pensieve-panel")) {
            player.sendMessage("");
        }

        if (args.length == 0) {
            //TODO: Command list
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
                                        ComponentUtils.send(player, ComponentUtils.parse(prefix + "<red>This recorder already exists!"));
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
                                                ComponentUtils.send(player, ComponentUtils.parse(prefix + "<red>Could not parse coordinates of center position. Last three arguments must be numbers."));
                                                break;
                                            }
                                        } else {
                                            location = Vector3.getZero();
                                        }
                                        recorders.put(name.get(), Recorder.recorder(new HashSet<>(), location));
                                        sendRecordPanel(player, name.get());
                                        sendMessage(player, label, prefix + "<green>Recorder created! Use <click:suggest_command:'/pensieve recorder addplayer '><yellow>/pensieve recorder addplayer " + name.get() + " <name></yellow></click> to add players.");
                                        sendRecordPanel(player, name.get());
                                        if (sender instanceof Player) {
                                            sendMessage(player, label, String.format("%s<gray>Click <click:run_command:'/pensieve recorder addplayer %s %s'><green><bold><underlined>HERE</underlined></bold></green></click> to add yourself to the recorder.", prefix, name.get(), player.getName()));
                                        }
                                    }
                                } else {
                                    ComponentUtils.send(player, ComponentUtils.parse(prefix + "<red>You must specify a name for the recorder!"));
                                }
                                break;
                            }
                            case "restart": {
                                Optional<String> name = getNameFromArgs(args);
                                if (name.isPresent()) {
                                    if (recorders.containsKey(name.get())) {
                                        recorders.put(name.get(), Recorder.recorder(new HashSet<>(), recorders.get(name.get()).getCenter()));
                                        sendMessage(player, label, prefix + "<green>Recorder has been <red>reset</red>! Use <click:suggest_command:'/pensieve recorder addplayer '><yellow><underlined>/pensieve recorder addplayer " + name.get() + "<name></yellow></underlined></click> to add players.");
                                        sendMessage(player, label, String.format("%s<gray>Click <click:run_command:'/pensieve recorder addplayer %s %s'><green><bold><underlined>HERE</underlined></bold></green></click> to add yourself to the recorder.", prefix, name.get(), player.getName()));
                                    }
                                }
                                break;
                            }
                            case "delete": {
                                Optional<String> name = getNameFromArgs(args);
                                if (name.isPresent()) {
                                    if (recorders.containsKey(name.get())) {
                                        recorders.get(name.get()).stop();
                                        recorders.remove(name.get());
                                        sendMessage(player, label, prefix + "<green>Successfully removed recorder " + name.get());
                                    } else {
                                        ComponentUtils.send(player, ComponentUtils.parse(prefix + "<red>Could not find recorder with name: " + name.get()));
                                    }
                                } else {
                                    ComponentUtils.send(player, ComponentUtils.parse(prefix + "<red>You must specify a name for the recorder!"));
                                }
                                break;
                            }
                            case "addplayer": {
                                Optional<String> name = getNameFromArgs(args);
                                if (name.isPresent()) {
                                    if (!recorders.containsKey(name.get())) {
                                        sendMessage(player, label, prefix + "<red>Could not find recorder with name: " + name.get());
                                        ComponentUtils.send(player, ComponentUtils.parse(prefix + "<red>Could not find recorder with name: " + name.get()));
                                    } else {
                                        if (recorders.get(name.get()).containsPlayer(args[3])) {
                                            sendMessage(player, label, prefix + "<red>Player already exists in recorder " + name.get());
                                        } else {
                                            if (Bukkit.getPlayerExact(args[3]) == null) {
                                                sendMessage(player, label, prefix + "<red>Could not find player with name: " + args[3]);
                                            } else {
                                                recorders.get(name.get()).safeAddPlayer(Bukkit.getPlayerExact(args[3]));
                                                sendMessage(player, label, prefix + "<green>Successfully added player to recorder " + name.get());
                                            }
                                        }
                                    }
                                } else {
                                    ComponentUtils.send(player, ComponentUtils.parse(prefix + "<red>You must specify a name for the recorder!"));
                                }
                                break;
                            }
                            case "removeplayer": {
                                Optional<String> name = getNameFromArgs(args);
                                if (name.isPresent()) {
                                    if (recorders.get(name.get()).containsPlayer(args[3])) {
                                        if (Bukkit.getPlayerExact(args[3]) == null) {
                                            sendMessage(player, label, prefix + "<red>Could not find player with name: " + args[3]);
                                        } else {
                                            recorders.get(name.get()).safeRemovePlayer(Bukkit.getPlayerExact(args[3]));
                                            sendMessage(player, label, prefix + "<green>Successfully removed player from recorder " + name.get());
                                        }
                                    } else {
                                        sendMessage(player, label, prefix + "<red>Player does not exist in recorder " + name.get());
                                    }
                                } else {
                                    ComponentUtils.send(player, ComponentUtils.parse(prefix + "<red>You must specify a name for the recorder!"));
                                }
                                break;
                            }
                            case "start": {
                                Optional<String> name = getNameFromArgs(args);
                                if (name.isPresent()) {
                                    if (!recorders.containsKey(name.get())) {
                                        ComponentUtils.send(player, ComponentUtils.parse(prefix + "<red>Could not find recorder with name: " + name.get()));
                                    } else {
                                        try {
                                            RecorderImpl recorder = (RecorderImpl) recorders.get(name.get());
                                            if (recorder.getPlayers().isEmpty() && recorder.getPlayersToAdd().isEmpty() &&
                                                    recorder.getEntities().isEmpty() && recorder.getEntitiesToAdd().isEmpty()) {
                                                sendMessage(player, label, prefix + "<red>Recorder " + name.get() + " has no players or entities to record!");
                                            } else {
                                                recorder.start();
                                                sendMessage(player, label, prefix + "<green>Successfully started recorder " + name.get());
                                            }
                                        } catch (IllegalStateException e) {
                                            if (recorders.get(name.get()).isStopped()) {
                                                ComponentUtils.send(player, ComponentUtils.parse(prefix + "<red>Cannot start recorder " + name.get() + " because it is already stopped!"));
                                                if (sender instanceof Player) {
                                                    sendMessage(player, label, String.format("%s <gray>Click <click:run_command:'/pensieve recorder restart %s'><green><bold><underlined>HERE</underlined></bold></green></click> to <red><underlined>reset</underlined></red> recorder.", prefix, name.get()));
                                                }
                                            } else {
                                                ComponentUtils.send(player, ComponentUtils.parse(prefix + "<red>Recorder " + name.get() + " is already running!"));
                                            }
                                        }
                                    }
                                } else {
                                    ComponentUtils.send(player, ComponentUtils.parse(prefix + "<red>You must specify a name for the recorder!"));
                                }
                                break;
                            }
                            case "stop": {
                                Optional<String> name = getNameFromArgs(args);
                                if (name.isPresent()) {
                                    if (!recorders.containsKey(name.get())) {
                                        ComponentUtils.send(player, ComponentUtils.parse(prefix + "<red>Could not find recorder with name: " + name.get()));
                                    } else {
                                        if (recorders.get(name.get()).stop()) {
                                            sendMessage(player, label, prefix + "<green>Successfully stopped recorder " + name.get());
                                        } else {
                                            ComponentUtils.send(player, ComponentUtils.parse(prefix + "<red>Recorder " + name.get() + " is not running or it is already stopped!"));
                                        }
                                    }
                                } else {
                                    ComponentUtils.send(player, ComponentUtils.parse(prefix + "<red>You must specify a name for the recorder!"));
                                }
                                break;
                            }
                            case "save": {
                                Optional<String> name = getNameFromArgs(args);
                                if (name.isPresent()) {
                                    if (!recorders.containsKey(name.get())) {
                                        ComponentUtils.send(player, ComponentUtils.parse(prefix + "<red>Could not find recorder with name: " + name.get()));
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
                                        path += ".pensieve";
                                        File storageFile = new File(Ruom.getPlugin().getDataFolder(), "storage");
                                        storageFile.mkdir();
                                        File file = new File(storageFile, path);
                                        if (file.exists()) {
                                            ComponentUtils.send(player, ComponentUtils.parse(prefix + "<red>File already exists: " + path));
                                        } else {
                                            try {
                                                PensieveGsonSerializer.get().serialize(file, recorders.get(name.get()).getRecordContainer(), compress);
                                                replayers.remove(name.get());
                                                sendMessage(player, label, prefix + "<green>Successfully saved recorder " + name.get());
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                                ComponentUtils.send(player, ComponentUtils.parse(prefix + "<red>Could not save recorder " + name.get()));
                                            }
                                        }
                                    }
                                } else {
                                    ComponentUtils.send(player, ComponentUtils.parse(prefix + "<red>You must specify a name for the recorder!"));
                                }
                                break;
                            }
                            case "list": {
                                ComponentUtils.send(player, ComponentUtils.parse(prefix + "<blue>Recorders:" + (recorders.isEmpty() ? " <red>None" : "") + "\n" +
                                        recorders.keySet().stream().map(
                                                s ->"<hover:show_text:'<green>Click here to open this recorder's panel.'><click:run_command:'/pensieve recorder " + s + "'><green>" + s + "</hover></click>"
                                        ).collect(Collectors.joining("\n"))));
                                break;
                            }
                            default: {
                                if (recorders.containsKey(args[1])) {
                                    sendRecordPanel(player, args[1]);
                                } else {
                                    ComponentUtils.send(player, ComponentUtils.parse(prefix + "<red>Unknown subcommand! Use <yellow>/pensieve<red> to see the list of available commands."));
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
                    /replayer speed <name> <speed-preset>
                     */
                    if (args.length == 1) {
                        //TODO: List replayer commands
                    } else {
                        switch (args[1].toLowerCase()) {
                            case "load": {
                                Optional<String> fileName = getNameFromArgs(args);
                                if (!fileName.isPresent()) {
                                    ComponentUtils.send(player, ComponentUtils.parse(prefix + "<red>Invalid file name!"));
                                    break;
                                }
                                Optional<String> name = getNameFromArgs(args, 4);
                                if (!name.isPresent()) {
                                    ComponentUtils.send(player, ComponentUtils.parse(prefix + "<red>Please specify a name for the replayer!"));
                                    break;
                                }
                                if (replayers.containsKey(name.get())) {
                                    ComponentUtils.send(player, ComponentUtils.parse(prefix + "<red>Replayer with name " + name.get() + " already exists!"));
                                    break;
                                }
                                Vector3 center = Vector3.getZero();
                                if (args.length == 7) {
                                    try {
                                        center = Vector3.at(
                                                Double.parseDouble(args[4]),
                                                Double.parseDouble(args[5]),
                                                Double.parseDouble(args[6])
                                        );
                                    } catch (NumberFormatException e) {
                                        ComponentUtils.send(player, ComponentUtils.parse(prefix + "<red>Could not parse coordinates of center position. Last three arguments must be numbers."));
                                        break;
                                    }
                                }
                                if (fileName.isPresent()) {
                                    File storageFile = new File(Ruom.getPlugin().getDataFolder(), "storage");
                                    File file = new File(storageFile, fileName.get());
                                    if (file.exists()) {
                                        try {
                                            Replayer replayer = Replayer.replayer(PensieveGsonSerializer.get().deserialize(file, compress), player.getWorld(), center);
                                            replayers.put(name.get(), replayer);
                                            ComponentUtils.send(player, ComponentUtils.parse(prefix + "<green>Replayer " + name.get() + " loaded successfully!"));
                                            sendReplayPanel(player, name.get());
                                        } catch (IOException e) {
                                            ComponentUtils.send(player, ComponentUtils.parse(prefix + "<red>Could not load replayer " + fileName.get() + ": " + e.getMessage()));
                                        }
                                    } else {
                                        ComponentUtils.send(player, ComponentUtils.parse(prefix + "<red>Could not find replayer " + fileName.get() + "!"));
                                    }
                                }
                                break;
                            }
                            case "start": {
                                Optional<String> name = getNameFromArgs(args);
                                if (!name.isPresent()) {
                                    ComponentUtils.send(player, ComponentUtils.parse(prefix + "<red>Please specify a name for the replayer!"));
                                } else {
                                    if (!replayers.containsKey(name.get())) {
                                        ComponentUtils.send(player, ComponentUtils.parse(prefix + "<red>Replayer with name " + name.get() + " does not exist!"));
                                    } else {
                                        if (replayers.get(name.get()).isStarted()) {
                                            ComponentUtils.send(player, ComponentUtils.parse(prefix + "<red>Replayer " + name.get() + " is already running!"));
                                        } else {
                                            sendMessage(player, label, prefix + "<green>Preparing replayer " + name.get() + "...");
                                            doNotSendPanel = true;
                                            replayers.get(name.get()).prepare().whenComplete((v, err) -> {
                                                if (err != null) {
                                                    ComponentUtils.send(player, ComponentUtils.parse(prefix + "<red>Could not prepare replayer: " + err.getMessage()));
                                                } else {
                                                    try {
                                                        replayers.get(name.get()).start();
                                                        sendMessage(player, label, prefix + "<green>Replayer " + name.get() + " started!");
                                                        ReplayUI replayUI = new ReplayUI(replayers.get(name.get()));
                                                        replayUI.addPlayer(player);
                                                        if (label.equalsIgnoreCase("pensieve-panel")) {
                                                            sendReplayPanel(player, name.get());
                                                        }
                                                    } catch (IllegalStateException e) {
                                                        ComponentUtils.send(player, ComponentUtils.parse(prefix + "<red>Could not start replayer: " + e.getMessage()));
                                                    }
                                                }
                                            });
                                        }
                                    }
                                }
                                break;
                            }
                            case "pause": {
                                Optional<String> name = getNameFromArgs(args);
                                if (!name.isPresent()) {
                                    ComponentUtils.send(player, ComponentUtils.parse(prefix + "<red>Please specify a name for the replayer!"));
                                } else {
                                    if (!replayers.containsKey(name.get())) {
                                        ComponentUtils.send(player, ComponentUtils.parse(prefix + "<red>Replayer with name " + name.get() + " does not exist!"));
                                    } else {
                                        if (replayers.get(name.get()).isStopped() && !replayers.get(name.get()).isStarted()) {
                                            ComponentUtils.send(player, ComponentUtils.parse(prefix + "<red>Replayer " + name.get() + " is not running!"));
                                        } else if (replayers.get(name.get()).getPlaybackControl().isPause()) {
                                            ComponentUtils.send(player, ComponentUtils.parse(prefix + "<red>Replayer " + name.get() + " is already paused!"));
                                        } else {
                                            replayers.get(name.get()).getPlaybackControl().setPause(true);
                                            sendMessage(player, label, prefix + "<green>Replayer " + name.get() + " paused!");
                                        }
                                    }
                                }
                                break;
                            }
                            case "resume": {
                                Optional<String> name = getNameFromArgs(args);
                                if (!name.isPresent()) {
                                    ComponentUtils.send(player, ComponentUtils.parse(prefix + "<red>Please specify a name for the replayer!"));
                                } else {
                                    if (!replayers.containsKey(name.get())) {
                                        ComponentUtils.send(player, ComponentUtils.parse(prefix + "<red>Replayer with name " + name.get() + " does not exist!"));
                                    } else {
                                        if (replayers.get(name.get()).getPlaybackControl().isPause()) {
                                            replayers.get(name.get()).getPlaybackControl().setPause(false);
                                            sendMessage(player, label, prefix + "<green>Replayer " + name.get() + " resumed!");
                                        } else {
                                            ComponentUtils.send(player, ComponentUtils.parse(prefix + "<red>Replayer " + name.get() + " is not paused!"));
                                        }
                                    }
                                }
                                break;
                            }
                            case "stop": {
                                Optional<String> name = getNameFromArgs(args);
                                if (!name.isPresent()) {
                                    ComponentUtils.send(player, ComponentUtils.parse(prefix + "<red>Please specify a name for the replayer!"));
                                } else {
                                    if (!replayers.containsKey(name.get())) {
                                        ComponentUtils.send(player, ComponentUtils.parse(prefix + "<red>Replayer with name " + name.get() + " does not exist!"));
                                    } else {
                                        if (replayers.get(name.get()).suspend()) {
                                            sendMessage(player, label, prefix + "<green>Replayer " + name.get() + " stopped!");
                                        } else {
                                            ComponentUtils.send(player, ComponentUtils.parse(prefix + "<red>Replayer " + name.get() + " is already stopped!"));
                                        }
                                    }
                                }
                                break;
                            }
                            case "skip": {
                                Optional<String> name = getNameFromArgs(args);
                                if (!name.isPresent()) {
                                    ComponentUtils.send(player, ComponentUtils.parse(prefix + "<red>Please specify a name for the replayer!"));
                                } else {
                                    if (!replayers.containsKey(name.get())) {
                                        ComponentUtils.send(player, ComponentUtils.parse(prefix + "<red>Replayer with name " + name.get() + " does not exist!"));
                                    } else {
                                        if (args.length != 4) {
                                            ComponentUtils.send(player, ComponentUtils.parse(prefix + "<red>Please specify a number of ticks to skip!"));
                                        } else {
                                            int ticks;
                                            try {
                                                ticks = Integer.parseInt(args[3]);
                                            } catch (NumberFormatException e) {
                                                ComponentUtils.send(player, ComponentUtils.parse(prefix + "<red>Number of ticks must be an integer!"));
                                                break;
                                            }
                                            replayers.get(name.get()).getPlaybackControl().setProgress(replayers.get(name.get()).getPlaybackControl().getProgress() + ticks);
                                            String skippedTick = ticks % 20 == 0 ? ticks % 20 == 1 ? "1 second" : ticks / 20 + " seconds" : ticks == 1 ? "1 tick" : ticks + " ticks";
                                            sendMessage(player, label, prefix + "<green>Replayer " + name.get() + " skipped " +
                                                    skippedTick + "!");
                                            if (label.equalsIgnoreCase("pensieve-noreply")) {
                                                AdventureApi.get().player(player).sendActionBar(ComponentUtils.parse("<green>Skipped " + skippedTick + "!"));
                                            }
                                        }
                                    }
                                }
                                break;
                            }
                            case "rewind": {
                                Optional<String> name = getNameFromArgs(args);
                                if (!name.isPresent()) {
                                    ComponentUtils.send(player, ComponentUtils.parse(prefix + "<red>Please specify a name for the replayer!"));
                                } else {
                                    if (!replayers.containsKey(name.get())) {
                                        ComponentUtils.send(player, ComponentUtils.parse(prefix + "<red>Replayer with name " + name.get() + " does not exist!"));
                                    } else {
                                        if (args.length != 4) {
                                            ComponentUtils.send(player, ComponentUtils.parse(prefix + "<red>Please specify a number of ticks to rewind!"));
                                        } else {
                                            int ticks;
                                            try {
                                                ticks = Integer.parseInt(args[3]);
                                            } catch (NumberFormatException e) {
                                                ComponentUtils.send(player, ComponentUtils.parse(prefix + "<red>Number of ticks must be an integer!"));
                                                break;
                                            }
                                            replayers.get(name.get()).getPlaybackControl().setProgress(replayers.get(name.get()).getPlaybackControl().getProgress() - ticks);
                                            String rewoundTicks = ticks % 20 == 0 ? ticks % 20 == 1 ? "1 second" : ticks / 20 + " seconds" : ticks == 1 ? "1 tick" : ticks + " ticks";
                                            sendMessage(player, label, prefix + "<green>Replayer " + name.get() + " rewound " +
                                                    rewoundTicks + "!");
                                            if (label.equalsIgnoreCase("pensieve-noreply")) {
                                                AdventureApi.get().player(player).sendActionBar(ComponentUtils.parse("<green>Rewound " + rewoundTicks + "!"));
                                            }
                                        }
                                    }
                                }
                                break;
                            }
                            case "speed": {
                                Optional<String> name = getNameFromArgs(args);
                                if (!name.isPresent()) {
                                    ComponentUtils.send(player, ComponentUtils.parse(prefix + "<red>Please specify a name for the replayer!"));
                                } else {
                                    if (!replayers.containsKey(name.get())) {
                                        ComponentUtils.send(player, ComponentUtils.parse(prefix + "<red>Replayer with name " + name.get() + " does not exist!"));
                                    } else {
                                        if (args.length != 4) {
                                            ComponentUtils.send(player, ComponentUtils.parse(prefix + "<red>Please specify a speed multiplier"));
                                        } else {
                                            PlayBackControl.Speed speed;
                                            try {
                                                speed = PlayBackControl.Speed.valueOf(args[3]);
                                            } catch (IllegalArgumentException e) {
                                                ComponentUtils.send(player, ComponentUtils.parse(prefix + "<red>Speed must be one of: " + Arrays.toString(PlayBackControl.Speed.values())));
                                                break;
                                            }
                                            replayers.get(name.get()).getPlaybackControl().setSpeed(speed);
                                            sendMessage(player, label, prefix + "<green>Replayer " + name.get() + " speed set to " + speed + "!");
                                        }
                                    }
                                }
                                break;
                            }
                            case "list": {
                                ComponentUtils.send(player, ComponentUtils.parse(prefix + "<blue>Replayers:" + (replayers.isEmpty() ? " <red>None" : "") + "\n" +
                                        replayers.keySet().stream().map(
                                                s ->"<hover:show_text:'<green>Click here to open this replayer's panel.'><click:run_command:'/pensieve replayer " + s + "'><green>" + s + "</hover></click>"
                                        ).collect(Collectors.joining("\n"))));
                                break;
                            }
                            case "delete": {
                                Optional<String> name = getNameFromArgs(args);
                                if (!name.isPresent()) {
                                    ComponentUtils.send(player, ComponentUtils.parse(prefix + "<red>Please specify a name for the replayer!"));
                                } else {
                                    if (!replayers.containsKey(name.get())) {
                                        ComponentUtils.send(player, ComponentUtils.parse(prefix + "<red>Replayer with name " + name.get() + " does not exist!"));
                                    } else {
                                        replayers.get(name.get()).suspend();
                                        replayers.remove(name.get());
                                        sendMessage(player, label, prefix + "<green>Replayer " + name.get() + " deleted!");
                                    }
                                }
                                break;
                            }
                            default: {
                                if (replayers.containsKey(args[1])) {
                                    sendReplayPanel(player, args[1]);
                                } else {
                                    ComponentUtils.send(player, ComponentUtils.parse(prefix + "<red>Unknown subcommand! Use <yellow>/pensieve<red> to see the list of available commands."));
                                }
                            }
                        }
                    }
                    break;
                }
            }
        }

        if (!doNotSendPanel && label.equalsIgnoreCase("pensieve-panel")) {
            getNameFromArgs(args).ifPresent(name -> {
                if (args[0].equalsIgnoreCase("recorder")) {
                    sendRecordPanel(player, name);
                } else if (args[0].equalsIgnoreCase("replayer")) {
                    sendReplayPanel(player, name);
                }
            });
        }

        return true;
    }

    private void sendRecordPanel(Player player, String recorderName) {
        RecorderImpl recorder = (RecorderImpl) recorders.get(recorderName);
        String startButton = "";
        String startText = "<#79A7D3>Start";
        String restartText = "<gold>Restart";
        boolean shouldSendRestartText = false;
        String stopButton = "";
        String saveButton = "";
        String status = "";
        String players = "<hover:show_text:'<gray>" + recorder.getPlayers().stream().map(Player::getName).collect(Collectors.joining(", ")) + "<dark_gray>" + recorder.getPlayersToAdd().stream().map(Player::getName).collect(Collectors.joining(", ")) + "'><white>" + (recorder.getPlayers().size() + recorder.getPlayersToAdd().size());
        String entities = "<white>" + (recorder.getEntities().size() + recorder.getEntitiesToAdd().size());
        String green = "<#CBD18F>";
        String red = "<#CC313D>";
        if (!recorder.isStarted() && !recorder.isStopped()) {
            status = green + "Ready";
            startButton = String.format("<#8A307F><click:run_command:'/pensieve-panel recorder start %s'><hover:show_text:'<dark_purple>• <green>Click here to start the recorder.'>[•]</hover></click>", recorderName);
            stopButton = "<#CC313D><hover:show_text:'<dark_purple>• <red>Recorder is not started.'>[•]</hover>";
            saveButton = "<#CC313D><hover:show_text:'<dark_purple>• <red>Recorder does not have anything to save.'>[•]</hover>";
        } else if (recorder.isStarted() && !recorder.isStopped()) {
            status = green + "Recording";
            startButton = "<#CC313D><hover:show_text:'<dark_purple>• <red>Recorder is already started.'>[•]</hover>";
            stopButton = String.format("<#8A307F><click:run_command:'/pensieve-panel recorder stop %s'><hover:show_text:'<dark_purple>• <red>Click here to stop the recorder'>[•]</hover></click>", recorderName);
            saveButton = "<#CC313D><hover:show_text:'<dark_purple>• <red>Recorder does not have anything to save.'>[•]</hover>";
        } else if (recorder.isStopped()) {
            status = red + "Stopped";
            shouldSendRestartText = true;
            startButton = String.format("<#CC313D><click:run_command:'/pensieve-panel recorder restart %s'><hover:show_text:'<dark_purple>• <green>Click here to restart the recorder.\n<red>NOTE: Recorder\\'s data will be lost if it is unsaved.'>[•]</hover></click>", recorderName);
            stopButton = "<#CC313D><hover:show_text:'<dark_purple>• <red>Recorder is already stopped.'>[•]</hover>";
            saveButton = String.format("<#8A307F><click:run_command:'/pensieve-panel recorder save %s'><hover:show_text:'<dark_purple>• <green>Click here to save the recorder.'>[•]</hover></click>", recorderName);
        }
        for (String message : ListUtils.toList(
                "",
                "<#3A6B35>]<strikethrough>                     </strikethrough>»<#CCF381> Recorder <#3A6B35>«<strikethrough>                     </strikethrough>[",
                "<#CBD18F>>",
                "<#CBD18F>>   <#8A307F>[•] <#E3B448>Name: <#CBD18F> " + recorderName,
                "<#CBD18F>>   <#8A307F>[•] <#E3B448>Status: " + status,
                "<#CBD18F>>   <#8A307F>[•] <#E3B448>Players: " + players,
                "<#CBD18F>>   <#8A307F>[•] <#E3B448>Entities: " + entities,
                "<#CBD18F>>",
                String.format("<#CBD18F>>   %s %s           %s <#79A7D3>Stop           %s <#79A7D3>Save ", startButton, shouldSendRestartText ? restartText : startText, stopButton, saveButton),
                String.format("<#CBD18F>>   <#8A307F><click:suggest_command:'/pensieve-panel recorder addplayer %s'><hover:show_text:'<dark_purple>• <green>Click here to add a player to the recorder.'>[•]</hover></click> <#79A7D3>Add Player" +
                        "    <#8A307F><click:suggest_command:'/pensieve-panel recorder removeplayer %s'><hover:show_text:'<dark_purple>• <green>Click here to remove a player to the recorder.'>[•]</hover></click> <#79A7D3>Remove Player", recorderName, recorderName),
                "<#CBD18F>>",
                "<#3A6B35>]<strikethrough>                                                           </strikethrough>["
        )) {
            ComponentUtils.send(player, ComponentUtils.parse(message));
        }
    }

    private void sendReplayPanel(Player player, String replayerName) {
        Ruom.runSync(() -> {
            ReplayerImpl replayer = (ReplayerImpl) replayers.get(replayerName);
            String startButton = "";
            String stopButton = "";
            String pauseButton = "";
            String speedButton = "";
            String skipRewindButtons = "";
            String status = "";
            String green = "<#CBD18F>";
            String red = "<#CC313D>";
            skipRewindButtons = String.format("<bold><#79A7D3><click:run_command:'/pensieve-noreply replayer rewind %s 1200'><hover:show_text:'<dark_purple>• <red>Click here to rewind 1 minute'> <<< </hover></click>" +
                    "    <#79A7D3><click:run_command:'/pensieve-noreply replayer rewind %s 200'><hover:show_text:'<dark_purple>• <red>Click here to rewind 10 seconds'> << </hover></click>" +
                    "    <#79A7D3><click:run_command:'/pensieve-noreply replayer rewind %s 20'><hover:show_text:'<dark_purple>• <red>Click here to rewind 1 second'> < </hover></click>" +
                    "    <#79A7D3><click:run_command:'/pensieve-noreply replayer skip %s 20'><hover:show_text:'<dark_purple>• <red>Click here to skip 1 second'> > </hover></click>" +
                    "    <#79A7D3><click:run_command:'/pensieve-noreply replayer skip %s 200'><hover:show_text:'<dark_purple>• <red>Click here to skip 10 seconds'> >> </hover></click>" +
                    "    <#79A7D3><click:run_command:'/pensieve-noreply replayer skip %s 1200'><hover:show_text:'<dark_purple>• <red>Click here to skip 1 minute'> >>> </hover></click>", replayerName, replayerName, replayerName, replayerName, replayerName, replayerName);
            if (!replayer.isStarted() && !replayer.isStopped()) {
                status = green + "Ready";
                startButton = String.format("<#8A307F><click:run_command:'/pensieve-panel replayer start %s'><hover:show_text:'<dark_purple>• <green>Click here to start the replayer.'>[•]</hover></click> <#79A7D3>Start", replayerName);
                stopButton = "<#CC313D><hover:show_text:'<dark_purple>• <red>Replayer is not started.'>[•]</hover>";
                pauseButton = "<#CC313D><hover:show_text:'<dark_purple>• <red>Replayer is not started.'>[•]</hover> <#79A7D3>Pause  ";
                speedButton = "<#CC313D><hover:show_text:'<dark_purple>• <red>Replayer is not started.'>[•]</hover> <#79A7D3>Speed";
            } else if (replayer.isStarted() && !replayer.isStopped()) {
                status = green + "Replaying" + (replayer.getPlaybackControl().isPause() ? " <dark_aqua>- " + red + "Paused" : "") + " <dark_aqua>- " + "<aqua>" + replayer.getPlaybackControl().getSpeed().name();
                startButton = "<#CC313D><hover:show_text:'<dark_purple>• <red>Replayer is already started.'>[•]</hover> <#79A7D3>Start";
                stopButton = String.format("<#8A307F><click:run_command:'/pensieve-panel replayer stop %s'><hover:show_text:'<dark_purple>• <red>Click here to stop the replayer'>[•]</hover></click>", replayerName);
                if (replayer.getPlaybackControl().isPause()) {
                    pauseButton = String.format("<#8A307F><click:run_command:'/pensieve-panel replayer resume %s'><hover:show_text:'<dark_purple>• <red>Click here to resume the replayer'>[•]</hover></click> <#79A7D3>Resume", replayerName);
                } else {
                    pauseButton = String.format("<#8A307F><click:run_command:'/pensieve-panel replayer pause %s'><hover:show_text:'<dark_purple>• <red>Click here to pause the replayer'>[•]</hover></click> <#79A7D3>Pause  ", replayerName);
                }
                speedButton = String.format("<#8A307F><click:suggest_command:'/pensieve-panel replayer speed %s '><hover:show_text:'<dark_purple>• <red>Click here to change the speed of the replayer'>[•]</hover></click> <#79A7D3>Speed", replayerName);
            } else if (replayer.isStopped()) {
                status = red + "Stopped";
                startButton = String.format("<#8A307F><click:run_command:'/pensieve-panel replayer start %s'><hover:show_text:'<dark_purple>• <green>Click here to start the replayer again.'>[•]</hover></click> <#79A7D3>Start", replayerName);
                stopButton = "<#CC313D><hover:show_text:'<dark_purple>• <red>Replayer is already stopped.'>[•]</hover>";
                pauseButton = "<#CC313D><hover:show_text:'<dark_purple>• <red>Replayer is stopped.'>[•]</hover> <#79A7D3>Pause  ";
                speedButton = "<#CC313D><hover:show_text:'<dark_purple>• <red>Replayer is stopped.'>[•]</hover> <#79A7D3>Speed";
            }
            for (String message : ListUtils.toList(
                    "",
                    "<#3A6B35>]<strikethrough>                     </strikethrough>»<blue> Replayer <#3A6B35>«<strikethrough>                     </strikethrough>[",
                    "<#CBD18F>>",
                    "<#CBD18F>>   <#8A307F>[•] <#E3B448>Name: <#CBD18F> " + replayerName,
                    "<#CBD18F>>   <#8A307F>[•] <#E3B448>Status: " + status,
                    "<#CBD18F>>",
                    String.format("<#CBD18F>>   %s           %s <#79A7D3>Stop", startButton, stopButton),
                    String.format("<#CBD18F>>   %s        %s", pauseButton, speedButton),
                    "<#CBD18F>>",
                    String.format("<#CBD18F>> %s", skipRewindButtons),
                    "<#CBD18F>>",
                    "<#3A6B35>]<strikethrough>                                                           </strikethrough>["
            )) {
                ComponentUtils.send(player, ComponentUtils.parse(message));
            }
        }, 3);
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

    private void sendMessage(CommandSender sender, String label, String message/*, boolean isError*/) {
        Component component = ComponentUtils.parse(message);
        if (!label.equalsIgnoreCase("pensieve-noreply")/* && isError*/) {
            if (sender instanceof Player) {
                ComponentUtils.send((Player) sender, component);
            } else {
                AdventureApi.get().console().sendMessage(component);
            }
        }
    }

}
