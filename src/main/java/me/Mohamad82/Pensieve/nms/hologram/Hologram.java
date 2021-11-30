package me.Mohamad82.Pensieve.nms.hologram;

import com.google.common.collect.ImmutableSet;
import me.Mohamad82.Pensieve.nms.EntityMetadata;
import me.Mohamad82.Pensieve.nms.npc.EntityNPC;
import me.Mohamad82.Pensieve.nms.npc.enums.EntityNPCType;
import me.Mohamad82.RUoM.Ruom;
import me.Mohamad82.RUoM.adventureapi.adventure.platform.bukkit.MinecraftComponentSerializer;
import me.Mohamad82.RUoM.vector.Vector3;
import me.Mohamad82.RUoM.vector.Vector3Utils;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

@SuppressWarnings("UnstableApiUsage")
public class Hologram {

    private List<HologramLine> lines = new ArrayList<>();
    private final Set<Player> viewers = new HashSet<>();

    private Location location;

    private Hologram(List<HologramLine> lines, Location location, Player... viewers) {
        this.location = location;
        this.viewers.addAll(Arrays.asList(viewers));
        reload(lines, location);
    }

    public static Hologram hologram(List<HologramLine> lines, Location location, Player... viewers) {
        return new Hologram(new ArrayList<>(lines), location, viewers);
    }

    private void reload(List<HologramLine> lines, Location location) {
        unload();
        List<HologramLine> newLines = new ArrayList<>(lines);
        this.lines.clear();

        int lineIndex = 0;
        Location suitableLocation = location.clone();
        for (HologramLine line : newLines) {
            if (lineIndex > 0) {
                suitableLocation.add(0, -line.getDistance(), 0);
            }
            EntityNPC armorstand = new EntityNPC(UUID.randomUUID(), suitableLocation, EntityNPCType.ARMOR_STAND);
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
            lineIndex++;
        }
    }

    public void reload() {
        reload(lines, location);
    }

    public void unload() {
        lines.forEach(line -> {
            if (line.getArmorstand() != null)
                line.getArmorstand().removeNPCPacket();
        });
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

    public void setLines(List<HologramLine> lines) {
        this.lines = lines;

        reload(lines, location);
    }

    public void editLine(int index, HologramLine line) {
        this.lines.set(index, line);

        reload();
    }

    public void addLine(HologramLine line) {
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
            for (HologramLine line : lines) {
                EntityNPC armorstand = line.getArmorstand();
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
