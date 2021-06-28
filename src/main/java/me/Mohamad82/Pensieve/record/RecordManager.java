package me.Mohamad82.Pensieve.record;

import me.Mohamad82.Pensieve.Main;
import me.Mohamad82.Pensieve.nms.enums.BlockDirection;
import me.Mohamad82.Pensieve.nms.enums.NPCState;
import me.Mohamad82.RUoM.Vector3;
import me.Mohamad82.RUoM.Vector3Utils;
import me.Mohamad82.RUoM.YamlConfig;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class RecordManager {

    private final Set<Recorder> recorders = new HashSet<>();

    private static RecordManager instance;
    public static RecordManager getInstance() {
        return instance;
    }

    public RecordManager() {
        instance = this;
    }

    public void writeToFile(File folder, String fileName, Set<Record> records) {
        YamlConfig data = new YamlConfig(Main.getInstance(), folder, fileName + ".pensieve", false);
        for (Record record : records) {
            ConfigurationSection section = data.getConfig().createSection(record.getPlayerUUID().toString());
            section.set("start_location", record.getStartLocation().toString());
            section.set("center_location", record.getCenter().toString());
            int i = 0;
            RecordTick lastTick = null;
            for (RecordTick tick : record.getRecordTicks()) {
                ConfigurationSection tickSection = section.createSection(String.valueOf(i));
                if (tick.getLocation() != null)
                    tickSection.set("location", tick.getLocation().toString());
                if (tick.getYaw() != -999)
                    tickSection.set("yaw", tick.getYaw());
                if (tick.getPitch() != -999)
                    tickSection.set("pitch", tick.getPitch());
                if (tick.getPing() != -999)
                    tickSection.set("ping", tick.getPing());
                if (tick.getHand() != null)
                    tickSection.set("hand", tick.getHand());
                if (tick.getOffHand() != null)
                    tickSection.set("offhand", tick.getOffHand());
                if (tick.getHelmet() != null)
                    tickSection.set("head", tick.getHelmet());
                if (tick.getChestplate() != null)
                    tickSection.set("chest", tick.getChestplate());
                if (tick.getLeggings() != null)
                    tickSection.set("legs", tick.getLeggings());
                if (tick.getBoots() != null)
                    tickSection.set("feet", tick.getBoots());
                if (tick.getHealth() != -999)
                    tickSection.set("health", tick.getHealth());
                if (tick.getHunger() != -999)
                    tickSection.set("food", tick.getHunger());
                if (tick.getPing() != -999)
                    tickSection.set("ping", tick.getPing());
                if (tick.getMessage() != null)
                    tickSection.set("message", tick.getMessage());
                if (tick.getState() != null)
                    tickSection.set("state", tick.getState().toString().toUpperCase());
                if (tick.tookDamage() || tick.didSwing() || tick.ateFood()) {
                    List<String> actions = new ArrayList<>();
                    if (tick.tookDamage())
                        actions.add("damage");
                    if (tick.didSwing())
                        actions.add("swing");
                    if (tick.ateFood())
                        actions.add("ate");
                    tickSection.set("actions", actions);
                }
                if (tick.getBlockPlaces() != null) {
                    ConfigurationSection placedSection = tickSection.createSection("placedblocks");
                    for (Vector3 placedLoc : tick.getBlockPlaces().keySet()) {
                        placedSection.set(placedLoc.toString(), tick.getBlockPlaces().get(placedLoc).toString().toUpperCase());
                    }
                }
                if (tick.getBlockBreaks() != null) {
                    ConfigurationSection brokeSection = tickSection.createSection("brokeblocks");
                    for (Vector3 brokeLoc : tick.getBlockBreaks().keySet()) {
                        brokeSection.set(brokeLoc.toString(), tick.getBlockBreaks().get(brokeLoc).toString().toUpperCase());
                    }
                }
                if (tick.getPendingBlockBreak() != null) {
                    ConfigurationSection pendingBlockBreakSection = tickSection.createSection("breaking");
                    if (lastTick.getPendingBlockBreak() == null || !lastTick.getPendingBlockBreak().getUuid().equals(tick.getPendingBlockBreak().getUuid())) {
                        pendingBlockBreakSection.set("stages", tick.getPendingBlockBreak().getAnimationStages());
                    }
                    pendingBlockBreakSection.set("uuid", tick.getPendingBlockBreak().getUuid().toString());
                    pendingBlockBreakSection.set("location", tick.getPendingBlockBreak().getLocation().toString());
                    pendingBlockBreakSection.set("material", tick.getPendingBlockBreak().getMaterial().toString());
                    pendingBlockBreakSection.set("direction", tick.getPendingBlockBreak().getBlockDirection().toString().toUpperCase());
                }
                if (tick.getEatingItem() != null) {
                    tickSection.set("eating", tick.getEatingItem().getType().toString());
                }

                lastTick = tick;
                i++;
            }
        }
        data.saveConfig();
    }

    public Set<Record> getFromFile(File folder, String fileName) throws FileNotFoundException {
        Set<Record> records = new HashSet<>();
        if (!fileName.endsWith(".pensieve"))
            fileName = fileName + ".pensieve";
        File file = new File(folder, fileName);
        if (!file.exists())
            throw new FileNotFoundException("Couldn't find pensieve record file!");
        YamlConfig data = new YamlConfig(Main.getInstance(), folder, fileName + ".pensieve");

        for (String uuid : data.getConfig().getConfigurationSection("").getKeys(false)) {
            Record record = new Record(UUID.fromString(uuid));
            List<RecordTick> ticks = new ArrayList<>();
            RecordTick lastTick = new RecordTick();

            for (String tickSectionName : data.getConfig().getConfigurationSection(uuid).getKeys(false)) {
                if (tickSectionName.equals("start_location")) {
                    record.setStartLocation(Vector3Utils.toVector3(data.getConfig().getConfigurationSection(uuid).getString("start_location")));
                    continue;
                } else if (tickSectionName.equals("center_location")) {
                    record.setCenter(Vector3Utils.toVector3(data.getConfig().getConfigurationSection(uuid).getString("center_location")));
                    continue;
                }
                RecordTick tick = new RecordTick();
                ConfigurationSection tickSection = data.getConfig().getConfigurationSection(uuid + "." + tickSectionName);

                if (tickSection.getString("location") != null)
                    tick.setLocation(Vector3Utils.toVector3(tickSection.getString("location")));
                if (tickSection.getString("yaw") != null)
                    tick.setYaw(Float.parseFloat(tickSection.getString("yaw")));
                if (tickSection.getString("pitch") != null)
                    tick.setPitch(Float.parseFloat(tickSection.getString("pitch")));
                if (tickSection.getItemStack("hand") != null)
                    tick.setHand(tickSection.getItemStack("hand"));
                if (tickSection.getItemStack("offhand") != null)
                    tick.setOffHand(tickSection.getItemStack("offhand"));
                if (tickSection.getItemStack("head") != null)
                    tick.setHelmet(tickSection.getItemStack("head"));
                if (tickSection.getItemStack("chest") != null)
                    tick.setChestplate(tickSection.getItemStack("chest"));
                if (tickSection.getItemStack("legs") != null)
                    tick.setLeggings(tickSection.getItemStack("legs"));
                if (tickSection.getItemStack("feet") != null)
                    tick.setBoots(tickSection.getItemStack("feet"));
                if (tickSection.getString("health") != null)
                    tick.setHealth(Double.parseDouble(tickSection.getString("health")));
                if (tickSection.getString("food") != null)
                    tick.setHunger(Integer.parseInt(tickSection.getString("food")));
                if (tickSection.getString("ping") != null)
                    tick.setPing(Integer.parseInt(tickSection.getString("ping")));
                if (tickSection.getString("message") != null)
                    tick.setMessage(tickSection.getString("message"));
                if (tickSection.getString("state") != null)
                    tick.setState(NPCState.valueOf(tickSection.getString("state")));
                if (tickSection.getList("actions") != null) {
                    List<String> actions = tickSection.getStringList("actions");
                    if (actions.contains("damage"))
                        tick.damage();
                    if (actions.contains("swing"))
                        tick.swing();
                    if (actions.contains("ate"))
                        tick.eatFood();
                }
                if (tickSection.getConfigurationSection("placedblocks") != null) {
                    ConfigurationSection placedBlocks = tickSection.getConfigurationSection("placedblocks");
                    Map<Vector3, Material> blockPlaces = new HashMap<>();
                    for (String location : placedBlocks.getKeys(false)) {
                        blockPlaces.put(Vector3Utils.toVector3(location), Material.valueOf(placedBlocks.getString(location)));
                    }
                    tick.setBlockPlaces(blockPlaces);
                }
                if (tickSection.getConfigurationSection("brokeblocks") != null) {
                    ConfigurationSection brokeBlocks = tickSection.getConfigurationSection("brokeblocks");
                    Map<Vector3, Material> blockBreaks = new HashMap<>();
                    for (String location : brokeBlocks.getKeys(false)) {
                        blockBreaks.put(Vector3Utils.toVector3(location), Material.valueOf(brokeBlocks.getString(location)));
                    }
                    tick.setBlockBreaks(blockBreaks);
                }
                if (tickSection.getConfigurationSection("breaking") != null) {
                    ConfigurationSection pendingBlockBreakSection = tickSection.getConfigurationSection("breaking");
                    if (lastTick.getPendingBlockBreak() == null ||
                            !lastTick.getPendingBlockBreak().getUuid().toString().equals(pendingBlockBreakSection.getString("uuid"))) {
                        PendingBlockBreak pendingBlockBreak = new PendingBlockBreak();
                        pendingBlockBreak.setUuid(UUID.fromString(pendingBlockBreakSection.getString("uuid")));
                        pendingBlockBreak.setAnimationStages(pendingBlockBreakSection.getIntegerList("stages"));
                        pendingBlockBreak.setBlockDirection(BlockDirection.valueOf(pendingBlockBreakSection.getString("direction")));
                        pendingBlockBreak.setMaterial(Material.valueOf(pendingBlockBreakSection.getString("material")));
                        pendingBlockBreak.setLocation(Vector3Utils.toVector3(pendingBlockBreakSection.getString("location")));
                        tick.setPendingBlockBreak(pendingBlockBreak);
                    } else {
                        tick.setPendingBlockBreak(lastTick.getPendingBlockBreak());
                    }
                }
                if (tickSection.getString("eating") != null) {
                    tick.setEatingItem(new ItemStack(Material.valueOf(tickSection.getString("eating"))));
                }
                lastTick = tick;
                ticks.add(tick);
            }
            record.setRecordTicks(ticks);
            records.add(record);
        }
        return records;
    }

    public Set<Recorder> getRecorders() {
        return recorders;
    }

}
