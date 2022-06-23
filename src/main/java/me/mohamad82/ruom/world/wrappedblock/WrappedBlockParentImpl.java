package me.mohamad82.ruom.world.wrappedblock;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.mohamad82.ruom.xseries.XMaterial;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class WrappedBlockParentImpl extends WrappedBlockImpl implements WrappedBlockParent {

    private final Collection<WrappedBlockChild> children;

    protected WrappedBlockParentImpl(Block block, Collection<WrappedBlockChild> children) {
        super(block);
        this.children = children;
    }

    protected WrappedBlockParentImpl(XMaterial material, BlockFace blockFace, @Nullable String modernData, byte legacyData, Collection<WrappedBlockChild> children) {
        super(material, blockFace, modernData, legacyData);
        this.children = children;
    }

    @Override
    public void placeBlock(Location location, boolean applyPhysics) {
        super.placeBlock(location, applyPhysics);
        children.forEach(child -> child.placeBlock(location, applyPhysics));
    }

    @Override
    public void breakBlock(Location location, boolean applyPhysics) {
        super.breakBlock(location, applyPhysics);
        children.forEach(child -> child.breakBlock(location, applyPhysics));
    }

    @Override
    public boolean hasChildren() {
        return !children.isEmpty();
    }

    @Override
    public Collection<WrappedBlockChild> getChildren() {
        return children;
    }

    @Override
    public JsonObject toJson(JsonObject json) {
        if (hasChildren()) {
            JsonArray childrenJson = new JsonArray();
            for (WrappedBlockChild child : children) {
                childrenJson.add(child.toJson(new JsonObject()));
            }
            json.add("children", childrenJson);
        }
        return super.toJson(json);
    }

}
