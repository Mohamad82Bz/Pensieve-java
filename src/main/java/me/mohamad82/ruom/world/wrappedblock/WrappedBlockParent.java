package me.mohamad82.ruom.world.wrappedblock;

import java.util.Collection;

public interface WrappedBlockParent extends WrappedBlock {

    boolean hasChildren();

    Collection<WrappedBlockChild> getChildren();

}
