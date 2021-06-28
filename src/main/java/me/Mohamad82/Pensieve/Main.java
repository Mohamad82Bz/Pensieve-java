package me.Mohamad82.Pensieve;

import me.Mohamad82.Pensieve.record.RecordManager;
import me.Mohamad82.Pensieve.record.listeners.PacketListener;
import me.Mohamad82.Pensieve.record.listeners.RecordListeners;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    private static Main instance;

    @Override
    public void onEnable() {
        instance = this;
        
    }

    @Override
    public void onDisable() {
        
    }

    public void initializeCommands() {

    }

    public void initializeEvents() {
        getServer().getPluginManager().registerEvents(new PacketListener(this), this);
        getServer().getPluginManager().registerEvents(new RecordListeners(), this);
    }

    public void initializeInstances() {
        //RecordManager has it's own getInstance method, so no need to store the class in our Main.
        new RecordManager();
    }

    public static Main getInstance() {
        return instance;
    }

}
