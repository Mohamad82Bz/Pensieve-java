package me.mohamad82.pensieve.utils.wrappedblock;

import com.google.gson.JsonObject;
import me.mohamad82.ruom.math.vector.Vector3;
import me.mohamad82.ruom.xseries.XMaterial;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.Nullable;

public class WrappedBlockChildImpl extends WrappedBlockImpl implements WrappedBlockChild {

    private final Vector3 offset;

    protected WrappedBlockChildImpl(Block block, Vector3 offset) {
        super(block);
        this.offset = offset;
    }

    protected WrappedBlockChildImpl(XMaterial material, BlockFace blockFace, @Nullable String modernData, byte legacyData, Vector3 offset) {
        super(material, blockFace, modernData, legacyData);
        this.offset = offset;
    }

    @Override
    public void placeBlock(Location location, boolean applyPhysics) {
        super.placeBlock(location.add(offset.getBlockX(), offset.getBlockY(), offset.getBlockZ()), applyPhysics);
    }

    @Override
    public void breakBlock(Location location, boolean applyPhysics) {
        super.breakBlock(location.add(offset.getBlockX(), offset.getBlockY(), offset.getBlockZ()), applyPhysics);
    }

    @Override
    public Vector3 getOffset() {
        return offset;
    }

    @Override
    public JsonObject toJson(JsonObject json) {
        json.addProperty("offset", offset.toString());
        return super.toJson(json);
    }

}
