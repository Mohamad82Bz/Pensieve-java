package me.mohamad82.pensieve.recording.record;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.mohamad82.pensieve.recording.PendingBlockBreak;
import me.mohamad82.pensieve.recording.enums.ActionType;
import me.mohamad82.pensieve.recording.enums.DamageType;
import me.mohamad82.pensieve.utils.Utils;
import me.mohamad82.ruom.math.vector.Vector3;
import me.mohamad82.ruom.math.vector.Vector3Utils;
import me.mohamad82.ruom.npc.NPC;
import me.mohamad82.ruom.utils.NMSUtils;
import me.mohamad82.pensieve.utils.wrappedblock.WrappedBlock;
import me.mohamad82.pensieve.utils.wrappedblock.WrappedBlockUtils;
import me.mohamad82.ruom.xseries.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class PlayerRecordTick extends RecordTick {

    private Map<Vector3, WrappedBlock> blockPlaces;
    private Map<Vector3, WrappedBlock> blockBreaks;
    private Map<Vector3, BlockData> blockData;

    private PendingBlockBreak pendingBlockBreak;
    private Material eatingMaterial;
    private Vector3 blockInteractionLocation;
    private Material blockInteractionType;
    private NPC.Pose pose;
    private DamageType takenDamageType;

    /**
     * Bitmask of actions
     * @see ActionType
     */
    private int actions;

    private double health = -1;
    private int hunger = -1;
    private int ping = -1;
    /**
     * 0 = No interaction
     * 1 = MainHand interaction
     * 2 = OffHand interaction
     * 3 = Release
     */
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

    public Map<Vector3, WrappedBlock> getBlockPlaces() {
        return blockPlaces;
    }

    public void initializeBlockPlaces() {
        blockPlaces = new HashMap<>();
    }

    public Map<Vector3, WrappedBlock> getBlockBreaks() {
        return blockBreaks;
    }

    public void initializeBlockBreaks() {
        blockBreaks = new HashMap<>();
    }

    public Map<Vector3, BlockData> getBlockData() {
        return blockData;
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

    public Material getEatingMaterial() {
        return eatingMaterial;
    }

    public void setEatingMaterial(Material eatingMaterial) {
        this.eatingMaterial = eatingMaterial;
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

    public boolean didSwing() {
        return (actions & ActionType.SWING.getBitMask()) != 0;
    }

    public void swing() {
        actions |= ActionType.SWING.getBitMask();
    }

    public boolean ateFood() {
        return (actions & ActionType.EAT_FOOD.getBitMask()) != 0;
    }

    public void eatFood() {
        actions |= ActionType.EAT_FOOD.getBitMask();
    }

    public boolean thrownProjectile() {
        return (actions & ActionType.THROW_PROJECTILE.getBitMask()) != 0;
    }

    public void throwProjectile() {
        actions |= ActionType.THROW_PROJECTILE.getBitMask();
    }

    public boolean thrownTrident() {
        return (actions & ActionType.THROW_TRIDENT.getBitMask()) != 0;
    }

    public void throwTrident() {
        actions |= ActionType.THROW_TRIDENT.getBitMask();
    }

    public boolean thrownFishingRod() {
        return (actions & ActionType.THROW_FISHING_ROD.getBitMask()) != 0;
    }

    public void throwFishingRod() {
        actions |= ActionType.THROW_FISHING_ROD.getBitMask();
    }

    public boolean thrownFirework() {
        return (actions & ActionType.THROW_FIREWORK.getBitMask()) != 0;
    }

    public void throwFirework() {
        actions |= ActionType.THROW_FIREWORK.getBitMask();
    }

    public boolean retrievedFishingRod() {
        return (actions & ActionType.RETRIEVE_FISHING_ROD.getBitMask()) != 0;
    }

    public void retrieveFishingRod() {
        actions |= ActionType.RETRIEVE_FISHING_ROD.getBitMask();
    }

    public boolean drawnCrossbow() {
        return (actions & ActionType.DRAW_CROSSBOW.getBitMask()) != 0;
    }

    public void drawCrossbow() {
        actions |= ActionType.DRAW_CROSSBOW.getBitMask();
    }

    public boolean shotCrossbow() {
        return (actions & ActionType.SHOOT_CROSSBOW.getBitMask()) != 0;
    }

    public void shootCrossbow() {
        actions |= ActionType.SHOOT_CROSSBOW.getBitMask();
    }

    public boolean didOpenChestInteraction() {
        return (actions & ActionType.OPEN_CHEST_INTERACTION.getBitMask()) != 0;
    }

    public void setOpenChestInteraction(boolean openChestInteraction) {
        if (openChestInteraction) {
            actions |= ActionType.OPEN_CHEST_INTERACTION.getBitMask();
        } else {
            if (didOpenChestInteraction()) {
                actions &= ~ActionType.OPEN_CHEST_INTERACTION.getBitMask();
            }
        }
    }

    public boolean wasBurning() {
        return (actions & ActionType.BURN.getBitMask()) != 0;
    }

    public void setBurning(boolean burning) {
        if (burning) {
            actions |= ActionType.BURN.getBitMask();
        } else {
            if (wasBurning()) {
                actions &= ~ActionType.BURN.getBitMask();
            }
        }
    }

    public boolean wasCrouching() {
        return (actions & ActionType.CROUCH.getBitMask()) != 0;
    }

    public void setCrouching(boolean crouching) {
        if (crouching) {
            actions |= ActionType.CROUCH.getBitMask();
        } else {
            if (wasCrouching()) {
                actions &= ~ActionType.CROUCH.getBitMask();
            }
        }
    }

    public boolean wasSprinting() {
        return (actions & ActionType.SPRINT.getBitMask()) != 0;
    }

    public void setSprinting(boolean sprinting) {
        if (sprinting) {
            actions |= ActionType.SPRINT.getBitMask();
        } else {
            if (wasSprinting()) {
                actions &= ~ActionType.SPRINT.getBitMask();
            }
        }
    }

    public boolean wasSwimming() {
        return (actions & ActionType.SWIM.getBitMask()) != 0;
    }

    public void setSwimming(boolean swimming) {
        if (swimming) {
            actions |= ActionType.SWIM.getBitMask();
        } else {
            if (wasSwimming()) {
                actions &= ~ActionType.SWIM.getBitMask();
            }
        }
    }

    public boolean wasInvisible() {
        return (actions & ActionType.INVISIBLE.getBitMask()) != 0;
    }

    public void setInvisible(boolean invisible) {
        if (invisible) {
            actions |= ActionType.INVISIBLE.getBitMask();
        } else {
            if (wasInvisible()) {
                actions &= ~ActionType.INVISIBLE.getBitMask();
            }
        }
    }

    public boolean wasGlowing() {
        return (actions & ActionType.GLOW.getBitMask()) != 0;
    }

    public void setGlowing(boolean glowing) {
        if (glowing) {
            actions |= ActionType.GLOW.getBitMask();
        } else {
            if (wasGlowing()) {
                actions &= ~ActionType.GLOW.getBitMask();
            }
        }
    }

    public boolean wasGliding() {
        return (actions & ActionType.GLIDE.getBitMask()) != 0;
    }

    public void setGliding(boolean gliding) {
        if (gliding) {
            actions |= ActionType.GLIDE.getBitMask();
        } else {
            if (wasGliding()) {
                actions &= ~ActionType.GLIDE.getBitMask();
            }
        }
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

    @Override
    public JsonObject toJson(JsonObject jsonObject) {
        if (blockPlaces != null) {
            JsonObject blockPlacesJson = new JsonObject();
            for (Map.Entry<Vector3, WrappedBlock> entry : blockPlaces.entrySet()) {
                blockPlacesJson.add(entry.getKey().toString(), entry.getValue().toJson());
            }
            jsonObject.add("bp", blockPlacesJson);
        }
        if (blockBreaks != null) {
            JsonObject blockBreaksJson = new JsonObject();
            for (Map.Entry<Vector3, WrappedBlock> entry : blockBreaks.entrySet()) {
                blockBreaksJson.add(entry.getKey().toString(), entry.getValue().toJson());
            }
            jsonObject.add("bb", blockBreaksJson);
        }
        if (blockData != null) {
            JsonObject blockDataJson = new JsonObject();
            for (Map.Entry<Vector3, BlockData> entry : blockData.entrySet()) {
                blockDataJson.addProperty(entry.getKey().toString(), entry.getValue().getAsString());
            }
            jsonObject.add("bd", blockDataJson);
        }
        if (pendingBlockBreak != null) {
            jsonObject.add("pbb", pendingBlockBreak.toJson());
        }
        if (eatingMaterial != null) {
            jsonObject.addProperty("em", XMaterial.matchXMaterial(eatingMaterial).name());
        }
        if (blockInteractionLocation != null) {
            jsonObject.addProperty("bil", blockInteractionLocation.toString());
        }
        if (blockInteractionType != null) {
            jsonObject.addProperty("bit", XMaterial.matchXMaterial(blockInteractionType).name());
        }
        if (pose != null) {
            jsonObject.addProperty("ps", pose.toString());
        }
        if (takenDamageType != null) {
            jsonObject.addProperty("tdt", takenDamageType.toString());
        }
        if (actions != 0) {
            jsonObject.addProperty("a", actions);
        }
        if (health != -1) {
            jsonObject.addProperty("heal", health);
        }
        if (hunger != -1) {
            jsonObject.addProperty("hung", hunger);
        }
        if (ping != -1) {
            jsonObject.addProperty("ping", ping);
        }
        if (useItemInteraction != -1) {
            jsonObject.addProperty("uii", useItemInteraction);
        }
        if (usedItemTime != -1) {
            jsonObject.addProperty("uit", usedItemTime);
        }
        if (bodyArrows != -1) {
            jsonObject.addProperty("ba", bodyArrows);
        }
        if (crossbowChargeLevel != -1) {
            jsonObject.addProperty("ccl", crossbowChargeLevel);
        }

        if (hand != null) {
            jsonObject.addProperty("h", NMSUtils.getItemStackNBTJson(hand));
        }
        if (offHand != null) {
            jsonObject.addProperty("oh", NMSUtils.getItemStackNBTJson(offHand));
        }
        if (helmet != null) {
            jsonObject.addProperty("helm", NMSUtils.getItemStackNBTJson(helmet));
        }
        if (chestplate != null) {
            jsonObject.addProperty("chest", NMSUtils.getItemStackNBTJson(chestplate));
        }
        if (leggings != null) {
            jsonObject.addProperty("leg", NMSUtils.getItemStackNBTJson(leggings));
        }
        if (boots != null) {
            jsonObject.addProperty("boot", NMSUtils.getItemStackNBTJson(boots));
        }

        return super.toJson(jsonObject);
    }

    @Override
    public PlayerRecordTick fromJson(SerializableRecordTick serializableRecordTick, JsonObject jsonObject) {
        PlayerRecordTick tick = (PlayerRecordTick) serializableRecordTick;

        if (jsonObject.has("bp")) {
            Map<Vector3, WrappedBlock> blockPlaces = new HashMap<>();
            for (Map.Entry<String, JsonElement> entry : jsonObject.get("bp").getAsJsonObject().entrySet()) {
                blockPlaces.put(Vector3Utils.toVector3(entry.getKey()), WrappedBlockUtils.fromJson(entry.getValue().getAsJsonObject()));
            }
            tick.blockPlaces = blockPlaces;
        }
        if (jsonObject.has("bb")) {
            Map<Vector3, WrappedBlock> blockBreaks = new HashMap<>();
            for (Map.Entry<String, JsonElement> entry : jsonObject.get("bb").getAsJsonObject().entrySet()) {
                blockBreaks.put(Vector3Utils.toVector3(entry.getKey()), WrappedBlockUtils.fromJson(entry.getValue().getAsJsonObject()));
            }
            tick.blockBreaks = blockBreaks;
        }
        if (jsonObject.has("bd")) {
            Map<Vector3, BlockData> blockData = new HashMap<>();
            for (Map.Entry<String, JsonElement> entry : jsonObject.get("bd").getAsJsonObject().entrySet()) {
                blockData.put(Vector3Utils.toVector3(entry.getKey()), Bukkit.createBlockData(entry.getValue().getAsString()));
            }
            tick.blockData = blockData;
        }

        if (jsonObject.has("pbb")) {
            tick.pendingBlockBreak = PendingBlockBreak.fromJson(jsonObject.get("pbb").getAsJsonObject());
        }
        if (jsonObject.has("em")) {
            tick.eatingMaterial = XMaterial.matchXMaterial(jsonObject.get("em").getAsString().toUpperCase()).orElse(null).parseMaterial();
        }
        if (jsonObject.has("bil")) {
            tick.blockInteractionLocation = Vector3Utils.toVector3(jsonObject.get("bil").getAsString());
        }
        if (jsonObject.has("bit")) {
            tick.blockInteractionType = XMaterial.matchXMaterial(jsonObject.get("bit").getAsString().toUpperCase()).orElse(null).parseMaterial();
        }
        if (jsonObject.has("ps")) {
            tick.pose = NPC.Pose.valueOf(jsonObject.get("ps").getAsString());
        }
        if (jsonObject.has("tdt")) {
            tick.takenDamageType = DamageType.valueOf(jsonObject.get("tdt").getAsString());
        }
        if (jsonObject.has("a")) {
            tick.actions = jsonObject.get("a").getAsInt();
        }
        if (jsonObject.has("heal")) {
            tick.health = jsonObject.get("heal").getAsDouble();
        }
        if (jsonObject.has("hung")) {
            tick.hunger = jsonObject.get("hung").getAsInt();
        }
        if (jsonObject.has("ping")) {
            tick.ping = jsonObject.get("ping").getAsInt();
        }
        if (jsonObject.has("uii")) {
            tick.useItemInteraction = jsonObject.get("uii").getAsByte();
        }
        if (jsonObject.has("uit")) {
            tick.usedItemTime = jsonObject.get("uit").getAsInt();
        }
        if (jsonObject.has("ba")) {
            tick.bodyArrows = jsonObject.get("ba").getAsInt();
        }
        if (jsonObject.has("ccl")) {
            tick.crossbowChargeLevel = jsonObject.get("ccl").getAsInt();
        }

        if (jsonObject.has("h")) {
            tick.hand = NMSUtils.getItemStackFromNBTJson(jsonObject.get("h").getAsString());
        }
        if (jsonObject.has("oh")) {
            tick.offHand = NMSUtils.getItemStackFromNBTJson(jsonObject.get("oh").getAsString());
        }
        if (jsonObject.has("helm")) {
            tick.helmet = NMSUtils.getItemStackFromNBTJson(jsonObject.get("helm").getAsString());
        }
        if (jsonObject.has("chest")) {
            tick.chestplate = NMSUtils.getItemStackFromNBTJson(jsonObject.get("chest").getAsString());
        }
        if (jsonObject.has("leg")) {
            tick.leggings = NMSUtils.getItemStackFromNBTJson(jsonObject.get("leg").getAsString());
        }
        if (jsonObject.has("boot")) {
            tick.boots = NMSUtils.getItemStackFromNBTJson(jsonObject.get("boot").getAsString());
        }

        return (PlayerRecordTick) super.fromJson(tick, jsonObject);
    }

}
