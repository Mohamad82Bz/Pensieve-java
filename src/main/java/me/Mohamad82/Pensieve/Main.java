package me.Mohamad82.Pensieve;

import me.Mohamad82.Pensieve.commands.PensieveCommand;
import me.Mohamad82.Pensieve.core.ReplayArenaManager;
import me.Mohamad82.Pensieve.data.DataManager;
import me.Mohamad82.Pensieve.gamemodes.eggwarsx.EWXListeners;
import me.Mohamad82.Pensieve.record.RecordManager;
import me.Mohamad82.Pensieve.record.listeners.PacketListener;
import me.Mohamad82.Pensieve.record.listeners.RecordListeners;
import me.Mohamad82.Pensieve.test.TestRecordCommand;
import me.Mohamad82.Pensieve.world.WorldManager;
import me.Mohamad82.RUoM.RUoMPlugin;
import me.Mohamad82.RUoM.Ruom;
import me.Mohamad82.RUoM.areaselection.AreaSelectionListener;
import me.Mohamad82.RUoM.utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public final class Main extends RUoMPlugin {

    private static Main instance;

    @Override
    public void onEnable() {
        getDataFolder().mkdir();
        instance = this;
        sendFiglet();

        initializeInstances();
        initializeCommands();
        initializeEvents();
    }

    @Override
    public void onDisable() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            PacketListener.getInstance().removePlayer(player);
        }
    }

    public void initializeCommands() {
        getCommand("pensieve").setExecutor(new PensieveCommand());

        //TODO: Remove
        getCommand("record").setExecutor(new TestRecordCommand());
    }

    public void initializeEvents() {
        getServer().getPluginManager().registerEvents(new PacketListener(this), this);
        getServer().getPluginManager().registerEvents(new RecordListeners(), this);
        getServer().getPluginManager().registerEvents(new AreaSelectionListener(), this);
        if (getServer().getPluginManager().getPlugin("Eggwars") != null)
            getServer().getPluginManager().registerEvents(new EWXListeners(), this);
    }

    public void initializeInstances() {
        new WorldManager();
        new RecordManager();
        new DataManager();
        new ReplayArenaManager();
        Ruom.initializeAreaSelection(ReplayArenaManager.wand);
    }

    public void sendFiglet() {
        List<String> list = new ArrayList<>();

        list.add("");
        list.add("&5-__ /\\\\                                        ");
        list.add("&5  ||  \\\\                    '       ;          ");
        list.add("&5 /||__||  _-_  \\\\/\\\\  _-_,&d \\\\  _-_  \\\\/\\  _-_  ");
        list.add("&5 \\||__|| || \\\\ || || ||_.  &d|| || \\\\ || | || \\\\ ");
        list.add("&d  ||  |, ||/   || ||  &5~ || || ||/   || | ||/   ");
        list.add("&d_-||-_/  \\\\,/  \\\\ \\\\ &5,-_-  \\\\ \\\\,/  \\\\/  \\\\,/  ");
        list.add("&d  ||                                           ");
        list.add("");

        for (String msg : list) {
            Bukkit.getConsoleSender().sendMessage(StringUtils.colorize(msg));
        }
    }

    public static Main getInstance() {
        return instance;
    }

}
