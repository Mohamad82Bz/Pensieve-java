package me.mohamad82.pensieve;

import me.mohamad82.pensieve.commands.PensieveCommand;
import me.mohamad82.pensieve.commands.PensieveTabCompleter;
import me.mohamad82.pensieve.hooks.PlaceholderAPIHook;
import me.mohamad82.pensieve.recording.RecordManager;
import me.mohamad82.pensieve.recording.listeners.PlayerActionListener;
import me.mohamad82.pensieve.recording.listeners.PlayerUseItemListener;
import me.mohamad82.pensieve.recording.listeners.RecordListeners;
import me.mohamad82.pensieve.test.TestRecordCommand;
import me.mohamad82.ruom.RUoMPlugin;
import me.mohamad82.ruom.Ruom;
import me.mohamad82.ruom.adventure.AdventureApi;
import me.mohamad82.ruom.skin.SkinBuilder;
import me.mohamad82.ruom.string.StringUtils;
import org.bukkit.Bukkit;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class Pensieve extends RUoMPlugin {

    private static Pensieve instance;

    @Override
    public void onEnable() {
        getDataFolder().mkdir();
        new File(getDataFolder(), "storage").mkdir();
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
        PensieveCommand pensieveCommand = new PensieveCommand();
        getCommand("pensieve").setExecutor(pensieveCommand);
        getCommand("pensieve").setTabCompleter(new PensieveTabCompleter());
        //TODO: Debug command, Remove in future
        getCommand("record").setExecutor(new TestRecordCommand());
    }

    public void initializeEvents() {
        Ruom.registerListener(new RecordListeners());
        new PlayerActionListener();
        new PlayerUseItemListener();
    }

    public void initializeInstances() {
        new RecordManager();
        new SkinBuilder();
        AdventureApi.initialize();
        Ruom.initializePacketListener();
        if (Ruom.hasPlugin("PlaceholderAPI")) {
            new PlaceholderAPIHook();
        }
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
