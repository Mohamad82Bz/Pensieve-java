package me.mohamad82.pensieve;

import me.mohamad82.pensieve.recording.RecordManager;
import me.mohamad82.pensieve.recording.listeners.PlayerActionListener;
import me.mohamad82.pensieve.recording.listeners.PlayerUseItemListener;
import me.mohamad82.pensieve.recording.listeners.RecordListeners;
import me.mohamad82.pensieve.test.TestRecordCommand;
import me.mohamad82.pensieve.world.WorldManager;
import me.mohamad82.ruom.RUoMPlugin;
import me.mohamad82.ruom.Ruom;
import me.mohamad82.ruom.areaselection.AreaSelectionListener;
import me.mohamad82.ruom.utils.StringUtils;
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
        //TODO: Debug command, Remove in future
        getCommand("record").setExecutor(new TestRecordCommand());
    }

    public void initializeEvents() {
        Ruom.registerListener(new RecordListeners());
        Ruom.registerListener(new AreaSelectionListener());
        new PlayerActionListener();
        new PlayerUseItemListener();
    }

    public void initializeInstances() {
        new RecordManager();
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
