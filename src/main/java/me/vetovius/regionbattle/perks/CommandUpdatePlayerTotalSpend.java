package me.vetovius.regionbattle.perks;

import me.vetovius.regionbattle.CommandDiscord;
import me.vetovius.regionbattle.SQLiteDB.DBUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
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
                if(Float.parseFloat(args[1]) >= 0){
                    if(Bukkit.getPlayerExact(args[0]) != null){

                        Player player = Bukkit.getPlayerExact(args[0]);

                        DBUtils.updatePlayerSpend(player, Float.parseFloat(args[1]));

                        //Update rank based on spend
                        //TODO probably should use the LuckPerms API for this, allows me to check for which groups a player already has,
                        //TODO and would let me ignore this check if the correct group is already applied to the player
                        float totalPlayerSpend = DBUtils.getPlayerSpend(player);

                        if(totalPlayerSpend > 0){
                            ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();

                          if(1 <= totalPlayerSpend && totalPlayerSpend < 5){
                              //Give Donor Rank
                              Bukkit.dispatchCommand(console, "lp user "+player.getName()+" parent add donor");

                          }
                          else if(5 <= totalPlayerSpend && totalPlayerSpend < 15){
                              //Give VIP Rank
                              Bukkit.dispatchCommand(console, "lp user "+player.getName()+" parent add vip");
                              //remove old groups
                              Bukkit.dispatchCommand(console, "lp user "+player.getName()+" parent remove donor");

                          }
                          else if(15 <= totalPlayerSpend && totalPlayerSpend < 30){
                              //Give VIP+ Rank
                              Bukkit.dispatchCommand(console, "lp user "+player.getName()+" parent add vip+");
                              //remove old groups
                              Bukkit.dispatchCommand(console, "lp user "+player.getName()+" parent remove vip");
                              Bukkit.dispatchCommand(console, "lp user "+player.getName()+" parent remove donor");

                          }
                          else if(30 <= totalPlayerSpend && totalPlayerSpend < 50){
                              //Give Champion Rank
                              Bukkit.dispatchCommand(console, "lp user "+player.getName()+" parent add champion");
                              //remove old groups
                              Bukkit.dispatchCommand(console, "lp user "+player.getName()+" parent remove vip+");
                              Bukkit.dispatchCommand(console, "lp user "+player.getName()+" parent remove vip");
                              Bukkit.dispatchCommand(console, "lp user "+player.getName()+" parent remove donor");


                          }
                          else if(50 <= totalPlayerSpend){
                              //Give Champion+ Rank
                              Bukkit.dispatchCommand(console, "lp user "+player.getName()+" parent add champion+");
                              //remove old groups
                              Bukkit.dispatchCommand(console, "lp user "+player.getName()+" parent remove champion");
                              Bukkit.dispatchCommand(console, "lp user "+player.getName()+" parent remove vip+");
                              Bukkit.dispatchCommand(console, "lp user "+player.getName()+" parent remove vip");
                              Bukkit.dispatchCommand(console, "lp user "+player.getName()+" parent remove donor");
                          }
                          else{
                              //Invalid amount for upgrading rank.
                              LOGGER.info("totalPlayerSpend not in range when checking to update donor/vip rank. totalPlayerSpend=" + totalPlayerSpend);
                          }

                        }else{
                            LOGGER.info("No need to update rank, player spend is 0 or less.");
                        }


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


