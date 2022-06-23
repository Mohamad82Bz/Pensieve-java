package me.mohamad82.pensieve.recording;

import com.google.gson.JsonObject;
import me.mohamad82.particle.ParticleBuilder;
import me.mohamad82.particle.ParticleEffect;
import me.mohamad82.particle.data.texture.ItemTexture;
import me.mohamad82.ruom.utils.NMSUtils;
import me.mohamad82.ruom.math.vector.Vector3;
import me.mohamad82.ruom.math.vector.Vector3Utils;
import me.mohamad82.ruom.xseries.XMaterial;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;

public class PendingBlockBreak {

    private UUID uuid;
    private final Vector3 location;
    private final Material material;
    private final BlockDirection blockDirection;
    private final Random random;
    private List<Integer> animationStages;

    @ApiStatus.Internal
    public int timeSpent = 0;

    public PendingBlockBreak(Vector3 location, BlockDirection blockDirection, Material material) {
        this.uuid = UUID.randomUUID();

        this.location = location;
        this.blockDirection = blockDirection;
        this.material = material;
        random = new Random();
    }

    public UUID getUuid() {
        return uuid;
    }

    public Vector3 getLocation() {
        return location;
    }

    public Material getMaterial() {
        return material;
    }

    public BlockDirection getBlockDirection() {
        return blockDirection;
    }

    public List<Integer> getAnimationStages() {
        if (animationStages == null)
            animationStages = new ArrayList<>();
        return animationStages;
    }

    public void setAnimationStages(List<Integer> animationStages) {
        this.animationStages = animationStages;
    }

    public void spawnParticle(World world, Vector3 location) {
        Location centerBlock = new Location(world, location.getCenterX(), location.getCenterY(), location.getCenterZ());
        if (blockDirection.equals(BlockDirection.UP))
            centerBlock.add(0, 0.504, 0);
        else if (blockDirection.equals(BlockDirection.DOWN))
            centerBlock.add(0, -0.504, 0);
        else if (blockDirection.equals(BlockDirection.NORTH))
            centerBlock.add(0, 0, -0.504);
        else if (blockDirection.equals(BlockDirection.SOUTH))
            centerBlock.add(0, 0, 0.504);
        else if (blockDirection.equals(BlockDirection.EAST))
            centerBlock.add(0.504, 0, 0);
        else if (blockDirection.equals(BlockDirection.WEST))
            centerBlock.add(-0.504, 0, 0);

        for (int i = 0; i <= 1; i++) {
            float first = (float) (random.nextInt(8) - 4) / 10;
            float second = (float) (random.nextInt(10) - 5) / 10;
            Location clone = centerBlock.clone();

            if (blockDirection.equals(BlockDirection.UP))
                clone.add(first, 0, second);
            else if (blockDirection.equals(BlockDirection.DOWN))
                clone.add(first, 0, second);
            else if (blockDirection.equals(BlockDirection.NORTH))
                clone.add(first, second, 0);
            else if (blockDirection.equals(BlockDirection.SOUTH))
                clone.add(first, second, 0);
            else if (blockDirection.equals(BlockDirection.EAST))
                clone.add(0, first, second);
            else if (blockDirection.equals(BlockDirection.WEST))
                clone.add(0, first, second);

            new ParticleBuilder(ParticleEffect.BLOCK_DUST, clone).setParticleData(new ItemTexture(new ItemStack(material))).display();
        }
    }

    public void animateBlockBreak(Set<Player> players, int stage, Vector3 location) {
        if (animationStages == null || animationStages.isEmpty()) return;

        if (stage >= animationStages.size()) {
            NMSUtils.sendBlockDestruction(players, location, 9);
        } else {
            NMSUtils.sendBlockDestruction(players, location, animationStages.get(stage));
        }
    }

    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("u", uuid.toString());
        jsonObject.addProperty("l", location.toString());
        jsonObject.addProperty("m", XMaterial.matchXMaterial(material).name());
        jsonObject.addProperty("b", blockDirection.toString());
        if (animationStages != null) {
            StringBuilder stagesStringBuilder = new StringBuilder();
            for (int stage : animationStages) {
                stagesStringBuilder.append(stage).append(",");
            }
            String stages = stagesStringBuilder.substring(0, stagesStringBuilder.length() - 1);
            jsonObject.addProperty("s", stages);
        }

        return jsonObject;
    }

    public static PendingBlockBreak fromJson(JsonObject jsonObject) {
        PendingBlockBreak pendingBlockBreak = new PendingBlockBreak(
                Vector3Utils.toVector3(jsonObject.get("l").getAsString()),
                BlockDirection.valueOf(jsonObject.get("b").getAsString().toUpperCase()),
                XMaterial.matchXMaterial(jsonObject.get("m").getAsString()).get().parseMaterial()
        );
        pendingBlockBreak.uuid = UUID.fromString(jsonObject.get("u").getAsString());
        if (jsonObject.has("s")) {
            String[] stagesSplit = jsonObject.get("s").getAsString().split(",");
            List<Integer> stages = new ArrayList<>();
            for (String stageString : stagesSplit) {
                stages.add(Integer.parseInt(stageString));
            }
            pendingBlockBreak.animationStages = stages;
        }

        return pendingBlockBreak;
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    public enum BlockDirection {
        UP,
        DOWN,
        NORTH,
        SOUTH,
        EAST,
        WEST
    }

}
