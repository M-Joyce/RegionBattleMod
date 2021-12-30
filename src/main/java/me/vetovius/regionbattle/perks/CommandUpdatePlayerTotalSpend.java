package me.vetovius.regionbattle.perks;

import me.vetovius.regionbattle.CommandDiscord;
import me.vetovius.regionbattle.SQLiteDB.DBUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.logging.Logger;

public class CommandUpdatePlayerTotalSpend implements CommandExecutor {

    private static final Logger LOGGER = Logger.getLogger( me.vetovius.regionbattle.perks.CommandUpdatePlayerTotalSpend.class.getName() );

    // This method is called, when somebody uses our command
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(args.length == 2) {

            try {
                Float.parseFloat(args[1]);
                if(Float.parseFloat(args[1]) > 0){
                    if(Bukkit.getPlayerExact(args[0]) != null){
                        DBUtils.updatePlayerSpend(Bukkit.getPlayerExact(args[0]), Float.parseFloat(args[1]));
                    }
                    else{
                        LOGGER.info("Could not find player by that name, cannot update playerTotalSpend.");
                    }
                }
                else{
                    LOGGER.info("Invalid amount passed for updateplayertotalspend command.");
                }

            } catch (final NumberFormatException e) {
                LOGGER.info("Parameter for amount could not be parsed as a float.");
            }
        }
        else{
            LOGGER.info("Invalid usage. Try /updateplayertotalspend playername amountToAdd");
        }


        return true;
    }

}


