package me.mohamad82.pensieve.utils.wrappedblock;

import java.util.Collection;

public interface WrappedBlockParent extends WrappedBlock {

    boolean hasChildren();

    Collection<WrappedBlockChild> getChildren();

}
