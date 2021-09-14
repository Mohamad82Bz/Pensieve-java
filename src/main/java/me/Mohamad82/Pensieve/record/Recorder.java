package me.Mohamad82.Pensieve.record;

import me.Mohamad82.Pensieve.nms.enums.NPCState;
import me.Mohamad82.RUoM.utils.ServerVersion;
import me.Mohamad82.RUoM.vector.Vector3;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class Recorder implements Listener {

    private final Recorder thisRecorder = this;

    private final JavaPlugin plugin;
    private Vector3 center;

    private final Set<Player> players;
    private final Set<Record> records = new HashSet<>();
    private final Map<Player, RecordTick> currentTick = new HashMap<>();
    private final Map<Player, RecordTick> lastTicks = new HashMap<>();
    private final Map<UUID, RecordTick> lastNonNullTicks = new HashMap<>();

    private BukkitTask runnable;
    private int currentTickIndex = 0;

    public Recorder(JavaPlugin plugin, Set<Player> player, Vector3 center) {
        this.plugin = plugin;
        this.players = player;
        this.center = Vector3.at(center.getBlockX() + 0.5, center.getBlockY(), center.getBlockZ() + 0.5);
    }

    public Recorder(JavaPlugin plugin, Player player) {
        this.plugin = plugin;
        Set<Player> players = new HashSet<>();
        players.add(player);
        this.players = players;
    }

    public void start() {
        for (Player player : players) {
            records.add(new Record(player.getUniqueId(), player.getName()));
            getPlayerRecord(player).setCenter(center);
            if (players.size() == 1)
                center = Vector3.at(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ());
        }
        RecordManager.getInstance().getRecorders().add(this);
        runnable = new BukkitRunnable() {
            int i = 0;
            public void run() {
                try {
                    for (Player player : players) {
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.translateAlternateColorCodes('&',
                                "&9Recorded &d" + i + " &9Ticks")));

                        RecordTick tick = new RecordTick();
                        currentTick.put(player, tick);

                        if (i == 0) {
                            getPlayerRecord(player).setStartLocation(Vector3.at(
                                    player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ()));

                            tick.setLocation(Vector3.at(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ()));
                            tick.setYaw(player.getLocation().getYaw());
                            tick.setPitch(player.getLocation().getPitch());
                            tick.setState(getPlayerState(player));
                            tick.setHealth(player.getHealth());
                            tick.setHunger(player.getFoodLevel());
                            tick.setHand(getPlayerEquipment(player, EquipmentSlot.HAND));
                            tick.setOffHand(getPlayerEquipment(player, EquipmentSlot.OFF_HAND));
                            tick.setHelmet(getPlayerEquipment(player, EquipmentSlot.HEAD));
                            tick.setChestplate(getPlayerEquipment(player, EquipmentSlot.CHEST));
                            tick.setLeggings(getPlayerEquipment(player, EquipmentSlot.LEGS));
                            tick.setBoots(getPlayerEquipment(player, EquipmentSlot.FEET));

                            lastNonNullTicks.put(player.getUniqueId(), tick.clone());
                        } else {
                            Vector3 location = Vector3.at(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ());
                            RecordTick lastTick = lastTicks.get(player);
                            RecordTick lastNonNullTick = lastNonNullTicks.get(player.getUniqueId());
                            if (!lastNonNullTick.getLocation().equals(location)) {
                                tick.setLocation(location);
                            }

                            if (lastNonNullTick.getYaw() != player.getLocation().getYaw())
                                tick.setYaw(player.getLocation().getYaw());

                            if (lastNonNullTick.getPitch() != player.getLocation().getPitch())
                                tick.setPitch(player.getLocation().getPitch());

                            NPCState state = getPlayerState(player);
                            if (!lastNonNullTick.getState().equals(state))
                                tick.setState(state);

                            /* Added in the Listener class
                            double health = player.getHealth();
                            if (lastTick.getHealth() != health)
                                tick.setHealth(health);

                            int food = player.getFoodLevel();
                            if (lastTick.getHunger() != food)
                                tick.setHunger(food);*/

                            ItemStack hand = getPlayerEquipment(player, EquipmentSlot.HAND);
                            if (!lastNonNullTick.getHand().equals(hand)) {
                                if (hand == null)
                                    tick.setHand(new ItemStack(Material.AIR));
                                else
                                    tick.setHand(hand);
                            }

                            ItemStack offHand = getPlayerEquipment(player, EquipmentSlot.OFF_HAND);
                            if (!lastNonNullTick.getOffHand().equals(offHand)) {
                                if (offHand == null)
                                    tick.setOffHand(new ItemStack(Material.AIR));
                                else
                                    tick.setOffHand(offHand);
                            }

                            ItemStack head = getPlayerEquipment(player, EquipmentSlot.HEAD);
                            if (!lastNonNullTick.getHelmet().equals(head)) {
                                if (head == null)
                                    tick.setHelmet(new ItemStack(Material.AIR));
                                else
                                    tick.setHelmet(head);
                            }

                            ItemStack chest = getPlayerEquipment(player, EquipmentSlot.CHEST);
                            if (!lastNonNullTick.getChestplate().equals(chest)) {
                                if (chest == null)
                                    tick.setChestplate(new ItemStack(Material.AIR));
                                else
                                    tick.setChestplate(chest);
                            }

                            ItemStack legs = getPlayerEquipment(player, EquipmentSlot.LEGS);
                            if (!lastNonNullTick.getLeggings().equals(legs)) {
                                if (legs == null)
                                    tick.setLeggings(new ItemStack(Material.AIR));
                                else
                                    tick.setLeggings(legs);
                            }

                            ItemStack feet = getPlayerEquipment(player, EquipmentSlot.FEET);
                            if (!lastNonNullTick.getBoots().equals(feet)) {
                                if (feet == null)
                                    tick.setBoots(new ItemStack(Material.AIR));
                                else
                                    tick.setBoots(feet);
                            }

                            if (tick.getLocation() != null)
                                lastNonNullTick.setLocation(tick.getLocation());
                            if (tick.getYaw() != -999)
                                lastNonNullTick.setYaw(tick.getYaw());
                            if (tick.getPitch() != -999)
                                lastNonNullTick.setPitch(tick.getPitch());
                            if (tick.getState() != null)
                                lastNonNullTick.setState(tick.getState());
                            if (tick.getHand() != null)
                                lastNonNullTick.setHand(tick.getHand());
                            if (tick.getOffHand() != null)
                                lastNonNullTick.setOffHand(tick.getOffHand());
                            if (tick.getHelmet() != null)
                                lastNonNullTick.setHelmet(tick.getHelmet());
                            if (tick.getChestplate() != null)
                                lastNonNullTick.setChestplate(tick.getChestplate());
                            if (tick.getLeggings() != null)
                                lastNonNullTick.setLeggings(tick.getLeggings());
                            if (tick.getBoots() != null)
                                lastNonNullTick.setBoots(tick.getBoots());
                        }

                        getPlayerRecord(player).getRecordTicks().add(tick);
                        lastTicks.put(player, tick);
                    }
                    i++;
                    currentTickIndex++;
                } catch (Exception e) {
                    RecordManager.getInstance().getRecorders().remove(thisRecorder);
                    cancel();
                    e.printStackTrace();
                    Bukkit.getConsoleSender().sendMessage("§4Something wrong happened with the recorder. Record process is now terminated" +
                            " to prevent furthur errors. Please contact the developer and provide the errors you see above.");
                }
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    public void stop() {
        RecordManager.getInstance().getRecorders().remove(this);
        runnable.cancel();
    }

    private NPCState getPlayerState(Player player) {
        NPCState state;

        if (player.isSneaking())
            state = NPCState.CROUCHING;
        else if (player.isSleeping())
            state = NPCState.SLEEPING;
        else if (ServerVersion.supports(13) && player.isSwimming())
            state = NPCState.SWIMMING;
        else if (player.isDead())
            state = NPCState.DYING;
        else if (player.isGliding())
            state = NPCState.SWIMMING;
        else
            state = NPCState.STANDING;

        return state;
    }

    private ItemStack getPlayerEquipment(Player player, EquipmentSlot slot) {
        if (player.getInventory().getItem(slot) == null)
            return new ItemStack(Material.AIR);
        else
            return player.getInventory().getItem(slot);
    }

    private Record getPlayerRecord(Player player) {
        for (Record record : records) {
            if (record.getPlayerUUID().equals(player.getUniqueId()))
                return record;
        }
        return null;
    }

    public int getCurrentTickIndex() {
        return currentTickIndex;
    }

    public boolean isRunning() {
        return !runnable.isCancelled();
    }

    public RecordTick getCurrentTick(Player player) {
        return currentTick.get(player);
    }

    public Set<Player> getPlayers() {
        return players;
    }

    public Set<Record> getRecords() {
        return records;
    }

}
