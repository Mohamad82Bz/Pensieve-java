package me.mohamad82.pensieve.test;

import me.Mohamad82.RUoM.Ruom;
import me.Mohamad82.RUoM.XSeries.ReflectionUtils;
import me.Mohamad82.RUoM.XSeries.XMaterial;
import me.Mohamad82.RUoM.adventureapi.ComponentUtils;
import me.Mohamad82.RUoM.adventureapi.adventure.platform.bukkit.MinecraftComponentSerializer;
import me.Mohamad82.RUoM.translators.skin.SkinBuilder;
import me.Mohamad82.RUoM.translators.skin.exceptions.NoSuchAccountNameException;
import me.Mohamad82.RUoM.utils.StringUtils;
import me.Mohamad82.RUoM.vector.Vector3;
import me.Mohamad82.RUoM.vector.Vector3Utils;
import me.mohamad82.pensieve.nms.EntityMetadata;
import me.mohamad82.pensieve.nms.NMSUtils;
import me.mohamad82.pensieve.nms.PacketUtils;
import me.mohamad82.pensieve.nms.accessors.ClientboundAddEntityPacketAccessor;
import me.mohamad82.pensieve.nms.accessors.Vec3Accessor;
import me.mohamad82.pensieve.nms.hologram.HologramOld;
import me.mohamad82.pensieve.nms.hologram.HologramLineOld;
import me.mohamad82.pensieve.nms.npc.*;
import me.mohamad82.pensieve.nms.npc.entity.*;
import me.mohamad82.pensieve.nms.znpcold.EntityNPCOld;
import me.mohamad82.pensieve.nms.znpcold.PlayerNPCOld;
import me.mohamad82.pensieve.recording.RecordTick;
import me.mohamad82.pensieve.recording.Recorder;
import me.mohamad82.pensieve.recording.record.EntityRecord;
import me.mohamad82.pensieve.replaying.Replay;
import me.mohamad82.pensieve.utils.Rotations;
import me.mohamad82.pensieve.utils.Utils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class TestRecordCommand implements CommandExecutor {

    Recorder recorder;
    PlayerNPCOld pNpc;
    EntityNPCOld eNpc;
    Object nmsItem = null;
    HologramOld hologram;
    EntityNPCOld arrow;

    ArmorStandNPC armorStandNPC;
    ArrowNPC arrowNPC;
    FallingBlockNPC fallingBlockNPC;
    ItemNPC itemNPC;
    ThrowableProjectileNPC throwableProjectileNPC;
    PlayerNPC playerNPC;
    TablistComponent tablist;

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;

        switch (args[0].toLowerCase()) {
            case "tablist": {
                tablist = TablistComponent.tablistComponent(ComponentUtils.parse(args[1]), args[2]);
                tablist.addViewers(Ruom.getOnlinePlayers());
                break;
            }
            case "tablistchange": {
                tablist.setComponent(ComponentUtils.parse(args[1]));
                break;
            }
            case "ignorerun": {
                Utils.ignoreExcRun(() -> {
                    Ruom.broadcast("Hello");
                });
                break;
            }
            case "newplayer": {
                if (args.length == 1) {
                    playerNPC = PlayerNPC.playerNPC("Test NPC", player.getLocation(), Optional.of(SkinBuilder.getInstance().getSkin(player)));
                    playerNPC.addViewers(Ruom.getOnlinePlayers());
                } else {
                    switch (args[1].toLowerCase()) {
                        case "name": {
                            playerNPC.setCustomNameVisible(true);
                            playerNPC.setCustomName(ComponentUtils.parse(args[2]));
                            break;
                        }
                        case "glow": {
                            playerNPC.setGlowing(Boolean.parseBoolean(args[2]));
                            break;
                        }
                    }
                }
                break;
            }
            case "newprojectile": {
                if (args.length == 1) {
                    throwableProjectileNPC = ThrowableProjectileNPC.throwableProjectileNPC(player.getLocation(), player.getInventory().getItemInMainHand());
                    throwableProjectileNPC.addViewers(Ruom.getOnlinePlayers());
                } else {
                    switch (args[1].toLowerCase()) {
                        case "glow": {
                            throwableProjectileNPC.setGlowing(Boolean.parseBoolean(args[2]));
                            break;
                        }
                        case "item": {
                            throwableProjectileNPC.setItem(player.getInventory().getItemInMainHand());
                            break;
                        }
                        case "gravity": {
                            throwableProjectileNPC.setNoGravity(Boolean.parseBoolean(args[2]));
                            break;
                        }
                        default: {
                            Ruom.broadcast("unknown argument");
                        }
                    }
                }
                break;
            }
            case "newitem2": {
                try {
                    int id = UUID.randomUUID().hashCode();

                    /*ReflectionUtils.sendPacket(player,
                            new PacketPlayOutSpawnEntity(id, UUID.randomUUID(), player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(),
                                    0f, 0f, EntityTypes.ITEM, 0, new Vec3D(0, 0, 0)));
                    Ruom.runSync(() -> {
                        Object packetPlayOutEntityMetadata = PacketUtils.getPacketPlayOutEntityMetadata(id, EntityMetadata.getDroppedItemMetadataId(), NMSUtils.getNmsItemStack(player.getInventory().getItemInMainHand()));
                        ReflectionUtils.sendPacket(Bukkit.getPlayerExact("Mohamad82"), packetPlayOutEntityMetadata);
                    }, 1);*/

                    NMSUtils.sendPacket(player,
                            ClientboundAddEntityPacketAccessor.getConstructor0().newInstance(id, UUID.randomUUID(),
                                    player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 0f, 0f, NPCType.ITEM.getNmsEntityType(),
                                    Integer.parseInt(args[1]), Vec3Accessor.getConstructor0().newInstance(0, 0, 0)),
                            PacketUtils.getEntityDataPacket(id, EntityMetadata.getDroppedItemMetadataId(), NMSUtils.getNmsItemStack(player.getInventory().getItemInMainHand())));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
            case "olditem": {
                EntityNPCOld item = new EntityNPCOld(UUID.randomUUID(), player.getLocation(), NPCType.ITEM);
                item.addViewers(Ruom.getOnlinePlayers());
                item.addNPCPacket();
                item.setMetadata(EntityMetadata.getDroppedItemMetadataId(), NMSUtils.getNmsItemStack(player.getInventory().getItemInMainHand()));
                break;
            }
            case "newitem": {
                if (args.length == 1) {
                    itemNPC = ItemNPC.itemNPC(player.getLocation(), player.getInventory().getItemInMainHand());
                    itemNPC.addViewers(Ruom.getOnlinePlayers());
                } else {
                    switch (args[1].toLowerCase()) {
                        case "item": {
                            itemNPC.setItem(player.getInventory().getItemInMainHand());
                            break;
                        }
                        case "glow": {
                            itemNPC.setGlowing(Boolean.parseBoolean(args[2]));
                            break;
                        }
                        case "teleport": {
                            itemNPC.teleport(Vector3Utils.toVector3(player.getLocation()), 0, 0);
                            break;
                        }
                        default: {
                            Ruom.broadcast("unknown argument");
                        }
                    }
                }
                break;
            }
            case "newarmorstand": {
                if (args.length == 1) {
                    armorStandNPC = ArmorStandNPC.armorStandNPC(player.getLocation());
                    armorStandNPC.addViewers(Ruom.getOnlinePlayers());
                } else {
                    switch (args[1].toLowerCase()) {
                        case "nobaseplate": {
                            armorStandNPC.setNoBasePlate(Boolean.parseBoolean(args[2]));
                            break;
                        }
                        case "small": {
                            armorStandNPC.setSmall(Boolean.parseBoolean(args[2]));
                            break;
                        }
                        case "marker": {
                            armorStandNPC.setMarker(Boolean.parseBoolean(args[2]));
                            break;
                        }
                        case "equipment": {
                            armorStandNPC.setEquipment(NPC.EquipmentSlot.valueOf(args[2]), player.getInventory().getItemInMainHand());
                            break;
                        }
                        case "invisible": {
                            armorStandNPC.setInvisible(Boolean.parseBoolean(args[2]));
                            break;
                        }
                        case "showarms": {
                            armorStandNPC.setShowArms(Boolean.parseBoolean(args[2]));
                            break;
                        }
                        case "rotation": {
                            armorStandNPC.setRightArmPose(Rotations.rotations(Float.parseFloat(args[2]), Float.parseFloat(args[3]), Float.parseFloat(args[4])));
                            break;
                        }
                        case "name": {
                            armorStandNPC.setCustomNameVisible(true);
                            armorStandNPC.setCustomName(ComponentUtils.parse(args[2]));
                            break;
                        }
                        case "gravity": {
                            armorStandNPC.setNoGravity(true);
                            break;
                        }
                        case "glowing": {
                            armorStandNPC.setGlowing(true);
                            break;
                        }
                        default: {
                            Ruom.broadcast("unknown argument");
                        }
                    }
                }
                break;
            }
            case "fallingblock": {
                if (args.length == 1) {
                    fallingBlockNPC = FallingBlockNPC.fallingBlockNPC(player.getLocation(), player.getInventory().getItemInMainHand().getType());
                    fallingBlockNPC.addViewers(Ruom.getOnlinePlayers());
                } else {
                    switch (args[1].toLowerCase()) {
                        case "glow": {
                            fallingBlockNPC.setGlowing(Boolean.parseBoolean(args[2]));
                            break;
                        }
                        case "move": {
                            fallingBlockNPC.move(Vector3.at(Double.parseDouble(args[2]), Double.parseDouble(args[3]), Double.parseDouble(args[4])));
                            break;
                        }
                        case "gravity": {
                            fallingBlockNPC.setNoGravity(Boolean.parseBoolean(args[2]));
                            break;
                        }
                        default: {
                            Ruom.broadcast("unknown argument");
                        }
                    }
                }
                break;
            }
            case "newarrow": {
                if (args.length == 1) {
                    arrowNPC = ArrowNPC.arrowNPC(player.getLocation());
                    arrowNPC.addViewers(Ruom.getOnlinePlayers());
                } else {
                    switch (args[1].toLowerCase()) {
                        case "invisible": {
                            arrowNPC.setInvisible(Boolean.parseBoolean(args[2]));
                            break;
                        }
                        case "name": {
                            arrowNPC.setCustomNameVisible(true);
                            arrowNPC.setCustomName(ComponentUtils.parse(args[2]));
                            break;
                        }
                        case "color": {
                            arrowNPC.setEffectsFromItem(player.getInventory().getItemInMainHand());
                            Ruom.broadcast("Color: " + arrowNPC.getColor());
                            break;
                        }
                        case "gravity": {
                            arrowNPC.setNoGravity(true);
                            break;
                        }
                        case "glowing": {
                            arrowNPC.setGlowing(true);
                            break;
                        }
                        default: {
                            Ruom.broadcast("unknown argument");
                        }
                    }
                }
                break;
            }
            case "arrownog": {
                arrow = new EntityNPCOld(UUID.randomUUID(), player.getLocation(), NPCType.ARROW);
                arrow.addViewers(Ruom.getOnlinePlayers());
                arrow.addNPCPacket();
                arrow.setMetadata(EntityMetadata.getEntityGravityId(), true);
                break;
            }
            case "movearrow": {
                arrow.moveAndLook(Vector3.at(Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3])), player.getLocation().getYaw(), player.getLocation().getPitch(), false);
                break;
            }
            case "mounttroll": {
                Entity previousEntity = null;
                for (Entity entity : player.getNearbyEntities(30, 30, 30)) {
                    if (previousEntity != null)
                        NMSUtils.setPassengers(Ruom.getOnlinePlayers(), entity.getEntityId(), previousEntity.getEntityId());
                    previousEntity = entity;
                }
                break;
            }
            case "mount": {
                PlayerNPCOld entity = new PlayerNPCOld("Koobs", player.getLocation(), Optional.empty());
                EntityNPCOld entity1 = new EntityNPCOld(UUID.randomUUID(), player.getLocation(), NPCType.ARMOR_STAND);
                EntityNPCOld entity2 = new EntityNPCOld(UUID.randomUUID(), player.getLocation(), NPCType.ARMOR_STAND);
                EntityNPCOld entity3 = new EntityNPCOld(UUID.randomUUID(), player.getLocation(), NPCType.ARMOR_STAND);
                entity.addViewers(Ruom.getOnlinePlayers());
                entity1.addViewers(Ruom.getOnlinePlayers());
                entity2.addViewers(Ruom.getOnlinePlayers());
                entity3.addViewers(Ruom.getOnlinePlayers());
                entity.addNPCPacket();
                entity1.addNPCPacket();
                entity2.addNPCPacket();
                entity3.addNPCPacket();
                entity1.setMetadata(EntityMetadata.EntityStatus.getMetadataId(), EntityMetadata.EntityStatus.INVISIBLE.getBitMask());
                entity1.setMetadata(EntityMetadata.getEntityCustomNameVisibilityId(), true);
                entity1.setMetadata(EntityMetadata.getEntityCustomNameId(), Optional.of(MinecraftComponentSerializer.get().serialize(ComponentUtils.parse("Entity One"))));
                entity1.setMetadata(EntityMetadata.ArmorStand.getMetadataId(), EntityMetadata.ArmorStand.getBitMasks(
                        EntityMetadata.ArmorStand.SMALL, EntityMetadata.ArmorStand.NO_BASE_PLATE));
                entity2.setMetadata(EntityMetadata.EntityStatus.getMetadataId(), EntityMetadata.EntityStatus.INVISIBLE.getBitMask());
                entity2.setMetadata(EntityMetadata.getEntityCustomNameVisibilityId(), true);
                entity2.setMetadata(EntityMetadata.getEntityCustomNameId(), Optional.of(MinecraftComponentSerializer.get().serialize(ComponentUtils.parse("Entity Two"))));
                entity2.setMetadata(EntityMetadata.ArmorStand.getMetadataId(), EntityMetadata.ArmorStand.getBitMasks(
                        EntityMetadata.ArmorStand.SMALL, EntityMetadata.ArmorStand.NO_BASE_PLATE));
                entity3.setMetadata(EntityMetadata.EntityStatus.getMetadataId(), EntityMetadata.EntityStatus.INVISIBLE.getBitMask());
                entity3.setMetadata(EntityMetadata.getEntityCustomNameVisibilityId(), true);
                entity3.setMetadata(EntityMetadata.getEntityCustomNameId(), Optional.of(MinecraftComponentSerializer.get().serialize(ComponentUtils.parse("Entity Three"))));
                entity3.setMetadata(EntityMetadata.ArmorStand.getMetadataId(), EntityMetadata.ArmorStand.getBitMasks(
                        EntityMetadata.ArmorStand.SMALL, EntityMetadata.ArmorStand.NO_BASE_PLATE));

                NMSUtils.setPassengers(Ruom.getOnlinePlayers(), entity.getId(), entity1.getId());
                NMSUtils.setPassengers(Ruom.getOnlinePlayers(), entity1.getId(), entity2.getId());
                NMSUtils.setPassengers(Ruom.getOnlinePlayers(), entity2.getId(), entity3.getId());
                break;
            }
            case "holo": {
                List<HologramLineOld> lines = new ArrayList<>();
                lines.add(HologramLineOld.hologramLine(ComponentUtils.parse("<blue> Test Hologram"), 0));
                lines.add(HologramLineOld.hologramLine(ComponentUtils.parse("<gradient:blue:red> Test Gradient Colors"), 0.3f));
                lines.add(HologramLineOld.hologramLine(ComponentUtils.parse("<rainbow>|||||||||||||||||||||||||||"), 0.5f));

                hologram = HologramOld.hologram(lines, player.getLocation(), Ruom.getOnlinePlayers().toArray(new Player[0]));
                break;
            }
            case "addline": {
                StringBuilder stringBuilder = new StringBuilder();
                int index = 0;
                for (String arg : args) {
                    if (index >= 1) {
                        stringBuilder.append(arg).append(" ");
                    }
                    index++;
                }
                String lineString = stringBuilder.substring(0, stringBuilder.length() - 1);

                hologram.addLine(HologramLineOld.hologramLine(ComponentUtils.parse(lineString), 0.3f));
                break;
            }
            case "removeline": {
                hologram.removeLine(Integer.parseInt(args[1]));
                break;
            }
            case "holoteleport": {
                hologram.teleport(player.getLocation());
                break;
            }
            case "holomove": {
                hologram.move(Vector3.at(Double.parseDouble(args[1]), Double.parseDouble(args[2]), Double.parseDouble(args[3])));
                break;
            }
            case "shield": {
                try {
                    pNpc = new PlayerNPCOld("Koobs", player.getLocation(), Optional.of(SkinBuilder.getInstance().getSkin(player.getName(), true)));
                    pNpc.addViewers(Ruom.getOnlinePlayers());
                    pNpc.addNPCPacket();
                    pNpc.setEquipment(EquipmentSlot.OFF_HAND, XMaterial.SHIELD.parseItem());
                } catch (NoSuchAccountNameException e) {
                    e.printStackTrace();
                }
                break;
            }
            case "shold": {
                pNpc.setMetadata(EntityMetadata.ItemUseKey.getMetadataId(), EntityMetadata.ItemUseKey.OFFHAND_HOLD.getBitMask());
                break;
            }
            case "srelease": {
                pNpc.setMetadata(EntityMetadata.ItemUseKey.getMetadataId(), EntityMetadata.ItemUseKey.OFFHAND_RELEASE.getBitMask());
                break;
            }
            case "look": {
                Ruom.broadcast("yaw: " + player.getLocation().getYaw() + "   pitch: " + player.getLocation().getPitch());
                break;
            }
            case "snow": {
                player.getWorld().spawnParticle(Particle.SNOWBALL, new Location(player.getWorld(), 41, 171, 17), Integer.parseInt(args[1]));
                break;
            }
            case "tab": {
                StringBuilder stringBuilder = new StringBuilder();
                for (int i = 1; i < args.length; i++) {
                    stringBuilder.append(args[i]).append(" ");
                }
                String arguments = stringBuilder.substring(0, stringBuilder.toString().length() - 1);
                pNpc.setTabList(StringUtils.colorize(arguments));
                break;
            }
            case "itype": {
                eNpc.setMetadata(EntityMetadata.getDroppedItemMetadataId(), NMSUtils.getNmsItemStack(player.getInventory().getItemInMainHand()));
                break;
            }
            case "create": {
                if (nmsItem == null) {
                    nmsItem = NMSUtils.getNmsItemStack(player.getInventory().getItemInMainHand());
                }
                eNpc = new EntityNPCOld(UUID.randomUUID(), player.getLocation(), NPCType.valueOf(args[1].toUpperCase()));
                eNpc.addViewers(Ruom.getOnlinePlayers());
                eNpc.addNPCPacket();
                eNpc.setMetadata(EntityMetadata.getDroppedItemMetadataId(), nmsItem);
                break;
            }
            case "pcreate": {
                pNpc = new PlayerNPCOld("TestName", player.getLocation(), Optional.of(SkinBuilder.getInstance().getSkin(player)));
                pNpc.addViewer(player);
                pNpc.addNPCPacket();
                pNpc.setEquipment(EquipmentSlot.HAND, new ItemStack(Material.BOW));
                pNpc.removeNPCTabList();
                break;
            }
            case "tcreate": {
                pNpc = new PlayerNPCOld("TestName", player.getLocation(), Optional.of(SkinBuilder.getInstance().getSkin(player)));
                pNpc.addViewers(Ruom.getOnlinePlayers());
                pNpc.addNPCTabList();
                break;
            }
            case "data": {
                ReflectionUtils.sendPacket(player,
                        PacketUtils.getPacketPlayOutEntityMetadata(pNpc.getId(), Integer.parseInt(args[1]), Byte.parseByte(args[2].toUpperCase())));
                break;
            }
            case "start": {
                recorder = new Recorder(Ruom.getPlugin(), Ruom.getOnlinePlayers(), Vector3.at(0, 0, 0));
                recorder.start();
                break;
            }
            case "stop": {
                recorder.stop();
                break;
            }
            case "play": {
                Replay replay = new Replay(recorder.getPlayerRecords(), recorder.getEntityRecords(), player.getWorld(), Vector3.at(0, 0, 0));
                replay.start();
                break;
            }
            case "debugarrow": {
                for (EntityRecord record : recorder.getEntityRecords()) {
                    /*EntityNPC eNpc = new EntityNPC(record.getUuid(), Vector3Utils.toLocation(player.getWorld(), record.getStartLocation()), record.getEntityType());
                    eNpc.addViewer(player);
                    eNpc.addNPCPacket();*/
                    final Vector3[] lastLoc = {record.getStartLocation()};

                    final int[] i = {0};
                    new BukkitRunnable() {
                        public void run() {
                            if (i[0] >= record.getRecordTicks().size()) {
                                cancel();
                                return;
                            }
                            RecordTick tick = record.getRecordTicks().get(i[0]);
                            if (tick.getLocation() != null) {
                                Location loc = Vector3Utils.toLocation(player.getWorld(), tick.getLocation());
                                loc.setYaw(tick.getYaw());
                                loc.setPitch(tick.getPitch());
                                player.teleport(loc);
                            }
                            i[0]++;

                            /*if (i[0] >= record.getRecordTicks().size()) {
                                cancel();
                                return;
                            }
                            RecordTick tick = record.getRecordTicks().get(i[0]);
                            if (tick.getLocation() != null) {
                                Vector3 travelDistance = Vector3Utils.getTravelDistance(lastLoc[0], tick.getLocation());
                                lastLoc[0] = tick.getLocation().clone();
                                if (travelDistance.equals(Vector3.at(0, 0, 0))) {
                                    if (tick.getVelocity() != null) {
                                        eNpc.velocity(tick.getVelocity());
                                    }
                                } else {
                                    eNpc.moveAndLook(travelDistance, tick.getYaw(), tick.getPitch(), false);
                                }
                            }
                            i[0]++;*/
                        }
                    }.runTaskTimer(Ruom.getPlugin(), 0, 10);
                }
                break;
            }
        }
        return true;
    }

}
