package me.Mohamad82.Pensieve.record;

import me.Mohamad82.Pensieve.nms.enums.NPCState;
import me.Mohamad82.Pensieve.record.enums.DamageType;
import me.Mohamad82.RUoM.vector.Vector3;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class RecordTick implements Cloneable {

    private Map<Vector3, Material> blockPlaces;
    private Map<Vector3, Material> blockBreaks;

    private PendingBlockBreak pendingBlockBreak;
    private ItemStack eatingItem;
    private Vector3 location;
    private NPCState state;
    private DamageType takenDamageType;
    private String message;

    private boolean swing;
    private boolean takeDamage;
    private boolean eatFood;

    private float yaw = -999;
    private float pitch = -999;

    private double health = -999;
    private int hunger = -999;
    private int ping = -999;

    private ItemStack hand;
    private ItemStack offHand;
    private ItemStack helmet;
    private ItemStack chestplate;
    private ItemStack leggings;
    private ItemStack boots;

    public Map<Vector3, Material> getBlockPlaces() {
        return blockPlaces;
    }

    public void setBlockPlaces(Map<Vector3, Material> blockPlaces) {
        this.blockPlaces = blockPlaces;
    }

    public void initializeBlockPlaces() {
        blockPlaces = new HashMap<>();
    }

    public Map<Vector3, Material> getBlockBreaks() {
        return blockBreaks;
    }

    public void setBlockBreaks(Map<Vector3, Material> blockBreaks) {
        this.blockBreaks = blockBreaks;
    }

    public void initializeBlockBreaks() {
        blockBreaks = new HashMap<>();
    }

    public ItemStack getEatingItem() {
        return eatingItem;
    }

    public void setEatingItem(ItemStack eatingItem) {
        this.eatingItem = eatingItem;
    }

    public PendingBlockBreak getPendingBlockBreak() {
        return pendingBlockBreak;
    }

    public void setPendingBlockBreak(PendingBlockBreak pendingBlockBreak) {
        this.pendingBlockBreak = pendingBlockBreak;
    }

    public Vector3 getLocation() {
        return location;
    }

    public void setLocation(Vector3 location) {
        this.location = location;
    }

    public NPCState getState() {
        return state;
    }

    public void setState(NPCState state) {
        this.state = state;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public boolean didSwing() {
        return swing;
    }

    public void swing() {
        this.swing = true;
    }

    public boolean tookDamage() {
        return takenDamageType != null;
    }

    public DamageType getTakenDamageType() {
        return takenDamageType;
    }

    public void damage(DamageType damageType) {
        this.takenDamageType = damageType;
    }

    public boolean ateFood() {
        return eatFood;
    }

    public void eatFood() {
        this.eatFood = true;
    }

    public double getHealth() {
        return health;
    }

    public void setHealth(double health) {
        this.health = health;
    }

    public int getHunger() {
        return hunger;
    }

    public void setHunger(int hunger) {
        this.hunger = hunger;
    }

    public int getPing() {
        return ping;
    }

    public void setPing(int ping) {
        this.ping = ping;
    }

    public ItemStack getHand() {
        return hand;
    }

    public void setHand(ItemStack hand) {
        this.hand = hand;
    }

    public ItemStack getOffHand() {
        return offHand;
    }

    public void setOffHand(ItemStack offHand) {
        this.offHand = offHand;
    }

    public ItemStack getHelmet() {
        return helmet;
    }

    public void setHelmet(ItemStack helmet) {
        this.helmet = helmet;
    }

    public ItemStack getChestplate() {
        return chestplate;
    }

    public void setChestplate(ItemStack chestplate) {
        this.chestplate = chestplate;
    }

    public ItemStack getLeggings() {
        return leggings;
    }

    public void setLeggings(ItemStack leggings) {
        this.leggings = leggings;
    }

    public ItemStack getBoots() {
        return boots;
    }

    public void setBoots(ItemStack boots) {
        this.boots = boots;
    }

    public RecordTick clone() {
        try {
            RecordTick tick = (RecordTick) super.clone();
            if (this.getBlockPlaces() != null)
                tick.setBlockPlaces(new HashMap<>(this.getBlockPlaces()));
            if (this.getBlockBreaks() != null)
                tick.setBlockBreaks(new HashMap<>(this.getBlockBreaks()));
            if (this.getLocation() != null)
                tick.setLocation(this.getLocation().clone());
            if (this.getHelmet() != null)
                tick.setHelmet(this.getHelmet().clone());
            if (this.getChestplate() != null)
                tick.setChestplate(this.getChestplate().clone());
            if (this.getLeggings() != null)
                tick.setLeggings(this.getLeggings().clone());
            if (this.getBoots() != null)
                this.setBoots(this.getBoots().clone());

            return tick;
        } catch (CloneNotSupportedException e) {
            throw new Error(e);
        }
    }
    
}
