package me.mohamad82.pensieve.recording;

import me.mohamad82.pensieve.nms.npc.enums.NPCState;
import me.mohamad82.pensieve.recording.enums.DamageType;
import me.Mohamad82.RUoM.vector.Vector3;
import org.bukkit.Material;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class RecordTick implements Cloneable {

    private Map<Vector3, Material> blockPlaces;
    private Map<Vector3, Material> blockBreaks;
    private Map<Vector3, String> blockData;

    private PendingBlockBreak pendingBlockBreak;
    private ItemStack eatingItem;
    private Vector3 location;
    private Vector3 velocity;
    private Vector3 blockInteractionLocation;
    private Material blockInteractionType;
    private NPCState state;
    private DamageType takenDamageType;
    private String message;

    private boolean swing;
    private boolean eatFood;
    private boolean throwProjectile;
    private boolean drawCrossbow;
    private boolean shootCrossbow;
    private boolean drawBowWithOffHand;
    private boolean openChestInteraction;

    private float yaw = -1;
    private float pitch = -1;

    private double health = -1;
    private int hunger = -1;
    private int ping = -1;
    private int drawBow = -1;
    private int crossbowChargeLevel = -1;
    private int bodyArrows = -1;
    private int potionColor = -1;
    private int itemAmount = -1;
    private byte entityMetadata = -1;

    private ItemStack hand;
    private ItemStack offHand;
    private ItemStack helmet;
    private ItemStack chestplate;
    private ItemStack leggings;
    private ItemStack boots;

    protected RecordTick() {

    }

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

    public Map<Vector3, String> getBlockData() {
        return blockData;
    }

    public void setBlockData(Map<Vector3, String> blockData) {
        this.blockData = blockData;
    }

    public void initializeBlockData() {
        blockData = new HashMap<>();
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

    public Vector3 getVelocity() {
        return velocity;
    }

    public void setVelocity(Vector3 velocity) {
        this.velocity = velocity;
    }

    public Vector3 getBlockInteractionLocation() {
        return blockInteractionLocation;
    }

    public void setBlockInteractionLocation(Vector3 blockInteractionLocation) {
        this.blockInteractionLocation = blockInteractionLocation;
    }

    public Material getBlockInteractionType() {
        return blockInteractionType;
    }

    public void setBlockInteractionType(Material blockInteractionType) {
        this.blockInteractionType = blockInteractionType;
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

    public boolean threwProjectile() {
        return throwProjectile;
    }

    public void throwProjectile() {
        this.throwProjectile = true;
    }

    public boolean drawnCrossbow() {
        return drawCrossbow;
    }

    public void drawCrossbow() {
        this.drawCrossbow = true;
    }

    public boolean shotCrossbow() {
        return shootCrossbow;
    }

    public void shootCrossbow() {
        this.shootCrossbow = true;
    }

    public int getCrossbowChargeLevel() {
        return crossbowChargeLevel;
    }

    public void setCrossbowChargeLevel(int crossbowChargeLevel) {
        this.crossbowChargeLevel = crossbowChargeLevel;
    }

    public int getBodyArrows() {
        return bodyArrows;
    }

    public void setBodyArrows(int bodyArrows) {
        this.bodyArrows = bodyArrows;
    }

    public int getPotionColor() {
        return potionColor;
    }

    public void setPotionColor(int potionColor) {
        this.potionColor = potionColor;
    }

    public int getItemAmount() {
        return itemAmount;
    }

    public void setItemAmount(int itemAmount) {
        this.itemAmount = itemAmount;
    }

    public byte getEntityMetadata() {
        return entityMetadata;
    }

    public void setEntityMetadata(byte entityMetadata) {
        this.entityMetadata = entityMetadata;
    }

    public boolean drawnBowWithOffHand() {
        return drawBowWithOffHand;
    }

    public void drawBowWithOffHand() {
        this.drawBowWithOffHand = true;
    }

    public boolean isOpenChestInteraction() {
        return openChestInteraction;
    }

    public void setOpenChestInteraction(boolean openChestInteraction) {
        this.openChestInteraction = openChestInteraction;
    }

    public int getDrawBow() {
        return drawBow;
    }

    public void drawBow(int drawnBow) {
        this.drawBow = drawnBow;
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

    public ItemStack getItem(EquipmentSlot slot) {
        switch (slot) {
            case HAND:
                return hand;
            case OFF_HAND:
                return offHand;
            case HEAD:
                return helmet;
            case CHEST:
                return chestplate;
            case LEGS:
                return leggings;
            case FEET:
                return boots;
            default:
                return null;
        }
    }

    public void setItem(EquipmentSlot slot, ItemStack item) {
        switch (slot) {
            case HAND:
                this.hand = item;
                break;
            case OFF_HAND:
                this.offHand = item;
                break;
            case HEAD:
                this.helmet = item;
                break;
            case CHEST:
                this.chestplate = item;
                break;
            case LEGS:
                this.leggings = item;
                break;
            case FEET:
                this.boots = item;
                break;
        }
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
