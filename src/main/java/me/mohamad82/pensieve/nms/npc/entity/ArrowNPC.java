package me.mohamad82.pensieve.nms.npc.entity;

import me.Mohamad82.RUoM.vector.Vector3;
import me.mohamad82.pensieve.nms.NMSUtils;
import me.mohamad82.pensieve.nms.accessors.ArrowAccessor;
import me.mohamad82.pensieve.nms.accessors.EntityAccessor;
import me.mohamad82.pensieve.nms.npc.EntityNPC;
import me.mohamad82.pensieve.nms.npc.NPCType;
import me.mohamad82.pensieve.utils.Utils;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

public class ArrowNPC extends EntityNPC {

    private ArrowNPC(Location location) throws Exception {
        super(
                ArrowAccessor.getConstructor0().newInstance(NPCType.ARROW.getNmsEntityType(), NMSUtils.getServerLevel(location.getWorld())),
                location,
                NPCType.ARROW
        );
        Utils.ignoreExcRun(() -> EntityAccessor.getMethodSetPos1().invoke(entity, location.getX(), location.getY(), location.getZ()));
    }

    public static ArrowNPC arrowNPC(Location location) {
        try {
            return new ArrowNPC(location);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setEffectsFromItem(ItemStack item) {
        Utils.ignoreExcRun(() -> ArrowAccessor.getMethodSetEffectsFromItem1().invoke(entity, NMSUtils.getNmsItemStack(item)));
        sendEntityData();
    }

    public void makeParticles(Vector3 location) {
        Utils.ignoreExcRun(() -> {
            EntityAccessor.getMethodSetPos1().invoke(entity, location.getX(), location.getY(), location.getZ());
            ArrowAccessor.getMethodMakeParticle1().invoke(entity);
        });
    }

    public void setColor(int color) {
        Utils.ignoreExcRun(() -> ArrowAccessor.getMethodSetFixedColor1().invoke(entity, color));
        sendEntityData();
    }

    public int getColor() {
        try {
            return (int) ArrowAccessor.getMethodGetColor1().invoke(entity);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

}
