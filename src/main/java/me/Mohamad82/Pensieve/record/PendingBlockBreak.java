package me.Mohamad82.Pensieve.record;

import me.Mohamad82.Pensieve.nms.NMSProvider;
import me.Mohamad82.Pensieve.nms.enums.BlockDirection;
import me.Mohamad82.RUoM.vector.Vector3;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.*;

public class PendingBlockBreak implements Cloneable {

    private UUID uuid;
    private Vector3 location;
    private Material material;
    private BlockDirection blockDirection;
    private List<Integer> animationStages;

    public PendingBlockBreak() {
        this.uuid = UUID.randomUUID();
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public Vector3 getLocation() {
        return location;
    }

    public void setLocation(Vector3 location) {
        this.location = location;
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public BlockDirection getBlockDirection() {
        return blockDirection;
    }

    public void setBlockDirection(BlockDirection blockDirection) {
        this.blockDirection = blockDirection;
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
        Location centerBlock = new Location(world, location.getCenterX(), location.getCenterY() + 0.5, location.getCenterZ());
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
        Random random = new Random();


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

            centerBlock.getWorld().spawnParticle(Particle.BLOCK_DUST,
                    clone, 1, material.createBlockData());
        }
    }

    public void animateBlockBreak(Set<Player> players, int stage, Vector3 location) {
        if (animationStages == null || animationStages.isEmpty()) return;

        if (stage >= animationStages.size())
            NMSProvider.sendBlockBreakAnimation(players, location, 9);
        else
            NMSProvider.sendBlockBreakAnimation(players, location, animationStages.get(stage));
    }

    public PendingBlockBreak clone() {
        try {
            return (PendingBlockBreak) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new Error(e);
        }
    }

}
