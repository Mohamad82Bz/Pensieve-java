package me.mohamad82.pensieve.commands;

import me.mohamad82.pensieve.core.AreaSelection;
import me.mohamad82.pensieve.core.ReplayArenaManager;
import me.mohamad82.pensieve.menu.PensieveGUI;
import me.Mohamad82.RUoM.areaselection.AreaSelectionManager;
import me.Mohamad82.RUoM.vector.Vector3Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PensieveCommand implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            //TODO MESSAGE
            sender.sendMessage("You cannot run this command in console.");
            return true;
        }
        Player player = (Player) sender;
        /*
        /pensieve setup eggwarsx desert
        /pensieve setup save
         */
        if (args.length == 0) {
            PensieveGUI gui = new PensieveGUI(player.getUniqueId());
            player.openInventory(gui.getInventory());
            return true;
        }
        if (!(player.hasPermission("pensieve.admin"))) {
            //TODO MESSAGE
            player.sendMessage("No permission.");
            return true;
        }
        if (args[0].equalsIgnoreCase("help")) {
            //TODO MESSAGE: Admin commands list.
            player.sendMessage("Commands List");
            return true;
        }
        if (args[0].equalsIgnoreCase("setup")) {
            if (args.length == 1) {
                //TODO MESSAGE
                player.sendMessage("Please specify a setup type, Available: eggwarsx");
                return true;
            }
            if (args[1].equalsIgnoreCase("wand")) {
                if (ReplayArenaManager.getInstance().giveWand(player)) {
                    if (!AreaSelectionManager.getInstance().containsPlayerAreaSelection(player))
                        AreaSelectionManager.getInstance().addAreaSelection(new AreaSelection(player, ReplayArenaManager.wand));
                    //TODO MESSAGE
                    player.sendMessage("Wand has been given to you. set two positions and then run /pensieve setup save <name>");
                } else {
                    //TODO MESSAGE
                    player.sendMessage("Your inventory is full. Please have one free slot.");
                }
                return true;
            }
            if (args[1].equalsIgnoreCase("save")) {
                if (args.length == 2) {
                    player.sendMessage("Please specify a name for this setup.");
                    return true;
                }
                me.Mohamad82.RUoM.areaselection.AreaSelection areaSelection = AreaSelectionManager.getInstance().getPlayerAreaSelection(player);
                if (areaSelection.getFirstPos() == null) {
                    player.sendMessage("You did not set first pos.");
                    return true;
                } else if (areaSelection.getSecondPos() == null) {
                    player.sendMessage("You did not set second pos.");
                    return true;
                }
                ReplayArenaManager.getInstance().createReplayArena(args[2].toLowerCase(),
                        Vector3Utils.toVector3(areaSelection.getFirstPos()), Vector3Utils.toVector3(areaSelection.getSecondPos()));

                return true;
            }
        }
        return false;
    }

}
