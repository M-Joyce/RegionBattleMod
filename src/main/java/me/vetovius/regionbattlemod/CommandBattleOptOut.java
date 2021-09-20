package me.vetovius.regionbattlemod;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.logging.Logger;

public class CommandBattleOptOut implements CommandExecutor {

    private static final Logger LOGGER = Logger.getLogger( CommandBattleOptOut.class.getName() );
    public static Battle battle;

    // This method is called, when somebody uses our command
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        //Used to toggle if this player will join battles or not.

        if(Battle.optOutPlayersList.contains(Bukkit.getPlayer(sender.getName()))){
            Bukkit.getPlayer(sender.getName()).sendMessage("You are no longer opting out of battles!");
            Battle.optOutPlayersList.remove(Bukkit.getPlayer(sender.getName()));
        }
        else {
            Bukkit.getPlayer(sender.getName()).sendMessage("You are now opting out of battles!");
            Battle.optOutPlayersList.add(Bukkit.getPlayer(sender.getName()));
        }
        return true;
    }

}
