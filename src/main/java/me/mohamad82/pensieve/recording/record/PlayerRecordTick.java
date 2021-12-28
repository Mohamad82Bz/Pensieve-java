package me.mohamad82.pensieve.recording.record;

import me.mohamad82.pensieve.recording.PendingBlockBreak;
import me.mohamad82.pensieve.recording.enums.DamageType;
import me.mohamad82.pensieve.utils.Utils;
import me.mohamad82.ruom.npc.NPC;
import me.mohamad82.ruom.vector.Vector3;
import org.bukkit.Material;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class PlayerRecordTick extends RecordTick {

    private Map<Vector3, Material> blockPlaces;
    private Map<Vector3, Material> blockBreaks;
    private Map<Vector3, String> blockData;

    private PendingBlockBreak pendingBlockBreak;
    private ItemStack eatingItem;
    private Vector3 blockInteractionLocation;
    private Material blockInteractionType;
    private NPC.Pose pose;
    private DamageType takenDamageType;
    private String message;

    private boolean swing;
    private boolean eatFood;
    private boolean throwProjectile;
    private boolean drawCrossbow;
    private boolean shootCrossbow;
    private boolean openChestInteraction;
    private boolean burning;
    private boolean crouching;
    private boolean sprinting;
    private boolean swimming;
    private boolean invisible;
    private boolean glowing;
    private boolean gliding;

    private double health = -1;
    private int hunger = -1;
    private int ping = -1;
    private byte useItemInteraction = -1;
    private int usedItemTime = -1;
    private int bodyArrows = -1;
    private int crossbowChargeLevel = -1;

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

    public Map<Vector3, String> getBlockData() {
        return blockData;
    }

    public void setBlockData(Map<Vector3, String> blockData) {
        this.blockData = blockData;
    }

    public void initializeBlockData() {
        blockData = new HashMap<>();
    }

    public PendingBlockBreak getPendingBlockBreak() {
        return pendingBlockBreak;
    }

    public void setPendingBlockBreak(PendingBlockBreak pendingBlockBreak) {
        this.pendingBlockBreak = pendingBlockBreak;
    }

    public ItemStack getEatingItem() {
        return eatingItem;
    }

    public void setEatingItem(ItemStack eatingItem) {
        this.eatingItem = eatingItem;
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

    public NPC.Pose getPose() {
        return pose;
    }

    public void setPose(NPC.Pose pose) {
        this.pose = pose;
    }

    public DamageType getTakenDamageType() {
        return takenDamageType;
    }

    public void damage(DamageType takenDamageType) {
        this.takenDamageType = takenDamageType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean didSwing() {
        return swing;
    }

    public void swing() {
        this.swing = true;
    }

    public boolean ateFood() {
        return eatFood;
    }

    public void eatFood() {
        this.eatFood = true;
    }

    public boolean thrownProjectile() {
        return throwProjectile;
    }

    public void throwProjectile() {
        this.throwProjectile = true;
    }

    public boolean drawnCrossbow() {
        return drawCrossbow;
    }

    public void drawCrossbow() {
        this.drawCrossbow = drawCrossbow;
    }

    public boolean shotCrossbow() {
        return shootCrossbow;
    }

    public void shootCrossbow() {
        this.shootCrossbow = true;
    }

    public boolean didOpenChestInteraction() {
        return openChestInteraction;
    }

    public void setOpenChestInteraction(boolean openChestInteraction) {
        this.openChestInteraction = openChestInteraction;
    }

    public boolean wasBurning() {
        return burning;
    }

    public void setBurning(boolean burning) {
        this.burning = burning;
    }

    public boolean wasCrouching() {
        return crouching;
    }

    public void setCrouching(boolean crouching) {
        this.crouching = crouching;
    }

    public boolean wasSprinting() {
        return sprinting;
    }

    public void setSprinting(boolean sprinting) {
        this.sprinting = sprinting;
    }

    public boolean wasSwimming() {
        return swimming;
    }

    public void setSwimming(boolean swimming) {
        this.swimming = swimming;
    }

    public boolean wasInvisible() {
        return invisible;
    }

    public void setInvisible(boolean invisible) {
        this.invisible = invisible;
    }

    public boolean wasGlowing() {
        return glowing;
    }

    public void setGlowing(boolean glowing) {
        this.glowing = glowing;
    }

    public boolean wasGliding() {
        return gliding;
    }

    public void setGliding(boolean gliding) {
        this.gliding = gliding;
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

    /**
     * 0 = No interaction
     * 1 = MainHand interaction
     * 2 = OffHand interaction
     * 3 = Release
     */
    public byte getUseItemInteractionHand() {
        return useItemInteraction;
    }

    public void useItemInteraction(byte useItemInteraction) {
        this.useItemInteraction = useItemInteraction;
    }

    public int getUsedItemTime() {
        return usedItemTime;
    }

    public void setUsedItemTime(int usedItemTime) {
        this.usedItemTime = usedItemTime;
    }

    public int getBodyArrows() {
        return bodyArrows;
    }

    public void setBodyArrows(int bodyArrows) {
        this.bodyArrows = bodyArrows;
    }

    public int getCrossbowChargeLevel() {
        return crossbowChargeLevel;
    }

    public void setCrossbowChargeLevel(int crossbowChargeLevel) {
        this.crossbowChargeLevel = crossbowChargeLevel;
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

    @Override
    public PlayerRecordTick copy() {
        return Utils.copy(this, new PlayerRecordTick());
    }

}
