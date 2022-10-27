package me.mohamad82.pensieve.replaying;

import me.mohamad82.ruom.math.vector.Vector3;

import java.util.Map;

public class TickAdditions {

    private boolean swing;
    private boolean foodEatParticle;
    private boolean foodEatSound;
    private boolean drinking;
    private Vector3 blockBreakParticle;
    private Map.Entry<Vector3, Integer> blockDestructionStage;

    public TickAdditions() {

    }

    public void swing() {
        swing = true;
    }

    public boolean didSwing() {
        return swing;
    }

    public boolean isSwing() {
        return swing;
    }

    public void setSwing(boolean swing) {
        this.swing = swing;
    }

    public boolean isFoodEatParticle() {
        return foodEatParticle;
    }

    public void setFoodEatParticle(boolean foodEatParticle) {
        this.foodEatParticle = foodEatParticle;
    }

    public boolean isFoodEatSound() {
        return foodEatSound;
    }

    public void setFoodEatSound(boolean foodEatSound) {
        this.foodEatSound = foodEatSound;
    }

    public boolean isDrinking() {
        return drinking;
    }

    public void setDrinking(boolean drinking) {
        this.drinking = drinking;
    }

    public Vector3 getBlockBreakParticle() {
        return blockBreakParticle;
    }

    public void setBlockBreakParticle(Vector3 blockBreakParticle) {
        this.blockBreakParticle = blockBreakParticle;
    }

    public Map.Entry<Vector3, Integer> getBlockDestructionStage() {
        return blockDestructionStage;
    }

    public void setBlockDestructionStage(Map.Entry<Vector3, Integer> blockDestructionStage) {
        this.blockDestructionStage = blockDestructionStage;
    }

}
