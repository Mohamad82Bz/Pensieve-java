package me.Mohamad82.Pensieve.menu;

import me.Mohamad82.Pensieve.Main;
import me.Mohamad82.Pensieve.data.DataManager;
import me.Mohamad82.RUoM.GUI.GUI;
import me.Mohamad82.RUoM.ListUtils;
import me.Mohamad82.RUoM.StringUtils;
import me.Mohamad82.RUoM.Translators.ItemReader;
import me.Mohamad82.RUoM.YamlConfig;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.*;

public class PensieveGUI extends GUI {

    private final Map<Integer, ItemStack> replayItems = new HashMap<>();
    private final List<Integer> replaySlots;

    private final UUID playerUuid;
    private final ItemReader itemReader;

    private int currentPage = 1;
    private int maxPages;

    public PensieveGUI(UUID playerUuid) {
        super(StringUtils.colorize("&5Pensieve"));
        this.playerUuid = playerUuid;
        itemReader = new ItemReader(null, Main.getInstance().getLogger());
        replaySlots = ListUtils.toIntegerList(10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22,
                23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43);

        File folder = DataManager.getInstance().getFile(playerUuid.toString());
        if (!folder.isDirectory()) return;

        int i = 0;
        for (File replayFile : folder.listFiles()) {
            YamlConfig replay = new YamlConfig(Main.getInstance(), folder, replayFile.getName(), false);

            int totalSeconds = Integer.parseInt(replay.getConfig().getString("info.lenght")) / 20;
            int minutes = totalSeconds / 60;
            int seconds = totalSeconds % 60;
            String time = String.format("%02d:%02d", minutes, seconds);

            ItemStack item = itemReader.toItemStack(String.format("PAPER ; Name:%s",
                    replayFile.getName()));
            ItemMeta meta = item.getItemMeta();
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(StringUtils.colorize("&dReplay lenght: &3" + time));
            lore.add(StringUtils.colorize("&dPlayers:"));
            lore.add("");
            int j = 1;
            for (String uuid : replay.getConfig().getConfigurationSection("").getKeys(false)) {
                OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(uuid));
                lore.add(j + ", " + player.getName());
                j++;
            }
            lore.add("");
            meta.setLore(lore);
            item.setItemMeta(meta);

            replayItems.put(i, item);
            i++;
        }

        maxPages = (replayItems.size() / replaySlots.size()) + 1;
        setPage(1);
    }

    public void setPage(int page) {
        for (int i = 0; i < page * replaySlots.size(); i++) {
            int slot = replaySlots.get(i);
            //getInventory().setItem(slot, replayItems.get());
        }
    }

    @Override
    public boolean onClick(Player player, int slot, ClickType clickType) {

        return true;
    }

    @Override
    public void onOpen(Player player) {

    }

    @Override
    public void onClose(Player player) {

    }

}
