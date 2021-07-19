package me.Mohamad82.Pensieve.core;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class AreaSelection extends me.Mohamad82.RUoM.AreaSelection.AreaSelection {

    public AreaSelection(Player player, ItemStack wand) {
        super(player, wand);
    }

    @Override
    public void onFirstPos(Player player, boolean updated) {
        //TODO MESSAGE
        if (updated)
            player.sendMessage("First pos set.");
    }

    @Override
    public void onSecondPos(Player player, boolean updated) {
        //TODO MESSAGE
        if (updated)
            player.sendMessage("Second pos set.");
    }

    @Override
    public void onFirstPosNotify(Player player) {

    }

    @Override
    public void onSecondPosNotify(Player player) {

    }

}
