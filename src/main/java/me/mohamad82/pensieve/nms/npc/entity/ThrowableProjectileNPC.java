package me.mohamad82.pensieve.nms.npc.entity;

import me.Mohamad82.RUoM.Ruom;
import me.mohamad82.pensieve.nms.NMSUtils;
import me.mohamad82.pensieve.nms.PacketUtils;
import me.mohamad82.pensieve.nms.accessors.EntityAccessor;
import me.mohamad82.pensieve.nms.accessors.ThrowableItemProjectileAccessor;
import me.mohamad82.pensieve.nms.accessors.ThrownPotionAccessor;
import me.mohamad82.pensieve.nms.npc.EntityNPC;
import me.mohamad82.pensieve.nms.npc.NPCType;
import me.mohamad82.pensieve.utils.Utils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ThrowableProjectileNPC extends EntityNPC {

    private ThrowableProjectileNPC(Location location, ItemStack item) throws Exception {
        super(
                ThrownPotionAccessor.getConstructor0().newInstance(NPCType.POTION.getNmsEntityType(), NMSUtils.getServerLevel(location.getWorld())),
                location,
                NPCType.POTION
        );
        Utils.ignoreExcRun(() -> EntityAccessor.getMethodSetPos1().invoke(entity, location.getX(), location.getY(), location.getZ()));
        setItem(item);
    }

    public static ThrowableProjectileNPC throwableProjectileNPC(Location location, ItemStack item) {
        try {
            return new ThrowableProjectileNPC(location, item);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setItem(ItemStack item) {
        Utils.ignoreExcRun(() -> ThrowableItemProjectileAccessor.getMethodSetItem1().invoke(entity, NMSUtils.getNmsItemStack(item)));
        sendEntityData();
    }

    public ItemStack getItem() {
        try {
            return NMSUtils.getBukkitItemStack(ThrowableItemProjectileAccessor.getMethodGetItemRaw1().invoke(entity));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
