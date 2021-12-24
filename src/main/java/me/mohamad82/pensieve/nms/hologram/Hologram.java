package me.mohamad82.pensieve.nms.hologram;

import me.Mohamad82.RUoM.Ruom;
import me.Mohamad82.RUoM.adventureapi.adventure.text.Component;
import me.Mohamad82.RUoM.vector.Vector3;
import me.Mohamad82.RUoM.vector.Vector3Utils;
import me.mohamad82.pensieve.nms.Viewered;
import me.mohamad82.pensieve.nms.npc.entity.ArmorStandNPC;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Hologram extends Viewered {

    private List<HologramLine> lines = new ArrayList<>();

    private final Map<HologramLine, Integer> displayedLines = new HashMap<>();
    private Location location;
    private BukkitTask refreshTask;

    private Hologram(List<HologramLine> lines, Location location) {
        this.location = location;
        reload(lines, location);
    }

    public static Hologram hologram(List<HologramLine> lines, Location location) {
        return new Hologram(lines, location);
    }

    private void reload(List<HologramLine> lines, Location location) {
        unload();
        List<HologramLine> newLines = new ArrayList<>(lines);
        this.lines.clear();
        this.refreshTask.cancel();
        this.displayedLines.clear();

        int lineIndex = 0;
        Location suitableLocation = location.clone();
        for (HologramLine line : newLines) {
            if (lineIndex > 0) {
                suitableLocation.add(0, -line.getDistance(), 0);
            }
            ArmorStandNPC armorstand = ArmorStandNPC.armorStandNPC(suitableLocation);

            armorstand.setCustomNameVisible(true);
            armorstand.setCustomName(line.getComponent());
            armorstand.setNoGravity(true);
            armorstand.setInvisible(true);
            armorstand.setSmall(true);
            armorstand.setMarker(true);
            armorstand.setNoBasePlate(true);

            armorstand.addViewers(getViewers());

            line.location = suitableLocation;
            line.setArmorstand(armorstand);

            this.lines.add(line);
            this.displayedLines.put(line, 0);
            lineIndex++;
        }

        refreshTask = Ruom.runAsync(new Runnable() {
            int tickIndex = 0;
            public void run() {
                for (HologramLine line : lines) {
                    if (line.getComponents() == null) continue;
                    if (tickIndex % line.getRefresh() != 0) continue;

                    int displayedLine = displayedLines.get(line);
                    int shouldDisplay = displayedLine + 1;
                    if (!(line.getComponents().size() > shouldDisplay)) {
                        shouldDisplay = 0;
                    }
                    line.getArmorstand().setCustomName(line.getComponent(shouldDisplay));
                    displayedLines.put(line, shouldDisplay);
                }

                tickIndex++;
            }
        }, 0, 1);
    }

    public void reload() {
        reload(lines, location);
    }

    public void unload() {
        for (HologramLine line : lines) {
            if (line.getArmorstand() != null) {
                line.getArmorstand().removeViewers(line.getArmorstand().getViewers());
            }
        }
    }

    public void move(Vector3 vector3) {
        location.add(vector3.getX(), vector3.getY(), vector3.getZ());
        for (HologramLine line : lines) {
            if (!line.getArmorstand().move(vector3)) {
                line.location.add(vector3.getX(), vector3.getY(), vector3.getZ());
                line.getArmorstand().teleport(Vector3Utils.toVector3(line.location), 0, 0);
            }
        }
    }

    public void teleport(Location location) {
        this.location = location;

        reload();
    }

    public void setLines(List<HologramLine> lines) {
        this.lines = lines;

        reload();
    }

    public void setLine(int index, HologramLine line) {
        lines.set(index, line);

        reload();
    }

    public void editLine(int index, Component component) {
        lines.get(index).getArmorstand().setCustomName(component);
        lines.get(index).setComponent(component);
    }

    public void addLine(HologramLine line) {
        lines.add(line);

        reload();
    }

    public boolean removeLine(int nonZeroBasedIndex) {
        if (nonZeroBasedIndex < 1 || nonZeroBasedIndex > lines.size()) return false;
        lines.get(nonZeroBasedIndex - 1).getArmorstand().removeViewers(lines.get(nonZeroBasedIndex - 1).getArmorstand().getViewers());
        lines.remove(nonZeroBasedIndex - 1);

        reload();
        return true;
    }

    @Override
    protected void addViewer(Player player) {
        for (HologramLine line : lines) {
            if (line.getArmorstand() != null)
                line.getArmorstand().addViewers(player);
        }
    }

    @Override
    protected void removeViewer(Player player) {
        for (HologramLine line : lines) {
            if (line.getArmorstand() != null)
                line.getArmorstand().removeViewers(player);
        }
    }

}
