package me.vetovius.regionbattle.behemoth;

import me.vetovius.regionbattle.RegionBattle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.logging.Logger;


public class CommandSpawnBehemoth implements CommandExecutor {

    protected Behemoth behemoth;

    private static final Logger LOGGER = Logger.getLogger( me.vetovius.regionbattle.behemoth.CommandSpawnBehemoth.class.getName() );

    // This method is called, when somebody uses our command
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {


        if(sender instanceof Player){

            Player player = ((Player) sender).getPlayer();

            RegionBattle plugin = RegionBattle.getPlugin(RegionBattle.class);

            LOGGER.info("--Spawning a Behemoth--");
            this.behemoth = new Behemoth(plugin,player,this); //Init Behemoth
        }



        return true;
    }


}




