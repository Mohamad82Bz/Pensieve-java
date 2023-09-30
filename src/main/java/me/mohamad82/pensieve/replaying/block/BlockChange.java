package me.mohamad82.pensieve.replaying.block;

import me.mohamad82.pensieve.utils.wrappedblock.WrappedBlock;
import org.bukkit.Location;

public class BlockChange implements BlockModification {

    private final Location location;
    private final WrappedBlock beforeBlockData;
    private final WrappedBlock afterBlockData;
    private final BlockModificationType changeType;

    private BlockChange(Location location, WrappedBlock beforeBlockData, WrappedBlock afterBlockData, BlockModificationType changeType) {
        this.location = location.clone();
        this.beforeBlockData = beforeBlockData;
        this.afterBlockData = afterBlockData;
        this.changeType = changeType;
    }

    public static BlockChange breakChange(Location location, WrappedBlock beforeBlockData) {
        return new BlockChange(location, beforeBlockData, null, BlockModificationType.BREAK);
    }

    public static BlockChange placeChange(Location location, WrappedBlock afterBlockData) {
        return new BlockChange(location, null, afterBlockData, BlockModificationType.PLACE);
    }

    public Location getLocation() {
        return location;
    }

    public WrappedBlock getBeforeBlockData() {
        return beforeBlockData;
    }

    public WrappedBlock getAfterBlockData() {
        return afterBlockData;
    }

    public BlockModificationType getType() {
        return changeType;
    }

    public void apply() {
        if (beforeBlockData != null) {
            beforeBlockData.breakBlock(location, false);
        }

        if (afterBlockData != null) {
            afterBlockData.placeBlock(location, false);
        }
    }

    public void rollback() {
        if (afterBlockData != null) {
            afterBlockData.breakBlock(location, false);
        }
        if (beforeBlockData != null) {
            beforeBlockData.placeBlock(location, false);
        }
    }

}
