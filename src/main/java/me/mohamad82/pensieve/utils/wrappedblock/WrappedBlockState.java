package me.mohamad82.pensieve.utils.wrappedblock;

import org.bukkit.Location;

public class WrappedBlockState {

    private final Location location;
    private final WrappedBlock block;
    private boolean broken;

    protected WrappedBlockState(Location location, WrappedBlock block) {
        this.location = location;
        this.block = block;
    }

    public void breakBlock(boolean applyPhysics) {
        this.broken = true;
        ((WrappedBlockParentImpl) block).breakBlock(location, applyPhysics);
    }

    public boolean isBroken() {
        return broken;
    }

    public boolean containsWrappedBlock(Location location) {
        if (location.equals(this.location)) return true;
        else {
            WrappedBlockParent parentBlock = (WrappedBlockParent) block;
            for (WrappedBlockChild childBlock : parentBlock.getChildren()) {
                if (location.equals(this.location.clone().add(childBlock.getOffset().getBlockX(), childBlock.getOffset().getBlockY(), childBlock.getOffset().getBlockZ()))) {
                    return true;
                }
            }
            return false;
        }
    }

}
