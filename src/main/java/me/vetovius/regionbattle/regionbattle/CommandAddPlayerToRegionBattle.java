package me.vetovius.regionbattle.regionbattle;

import me.vetovius.regionbattle.RegionBattle;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.logging.Logger;

public class CommandAddPlayerToRegionBattle implements CommandExecutor {

    private static final Logger LOGGER = Logger.getLogger( CommandAddPlayerToRegionBattle.class.getName() );
    public static Battle battle;

    // This method is called, when somebody uses our command
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        RegionBattle plugin = RegionBattle.getPlugin(RegionBattle.class);
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (battle != null) { //check that a battle exists for this.
                if(args.length == 2) { //check if 2 args, else not valid command
                    if(Bukkit.getPlayerExact(args[0]) instanceof Player){
                        if(args[1].equalsIgnoreCase("red")){
                            battle.forceAddPlayerToTeam("red", Bukkit.getPlayerExact(args[0]));
                        }
                        if(args[1].equalsIgnoreCase("blue")){
                            battle.forceAddPlayerToTeam("blue", Bukkit.getPlayerExact(args[0]));
                        }
                    }
                    else{
                        sender.sendMessage("No player by that name found.");
                    }
                }
                else{
                    player.sendMessage("Invalid usage: try /addplayertoregionbattle playername team(red/blue)");
                }
            }
            else{
                player.sendMessage("There is no battle to add this player to.");
            }

        }

        return true;
    }


}
