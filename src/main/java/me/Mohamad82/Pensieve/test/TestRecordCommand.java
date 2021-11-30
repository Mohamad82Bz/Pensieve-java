package me.Mohamad82.Pensieve.test;

import me.Mohamad82.Pensieve.nms.EntityMetadata;
import me.Mohamad82.Pensieve.nms.NMSProvider;
import me.Mohamad82.Pensieve.nms.PacketProvider;
import me.Mohamad82.Pensieve.nms.hologram.Hologram;
import me.Mohamad82.Pensieve.nms.hologram.HologramLine;
import me.Mohamad82.Pensieve.nms.npc.EntityNPC;
import me.Mohamad82.Pensieve.nms.npc.PlayerNPC;
import me.Mohamad82.Pensieve.nms.npc.enums.EntityNPCType;
import me.Mohamad82.Pensieve.record.EntityRecord;
import me.Mohamad82.Pensieve.record.RecordTick;
import me.Mohamad82.Pensieve.record.Recorder;
import me.Mohamad82.Pensieve.replay.Replay;
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
    PlayerNPC pNpc;
    EntityNPC eNpc;
    Object nmsItem = null;
    Hologram hologram;

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;

        switch (args[0].toLowerCase()) {
            case "mounttroll": {
                Entity previousEntity = null;
                for (Entity entity : player.getNearbyEntities(30, 30, 30)) {
                    if (previousEntity != null)
                        NMSProvider.setPassengers(Ruom.getOnlinePlayers(), entity.getEntityId(), previousEntity.getEntityId());
                    previousEntity = entity;
                }
                break;
            }
            case "mount": {
                PlayerNPC entity = new PlayerNPC("Koobs", player.getLocation(), Optional.empty());
                EntityNPC entity1 = new EntityNPC(UUID.randomUUID(), player.getLocation(), EntityNPCType.ARMOR_STAND);
                EntityNPC entity2 = new EntityNPC(UUID.randomUUID(), player.getLocation(), EntityNPCType.ARMOR_STAND);
                EntityNPC entity3 = new EntityNPC(UUID.randomUUID(), player.getLocation(), EntityNPCType.ARMOR_STAND);
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

                NMSProvider.setPassengers(Ruom.getOnlinePlayers(), entity.getId(), entity1.getId());
                NMSProvider.setPassengers(Ruom.getOnlinePlayers(), entity1.getId(), entity2.getId());
                NMSProvider.setPassengers(Ruom.getOnlinePlayers(), entity2.getId(), entity3.getId());
                break;
            }
            case "holo": {
                List<HologramLine> lines = new ArrayList<>();
                lines.add(HologramLine.hologramLine(ComponentUtils.parse("<blue> Test Hologram"), 0));
                lines.add(HologramLine.hologramLine(ComponentUtils.parse("<gradient:blue:red> Test Gradient Colors"), 0.3f));
                lines.add(HologramLine.hologramLine(ComponentUtils.parse("<rainbow>|||||||||||||||||||||||||||"), 0.5f));

                hologram = Hologram.hologram(lines, player.getLocation(), Ruom.getOnlinePlayers().toArray(new Player[0]));
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

                hologram.addLine(HologramLine.hologramLine(ComponentUtils.parse(lineString), 0.3f));
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
                    pNpc = new PlayerNPC("Koobs", player.getLocation(), Optional.of(SkinBuilder.getInstance().getSkin(player.getName(), true)));
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
                eNpc.setMetadata(EntityMetadata.getDroppedItemMetadataId(), NMSProvider.getNmsItemStack(player.getInventory().getItemInMainHand()));
                break;
            }
            case "create": {
                if (nmsItem == null) {
                    nmsItem = NMSProvider.getNmsItemStack(player.getInventory().getItemInMainHand());
                }
                eNpc = new EntityNPC(UUID.randomUUID(), player.getLocation(), EntityNPCType.valueOf(args[1].toUpperCase()));
                eNpc.addViewers(Ruom.getOnlinePlayers());
                eNpc.addNPCPacket();
                eNpc.setMetadata(EntityMetadata.getDroppedItemMetadataId(), nmsItem);
                break;
            }
            case "pcreate": {
                pNpc = new PlayerNPC("TestName", player.getLocation(), Optional.of(SkinBuilder.getInstance().getSkin(player)));
                pNpc.addViewer(player);
                pNpc.addNPCPacket();
                pNpc.setEquipment(EquipmentSlot.HAND, new ItemStack(Material.BOW));
                pNpc.removeNPCTabList();
                break;
            }
            case "tcreate": {
                pNpc = new PlayerNPC("TestName", player.getLocation(), Optional.of(SkinBuilder.getInstance().getSkin(player)));
                pNpc.addViewers(Ruom.getOnlinePlayers());
                pNpc.addNPCTabList();
                break;
            }
            case "data": {
                ReflectionUtils.sendPacket(player,
                        PacketProvider.getPacketPlayOutEntityMetadata(pNpc.getId(), Integer.parseInt(args[1]), Byte.parseByte(args[2].toUpperCase())));
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
                Replay replay = new Replay(Ruom.getPlugin(), recorder.getPlayerRecords(), recorder.getEntityRecords(), player.getWorld(), Vector3.at(0, 0, 0));
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
