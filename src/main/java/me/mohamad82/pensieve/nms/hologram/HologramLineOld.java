package me.mohamad82.pensieve.nms.hologram;

import me.mohamad82.pensieve.nms.znpcold.EntityNPCOld;
import me.Mohamad82.RUoM.adventureapi.ComponentUtils;
import me.Mohamad82.RUoM.adventureapi.adventure.text.Component;

import java.util.List;

public class HologramLineOld {

    private Component component;
    private List<String> components;
    private EntityNPCOld armorstand;
    private final float distance;
    private int refresh;

    private HologramLineOld(Component component, float distance) {
        this.component = component;
        this.distance = distance;
    }

    public HologramLineOld(List<String> components, float distance, int refresh) {
        this.components = components;
        this.distance = distance;
        this.refresh = refresh;
    }

    public static HologramLineOld hologramLine(Component component, float distance) {
        return new HologramLineOld(component, distance);
    }

    private static HologramLineOld hologramLine(List<String> components, float distance, int refresh) {
        return new HologramLineOld(components, distance, refresh);
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

    public void setArmorstand(EntityNPCOld armorstand) {
        this.armorstand = armorstand;
    }

    public EntityNPCOld getArmorstand() {
        return armorstand;
    }

    public float getDistance() {
        return distance;
    }

    public int getRefresh() {
        return refresh;
    }

}
