package me.mohamad82.pensieve.test;

import me.mohamad82.pensieve.recording.RecordContainer;
import me.mohamad82.pensieve.recording.Recorder;
import me.mohamad82.pensieve.replaying.PlayBackControl;
import me.mohamad82.pensieve.replaying.Replayer;
import me.mohamad82.pensieve.serializer.PensieveGsonSerializer;
import me.mohamad82.ruom.Ruom;
import me.mohamad82.ruom.adventure.ComponentUtils;
import me.mohamad82.ruom.hologram.Hologram;
import me.mohamad82.ruom.math.MathUtils;
import me.mohamad82.ruom.math.vector.Vector3UtilsBukkit;
import me.mohamad82.ruom.npc.entity.FishingHookNPC;
import me.mohamad82.ruom.utils.BlockUtils;
import me.mohamad82.ruom.utils.NMSUtils;
import me.mohamad82.ruom.utils.PlayerUtils;
import me.mohamad82.ruom.math.vector.Vector3;
import me.mohamad82.ruom.math.vector.Vector3Utils;
import me.mohamad82.ruom.xseries.NMSExtras;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class TestRecordCommand implements CommandExecutor, Listener {

    Recorder recorder;
    Replayer replayer;
    PlayBackControl playBackControl;

    public TestRecordCommand() {
        Ruom.registerListener(this);
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;

        switch (args[0].toLowerCase()) {
            case "itemholo": {
                Ruom.runSync(new Runnable() {
                    Map<Player, Item> shownItems = new HashMap<>();
                    Map<Item, Hologram> itemHolograms = new HashMap<>();
                    public void run() {
                        for (Map.Entry<Player, Item> entry : shownItems.entrySet()) {
                            if (entry.getValue().isDead()) {
                                shownItems.remove(entry.getKey());
                                itemHolograms.get(entry.getValue()).removeViewers(Ruom.getOnlinePlayers());
                                itemHolograms.remove(entry.getValue());
                            }
                        }
                        for (Player onlinePlayer : Ruom.getOnlinePlayers()) {
                            for (Entity nearbyEntity : onlinePlayer.getNearbyEntities(3, 3, 3)) {
                                if (nearbyEntity.getType() == EntityType.DROPPED_ITEM) {
                                    Item item = (Item) nearbyEntity;
                                }
                            }
                        }
                    }
                }, 0, 1);
                break;
            }
            case "itemjson": {
                Ruom.broadcast(NMSUtils.getItemStackNBTJson(player.getInventory().getItemInMainHand()));
                player.getInventory().addItem(NMSUtils.getItemStackFromNBTJson(NMSUtils.getItemStackNBTJson(player.getInventory().getItemInMainHand())));
                break;
            }
            case "maxhealth": {
                double maxHealth;
                try {
                    maxHealth = Double.parseDouble(args[1]);
                    player.setMaxHealth(maxHealth);
                } catch (NumberFormatException e) {
                    player.sendMessage("NUMBER you DUMB shit as fuck ashole, bitch");
                    Ruom.runSync(() -> {
                        player.sendMessage("Pofiyoz");
                        Ruom.runSync(() -> {
                            player.sendMessage("Koskhole Yevari");
                            Ruom.runSync(() -> {
                                player.sendMessage("Gav");
                                Ruom.runSync(() -> {
                                    player.sendMessage("'Number', 'Adad', Understood?");
                                }, 50);
                            }, 70);
                        }, 40);
                    }, 40);
                }
                break;
            }
            case "cirsmite": {
                Ruom.runSync(() -> {

                    Set<Vector3> points = MathUtils.cylinder(Integer.parseInt(args[1]), Integer.parseInt(args[2]), true, false);
                    List<Vector3> pointsArray = new ArrayList<>(points);
                    new BukkitRunnable() {
                        int i = 0;
                        public void run() {
                            if (i < pointsArray.size()) {
                                Vector3 point = pointsArray.get(i);
                                NMSExtras.lightning(Ruom.getOnlinePlayers(), Vector3UtilsBukkit.toLocation(player.getWorld(), PlayerUtils.getPlayerVector3Location(player).add(point)), false);
                                i++;
                            } else {
                                cancel();
                            }
                        }
                    }.runTaskTimer(Ruom.getPlugin(), 0, 1);
                }, 0, 60);
                break;
            }
            case "save": {
                Ruom.runAsync(() -> {
                    try {
                        File file = new File(Ruom.getPlugin().getDataFolder(), "testReplay.json");
                        file.delete();
                        file.createNewFile();
                        FileWriter writer = new FileWriter(file);
                        writer.write(PensieveGsonSerializer.get().serialize(recorder.getRecordContainer(), false));
                        writer.flush();
                        writer.close();

                        Ruom.broadcast(ComponentUtils.parse("<rainbow>COMPLETED"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                break;
            }
            case "load": {
                Ruom.runAsync(() -> {
                    RecordContainer container = PensieveGsonSerializer.get().deserialize(new File(Ruom.getPlugin().getDataFolder(), "testReplay.json"));
                    replayer = Replayer.replayer(container, player.getWorld(), Vector3.at(0, 0, 0));

                    try {
                        File file = new File(Ruom.getPlugin().getDataFolder(), "loadedReplay.json");
                        file.delete();
                        file.createNewFile();
                        FileWriter writer = new FileWriter(file);
                        writer.write(PensieveGsonSerializer.get().serialize(container, true));
                        writer.flush();
                        writer.close();
                        Ruom.broadcast(ComponentUtils.parse("<rainbow>COMPLETED"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

                break;
            }
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
                replayer = Replayer.replayer(recorder.getRecordContainer(), player.getWorld(), Vector3.at(0, 0, 0));
                break;
            }
            case "play": {
                replayer.prepare().whenComplete((v, err) -> {
                    playBackControl = replayer.start();
                });
                break;
            }
            case "progress": {
                playBackControl.setProgress(Integer.parseInt(args[1]));
                break;
            }
            case "speed": {
                try {
                    playBackControl.setSpeed(PlayBackControl.Speed.valueOf(args[1]));
                } catch (IllegalArgumentException e) {
                    player.sendMessage("Wrong speed. Available speeds: " + Arrays.toString(PlayBackControl.Speed.values()));
                }
                break;
            }
        }
        return true;
    }

}
