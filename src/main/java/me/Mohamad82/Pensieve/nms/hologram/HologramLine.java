package me.Mohamad82.Pensieve.nms.hologram;

import me.Mohamad82.Pensieve.nms.npc.EntityNPC;
import me.Mohamad82.RUoM.adventureapi.adventure.text.Component;

public class HologramLine {

    private final Component component;
    private EntityNPC armorstand;
    private final float distance;

    private HologramLine(Component component, float distance) {
        this.component = component;
        this.distance = distance;
    }

    public static HologramLine hologramLine(Component component, float distance) {
        return new HologramLine(component, distance);
    }

    public Component getComponent() {
        return component;
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

}
