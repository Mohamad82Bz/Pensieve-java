package me.Mohamad82.Pensieve.utils;

import me.Mohamad82.RUoM.ServerVersion;
import me.Mohamad82.RUoM.StringUtils;
import me.Mohamad82.RUoM.XSeries.ReflectionUtils;
import org.bukkit.Material;
import org.bukkit.Sound;

import java.lang.reflect.Field;

public class BlockSoundUtils {

    private static Class<?> CRAFT_BLOCKDATA, IBLOCKDATA;

    static {
        try {
            CRAFT_BLOCKDATA = ReflectionUtils.getCraftClass("block.data.CraftBlockData");
            IBLOCKDATA = ReflectionUtils.getNMSClass("world.level.block.state", "IBlockData");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Sound getBlockSound(SoundType type, Material blockMaterial) {
        try {
            Object nmsBlock = getNmsBlock(blockMaterial);
            Object nmsIBlockData = getNmsIBlockData(blockMaterial);

            if (ServerVersion.supports(9)) {
                Object soundEffectType = nmsBlock.getClass().getMethod("getStepSound", IBLOCKDATA).invoke(nmsBlock, nmsIBlockData);
                Object soundEffect = getBlockSoundEffect(type, soundEffectType);
                Object minecraftKey = getSoundMinecraftKey(soundEffect);

                return Sound.valueOf(((String) minecraftKey.getClass()
                        .getMethod("getKey").invoke(minecraftKey)).replace(".", "_").toUpperCase());
            } else {
                Class<?> STEP_SOUND = ReflectionUtils.getNMSClass("Block$StepSound");
                return Sound.valueOf(((String) STEP_SOUND.getMethod("get" + StringUtils.capitalize(type.toString()) + "Sound")
                        .invoke(nmsBlock)).replace(".", "_").toUpperCase());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Object getSoundMinecraftKey(Object soundEffect) throws Exception {
        Field minecraftKeyField;
        String minecraftKeyMethod;

        switch (ReflectionUtils.VER) {
            case 12:
            case 16:
            case 17:
                minecraftKeyMethod = "b";
                break;
            case 13:
            case 14:
            case 15:
                minecraftKeyMethod = "a";
                break;
            default:
                return null;
        }

        minecraftKeyField = soundEffect.getClass().getDeclaredField(minecraftKeyMethod);
        minecraftKeyField.setAccessible(true);

        return minecraftKeyField.get(soundEffect);
    }

    private static Object getBlockSoundEffect(SoundType type, Object soundEffectType) throws Exception {
        Field soundEffectField;
        String soundEffectMethod = "";

        switch (type) {
            case BREAK:
                switch (ReflectionUtils.VER) {
                    case 17:
                        soundEffectMethod = "aA";
                        break;
                    case 16:
                        soundEffectMethod = "breakSound";
                        break;
                    case 15:
                        soundEffectMethod = "z";
                        break;
                    case 14:
                        soundEffectMethod = "y";
                        break;
                    case 13:
                        soundEffectMethod = "q";
                        break;
                    case 12:
                        soundEffectMethod = "o";
                        break;
                    default:
                        return null;
                }
                break;
            case PLACE:
                switch (ReflectionUtils.VER) {
                    case 17:
                        soundEffectMethod = "aC";
                        break;
                    case 16:
                        soundEffectMethod = "placeSound";
                        break;
                    case 15:
                        soundEffectMethod = "B";
                        break;
                    case 14:
                        soundEffectMethod = "A";
                        break;
                    case 13:
                        soundEffectMethod = "s";
                        break;
                    case 12:
                        soundEffectMethod = "q";
                        break;
                    default:
                        return null;
                }
                break;
            case HIT:
                switch (ReflectionUtils.VER) {
                    case 17:
                        soundEffectMethod = "aD";
                        break;
                    case 16:
                        soundEffectMethod = "hitSound";
                        break;
                    case 15:
                        soundEffectMethod = "C";
                        break;
                    case 14:
                        soundEffectMethod = "B";
                        break;
                    case 13:
                        soundEffectMethod = "t";
                        break;
                    case 12:
                        soundEffectMethod = "r";
                        break;
                    default:
                        return null;
                }
                break;
            case STEP:
                switch (ReflectionUtils.VER) {
                    case 17:
                        soundEffectMethod = "aB";
                        break;
                    case 16:
                        soundEffectMethod = "stepSound";
                        break;
                    case 15:
                        soundEffectMethod = "A";
                        break;
                    case 14:
                        soundEffectMethod = "z";
                        break;
                    case 13:
                        soundEffectMethod = "r";
                        break;
                    case 12:
                        soundEffectMethod = "p";
                        break;
                    default:
                        return null;
                }
                break;
            case FALL:
                switch (ReflectionUtils.VER) {
                    case 17:
                        soundEffectMethod = "aE";
                        break;
                    case 16:
                        soundEffectMethod = "placeSound";
                        break;
                    case 15:
                        soundEffectMethod = "D";
                        break;
                    case 14:
                        soundEffectMethod = "C";
                        break;
                    case 13:
                        soundEffectMethod = "u";
                        break;
                    case 12:
                        soundEffectMethod = "s";
                        break;
                    default:
                        return null;
                }
                break;
        }
        
        soundEffectField = soundEffectType.getClass().getField(soundEffectMethod);
        if (!ReflectionUtils.supports(16))
            soundEffectField.setAccessible(true);

        return soundEffectField.get(soundEffectType);
    }

    public static Object getNmsBlock(Material material) {
        try {
            Object iBlockData = getNmsIBlockData(material);

            return iBlockData.getClass().getMethod("getBlock").invoke(iBlockData);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Object getNmsIBlockData(Material material) {
        try {
            Object craftBlockData = CRAFT_BLOCKDATA.getMethod("newData", Material.class, String.class)
                    .invoke(null, material, null);

            return CRAFT_BLOCKDATA.getMethod("getState").invoke(craftBlockData);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public enum SoundType {
        PLACE,
        BREAK,
        HIT,
        STEP,
        FALL
    }

}
