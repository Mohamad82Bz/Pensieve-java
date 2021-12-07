package me.mohamad82.pensieve.nms.hologram;

import me.mohamad82.pensieve.nms.npc.EntityNPC;
import me.Mohamad82.RUoM.adventureapi.ComponentUtils;
import me.Mohamad82.RUoM.adventureapi.adventure.text.Component;

import java.util.List;

public class HologramLine {

    private Component component;
    private List<String> components;
    private EntityNPC armorstand;
    private final float distance;
    private int refresh;

    private HologramLine(Component component, float distance) {
        this.component = component;
        this.distance = distance;
    }

    public HologramLine(List<String> components, float distance, int refresh) {
        this.components = components;
        this.distance = distance;
        this.refresh = refresh;
    }

    public static HologramLine hologramLine(Component component, float distance) {
        return new HologramLine(component, distance);
    }

    private static HologramLine hologramLine(List<String> components, float distance, int refresh) {
        return new HologramLine(components, distance, refresh);
    }

    public Component getComponent() {
        if (component == null)
            return ComponentUtils.parse(components.get(0));
        return component;
    }

    public Component getComponent(int index) {
        return ComponentUtils.parse(components.get(index));
    }

    public void setComponent(Component component) {
        this.component = component;
    }

    public List<String> getComponents() {
        return components;
    }

    public void setArmorstand(EntityNPC armorstand) {
        this.armorstand = armorstand;
    }

    public EntityNPC getArmorstand() {
        return armorstand;
    }

    public float getDistance() {
        return distance;
    }

    public int getRefresh() {
        return refresh;
    }

}
