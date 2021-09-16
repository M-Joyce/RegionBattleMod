package me.vetovius.regionbattlemod;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.logging.Logger;

public class CommandSeek implements CommandExecutor {

    private static final Logger LOGGER = Logger.getLogger( CommandSeek.class.getName() );
    public static Battle battle;

    // This method is called, when somebody uses our command
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(battle != null){
            LOGGER.info("--Seek Command Called...--");

            if(sender instanceof Player) {
                Player player = (Player) sender;
                UUID uuid = player.getUniqueId(); // this should work
                battle.seekPlayers(uuid);
            }
        }
        //TODO this wont work right for multiple battle objects?
        else{
            Bukkit.getPlayer(sender.getName()).sendMessage("There is no battle!");
        }


        return true;
    }

}
