package me.mohamad82.pensieve.nms.hologram;

import com.google.common.collect.ImmutableSet;
import me.Mohamad82.RUoM.Ruom;
import me.Mohamad82.RUoM.adventureapi.adventure.platform.bukkit.MinecraftComponentSerializer;
import me.Mohamad82.RUoM.adventureapi.adventure.text.Component;
import me.Mohamad82.RUoM.vector.Vector3;
import me.Mohamad82.RUoM.vector.Vector3Utils;
import me.mohamad82.pensieve.nms.EntityMetadata;
import me.mohamad82.pensieve.nms.znpcold.EntityNPCOld;
import me.mohamad82.pensieve.nms.npc.NPCType;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

@SuppressWarnings("UnstableApiUsage")
public class HologramOld {

    private List<HologramLineOld> lines = new ArrayList<>();
    private final Set<Player> viewers = new HashSet<>();

    private final Map<HologramLineOld, Integer> displayedLines = new HashMap<>();
    private Location location;
    private BukkitTask bukkitTask;

    private HologramOld(List<HologramLineOld> lines, Location location, Player... viewers) {
        this.location = location;
        this.viewers.addAll(Arrays.asList(viewers));
        reload(lines, location);
    }

    public static HologramOld hologram(List<HologramLineOld> lines, Location location, Player... viewers) {
        return new HologramOld(new ArrayList<>(lines), location, viewers);
    }

    private void reload(List<HologramLineOld> lines, Location location) {
        unload();
        List<HologramLineOld> newLines = new ArrayList<>(lines);
        this.lines.clear();
        this.displayedLines.clear();

        int lineIndex = 0;
        Location suitableLocation = location.clone();
        for (HologramLineOld line : newLines) {
            if (lineIndex > 0) {
                suitableLocation.add(0, -line.getDistance(), 0);
            }
            EntityNPCOld armorstand = new EntityNPCOld(UUID.randomUUID(), suitableLocation, NPCType.ARMOR_STAND);
            armorstand.addViewers(viewers);
            armorstand.addNPCPacket();
            armorstand.setMetadata(EntityMetadata.getEntityGravityId(), false);
            Ruom.runSync(() -> {
                armorstand.setMetadata(EntityMetadata.EntityStatus.getMetadataId(), EntityMetadata.EntityStatus.INVISIBLE.getBitMask());
                armorstand.setMetadata(EntityMetadata.getEntityCustomNameVisibilityId(), true);
                armorstand.setMetadata(EntityMetadata.getEntityCustomNameId(), Optional.of(MinecraftComponentSerializer.get().serialize(line.getComponent())));
                armorstand.setMetadata(EntityMetadata.ArmorStand.getMetadataId(), EntityMetadata.ArmorStand.getBitMasks(
                        EntityMetadata.ArmorStand.SMALL, EntityMetadata.ArmorStand.MARKER, EntityMetadata.ArmorStand.NO_BASE_PLATE));
            }, 1);
            line.setArmorstand(armorstand);

            this.lines.add(line);
            this.displayedLines.put(line, 0);
            lineIndex++;
        }

        bukkitTask = Ruom.runSync(new Runnable() {
            int tickIndex = 0;
            public void run() {
                for (HologramLineOld line : lines) {
                    if (line.getComponents() != null) {
                        int displayedLine = displayedLines.get(line);
                        if (line.getComponents().size() > displayedLine + 1) {
                            line.getArmorstand().setMetadata(EntityMetadata.getEntityCustomNameId(),
                                    Optional.of(MinecraftComponentSerializer.get().serialize(line.getComponent(displayedLine + 1))));
                            displayedLines.put(line, displayedLine + 1);
                        }
                    }
                }
                tickIndex++;
            }
        }, 1);
    }

    public void reload() {
        reload(lines, location);
    }

    public void unload() {
        lines.forEach(line -> {
            if (line.getArmorstand() != null)
                line.getArmorstand().removeNPCPacket();
        });
        if (bukkitTask != null)
            bukkitTask.cancel();
    }

    public void move(Vector3 vector) {
        location.add(vector.getX(), vector.getY(), vector.getZ());
        lines.forEach(line -> {
            if (!line.getArmorstand().move(vector)) {
                Location location = line.getArmorstand().getLocation().add(vector.getX(), vector.getY(), vector.getZ());
                line.getArmorstand().teleport(location.getX(), location.getY(), location.getZ(), 0, 0, false);
            }
        });
    }

    public void teleport(Location location) {
        this.location = location;

        reload();
    }

    public void teleport(Vector3 location) {
        this.location = Vector3Utils.toLocation(this.location.getWorld(), location);

        reload();
    }

    public void setLines(List<HologramLineOld> lines) {
        this.lines = lines;

        reload(lines, location);
    }

    public void setLine(int index, HologramLineOld line) {
        this.lines.set(index, line);

        reload();
    }

    public void editLine(int index, Component newComponent) {
        this.lines.get(index).getArmorstand().setMetadata(EntityMetadata.getEntityCustomNameId(),
                Optional.of(MinecraftComponentSerializer.get().serialize(newComponent)));
        this.lines.get(index).setComponent(newComponent);
    }

    public void addLine(HologramLineOld line) {
        lines.add(line);

        reload();
    }

    public boolean removeLine(int index) {
        if (index < 1 || index > lines.size()) return false;
        lines.get(index - 1).getArmorstand().removeNPCPacket();
        lines.remove(index - 1);

        reload();
        return true;
    }

    public void addViewer(Player... players) {
        for (Player player : players) {
            for (HologramLineOld line : lines) {
                EntityNPCOld armorstand = line.getArmorstand();
                armorstand.addViewer(player);
                armorstand.addNPCPacket(player);
                armorstand.setMetadata(EntityMetadata.getEntityGravityId(), false, player);
                Ruom.runSync(() -> {
                    armorstand.setMetadata(EntityMetadata.EntityStatus.getMetadataId(), EntityMetadata.EntityStatus.INVISIBLE.getBitMask(), player);
                    armorstand.setMetadata(EntityMetadata.getEntityCustomNameVisibilityId(), true, player);
                    armorstand.setMetadata(EntityMetadata.getEntityCustomNameId(), Optional.of(MinecraftComponentSerializer.get().serialize(line.getComponent())), player);
                    armorstand.setMetadata(EntityMetadata.ArmorStand.getMetadataId(), EntityMetadata.ArmorStand.getBitMasks(
                            EntityMetadata.ArmorStand.SMALL, EntityMetadata.ArmorStand.MARKER, EntityMetadata.ArmorStand.NO_BASE_PLATE), player);
                }, 1);
            }
            viewers.add(player);
        }
    }

    public void removeViewer(Player... players) {
        for (Player player : players) {
            lines.forEach(line -> {
                line.getArmorstand().removeViewer(player);
                line.getArmorstand().removeNPCPacket(player);
            });
            viewers.remove(player);
        }
    }

    public Set<Player> getViewers() {
        return ImmutableSet.copyOf(viewers);
    }

}
