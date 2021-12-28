package me.mohamad82.pensieve.recording;

import me.mohamad82.pensieve.recording.record.*;
import me.mohamad82.ruom.Ruom;
import me.mohamad82.ruom.npc.NPC;
import me.mohamad82.ruom.utils.NMSUtils;
import me.mohamad82.ruom.utils.ServerVersion;
import me.mohamad82.ruom.utils.StringUtils;
import me.mohamad82.ruom.vector.Vector3;
import me.mohamad82.ruom.vector.Vector3Utils;
import me.mohamad82.ruom.xseries.XMaterial;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class Recorder implements Listener {

    private final Recorder instance = this;

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

    public Recorder(Set<Player> player, Vector3 center) {
        this.players = player;
        this.center = Vector3Utils.simplifyToCenter(center);
    }

    public Recorder(Player player) {
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

                        PlayerRecordTick tick = new PlayerRecordTick();
                        playerCurrentTick.put(player, tick);

                        if (currentTickIndex == 0) {
                            getPlayerRecord(player).setStartLocation(Vector3.at(
                                    player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ()));

                            tick.setLocation(Vector3Utils.toVector3(player.getLocation()));
                            tick.setYaw(player.getLocation().getYaw());
                            tick.setPitch(player.getLocation().getPitch());
                            tick.setPose(getPlayerPose(player));
                            tick.setHealth(player.getHealth());
                            tick.setHunger(player.getFoodLevel());
                            tick.setHand(getPlayerEquipment(player, EquipmentSlot.HAND));
                            tick.setOffHand(getPlayerEquipment(player, EquipmentSlot.OFF_HAND));
                            tick.setHelmet(getPlayerEquipment(player, EquipmentSlot.HEAD));
                            tick.setChestplate(getPlayerEquipment(player, EquipmentSlot.CHEST));
                            tick.setLeggings(getPlayerEquipment(player, EquipmentSlot.LEGS));
                            tick.setBoots(getPlayerEquipment(player, EquipmentSlot.FEET));

                            lastNonNullTicks.put(player.getUniqueId(), tick.copy());
                        } else {
                            PlayerRecordTick lastNonNullTick = (PlayerRecordTick) lastNonNullTicks.get(player.getUniqueId());

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

                            NPC.Pose pose = getPlayerPose(player);
                            if (!lastNonNullTick.getPose().equals(pose)) {
                                tick.setPose(pose);
                                lastNonNullTick.setPose(tick.getPose());
                            }

                            setPlayerMetadataValues(player, tick);

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
                                if (lastNonNullTick.getEffectColor() != potionColor) {
                                    tick.setEffectColor(potionColor);
                                    lastNonNullTick.setEffectColor(potionColor);
                                }
                            } else {
                                if (lastNonNullTick.getEffectColor() != 0) {
                                    tick.setEffectColor(0);
                                    lastNonNullTick.setEffectColor(0);
                                }
                            }
                        }

                        getPlayerRecord(player).addRecordTick(tick);
                    }

                    Set<Entity> entitiesToRemove = new HashSet<>();
                    for (Entity entity : entities) {
                        EntityRecord record;
                        if (getEntityRecord(entity) == null) {
                            switch (entity.getType()) {
                                case DROPPED_ITEM: {
                                    record = new DroppedItemRecord(entity.getUniqueId(), center, currentTickIndex, ((Item) entity).getItemStack().clone());
                                    break;
                                }
                                case AREA_EFFECT_CLOUD: {
                                    AreaEffectCloud areaEffectCloud = (AreaEffectCloud) entity;
                                    record = new AreaEffectCloudRecord(entity.getUniqueId(), center, currentTickIndex, areaEffectCloud.getColor().asRGB());
                                    break;
                                }
                                case SPLASH_POTION:
                                case THROWN_EXP_BOTTLE:
                                case SNOWBALL:
                                case ENDER_PEARL:
                                case EGG: {
                                    ItemStack projectileItem;
                                    switch (entity.getType()) {
                                        case SPLASH_POTION: {
                                            projectileItem = ((ThrownPotion) entity).getItem();
                                            break;
                                        }
                                        case THROWN_EXP_BOTTLE: {
                                            projectileItem = XMaterial.EXPERIENCE_BOTTLE.parseItem();
                                            break;
                                        }
                                        case SNOWBALL: {
                                            projectileItem = XMaterial.SNOWBALL.parseItem();
                                            break;
                                        }
                                        case ENDER_PEARL: {
                                            projectileItem = XMaterial.ENDER_PEARL.parseItem();
                                            break;
                                        }
                                        case EGG: {
                                            projectileItem = XMaterial.EGG.parseItem();
                                            break;
                                        }
                                        default: {
                                            throw new IllegalStateException("Please report this to the plugin's developer. Entity type: " + entity.getType().toString());
                                        }
                                    }

                                    record = new ProjectileRecord(entity.getUniqueId(), center, currentTickIndex, projectileItem);
                                    break;
                                }
                                case SPECTRAL_ARROW:
                                case ARROW: {
                                    int color = -1;
                                    if (!(entity instanceof SpectralArrow)) {
                                        try {
                                            color = ((Arrow) entity).getColor().asRGB();
                                        } catch (IllegalArgumentException ignore) {
                                        }
                                    }
                                    record = new ArrowRecord(entity.getUniqueId(), center, currentTickIndex, color);
                                    break;
                                }
                                default: {
                                    Ruom.warn("Unsupported entity type was added to the recorder: " + entity.getType().toString().toLowerCase());
                                    record = null;
                                }
                            }
                            if (record != null) {
                                record.setStartLocation(Vector3Utils.toVector3(entity.getLocation()));
                                getEntityRecords().add(record);
                            }
                        } else {
                            record = getEntityRecord(entity);
                        }
                        RecordTick tick = record.createRecordTick();
                        entityCurrentTick.put(entity, tick);

                        if (tick instanceof AreaEffectCloudTick) {
                            ((AreaEffectCloudTick) tick).setRadius(((AreaEffectCloud) entity).getRadius());
                        } else {
                            if (!lastNonNullTicks.containsKey(entity.getUniqueId())) {
                                tick.setLocation(Vector3Utils.toVector3(entity.getLocation()));
                                tick.setYaw(entity.getLocation().getYaw());
                                tick.setPitch(entity.getLocation().getPitch());
                                tick.setVelocity(Vector3.at(entity.getVelocity().getX(), entity.getVelocity().getY(), entity.getVelocity().getZ()));

                                lastNonNullTicks.put(entity.getUniqueId(), tick.copy());
                            } else {
                                RecordTick lastNonNullTick = lastNonNullTicks.get(entity.getUniqueId());

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
                    RecordManager.getInstance().getRecorders().remove(instance);
                    cancel();
                    e.printStackTrace();
                    Ruom.error(StringUtils.colorize("&4Something wrong happened with the recorder. Record process is now terminated" +
                            " to prevent furthur errors. Please contact the developer and provide the errors you see above."));
                }
            }
        }.runTaskTimer(Ruom.getPlugin(), 0, 1);
    }

    public void stop() {
        RecordManager.getInstance().getRecorders().remove(this);
        bukkitTask.cancel();
    }

    private NPC.Pose getPlayerPose(Player player) {
        NPC.Pose pose;

        if (player.isSneaking())
            pose = NPC.Pose.CROUCHING;
        else if (player.isSleeping())
            pose = NPC.Pose.SLEEPING;
        else if (ServerVersion.supports(13) && player.isSwimming())
            pose = NPC.Pose.SWIMMING;
        else if (player.isDead())
            pose = NPC.Pose.DYING;
        else if (ServerVersion.supports(9) && player.isGliding())
            pose = NPC.Pose.SWIMMING;
        else
            pose = NPC.Pose.STANDING;

        return pose;
    }

    public void setPlayerMetadataValues(Player player, RecordTick tick) {
        PlayerRecordTick playerRecordTick = (PlayerRecordTick) tick;

        playerRecordTick.setBurning(player.getFireTicks() > 0);
        playerRecordTick.setCrouching(player.isSneaking());
        playerRecordTick.setSprinting(player.isSprinting());
        if (ServerVersion.supports(13))
            playerRecordTick.setSwimming(player.isSwimming());
        playerRecordTick.setInvisible(player.isInvisible());
        if (ServerVersion.supports(9))
            playerRecordTick.setGlowing(player.isGlowing());
        if (ServerVersion.supports(11))
            playerRecordTick.setGliding(player.isGliding());
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
            if (record.getUuid().equals(entity.getUniqueId())) {
                return record;
            }
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
