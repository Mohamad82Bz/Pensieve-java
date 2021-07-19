package me.Mohamad82.Pensieve.data;

import me.Mohamad82.Pensieve.Main;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DataManager {

    public static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private static DataManager instance;
    public static DataManager getInstance() {
        return instance;
    }

    public DataManager() {
        instance = this;
    }

    public File createFolder(String name) {
        File file = new File(Main.getInstance().getDataFolder(), name);
        file.mkdir();
        return file;
    }

    public int getFilesCountInFolder(String name) {
        File folder = new File(Main.getInstance().getDataFolder(), name);
        if (!folder.isDirectory()) return -1;

        return folder.listFiles().length;
    }

    public int getFilesCountInFolderInSameDate(String name, Date date) {
        File folder = getFile(name);
        if (!folder.isDirectory()) return -1;
        String formattedDate = dateFormat.format(date);
        int i = 1;
        for (File file : folder.listFiles()) {
            if (file.getName().contains(formattedDate))
                i++;
        }
        return i;
    }

    public File getFile(String name) {
        return new File(Main.getInstance().getDataFolder(), name);
    }

}
