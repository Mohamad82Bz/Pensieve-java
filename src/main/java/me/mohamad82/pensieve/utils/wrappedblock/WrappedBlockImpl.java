package me.mohamad82.pensieve.utils.wrappedblock;

import com.google.gson.JsonObject;
import me.mohamad82.ruom.Ruom;
import me.mohamad82.ruom.utils.ServerVersion;
import me.mohamad82.ruom.xseries.XBlock;
import me.mohamad82.ruom.xseries.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.Nullable;

public abstract class WrappedBlockImpl implements WrappedBlock {

    private final static boolean ISFLAT = ServerVersion.supports(13);

    private final XMaterial material;
    private final BlockFace blockFace;

    @Nullable
    private final String modernData;
    private final byte legacyData;

    protected WrappedBlockImpl(Block block) {
        this.material = XMaterial.matchXMaterial(block.getType());
        this.blockFace = XBlock.getDirection(block);
        if (ISFLAT) {
            this.modernData = block.getBlockData().getAsString();
        } else {
            this.modernData = null;
        }
        this.legacyData = block.getData();
    }

    protected WrappedBlockImpl(XMaterial material, BlockFace blockFace, @Nullable String modernData, byte legacyData) {
        this.material = material;
        this.blockFace = blockFace;
        this.modernData = modernData;
        this.legacyData = legacyData;
    }

    @Override
    public XMaterial getMaterial() {
        return material;
    }

    @Nullable
    @Override
    public String getModernData() {
        return modernData;
    }

    @Override
    public byte getLegacyData() {
        return legacyData;
    }

    @Override
    public BlockFace getBlockFace() {
        return blockFace;
    }

    @Override
    public void placeBlock(Location location, boolean applyPhysics) {
        if (ISFLAT) {
            location.getBlock().setBlockData(Bukkit.createBlockData(modernData));
        } else {
            XBlock.setType(location.getBlock(), material, applyPhysics);
            Ruom.run(() -> location.getBlock().getClass().getMethod("setData", byte.class).invoke(location.getBlock(), legacyData));
        }
    }

    @Override
    public void breakBlock(Location location, boolean applyPhysics) {
        XMaterial type = XMaterial.matchXMaterial(location.getBlock().getType());
        if (WrappedBlockUtils.DOORS.contains(type)) {
            if (WrappedBlockUtils.isTopPart(location.getBlock())) return;
        } else if (WrappedBlockUtils.BEDS.contains(type)) {
            if (!WrappedBlockUtils.isTopPart(location.getBlock())) return;
        }
        XBlock.setType(location.getBlock(), XMaterial.AIR, applyPhysics);
    }

    @Override
    public JsonObject toJson(JsonObject json) {
        json.addProperty("material", material.name());
        json.addProperty("face", blockFace.toString().toLowerCase());
        if (ISFLAT) {
            json.addProperty("modern_data", modernData);
        }
        json.addProperty("legacy_data", legacyData);

        return json;
    }

}
