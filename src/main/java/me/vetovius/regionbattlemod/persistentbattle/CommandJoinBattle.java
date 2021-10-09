package me.vetovius.regionbattlemod.persistentbattle;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.logging.Logger;

public class CommandJoinBattle implements CommandExecutor {

    private static final Logger LOGGER = Logger.getLogger( CommandJoinBattle.class.getName() );

    public static PersistentBattle battle;

    // This method is called, when somebody uses our command
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(battle != null) {
            if (Bukkit.getPlayer(sender.getName()) instanceof Player) {
                Player player = Bukkit.getPlayer(sender.getName());
                if (player.getWorld() == PersistentBattle.world) {
                    if(!battle.bluePlayers.contains(player) || !battle.redPlayers.contains(player)) {
                        battle.assignPlayerToTeam(player);
                    }
                    else{
                        player.sendMessage("You are already in a battle.");
                    }
                } else {
                    player.sendMessage("You need to join the RegionBattle world to play.");
                }


            }
        }
        else{
            sender.sendMessage("There is no battle.");
        }


        return true;
    }

}
