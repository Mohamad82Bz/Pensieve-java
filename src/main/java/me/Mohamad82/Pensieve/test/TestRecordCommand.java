package me.Mohamad82.Pensieve.test;

import me.Mohamad82.Pensieve.nms.EntityMetadata;
import me.Mohamad82.Pensieve.nms.NMSProvider;
import me.Mohamad82.Pensieve.nms.PacketProvider;
import me.Mohamad82.Pensieve.nms.npc.EntityNPC;
import me.Mohamad82.Pensieve.nms.npc.PlayerNPC;
import me.Mohamad82.Pensieve.nms.npc.enums.EntityNPCType;
import me.Mohamad82.Pensieve.record.EntityRecord;
import me.Mohamad82.Pensieve.record.RecordTick;
import me.Mohamad82.Pensieve.record.Recorder;
import me.Mohamad82.Pensieve.replay.Replay;
import me.Mohamad82.RUoM.Ruom;
import me.Mohamad82.RUoM.XSeries.ReflectionUtils;
import me.Mohamad82.RUoM.translators.skin.SkinBuilder;
import me.Mohamad82.RUoM.utils.StringUtils;
import me.Mohamad82.RUoM.vector.Vector3;
import me.Mohamad82.RUoM.vector.Vector3Utils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Optional;
import java.util.UUID;

public class TestRecordCommand implements CommandExecutor {

    Recorder recorder;
    PlayerNPC pNpc;
    EntityNPC eNpc;
    Object nmsItem = null;

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;

        switch (args[0].toLowerCase()) {
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
                eNpc.setMetadata(Integer.parseInt(args[1]), NMSProvider.getNmsItemStack(player.getInventory().getItemInMainHand()));
                break;
            }
            case "create": {
                if (nmsItem == null) {
                    nmsItem = NMSProvider.getNmsItemStack(player.getInventory().getItemInMainHand());
                }
                eNpc = new EntityNPC(UUID.randomUUID(), player.getLocation(), EntityNPCType.valueOf(args[1].toUpperCase()));
                eNpc.addViewer(player);
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
                pNpc.addViewer(player);
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
