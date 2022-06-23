package me.mohamad82.pensieve.recording.enums;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

public enum ActionType {
    SWING(1),
    EAT_FOOD(2),
    THROW_PROJECTILE(4),
    THROW_TRIDENT(8),
    THROW_FIREWORK(16),
    THROW_FISHING_ROD(32),
    RETRIEVE_FISHING_ROD(64),
    DRAW_CROSSBOW(128),
    SHOOT_CROSSBOW(256),
    OPEN_CHEST_INTERACTION(512),
    BURN(1024),
    CROUCH(2048),
    SPRINT(4096),
    SWIM(8192),
    GLOW(16384),
    GLIDE(32768),
    INVISIBLE(65536);

    private final int bitMask;

    private ActionType(int bitMask) {
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
