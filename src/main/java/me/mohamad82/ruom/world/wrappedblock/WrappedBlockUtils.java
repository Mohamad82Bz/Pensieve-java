package me.mohamad82.ruom.world.wrappedblock;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.mohamad82.ruom.math.vector.Vector3;
import me.mohamad82.ruom.math.vector.Vector3Utils;
import me.mohamad82.ruom.math.vector.Vector3UtilsBukkit;
import me.mohamad82.ruom.utils.ListUtils;
import me.mohamad82.ruom.utils.LocUtils;
import me.mohamad82.ruom.utils.ServerVersion;
import me.mohamad82.ruom.xseries.XMaterial;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Bed;
import org.bukkit.block.data.type.Door;

import java.util.ArrayList;
import java.util.List;

public class WrappedBlockUtils {

    public final static List<XMaterial> DOORS = ListUtils.toList(XMaterial.OAK_DOOR, XMaterial.ACACIA_DOOR, XMaterial.BIRCH_DOOR, XMaterial.DARK_OAK_DOOR, XMaterial.IRON_DOOR, XMaterial.JUNGLE_DOOR, XMaterial.SPRUCE_DOOR, XMaterial.CRIMSON_DOOR, XMaterial.WARPED_DOOR);
    public final static List<XMaterial> BEDS = ListUtils.toList(XMaterial.BLACK_BED, XMaterial.BLUE_BED, XMaterial.BROWN_BED, XMaterial.CYAN_BED, XMaterial.GRAY_BED, XMaterial.GREEN_BED, XMaterial.LIGHT_BLUE_BED, XMaterial.LIGHT_GRAY_BED, XMaterial.LIME_BED, XMaterial.MAGENTA_BED, XMaterial.ORANGE_BED, XMaterial.PINK_BED, XMaterial.PURPLE_BED, XMaterial.RED_BED, XMaterial.WHITE_BED, XMaterial.YELLOW_BED);
    private final static boolean ISFLAT = ServerVersion.supports(13);

    public static WrappedBlock create(Block block) {
        WrappedBlock wrappedBlock;
        if (DOORS.contains(XMaterial.matchXMaterial(block.getType()))) {
            if (ISFLAT) {
                Door door = (Door) block.getBlockData();
                Block otherBlock = door.getHalf() == Door.Half.TOP ? block.getRelative(BlockFace.DOWN) : block.getRelative(BlockFace.UP);
                wrappedBlock = new WrappedBlockParentImpl(
                        block,
                        ListUtils.toList(new WrappedBlockChildImpl(otherBlock, Vector3UtilsBukkit.toVector3(LocUtils.getTravelDistance(block.getLocation(), otherBlock.getLocation()))))
                );
            } else {
                org.bukkit.material.Door door = (org.bukkit.material.Door) block.getState().getData();
                Block otherBlock = door.isTopHalf() ? block.getRelative(BlockFace.DOWN) : block.getRelative(BlockFace.UP);
                wrappedBlock = new WrappedBlockParentImpl(
                        block,
                        ListUtils.toList(new WrappedBlockChildImpl(otherBlock, door.isTopHalf() ? Vector3.at(0, -1, 0) : Vector3.at(0, 1, 0)))
                );
            }
        } else if (BEDS.contains(XMaterial.matchXMaterial(block.getType()))) {
            Block otherBlock = getOtherBedPart(block);
            wrappedBlock = new WrappedBlockParentImpl(
                    block,
                    ListUtils.toList(new WrappedBlockChildImpl(otherBlock, Vector3UtilsBukkit.toVector3(LocUtils.getTravelDistance(block.getLocation(), otherBlock.getLocation()))))
            );
        } else {
            wrappedBlock = new WrappedBlockParentImpl(
                    block,
                    new ArrayList<>()
            );
        }
        return wrappedBlock;
    }

    public static Block getOtherBedPart(Block block) {
        Block otherBlock;
        if (ISFLAT) {
            Bed bed = (Bed) block.getBlockData();
            boolean isHeadPart = bed.getPart() == Bed.Part.HEAD;
            switch (bed.getFacing()) {
                case EAST: {
                    otherBlock = isHeadPart ? block.getRelative(BlockFace.WEST) : block.getRelative(BlockFace.EAST);
                    break;
                }
                case WEST: {
                    otherBlock = isHeadPart ? block.getRelative(BlockFace.EAST) : block.getRelative(BlockFace.WEST);
                    break;
                }
                case NORTH: {
                    otherBlock = isHeadPart ? block.getRelative(BlockFace.SOUTH) : block.getRelative(BlockFace.NORTH);
                    break;
                }
                case SOUTH: {
                    otherBlock = isHeadPart ? block.getRelative(BlockFace.NORTH) : block.getRelative(BlockFace.SOUTH);
                    break;
                }
                default: {
                    otherBlock = null;
                }
            }
        } else {
            org.bukkit.material.Bed bed = (org.bukkit.material.Bed) block.getState().getData();
            boolean isHeadPart = bed.isHeadOfBed();
            switch (bed.getFacing()) {
                case NORTH: {
                    otherBlock = isHeadPart ? block.getRelative(BlockFace.EAST) : block.getRelative(BlockFace.WEST);
                    break;
                }
                case SOUTH: {
                    otherBlock = isHeadPart ? block.getRelative(BlockFace.WEST) : block.getRelative(BlockFace.EAST);
                    break;
                }
                case WEST: {
                    otherBlock = isHeadPart ? block.getRelative(BlockFace.SOUTH) : block.getRelative(BlockFace.NORTH);
                    break;
                }
                case EAST: {
                    otherBlock = isHeadPart ? block.getRelative(BlockFace.NORTH) : block.getRelative(BlockFace.SOUTH);
                    break;
                }
                default: {
                    otherBlock = null;
                }
            }
        }
        return otherBlock;
    }

    public static boolean isDoubleBlock(Block block) {
        return DOORS.contains(XMaterial.matchXMaterial(block.getType())) || BEDS.contains(XMaterial.matchXMaterial(block.getType()));
    }

    public static boolean isTopPart(Block block) {
        if (DOORS.contains(XMaterial.matchXMaterial(block.getType()))) {
            if (ISFLAT) {
                Door door = (Door) block.getBlockData();
                return door.getHalf() == Door.Half.TOP;
            } else {
                org.bukkit.material.Door door = (org.bukkit.material.Door) block.getState().getData();
                return door.isTopHalf();
            }
        } else if (BEDS.contains(XMaterial.matchXMaterial(block.getType()))) {
            if (ISFLAT) {
                Bed bed = (Bed) block.getBlockData();
                return bed.getPart() == Bed.Part.HEAD;
            } else {
                org.bukkit.material.Bed bed = (org.bukkit.material.Bed) block.getState().getData();
                return bed.isHeadOfBed();
            }
        } else {
            return false;
        }
    }

    public static WrappedBlock fromJson(JsonObject json) {
        List<WrappedBlockChild> children = new ArrayList<>();
        if (json.has("children")) {
            JsonArray childrenJson = json.get("children").getAsJsonArray();
            for (JsonElement childJson : childrenJson) {
                JsonObject child = childJson.getAsJsonObject();
                children.add(new WrappedBlockChildImpl(
                        XMaterial.matchXMaterial(child.get("material").getAsString()).get(),
                        BlockFace.valueOf(child.get("face").getAsString().toUpperCase()),
                        child.has("modern_data") ? child.get("modern_data").getAsString() : null,
                        child.get("legacy_data").getAsByte(),
                        Vector3Utils.toVector3(child.get("offset").getAsString())
                ));
            }
        }
        return new WrappedBlockParentImpl(
                XMaterial.matchXMaterial(json.get("material").getAsString()).get(),
                BlockFace.valueOf(json.get("face").getAsString().toUpperCase()),
                json.has("modern_data") ? json.get("modern_data").getAsString() : null,
                json.get("legacy_data").getAsByte(),
                children
        );
    }

}
