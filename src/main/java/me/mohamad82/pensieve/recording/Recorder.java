package me.mohamad82.pensieve.recording;

import me.mohamad82.pensieve.nms.NMSUtils;
import me.mohamad82.pensieve.nms.EntityMetadata;
import me.mohamad82.pensieve.nms.npc.NPCType;
import me.mohamad82.pensieve.nms.npc.enums.NPCState;
import me.Mohamad82.RUoM.Ruom;
import me.Mohamad82.RUoM.XSeries.XMaterial;
import me.Mohamad82.RUoM.utils.ServerVersion;
import me.Mohamad82.RUoM.utils.StringUtils;
import me.Mohamad82.RUoM.vector.Vector3;
import me.Mohamad82.RUoM.vector.Vector3Utils;
import me.mohamad82.pensieve.recording.record.EntityRecord;
import me.mohamad82.pensieve.recording.record.PlayerRecord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
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
    private final Map<Player, RecordTick> playerCurrentTick = new HashMap<>();
    private final Map<Entity, RecordTick> entityCurrentTick = new HashMap<>();
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
                        playerCurrentTick.put(player, tick);

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

                            byte entityMetadata = getPlayerMetadata(player);
                            if (lastNonNullTick.getEntityMetadata() != entityMetadata) {
                                tick.setEntityMetadata(entityMetadata);
                                lastNonNullTick.setEntityMetadata(entityMetadata);
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

                            if (lastNonNullTick.getBodyArrows() != player.getArrowsInBody()) {
                                tick.setBodyArrows(player.getArrowsInBody());
                                lastNonNullTick.setBodyArrows(player.getArrowsInBody());
                            }

                            PotionEffect potionEffect = null;
                            for (PotionEffect effect : player.getActivePotionEffects()) {
                                if (effect.hasParticles())
                                    potionEffect = effect;
                            }
                            if (potionEffect != null) {
                                ItemStack potionItem = new ItemStack(XMaterial.POTION.parseMaterial());
                                PotionMeta potionMeta = (PotionMeta) potionItem.getItemMeta();
                                potionMeta.addCustomEffect(potionEffect, true);
                                potionItem.setItemMeta(potionMeta);
                                int potionColor = NMSUtils.getPotionColor(potionItem);
                                if (lastNonNullTick.getPotionColor() != potionColor) {
                                    tick.setPotionColor(potionColor);
                                    lastNonNullTick.setPotionColor(potionColor);
                                }
                            } else {
                                if (lastNonNullTick.getPotionColor() != 0) {
                                    tick.setPotionColor(0);
                                    lastNonNullTick.setPotionColor(0);
                                }
                            }
                        }

                        getPlayerRecord(player).addRecordTick(tick);
                    }

                    Set<Entity> entitiesToRemove = new HashSet<>();
                    for (Entity entity : entities) {
                        if (getEntityRecord(entity) == null) {
                            EntityRecord record = new EntityRecord(entity.getUniqueId(), center, NPCType.getByEntityType(entity.getType()), currentTickIndex);
                            record.setStartLocation(Vector3Utils.toVector3(entity.getLocation()));
                            if (entity instanceof ThrownPotion) {
                                record.setItem(((ThrownPotion) entity).getItem());
                            }
                            if (entity instanceof Item) {
                                record.setDroppedItem(((Item) entity).getItemStack().clone());
                            }
                            getEntityRecords().add(record);
                        }
                        RecordTick tick = new RecordTick();
                        entityCurrentTick.put(entity, tick);
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

    public byte getPlayerMetadata(Player player) {
        Set<EntityMetadata.EntityStatus> metadata = new HashSet<>();

        if (player.getFireTicks() > 0)
            metadata.add(EntityMetadata.EntityStatus.BURNING);
        if (player.isSneaking())
            metadata.add(EntityMetadata.EntityStatus.CROUCHING);
        if (player.isSprinting())
            metadata.add(EntityMetadata.EntityStatus.SPRINTING);
        if (ServerVersion.supports(13) && player.isSwimming())
            metadata.add(EntityMetadata.EntityStatus.SWIMMING);
        if (player.isInvisible())
            metadata.add(EntityMetadata.EntityStatus.INVISIBLE);
        if (ServerVersion.supports(9) && player.isGlowing())
            metadata.add(EntityMetadata.EntityStatus.GLOWING);
        if (ServerVersion.supports(11) && player.isGliding()) {
            metadata.add(EntityMetadata.EntityStatus.GLIDING);
        }

        return EntityMetadata.EntityStatus.getBitMasks(metadata);
    }

    private ItemStack getPlayerEquipment(Player player, EquipmentSlot slot) {
        if (player.getInventory().getItem(slot) == null)
            return new ItemStack(Material.AIR);
        else
            return player.getInventory().getItem(slot).clone();
    }

    public PlayerRecord getPlayerRecord(Player player) {
        for (PlayerRecord record : playerRecords) {
            if (record.getUuid().equals(player.getUniqueId()))
                return record;
        }
        return null;
    }

    public EntityRecord getEntityRecord(Entity entity) {
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
        return playerCurrentTick.get(player);
    }

    public RecordTick getCurrentTick(Entity entity) {
        return entityCurrentTick.get(entity);
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
