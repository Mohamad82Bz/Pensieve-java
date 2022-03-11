package me.mohamad82.pensieve.recording.record;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.mohamad82.pensieve.recording.PendingBlockBreak;
import me.mohamad82.pensieve.recording.enums.DamageType;
import me.mohamad82.pensieve.utils.Utils;
import me.mohamad82.ruom.Ruom;
import me.mohamad82.ruom.npc.NPC;
import me.mohamad82.ruom.utils.NMSUtils;
import me.mohamad82.ruom.math.vector.Vector3;
import me.mohamad82.ruom.math.vector.Vector3Utils;
import me.mohamad82.ruom.xseries.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class PlayerRecordTick extends RecordTick {

    private Map<Vector3, BlockData> blockPlaces;
    private Map<Vector3, BlockData> blockBreaks;
    private Map<Vector3, BlockData> blockData;

    private PendingBlockBreak pendingBlockBreak;
    private Material eatingMaterial;
    private Vector3 blockInteractionLocation;
    private Material blockInteractionType;
    private NPC.Pose pose;
    private DamageType takenDamageType;

    private boolean swing;
    private boolean eatFood;
    private boolean throwProjectile;
    private boolean throwTrident;
    private boolean throwFishingRod;
    private boolean throwFirework;
    private boolean retrieveFishingRod;
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

    public Map<Vector3, BlockData> getBlockPlaces() {
        return blockPlaces;
    }

    public void initializeBlockPlaces() {
        blockPlaces = new HashMap<>();
    }

    public Map<Vector3, BlockData> getBlockBreaks() {
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

    public boolean thrownTrident() {
        return throwTrident;
    }

    public void throwTrident() {
        this.throwTrident = true;
    }

    public boolean thrownFishingRod() {
        return throwFishingRod;
    }

    public void throwFishingRod() {
        this.throwFishingRod = true;
    }

    public boolean thrownFirework() {
        return throwFirework;
    }

    public void throwFirework() {
        throwFirework = true;
    }

    public boolean retrievedFishingRod() {
        return retrieveFishingRod;
    }

    public void retrieveFishingRod() {
        this.retrieveFishingRod = true;
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

    @Override
    public JsonObject toJson(JsonObject jsonObject) {
        if (blockPlaces != null) {
            JsonObject blockPlacesJson = new JsonObject();
            for (Map.Entry<Vector3, BlockData> entry : blockPlaces.entrySet()) {
                blockPlacesJson.addProperty(entry.getKey().toString(), entry.getValue().getAsString());
            }
            jsonObject.add("blockplaces", blockPlacesJson);
        }
        if (blockBreaks != null) {
            JsonObject blockBreaksJson = new JsonObject();
            for (Map.Entry<Vector3, BlockData> entry : blockBreaks.entrySet()) {
                blockBreaksJson.addProperty(entry.getKey().toString(), entry.getValue().getAsString());
            }
            jsonObject.add("blockbreaks", blockBreaksJson);
        }
        if (blockData != null) {
            JsonObject blockDataJson = new JsonObject();
            for (Map.Entry<Vector3, BlockData> entry : blockData.entrySet()) {
                blockDataJson.addProperty(entry.getKey().toString(), entry.getValue().getAsString());
            }
            jsonObject.add("blockdata", blockDataJson);
        }
        if (pendingBlockBreak != null) {
            jsonObject.add("pendingblockbreak", pendingBlockBreak.toJson());
        }
        if (eatingMaterial != null) {
            jsonObject.addProperty("eatingmaterial", XMaterial.matchXMaterial(eatingMaterial).name());
        }
        if (blockInteractionLocation != null) {
            jsonObject.addProperty("blockinteractionlocation", blockInteractionLocation.toString());
        }
        if (blockInteractionType != null) {
            jsonObject.addProperty("blockinteractiontype", XMaterial.matchXMaterial(blockInteractionType).name());
        }
        if (pose != null) {
            jsonObject.addProperty("pose", pose.toString());
        }
        if (takenDamageType != null) {
            jsonObject.addProperty("takendamagetype", takenDamageType.toString());
        }

        JsonArray jsonArray = new JsonArray();
        if (swing) jsonArray.add("swang");
        if (eatFood) jsonArray.add("atefood");
        if (throwProjectile) jsonArray.add("thrownprojectile");
        if (throwTrident) jsonArray.add("throwntrident");
        if (throwFishingRod) jsonArray.add("thrownfishingrod");
        if (throwFirework) jsonArray.add("thrownfirework");
        if (retrieveFishingRod) jsonArray.add("retrievedfishingrod");
        if (drawCrossbow) jsonArray.add("drawncrossbow");
        if (shootCrossbow) jsonArray.add("shotcrossbow");
        if (openChestInteraction) jsonArray.add("openchestinteraction");
        if (burning) jsonArray.add("burning");
        if (crouching) jsonArray.add("crouching");
        if (sprinting) jsonArray.add("sprinting");
        if (swimming) jsonArray.add("swimming");
        if (invisible) jsonArray.add("invisible");
        if (glowing) jsonArray.add("glowing");
        if (gliding) jsonArray.add("gliding");
        if (jsonArray.size() != 0)
            jsonObject.add("actions", jsonArray);

        if (health != -1) {
            jsonObject.addProperty("health", health);
        }
        if (hunger != -1) {
            jsonObject.addProperty("hunger", hunger);
        }
        if (ping != -1) {
            jsonObject.addProperty("ping", ping);
        }
        if (useItemInteraction != -1) {
            jsonObject.addProperty("useiteminteraction", useItemInteraction);
        }
        if (usedItemTime != -1) {
            jsonObject.addProperty("useditemtime", usedItemTime);
        }
        if (bodyArrows != -1) {
            jsonObject.addProperty("bodyarrows", bodyArrows);
        }
        if (crossbowChargeLevel != -1) {
            jsonObject.addProperty("crossbowchargelevel", crossbowChargeLevel);
        }

        if (hand != null) {
            jsonObject.addProperty("hand", NMSUtils.getItemStackNBTJson(hand));
        }
        if (offHand != null) {
            jsonObject.addProperty("offhand", NMSUtils.getItemStackNBTJson(offHand));
        }
        if (helmet != null) {
            jsonObject.addProperty("helmet", NMSUtils.getItemStackNBTJson(helmet));
        }
        if (chestplate != null) {
            jsonObject.addProperty("chestplate", NMSUtils.getItemStackNBTJson(chestplate));
        }
        if (leggings != null) {
            jsonObject.addProperty("leggings", NMSUtils.getItemStackNBTJson(leggings));
        }
        if (boots != null) {
            jsonObject.addProperty("boots", NMSUtils.getItemStackNBTJson(boots));
        }

        return super.toJson(jsonObject);
    }

    @Override
    public PlayerRecordTick fromJson(SerializableRecordTick serializableRecordTick, JsonObject jsonObject) {
        PlayerRecordTick tick = (PlayerRecordTick) serializableRecordTick;

        if (jsonObject.has("blockplaces")) {
            Map<Vector3, BlockData> blockPlaces = new HashMap<>();
            for (Map.Entry<String, JsonElement> entry : jsonObject.get("blockplaces").getAsJsonObject().entrySet()) {
                blockPlaces.put(Vector3Utils.toVector3(entry.getKey()), Bukkit.createBlockData(entry.getValue().getAsString()));
            }
            tick.blockPlaces = blockPlaces;
        }
        if (jsonObject.has("blockbreaks")) {
            Map<Vector3, BlockData> blockBreaks = new HashMap<>();
            for (Map.Entry<String, JsonElement> entry : jsonObject.get("blockbreaks").getAsJsonObject().entrySet()) {
                blockBreaks.put(Vector3Utils.toVector3(entry.getKey()), Bukkit.createBlockData(entry.getValue().getAsString()));
            }
            tick.blockBreaks = blockBreaks;
        }
        if (jsonObject.has("blockdata")) {
            Map<Vector3, BlockData> blockData = new HashMap<>();
            for (Map.Entry<String, JsonElement> entry : jsonObject.get("blockdata").getAsJsonObject().entrySet()) {
                blockData.put(Vector3Utils.toVector3(entry.getKey()), Bukkit.createBlockData(entry.getValue().getAsString()));
            }
            tick.blockData = blockData;
        }

        if (jsonObject.has("pendingblockbreak")) {
            tick.pendingBlockBreak = PendingBlockBreak.fromJson(jsonObject.get("pendingblockbreak").getAsJsonObject());
        }
        if (jsonObject.has("eatingmaterial")) {
            tick.eatingMaterial = XMaterial.matchXMaterial(jsonObject.get("eatingmaterial").getAsString().toUpperCase()).get().parseMaterial();
        }
        if (jsonObject.has("blockinteractionlocation")) {
            tick.blockInteractionLocation = Vector3Utils.toVector3(jsonObject.get("blockinteractionlocation").getAsString());
        }
        if (jsonObject.has("blockinteractiontype")) {
            tick.blockInteractionType = XMaterial.matchXMaterial(jsonObject.get("blockinteractiontype").getAsString().toUpperCase()).get().parseMaterial();
        }
        if (jsonObject.has("pose")) {
            tick.pose = NPC.Pose.valueOf(jsonObject.get("pose").getAsString());
        }
        if (jsonObject.has("takendamagetype")) {
            tick.takenDamageType = DamageType.valueOf(jsonObject.get("takendamagetype").getAsString());
        }

        if (jsonObject.has("actions")) {
            for (JsonElement jsonElement : jsonObject.get("actions").getAsJsonArray()) {
                String actionType = jsonElement.getAsString();
                switch (actionType) {
                    case "swang": tick.swing = true; break;
                    case "atefood": tick.eatFood = true; break;
                    case "thrownprojectile": tick.throwProjectile = true; break;
                    case "throwntrident": tick.throwTrident = true; break;
                    case "thrownfirework": tick.throwFirework = true; break;
                    case "thrownfishingrod": tick.throwFishingRod = true; break;
                    case "retrievedfishingrod": tick.retrieveFishingRod = true; break;
                    case "drawncrossbow": tick.drawCrossbow = true; break;
                    case "shotcrossbow": tick.shootCrossbow(); break;
                    case "openchestinteraction": tick.openChestInteraction = true; break;
                    case "burning": tick.burning = true; break;
                    case "crouching": tick.crouching = true; break;
                    case "sprinting": tick.sprinting = true; break;
                    case "swimming": tick.swimming = true; break;
                    case "invisible": tick.invisible = true; break;
                    case "glowing": tick.glowing = true; break;
                    case "gliding": tick.gliding = true; break;
                    default: {
                        Ruom.warn("Unused action in pensieve file: " + actionType);
                    }
                }
            }
        }

        if (jsonObject.has("health")) {
            tick.health = jsonObject.get("health").getAsDouble();
        }
        if (jsonObject.has("hunger")) {
            tick.hunger = jsonObject.get("hunger").getAsInt();
        }
        if (jsonObject.has("ping")) {
            tick.ping = jsonObject.get("ping").getAsInt();
        }
        if (jsonObject.has("useiteminteraction")) {
            tick.useItemInteraction = jsonObject.get("useiteminteraction").getAsByte();
        }
        if (jsonObject.has("useditemtime")) {
            tick.usedItemTime = jsonObject.get("useditemtime").getAsInt();
        }
        if (jsonObject.has("bodyarrows")) {
            tick.bodyArrows = jsonObject.get("bodyarrows").getAsInt();
        }
        if (jsonObject.has("crossbowchargelevel")) {
            tick.crossbowChargeLevel = jsonObject.get("crossbowchargelevel").getAsInt();
        }

        if (jsonObject.has("hand")) {
            tick.hand = NMSUtils.getItemStackFromNBTJson(jsonObject.get("hand").getAsString());
        }
        if (jsonObject.has("offhand")) {
            tick.offHand = NMSUtils.getItemStackFromNBTJson(jsonObject.get("offhand").getAsString());
        }
        if (jsonObject.has("helmet")) {
            tick.helmet = NMSUtils.getItemStackFromNBTJson(jsonObject.get("helmet").getAsString());
        }
        if (jsonObject.has("chestplate")) {
            tick.chestplate = NMSUtils.getItemStackFromNBTJson(jsonObject.get("chestplate").getAsString());
        }
        if (jsonObject.has("leggings")) {
            tick.leggings = NMSUtils.getItemStackFromNBTJson(jsonObject.get("leggings").getAsString());
        }
        if (jsonObject.has("boots")) {
            tick.boots = NMSUtils.getItemStackFromNBTJson(jsonObject.get("boots").getAsString());
        }

        return (PlayerRecordTick) super.fromJson(tick, jsonObject);
    }

    public PlayerRecordTick fromJson(JsonObject jsonObject) {
        return fromJson(new PlayerRecordTick(), jsonObject);
    }

}
