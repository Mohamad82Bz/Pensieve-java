package me.mohamad82.ruom.world.wrappedblock;

import com.google.gson.JsonObject;
import me.mohamad82.ruom.xseries.XMaterial;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;

public interface WrappedBlock {

    XMaterial getMaterial();

    String getModernData();

    byte getLegacyData();

    BlockFace getBlockFace();

    void placeBlock(Location location, boolean applyPhysics);

    void breakBlock(Location location, boolean applyPhysics);

    JsonObject toJson(JsonObject json);

    default JsonObject toJson() {
        return toJson(new JsonObject());
    }

}
