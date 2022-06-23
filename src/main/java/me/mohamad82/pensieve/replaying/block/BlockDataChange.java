package me.mohamad82.pensieve.replaying.block;

import me.mohamad82.ruom.utils.BlockUtils;
import me.mohamad82.ruom.xseries.XMaterial;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;

public class BlockDataChange implements BlockModification {

    public final static BlockData AIR_BLOCKDATA = XMaterial.AIR.parseMaterial().createBlockData();

    private final Location location;
    private final BlockData beforeBlockData;
    private final BlockData afterBlockData;
    private final BlockModificationType changeType;

    private BlockDataChange(Location location, BlockData beforeBlockData, BlockData afterBlockData, BlockModificationType changeType) {
        this.location = location.clone();
        this.beforeBlockData = beforeBlockData.clone();
        this.afterBlockData = afterBlockData.clone();
        this.changeType = changeType;
    }

    public static BlockDataChange blockChange(Location location, BlockData afterBlockData) {
        return new BlockDataChange(location, location.getBlock().getBlockData(), afterBlockData, BlockModificationType.DATA_CHANGE);
    }

    public Location getLocation() {
        return location;
    }

    public BlockData getBeforeBlockData() {
        return beforeBlockData;
    }

    public BlockData getAfterBlockData() {
        return afterBlockData;
    }

    public BlockModificationType getType() {
        return changeType;
    }

    public void apply() {
        if (beforeBlockData.getMaterial().toString().contains("BED") && beforeBlockData.getMaterial() != XMaterial.BEDROCK.parseMaterial()) {
            BlockUtils.breakBed(location);
        } else if (beforeBlockData.getMaterial().toString().contains("_DOOR")) {
            BlockUtils.breakDoor(location);
        }

        if (afterBlockData.getMaterial().toString().contains("BED") && afterBlockData.getMaterial() != XMaterial.BEDROCK.parseMaterial()) {
            BlockUtils.placeBed(location, afterBlockData);
        } else if (afterBlockData.getMaterial().toString().contains("_DOOR")) {
            BlockUtils.placeDoor(location, afterBlockData);
        } else {
            location.getBlock().setBlockData(afterBlockData);
        }
    }

    public void rollback() {
        if (afterBlockData.getMaterial().toString().contains("BED") && afterBlockData.getMaterial() != XMaterial.BEDROCK.parseMaterial()) {
            BlockUtils.breakBed(location);
        } else if (afterBlockData.getMaterial().toString().contains("_DOOR")) {
            BlockUtils.breakDoor(location);
        }

        if (beforeBlockData.getMaterial().toString().contains("BED") && beforeBlockData.getMaterial() != XMaterial.BEDROCK.parseMaterial()) {
            BlockUtils.placeBed(location, beforeBlockData);
        } else if (beforeBlockData.getMaterial().toString().contains("_DOOR")) {
            BlockUtils.placeDoor(location, beforeBlockData);
        } else {
            location.getBlock().setBlockData(beforeBlockData);
        }
    }

}
