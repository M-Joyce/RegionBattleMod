package me.vetovius.regionbattle.viptokens;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.logging.Logger;

public class CommandGiveVIPToken implements CommandExecutor {

    private static final Logger LOGGER = Logger.getLogger( CommandGiveVIPToken.class.getName() );

    // This method is called, when somebody uses our command
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(args.length == 1) {
            if(Bukkit.getPlayerExact(args[0]) instanceof Player){
                Bukkit.getPlayerExact(args[0]).getInventory().addItem(VIPToken.getTokenItemStack(1));
            }
            else{
                sender.sendMessage("No player by that name found.");
            }
        }
        else if(args.length == 2) {

            try {
                Integer.parseInt(args[1]);
                if(Integer.parseInt(args[1]) < 64 && Integer.parseInt(args[1]) > 1){
                    if(Bukkit.getPlayerExact(args[0]) instanceof Player){
                        Bukkit.getPlayerExact(args[0]).getInventory().addItem(VIPToken.getTokenItemStack(Integer.parseInt(args[1])));
                    }
                    else{
                        sender.sendMessage("No player by that name found.");
                    }
                }
                else{
                    sender.sendMessage("Invalid VIP Token amount!");
                }
            } catch (final NumberFormatException e) {
                sender.sendMessage("Parameter for amount could not be parsed as an int.");
            }
        }
        else{
            sender.sendMessage("Invalid usage. Try /giveVIPtoken playername amount");
        }

        return true;
    }

}
