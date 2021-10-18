package me.vetovius.regionbattle.persistentbattle;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.logging.Logger;

public class CommandLeaveBattle implements CommandExecutor {

    public static PersistentBattle battle;

    private static final Logger LOGGER = Logger.getLogger( CommandLeaveBattle.class.getName() );

    // This method is called, when somebody uses our command
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(battle != null) {
            if(Bukkit.getPlayer(sender.getName()) instanceof Player){
                Player player = Bukkit.getPlayer(sender.getName());
                if(player.getWorld() == PersistentBattle.world){


                    if(battle.bluePlayers.contains(player) || battle.redPlayers.contains(player)){
                        //remove from team
                        battle.removePlayerFromBattle(player);
                    }
                    else{
                        player.sendMessage("You aren't in a battle.");
                    }

                }
                else{
                    player.sendMessage("You aren't even in the Battle world, why would you leave?");
                }
            }
        }
        else{
            sender.sendMessage("There is no battle.");
        }


        return true;
    }

}
