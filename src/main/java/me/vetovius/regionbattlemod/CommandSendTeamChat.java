package me.vetovius.regionbattlemod;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.logging.Logger;

public class CommandSendTeamChat implements CommandExecutor {

    private static final Logger LOGGER = Logger.getLogger( CommandSendTeamChat.class.getName() );
    public static Battle battle;

    // This method is called, when somebody uses our command
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(battle != null){
            Player player = Bukkit.getPlayer(sender.getName());

            if(player.getWorld() == Regions.world) {

                if (battle.redPlayers.contains(player)) {
                    for (Player p : battle.redPlayers) {
                        p.sendMessage(ChatColor.RED + "[Team Red] " + ChatColor.WHITE + player.getDisplayName() + ": " + String.join(" ", args));
                    }
                }

                if (battle.bluePlayers.contains(player)) {
                    for (Player p : battle.bluePlayers) {
                        p.sendMessage(ChatColor.BLUE + "[Team Blue] " + ChatColor.WHITE + player.getDisplayName() + ": " + String.join(" ", args));
                    }
                }
            }
        }
        //TODO this wont work right for multiple battle objects?
        else{
            Bukkit.getPlayer(sender.getName()).sendMessage("There is no battle!");
        }

        return true;
    }

}
