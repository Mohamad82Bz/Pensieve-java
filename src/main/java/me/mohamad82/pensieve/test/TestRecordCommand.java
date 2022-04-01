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
import me.mohamad82.ruom.math.vector.Vector3;
import me.mohamad82.ruom.math.vector.Vector3Utils;
import me.mohamad82.ruom.math.vector.Vector3UtilsBukkit;
import me.mohamad82.ruom.npc.entity.FallingBlockNPC;
import me.mohamad82.ruom.npc.entity.FishingHookNPC;
import me.mohamad82.ruom.npc.entity.ThrowableProjectileNPC;
import me.mohamad82.ruom.utils.NMSUtils;
import me.mohamad82.ruom.utils.PlayerUtils;
import me.mohamad82.ruom.world.Schematic;
import me.mohamad82.ruom.world.WorldEdit;
import me.mohamad82.ruom.xseries.NMSExtras;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

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
            case "paste": {
                Schematic schematic = new Schematic(WorldEdit.getClipboardFromSchematic(new File(Ruom.getPlugin().getDataFolder(), "arena.schem")).get(), player.getLocation(), true);
                schematic.prepare().whenComplete((v, err) -> {
                    new BukkitRunnable() {
                        public void run() {
                            for (Vector3 circleLoc : MathUtils.circle(7.5f, 10, true)) {
                                Vector3 circleLocation = circleLoc.add(PlayerUtils.getPlayerVector3Location(player));
                                Vector3 randomBlockLocation = schematic.getNearestBlock(schematic.nextLayerIndex(), circleLocation);
                                FallingBlockNPC fallingBlock = FallingBlockNPC.fallingBlockNPC(Vector3UtilsBukkit.toLocation(player.getWorld(), circleLocation), schematic.getBlockData(randomBlockLocation).getMaterial());
                                fallingBlock.setNoGravity(true);
                                fallingBlock.addViewers(Ruom.getOnlinePlayers());
                                fallingBlock.move(Vector3Utils.getTravelDistance(circleLocation, randomBlockLocation), 100).whenComplete((bool, error) -> {
                                    fallingBlock.removeViewers(Ruom.getOnlinePlayers());
                                    Ruom.runSync(() -> {
                                        schematic.applyAndUpdate(randomBlockLocation);
                                    });
                                });

                                if (ThreadLocalRandom.current().nextInt(100) < 10) {
                                    ThrowableProjectileNPC throwableProjectile = ThrowableProjectileNPC.throwableProjectileNPC(Vector3UtilsBukkit.toLocation(player.getWorld(), randomBlockLocation).add(0, 80, 0), new ItemStack(schematic.getBlockData(randomBlockLocation).getMaterial()));
                                    throwableProjectile.setGlowing(true);
                                    throwableProjectile.setNoGravity(true);
                                    throwableProjectile.addViewers(Ruom.getOnlinePlayers());
                                    throwableProjectile.move(Vector3Utils.getTravelDistance(randomBlockLocation.clone().add(0, 80, 0), randomBlockLocation), 100).whenComplete((bool, error) -> {
                                        throwableProjectile.removeViewers(Ruom.getOnlinePlayers());
                                    });
                                }

                                schematic.remove(randomBlockLocation);

                                if (schematic.isDone()) {
                                    cancel();
                                    Ruom.broadcast("Finished");
                                }
                            }
                        }
                    }.runTaskTimerAsynchronously(Ruom.getPlugin(), 0, 1);
                });
                break;
            }
            case "cirblock": {
                List<Vector3> circle = MathUtils.circle(5, 60, true);
                new BukkitRunnable() {
                    int index = 0;
                    private final Map<Integer, FallingBlockNPC> map = new HashMap<>();
                    public void run() {
                        Vector3 location = circle.get(index).clone().add(PlayerUtils.getPlayerVector3Location(player));
                        Vector3 nextLocation;
                        if (index + 1 >= circle.size()) {
                            nextLocation = circle.get(0).clone().add(PlayerUtils.getPlayerVector3Location(player));
                        } else {
                            nextLocation = circle.get(index + 1).clone().add(PlayerUtils.getPlayerVector3Location(player));
                        }

                        if (map.containsKey(index)) {
                            map.get(index).move(Vector3Utils.getTravelDistance(location, nextLocation));
                        } else {
                            FallingBlockNPC fallingBlock = FallingBlockNPC.fallingBlockNPC(Vector3UtilsBukkit.toLocation(player.getWorld(), location), Material.DIRT);
                            fallingBlock.setNoGravity(true);
                            fallingBlock.addViewers(Ruom.getOnlinePlayers());
                            map.put(index, fallingBlock);
                        }

                        index++;
                        if (index >= circle.size()) {
                            index = 0;
                        }
                    }
                }.runTaskTimerAsynchronously(Ruom.getPlugin(), 0, 1);
                break;
            }
            case "charkhesh": {
                Location playerLocation = player.getLocation().clone();
                for (int a = 1; a<= 4; a++) {
                    for (int i = 1; i <= 4; i++) {
                        List<Vector3> circle = MathUtils.circle(i, 200, false);
                        for (Vector3 location : circle) {
                            player.getWorld().spawnParticle(Particle.REDSTONE, Vector3UtilsBukkit.toLocation(player.getWorld(), location.clone().add(Vector3UtilsBukkit.toVector3(playerLocation))), 1, new Particle.DustOptions(Color.RED, 1f));
                        }
                        FallingBlockNPC fallingBlock = FallingBlockNPC.fallingBlockNPC(Vector3UtilsBukkit.toLocation(player.getWorld(), circle.get(0).clone().add(PlayerUtils.getPlayerVector3Location(player))), Material.WHITE_WOOL);
                        fallingBlock.setNoGravity(true);
                        fallingBlock.addViewers(Ruom.getOnlinePlayers());
                        Ruom.runAsync(new Runnable() {
                            int index = 0;
                            public void run() {
                                Vector3 location = Vector3UtilsBukkit.toVector3(playerLocation).add(circle.get(index));
                                Vector3 prevLocation = circle.get(index == 0 ? circle.size() - 1 : index - 1).add(Vector3UtilsBukkit.toVector3(playerLocation));
                                fallingBlock.move(Vector3Utils.getTravelDistance(prevLocation, location));
                                index++;
                                if (index >= circle.size()) {
                                    index = 0;
                                }
                            }
                        }, a * 50, 1);
                    }
                }
                break;
            }
            case "save": {
                Ruom.runAsync(() -> {
                    try {
                        File file = new File(Ruom.getPlugin().getDataFolder(), "testReplay.json");
                        file.delete();
                        file.createNewFile();
                        FileWriter writer = new FileWriter(file);
                        writer.write(PensieveGsonSerializer.get().serialize(recorder.getRecordContainer()));
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
                        writer.write(PensieveGsonSerializer.get().serialize(container));
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
