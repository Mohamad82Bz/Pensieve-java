package me.Mohamad82.Pensieve.nms.enums;

import me.Mohamad82.RUoM.utils.ServerVersion;

public class EntityMetadata {

    public static int getPotionMetadataId() { //TODO NMS LOWER VER TESTINGS...
        if (ServerVersion.supports(17)) {
            return 8;
        } else {
            return 7;
        }
    }

    public enum EntityStatus {
        NORMAL(0),
        BURNING(0x01),
        CROUCHING(0x02),
        RIDING(0x04),
        SPRINTING(0x08),
        SWIMMING(0x10),
        INVISIBLE(0x20),
        GLOWING(0x40),
        GLIDING(0x80);

        private final byte bitMask;

        EntityStatus(int bitMask) {
            this.bitMask = (byte) bitMask;
        }

        public byte getBitMask() {
            return bitMask;
        }

        public static byte getBitMasks(EntityStatus... entityStatuses) {
            byte bytes = 0;
            for (EntityStatus entityStatus : entityStatuses) {
                bytes += entityStatus.getBitMask();
            }
            return bytes;
        }

        //TODO NMS LOWER VERSION TESTINGS, 1.8 - 1.15
        public static int getMetadataId() {
            return 0;
        }
    }

    public enum PlayerSkin {
        CAPE(0x01),
        JACKET(0x02),
        LEFT_SLEEVE(0x04),
        RIGHT_SLEEVE(0x08),
        LEFT_PANTS(0x10),
        RIGHT_PANTS(0x20),
        HAT(0x40);

        private final byte bitMask;

        PlayerSkin(int bitMask) {
            this.bitMask = (byte) bitMask;
        }

        public byte getBitMask() {
            return bitMask;
        }

        public static byte getBitMasks(PlayerSkin... playerSkins) {
            byte bytes = 0;
            for (PlayerSkin playerSkin : playerSkins) {
                bytes += playerSkin.getBitMask();
            }
            return bytes;
        }

        public static byte getAllBitMasks() {
            byte bytes = 0;
            for (PlayerSkin playerSkin : PlayerSkin.values()) {
                bytes += playerSkin.getBitMask();
            }
            return bytes;
        }

        //TODO NMS LOWER VERSION TESTINGS, 1.8 - 1.15
        public static int getMetadataId() {
            if (ServerVersion.supports(17))
                return 17;
            else
                return 16;
        }
    }

    public enum ItemUseKey {
        RELEASE(0),
        HOLD(1),
        OFFHAND_RELEASE(2),
        OFFHAND_HOLD(3);

        private final byte bitMask;

        ItemUseKey(int bitMask) {
            this.bitMask = (byte) bitMask;
        }

        public byte getBitMask() {
            return bitMask;
        }

        //TODO NMS LOWER VERSION TESTINGS, 1.8 - 1.17 except 1.16
        public static int getMetadataId() {
            return 7;
        }
    }

}
