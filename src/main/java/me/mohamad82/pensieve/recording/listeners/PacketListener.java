package me.mohamad82.pensieve.recording.listeners;

import me.mohamad82.pensieve.nms.EntityMetadata;
import me.mohamad82.pensieve.nms.enums.BlockDirection;
import me.mohamad82.pensieve.recording.PendingBlockBreak;
import me.mohamad82.pensieve.recording.RecordManager;
import me.mohamad82.pensieve.recording.RecordTick;
import me.mohamad82.pensieve.recording.Recorder;
import me.Mohamad82.RUoM.Ruom;
import me.Mohamad82.RUoM.XSeries.ReflectionUtils;
import me.Mohamad82.RUoM.XSeries.XMaterial;
import me.Mohamad82.RUoM.events.packets.PacketContainer;
import me.Mohamad82.RUoM.events.packets.clientbound.AsyncClientBoundPacketEvent;
import me.Mohamad82.RUoM.events.packets.serverbound.AsyncServerBoundPacketEvent;
import me.Mohamad82.RUoM.utils.MilliCounter;
import me.Mohamad82.RUoM.utils.ServerVersion;
import me.Mohamad82.RUoM.vector.Vector3;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CrossbowMeta;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class PacketListener implements Listener {

    private static Class<?> PACKET_PLAY_IN_BLOCK_DIG, PACKET_PLAY_OUT_ENTITY_METADATA, BLOCK_POSITION, DATA_WATCHER_ITEM,
            DATA_WATCHER_OBJECT;

    private static Method PACKET_PLAY_IN_BLOCK_DIG_TYPE_METHOD, PACKET_PLAY_IN_BLOCK_DIG_GET_BLOCK_POSITION_METHOD, PACKET_PLAY_IN_BLOCK_DIG_GET_ENUM_DIRECTION,
            DATA_WATCHER_ITEM_A_METHOD, DATA_WATCHER_ITEM_B_METHOD, DATA_WATCHER_OBJECT_A_METHOD, BLOCK_POSITION_GET_X, BLOCK_POSITION_GET_Y, BLOCK_POSITION_GET_Z;

    private static Field PACKET_PLAY_OUT_ENTITY_METADATA_ID_FIELD, PACKET_PLAY_OUT_ENTITY_METADATA_ITEMS_FIELD;

    static {
        try {
            {
                PACKET_PLAY_IN_BLOCK_DIG = ReflectionUtils.getNMSClass("network.protocol.game", "PacketPlayInBlockDig");
                PACKET_PLAY_OUT_ENTITY_METADATA = ReflectionUtils.getNMSClass("network.protocol.game", "PacketPlayOutEntityMetadata");
                BLOCK_POSITION = ReflectionUtils.getNMSClass("core", "BlockPosition");
                DATA_WATCHER_ITEM = ReflectionUtils.getNMSClass("network.syncher", "DataWatcher$Item");
                DATA_WATCHER_OBJECT = ReflectionUtils.getNMSClass("network.syncher", "DataWatcherObject");
            }
            {
                PACKET_PLAY_IN_BLOCK_DIG_TYPE_METHOD = PACKET_PLAY_IN_BLOCK_DIG.getMethod(ServerVersion.supports(13) ? "d" : "c");
                PACKET_PLAY_IN_BLOCK_DIG_GET_BLOCK_POSITION_METHOD = PACKET_PLAY_IN_BLOCK_DIG.getMethod(ServerVersion.supports(13) ? "b" : "a");
                PACKET_PLAY_IN_BLOCK_DIG_GET_ENUM_DIRECTION = PACKET_PLAY_IN_BLOCK_DIG.getMethod(ServerVersion.supports(13) ? "c" : "b");
                DATA_WATCHER_ITEM_A_METHOD = DATA_WATCHER_ITEM.getMethod("a");
                DATA_WATCHER_ITEM_B_METHOD = DATA_WATCHER_ITEM.getMethod("b");
                DATA_WATCHER_OBJECT_A_METHOD = DATA_WATCHER_OBJECT.getMethod("a");
                BLOCK_POSITION_GET_X = BLOCK_POSITION.getMethod("getX");
                BLOCK_POSITION_GET_Y = BLOCK_POSITION.getMethod("getY");
                BLOCK_POSITION_GET_Z = BLOCK_POSITION.getMethod("getZ");
            }
            {
                PACKET_PLAY_OUT_ENTITY_METADATA_ID_FIELD = PACKET_PLAY_OUT_ENTITY_METADATA.getDeclaredField("a");
                PACKET_PLAY_OUT_ENTITY_METADATA_ITEMS_FIELD = PACKET_PLAY_OUT_ENTITY_METADATA.getDeclaredField("b");
                PACKET_PLAY_OUT_ENTITY_METADATA_ID_FIELD.setAccessible(true);
                PACKET_PLAY_OUT_ENTITY_METADATA_ITEMS_FIELD.setAccessible(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public final Map<Player, PendingBlockBreak> breakingPlayers = new HashMap<>();
    public final Map<Player, ItemStack> eatingPlayers = new HashMap<>();
    public final Map<UUID, MilliCounter> drawnBowCounters = new HashMap<>();

    private static PacketListener instance;
    public static PacketListener getInstance() {
        return instance;
    }

    private PacketListener() {
        instance = this;
        start();

        Ruom.registerListener(this);
    }

    public static PacketListener initialize() {
        return new PacketListener();
    }

    public void start() {
        Ruom.runSync(() -> {
            for (Player player : Ruom.getOnlinePlayers()) {
                if (breakingPlayers.containsKey(player) || eatingPlayers.containsKey(player)) {
                    RecordTick currentTick = RecordManager.getInstance().getCurrentRecordTick(player);
                    if (currentTick == null) return;

                    if (breakingPlayers.containsKey(player)) {
                        currentTick.setPendingBlockBreak(breakingPlayers.get(player));
                    }
                    if (eatingPlayers.containsKey(player)) {
                        ItemStack foodItem = eatingPlayers.get(player);
                        if (foodItem.getAmount() == 0) {
                            eatingPlayers.remove(player);
                        } else {
                            if (!(player.getInventory().getItem(EquipmentSlot.HAND).getType().isEdible()) &&
                                    (ServerVersion.supports(9) &&
                                            !player.getInventory().getItem(EquipmentSlot.OFF_HAND).getType().isEdible())) {
                                eatingPlayers.remove(player);
                            } else {
                                currentTick.setEatingItem(foodItem);
                            }
                        }
                    }
                }
            }
        }, 0, 1);
    }

    @EventHandler
    public void onAsyncServerBoundPacket(AsyncServerBoundPacketEvent event) {
        Player player = event.getPlayer();
        PacketContainer packetContainer = event.getPacket();
        Object packet = packetContainer.getPacket();

        try {
            if (packetContainer.getName().equals("PacketPlayInBlockDig")) {
                RecordTick currentTick = RecordManager.getInstance().getCurrentRecordTick(player);
                if (currentTick != null) {
                    Object digType = PACKET_PLAY_IN_BLOCK_DIG_TYPE_METHOD.invoke(packet);
                    switch (digType.toString()) {
                        case "START_DESTROY_BLOCK":
                            if (player.getGameMode().equals(GameMode.SURVIVAL)) {
                                final PendingBlockBreak pendingBlockBreak = new PendingBlockBreak();
                                currentTick.setPendingBlockBreak(pendingBlockBreak);
                                Object blockPosition = PACKET_PLAY_IN_BLOCK_DIG_GET_BLOCK_POSITION_METHOD.invoke(packet);
                                int x = (int) BLOCK_POSITION_GET_X.invoke(blockPosition);
                                int y = (int) BLOCK_POSITION_GET_Y.invoke(blockPosition);
                                int z = (int) BLOCK_POSITION_GET_Z.invoke(blockPosition);
                                pendingBlockBreak.setLocation(Vector3.at(x, y, z));
                                pendingBlockBreak.setMaterial(player.getWorld().getBlockAt(x, y, z).getType());
                                pendingBlockBreak.setBlockDirection(BlockDirection.valueOf(
                                        PACKET_PLAY_IN_BLOCK_DIG_GET_ENUM_DIRECTION.invoke(packet).toString().toUpperCase()));
                                breakingPlayers.put(player, pendingBlockBreak);
                            }
                            break;
                        case "ABORT_DESTROY_BLOCK":
                        case "STOP_DESTROY_BLOCK":
                            breakingPlayers.remove(player);
                            break;
                        case "RELEASE_USE_ITEM":
                            eatingPlayers.remove(player);
                            break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onAsyncClientBoundPacket(AsyncClientBoundPacketEvent event) throws Exception {
        Player player = event.getPlayer();
        PacketContainer packetContainer = event.getPacket();
        Object packet = packetContainer.getPacket();

        if (packetContainer.getName().equals("PacketPlayOutEntityMetadata")) {
            RecordTick currentTick = RecordManager.getInstance().getCurrentRecordTick(player);

            @SuppressWarnings("unchecked")
            List<Object> itemsObject = (List<Object>) PACKET_PLAY_OUT_ENTITY_METADATA_ITEMS_FIELD.get(packet);
            int i = 1;
            for (Object itemObject : itemsObject) {
                int metadataId = (int) DATA_WATCHER_OBJECT_A_METHOD.invoke(DATA_WATCHER_ITEM_A_METHOD.invoke(itemObject));
                Object value = DATA_WATCHER_ITEM_B_METHOD.invoke(itemObject);

                if (currentTick != null) {
                    if (metadataId == EntityMetadata.ItemUseKey.getMetadataId() && value instanceof Byte)  {
                        byte byteValue = (byte) value;
                        EquipmentSlot slot;
                        if (byteValue == EntityMetadata.ItemUseKey.RELEASE.getBitMask() || byteValue == EntityMetadata.ItemUseKey.HOLD.getBitMask())
                            slot = EquipmentSlot.HAND;
                        else
                            slot = EquipmentSlot.OFF_HAND;

                        if (player.getInventory().getItem(slot).getType().equals(XMaterial.BOW.parseMaterial()) ||
                                (ServerVersion.supports(14) && player.getInventory().getItem(slot).getType().equals(XMaterial.CROSSBOW.parseMaterial()))) {
                            if (byteValue == EntityMetadata.ItemUseKey.RELEASE.getBitMask() || byteValue == EntityMetadata.ItemUseKey.OFFHAND_RELEASE.getBitMask()) {
                                if (drawnBowCounters.containsKey(player.getUniqueId())) {
                                    MilliCounter drawBowCounter = drawnBowCounters.get(player.getUniqueId());
                                    drawBowCounter.stop();
                                    currentTick.drawBow((int) drawBowCounter.get());
                                    drawnBowCounters.remove(player.getUniqueId());
                                    if (slot.equals(EquipmentSlot.OFF_HAND)) {
                                        currentTick.drawBowWithOffHand();
                                    }
                                    if (ServerVersion.supports(14) && player.getInventory().getItem(slot).getType().equals(XMaterial.CROSSBOW.parseMaterial())) {
                                        currentTick.drawCrossbow();
                                        Recorder recorder = RecordManager.getInstance().getPlayerRecorder(player);

                                        if (drawBowCounter.get() < 100) {
                                            currentTick.setCrossbowChargeLevel(0);
                                        } else if (drawBowCounter.get() < 700) {
                                            currentTick.setCrossbowChargeLevel(1);
                                            recorder.getRecordTick(player, recorder.getCurrentTickIndex() - 10).setCrossbowChargeLevel(0);
                                        } else if (drawBowCounter.get() > 990) {
                                            currentTick.setCrossbowChargeLevel(2);
                                            recorder.getRecordTick(player, recorder.getCurrentTickIndex() - 10).setCrossbowChargeLevel(1);
                                            recorder.getRecordTick(player, recorder.getCurrentTickIndex() - 20).setCrossbowChargeLevel(0);

                                            RecordTick lastNonNullTick = RecordManager.getInstance().getPlayerRecorder(player).getLastNonNullTick(player.getUniqueId());
                                            ItemStack crossbowItem = lastNonNullTick.getItem(slot).clone();
                                            if (crossbowItem.getType().equals(XMaterial.CROSSBOW.parseMaterial())) {
                                                CrossbowMeta crossbowMeta = (CrossbowMeta) crossbowItem.getItemMeta();
                                                List<ItemStack> projectiles = new ArrayList<>();
                                                projectiles.add(XMaterial.ARROW.parseItem());
                                                crossbowMeta.setChargedProjectiles(projectiles);
                                                crossbowItem.setItemMeta(crossbowMeta);
                                                currentTick.setItem(slot, crossbowItem);
                                            }
                                        }
                                    }
                                }
                            } else {
                                if (!drawnBowCounters.containsKey(player.getUniqueId())) {
                                    currentTick.drawBow(0);
                                    MilliCounter drawBowCounter = new MilliCounter();
                                    drawBowCounter.start();
                                    drawnBowCounters.put(player.getUniqueId(), drawBowCounter);
                                    if (slot.equals(EquipmentSlot.OFF_HAND)) {
                                        currentTick.drawBowWithOffHand();
                                    }
                                }
                            }
                        }
                    }
                } else {
                    if (player.hasPermission("pensieve.debug"))
                        if (false) //TODO CONFIGURATION (debug feature)
                            try {
                                int entityId = (int) PACKET_PLAY_OUT_ENTITY_METADATA_ID_FIELD.get(packet);
                                Ruom.log("--------------  " + i + "  --------------");
                                Ruom.log("entityId: " + entityId);
                                Ruom.log(DATA_WATCHER_ITEM_A_METHOD.invoke(itemObject).toString());
                                Ruom.log(DATA_WATCHER_ITEM_B_METHOD.invoke(itemObject).toString());
                                Ruom.log("------------  DEBUG  ------------");
                                i++;
                            } catch (Exception e) {
                                e.printStackTrace();
                                Ruom.warn("This is a debugging error. Please report this to the plugin's author(s): " + Ruom.getPlugin().getDescription().getAuthors());
                            }
                }
            }
        }
    }

}
