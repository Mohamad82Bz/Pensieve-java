package me.mohamad82.pensieve.recording.enums;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

public enum ActionType {
    SWING(0x01),
    EAT_FOOD(0x02),
    THROW_PROJECTILE(0x04),
    THROW_TRIDENT(0x08),
    THROW_FIREWORK(0x10),
    THROW_FISHING_ROD(0x20),
    RETRIEVE_FISHING_ROD(0x40),
    DRAW_CROSSBOW(0x80),
    SHOOT_CROSSBOW(0x100),
    OPEN_CHEST_INTERACTION(0x200),
    BURN(0x400),
    CROUCH(0x800),
    SPRINT(0x1000),
    SWIM(0x2000),
    GLOW(0x4000),
    GLIDE(0x8000),
    INVISIBLE(0x10000);

    private final int bitMask;

    ActionType(int bitMask) {
        this.bitMask = bitMask;
    }

    public int getBitMask() {
        return this.bitMask;
    }

    public static int getBitMasks(ActionType... actionTypes) {
        return getBitMasks(Arrays.asList(actionTypes));
    }

    public static int getBitMasks(Collection<ActionType> actionTypes) {
        int bitMask = 0;
        for (ActionType actionType : actionTypes) {
            bitMask |= actionType.getBitMask();
        }
        return bitMask;
    }

    public static Set<ActionType> getActionTypes(int bitMask) {
        Set<ActionType> actionTypes = EnumSet.noneOf(ActionType.class);
        for (ActionType actionType : values()) {
            if ((bitMask & actionType.getBitMask()) == actionType.getBitMask()) {
                actionTypes.add(actionType);
            }
        }
        return actionTypes;
    }
}
