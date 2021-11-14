package me.Mohamad82.Pensieve;

import me.Mohamad82.Pensieve.commands.PensieveCommand;
import me.Mohamad82.Pensieve.core.ReplayArenaManager;
import me.Mohamad82.Pensieve.data.DataManager;
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

import java.util.ArrayList;
import java.util.List;

public final class Pensieve extends RUoMPlugin {

    private static Pensieve instance;

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
        Ruom.shutdown();
    }

    public void initializeCommands() {
        getCommand("pensieve").setExecutor(new PensieveCommand());

        //TODO: Remove
        getCommand("record").setExecutor(new TestRecordCommand());
    }

    public void initializeEvents() {
        Ruom.registerListener(new RecordListeners());
        Ruom.registerListener(new AreaSelectionListener());
    }

    public void initializeInstances() {
        new WorldManager();
        new RecordManager();
        new DataManager();
        new ReplayArenaManager();
        PacketListener.initialize();
        Ruom.initializeAreaSelection();
        Ruom.initializeSkinBuilder();
        Ruom.initializePacketListener();
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

    public static Pensieve getInstance() {
        return instance;
    }

}
