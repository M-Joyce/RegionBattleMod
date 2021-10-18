package me.vetovius.regionbattle.tokenshop;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.logging.Logger;

public class CommandGivePlayerToken implements CommandExecutor {

    private static final Logger LOGGER = Logger.getLogger( CommandGivePlayerToken.class.getName() );

    // This method is called, when somebody uses our command
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(args.length == 1) {
            if(Bukkit.getPlayerExact(args[0]) instanceof Player){
                Bukkit.getPlayerExact(args[0]).getInventory().addItem(Token.getTokenItemStack());
            }
            else{
                sender.sendMessage("No player by that name found.");
            }
        }
        else{
            sender.sendMessage("Invalid usage. Try /giveplayertoken playername");
        }

        return true;
    }

}
