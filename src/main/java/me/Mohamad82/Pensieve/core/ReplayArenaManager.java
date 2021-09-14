package me.Mohamad82.Pensieve.core;

import me.Mohamad82.Pensieve.Main;
import me.Mohamad82.RUoM.configuration.YamlConfig;
import me.Mohamad82.RUoM.translators.ItemReader;
import me.Mohamad82.RUoM.vector.Vector3;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ReplayArenaManager {

    private final YamlConfig arenas;
    public static ItemStack wand;

    private static ReplayArenaManager instance;
    public static ReplayArenaManager getInstance() {
        return instance;
    }

    public ReplayArenaManager() {
        instance = this;
        arenas = new YamlConfig(Main.getInstance(), Main.getInstance().getDataFolder(), "replay-arenas.yml", false);
        wand = new ItemReader().toItemStack("STONE_AXE ; Name:&d• &5Pensieve &6Wand" +
                " ; Lore:&r|&d• &bLeft Click: &7First Position|&d• &bRight Click: &7Second Position|&r ; ItemFlag:HIDE_ATTRIBUTES" +
                " ; ItemFlag:HIDE_UNBREAKABLE ; UNBREAKABLE");
    }

    public void createReplayArena(String arenaName, Vector3 pos1, Vector3 pos2) {
        ConfigurationSection arenasSection = arenas.getConfig().getConfigurationSection("arenas");
        if (arenasSection == null) arenasSection = arenas.getConfig().createSection("arenas");

        ConfigurationSection arenaSection = arenasSection.createSection(arenaName);
        arenaSection.set("first_position", pos1.toString());
        arenaSection.set("second_position", pos2.toString());

        arenas.saveConfig();
    }

    public boolean arenaExists(String name) {
        ConfigurationSection arenasSection = arenas.getConfig().getConfigurationSection("arenas");
        if (arenasSection == null) return false;
        return arenasSection.contains(name);
    }

    public boolean giveWand(Player player) {
        if (player.getInventory().firstEmpty() == -1) return false;
        int heldSlot = player.getInventory().getHeldItemSlot();
        ItemStack heldItem = player.getInventory().getItem(heldSlot);
        player.getInventory().setItem(heldSlot, wand);
        if (heldItem != null)
            player.getInventory().addItem(heldItem);
        return true;
    }

}
