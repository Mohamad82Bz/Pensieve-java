package me.mohamad82.pensieve.nms.npc.entity;

import me.Mohamad82.RUoM.Ruom;
import me.mohamad82.pensieve.nms.EntityMetadata;
import me.mohamad82.pensieve.nms.NMSUtils;
import me.mohamad82.pensieve.nms.PacketUtils;
import me.mohamad82.pensieve.nms.accessors.ItemEntityAccessor;
import me.mohamad82.pensieve.nms.npc.EntityNPC;
import me.mohamad82.pensieve.nms.npc.NPCType;
import me.mohamad82.pensieve.nms.npc.PlayerNPC;
import me.mohamad82.pensieve.utils.Utils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ItemNPC extends EntityNPC {

    private ItemNPC(Location location, ItemStack item) throws Exception {
        super(
                ItemEntityAccessor.getConstructor0().newInstance(NMSUtils.getServerLevel(location.getWorld()), location.getX(), location.getY(), location.getZ(), NMSUtils.getNmsItemStack(item)),
                location,
                NPCType.ITEM
        );
    }

    public static ItemNPC itemNPC(Location location, ItemStack item) {
        try {
            return new ItemNPC(location, item);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setItem(ItemStack item) {
        Utils.ignoreExcRun(() -> ItemEntityAccessor.getMethodSetItem1().invoke(entity, NMSUtils.getNmsItemStack(item)));
        sendEntityData();
    }

    public ItemStack getItem() {
        try {
            return NMSUtils.getBukkitItemStack(ItemEntityAccessor.getMethodGetItem1().invoke(entity));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setAmount(int amount) {
        ItemStack fixedAmountItem = getItem();
        fixedAmountItem.setAmount(amount);
        setItem(fixedAmountItem);
    }

    public int getAmount() {
        return getItem().getAmount();
    }

    public void collect(int collectorEntityId) {
        super.collect(id, collectorEntityId, getAmount());
    }

}
