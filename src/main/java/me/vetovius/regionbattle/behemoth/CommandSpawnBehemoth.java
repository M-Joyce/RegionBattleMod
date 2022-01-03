package me.vetovius.regionbattle.behemoth;

import me.vetovius.regionbattle.RegionBattle;
import me.vetovius.regionbattle.miniboss.MiniBoss;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.logging.Logger;


public class CommandSpawnBehemoth implements CommandExecutor {

    private static final Logger LOGGER = Logger.getLogger( me.vetovius.regionbattle.behemoth.CommandSpawnBehemoth.class.getName() );

    // This method is called, when somebody uses our command
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {


        if(sender instanceof Player){

            Player player = ((Player) sender).getPlayer();

            RegionBattle plugin = RegionBattle.getPlugin(RegionBattle.class);

            LOGGER.info("--Spawning a Behemoth--");
            new Behemoth(plugin,player); //Init Behemoth
        }



        return true;
    }


}




