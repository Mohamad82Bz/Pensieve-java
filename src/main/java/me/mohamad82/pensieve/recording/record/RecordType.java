package me.mohamad82.pensieve.recording.record;

import java.lang.reflect.Constructor;

public enum RecordType {
    AREA_EFFECT_CLOUD(AreaEffectCloudRecord.class, AreaEffectCloudRecordTick.class),
    ARROW(ArrowRecord.class, ArrowRecordTick.class),
    DROPPED_ITEM(DroppedItemRecord.class, DroppedItemRecordTick.class),
    FIREWORK(FireworkRecord.class, FireworkRecordTick.class),
    FISHING_HOOK(FishingHookRecord.class, FishingHookRecordTick.class),
    PLAYER(PlayerRecord.class, PlayerRecordTick.class),
    PROJECTILE(ProjectileRecord.class, ProjectileRecordTick.class),
    TRIDENT(TridentRecord.class, TridentRecordTick.class),

    RAW(RawRecord.class, RawRecordTick.class);

    private final Class<?> recordClass;
    private final Class<?> recordTickClass;

    RecordType(Class<?> recordClass, Class<?> recordTickClass) {
        this.recordClass = recordClass;
        this.recordTickClass = recordTickClass;
    }

    public Record createEmptyObject() {
        try {
            Constructor<?> constructor = recordClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            return (Record) constructor.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Class<?> getRecordClass() {
        return recordClass;
    }

    public Class<?> getRecordTickClass() {
        return recordTickClass;
    }

}
