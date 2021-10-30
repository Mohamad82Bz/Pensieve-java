package me.Mohamad82.Pensieve.record;

import me.Mohamad82.Pensieve.nms.enums.EntityNPCType;
import me.Mohamad82.Pensieve.nms.enums.NPCState;
import me.Mohamad82.RUoM.Ruom;
import me.Mohamad82.RUoM.utils.ServerVersion;
import me.Mohamad82.RUoM.utils.StringUtils;
import me.Mohamad82.RUoM.vector.Vector3;
import me.Mohamad82.RUoM.vector.Vector3Utils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
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
    private final Vector3 center;

    private final Set<Player> players;
    private final Set<Entity> entities = new HashSet<>();
    private final Set<PlayerRecord> playerRecords = new HashSet<>();
    private final Set<EntityRecord> entityRecords = new HashSet<>();
    private final Map<Player, RecordTick> currentTick = new HashMap<>();
    private final Map<UUID, RecordTick> lastNonNullTicks = new HashMap<>();

    private BukkitTask bukkitTask;
    private int currentTickIndex = 0;

    public Recorder(JavaPlugin plugin, Set<Player> player, Vector3 center) {
        this.plugin = plugin;
        this.players = player;
        this.center = Vector3Utils.simplifyToCenter(center);
    }

    public Recorder(JavaPlugin plugin, Player player) {
        this.plugin = plugin;
        Set<Player> players = new HashSet<>();
        players.add(player);
        this.players = players;
        this.center = Vector3.at(0.5, 100, 0.5);
    }

    public void start() {
        for (Player player : players) {
            playerRecords.add(new PlayerRecord(player, center));
        }
        RecordManager.getInstance().getRecorders().add(this);
        bukkitTask = new BukkitRunnable() {
            public void run() {
                try {
                    for (Player player : players) {
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.translateAlternateColorCodes('&',
                                "&9Recorded &d" + currentTickIndex + " &9Ticks")));

                        RecordTick tick = new RecordTick();
                        currentTick.put(player, tick);

                        if (currentTickIndex == 0) {
                            getPlayerRecord(player).setStartLocation(Vector3.at(
                                    player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ()));

                            tick.setLocation(Vector3Utils.toVector3(player.getLocation()));
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
                            RecordTick lastNonNullTick = lastNonNullTicks.get(player.getUniqueId());

                            Vector3 location = Vector3Utils.toVector3(player.getLocation());
                            if (!lastNonNullTick.getLocation().equals(location)) {
                                tick.setLocation(location);
                                lastNonNullTick.setLocation(tick.getLocation());
                            }

                            float yaw = player.getLocation().getYaw();
                            if (lastNonNullTick.getYaw() != yaw) {
                                tick.setYaw(yaw);
                                lastNonNullTick.setYaw(tick.getYaw());
                            }

                            float pitch = player.getLocation().getPitch();
                            if (lastNonNullTick.getPitch() != pitch) {
                                tick.setPitch(pitch);
                                lastNonNullTick.setPitch(tick.getPitch());
                            }

                            NPCState state = getPlayerState(player);
                            if (!lastNonNullTick.getState().equals(state)) {
                                tick.setState(state);
                                lastNonNullTick.setState(tick.getState());
                            }

                            ItemStack hand = getPlayerEquipment(player, EquipmentSlot.HAND);
                            if (!lastNonNullTick.getHand().equals(hand) && tick.getHand() == null) {
                                if (hand == null)
                                    tick.setHand(new ItemStack(Material.AIR));
                                else
                                    tick.setHand(hand);
                                lastNonNullTick.setHand(tick.getHand());
                            }

                            ItemStack offHand = getPlayerEquipment(player, EquipmentSlot.OFF_HAND);
                            if (!lastNonNullTick.getOffHand().equals(offHand) && tick.getOffHand() == null) {
                                if (offHand == null)
                                    tick.setOffHand(new ItemStack(Material.AIR));
                                else
                                    tick.setOffHand(offHand);
                                lastNonNullTick.setOffHand(tick.getOffHand());
                            }

                            ItemStack head = getPlayerEquipment(player, EquipmentSlot.HEAD);
                            if (!lastNonNullTick.getHelmet().equals(head) && tick.getHelmet() == null) {
                                if (head == null)
                                    tick.setHelmet(new ItemStack(Material.AIR));
                                else
                                    tick.setHelmet(head);
                                lastNonNullTick.setHelmet(tick.getHelmet());
                            }

                            ItemStack chest = getPlayerEquipment(player, EquipmentSlot.CHEST);
                            if (!lastNonNullTick.getChestplate().equals(chest) && tick.getChestplate() == null) {
                                if (chest == null)
                                    tick.setChestplate(new ItemStack(Material.AIR));
                                else
                                    tick.setChestplate(chest);
                                lastNonNullTick.setChestplate(tick.getChestplate());
                            }

                            ItemStack legs = getPlayerEquipment(player, EquipmentSlot.LEGS);
                            if (!lastNonNullTick.getLeggings().equals(legs) && tick.getLeggings() == null) {
                                if (legs == null)
                                    tick.setLeggings(new ItemStack(Material.AIR));
                                else
                                    tick.setLeggings(legs);
                                lastNonNullTick.setLeggings(tick.getLeggings());
                            }

                            ItemStack feet = getPlayerEquipment(player, EquipmentSlot.FEET);
                            if (!lastNonNullTick.getBoots().equals(feet) && tick.getBoots() == null) {
                                if (feet == null)
                                    tick.setBoots(new ItemStack(Material.AIR));
                                else
                                    tick.setBoots(feet);
                                lastNonNullTick.setBoots(tick.getBoots());
                            }
                        }

                        getPlayerRecord(player).addRecordTick(tick);
                    }

                    Set<Entity> entitiesToRemove = new HashSet<>();
                    for (Entity entity : entities) {
                        if (getEntityRecord(entity) == null) {
                            EntityRecord record = new EntityRecord(entity.getUniqueId(), center, EntityNPCType.getByEntityType(entity.getType()), currentTickIndex);
                            record.setStartLocation(Vector3Utils.toVector3(entity.getLocation()));
                            if (entity instanceof ThrownPotion) {
                                record.setItem(((ThrownPotion) entity).getItem());
                            }
                            getEntityRecords().add(record);
                        }
                        RecordTick tick = new RecordTick();
                        RecordTick lastNonNullTick;

                        if (!lastNonNullTicks.containsKey(entity.getUniqueId())) {
                            tick.setLocation(Vector3Utils.toVector3(entity.getLocation()));
                            tick.setYaw(entity.getLocation().getYaw());
                            tick.setPitch(entity.getLocation().getPitch());
                            tick.setVelocity(Vector3.at(entity.getVelocity().getX(), entity.getVelocity().getY(), entity.getVelocity().getZ()));

                            lastNonNullTicks.put(entity.getUniqueId(), tick.clone());
                        } else {
                            lastNonNullTick = lastNonNullTicks.get(entity.getUniqueId());

                            Vector3 location = Vector3Utils.toVector3(entity.getLocation());
                            if (!lastNonNullTick.getLocation().equals(location)) {
                                tick.setLocation(location);
                                lastNonNullTick.setLocation(location);
                            } else {
                                Vector3 velocity = Vector3.at(entity.getVelocity().getX(), entity.getVelocity().getY(), entity.getVelocity().getZ());
                                if (lastNonNullTick.getVelocity() == null || !lastNonNullTick.getVelocity().equals(velocity)) {
                                    tick.setVelocity(velocity);
                                    lastNonNullTick.setVelocity(velocity);
                                }
                            }

                            if (lastNonNullTick.getYaw() != entity.getLocation().getYaw()) {
                                tick.setYaw(entity.getLocation().getYaw());
                                lastNonNullTick.setYaw(entity.getLocation().getYaw());
                            }

                            if (lastNonNullTick.getPitch() != entity.getLocation().getPitch()) {
                                tick.setPitch(entity.getLocation().getPitch());
                                lastNonNullTick.setPitch(entity.getLocation().getPitch());
                            }
                        }

                        if (entity.isDead()) {
                            entitiesToRemove.add(entity);
                        } else {
                            getEntityRecord(entity).addRecordTick(tick);
                        }
                    }
                    entitiesToRemove.forEach(entities::remove);

                    currentTickIndex++;
                } catch (Exception e) {
                    RecordManager.getInstance().getRecorders().remove(thisRecorder);
                    cancel();
                    e.printStackTrace();
                    Ruom.error(StringUtils.colorize("&4Something wrong happened with the recorder. Record process is now terminated" +
                            " to prevent furthur errors. Please contact the developer and provide the errors you see above."));
                }
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    public void stop() {
        RecordManager.getInstance().getRecorders().remove(this);
        bukkitTask.cancel();
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
        else if (ServerVersion.supports(9) && player.isGliding())
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

    private PlayerRecord getPlayerRecord(Player player) {
        for (PlayerRecord record : playerRecords) {
            if (record.getUuid().equals(player.getUniqueId()))
                return record;
        }
        return null;
    }

    private EntityRecord getEntityRecord(Entity entity) {
        for (EntityRecord record : entityRecords) {
            if (record.getUuid().equals(entity.getUniqueId()))
                return record;
        }
        return null;
    }

    public int getCurrentTickIndex() {
        return currentTickIndex;
    }

    public boolean isRunning() {
        return !bukkitTask.isCancelled();
    }

    public RecordTick getRecordTick(Player player, int tickIndex) {
        return getPlayerRecord(player).getRecordTicks().get(tickIndex);
    }

    public RecordTick getCurrentTick(Player player) {
        return currentTick.get(player);
    }

    public RecordTick getLastNonNullTick(UUID uuid) {
        return lastNonNullTicks.get(uuid);
    }

    public Set<Player> getPlayers() {
        return players;
    }

    public Set<Entity> getEntities() {
        return entities;
    }

    public Set<PlayerRecord> getPlayerRecords() {
        return playerRecords;
    }

    public Set<EntityRecord> getEntityRecords() {
        return entityRecords;
    }

}
