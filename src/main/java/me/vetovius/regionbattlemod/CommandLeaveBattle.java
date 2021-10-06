package me.vetovius.regionbattlemod;

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

        if(Bukkit.getPlayer(sender.getName()) instanceof Player){
            Player player = Bukkit.getPlayer(sender.getName());
            if(player.getWorld() == Regions.world){
                //remove from team
                battle.removePlayerFromBattle(player);
            }
            else{
                player.sendMessage("You aren't even in the RegionBattle world, why would you leave?");
            }
        }

        return true;
    }

}
