package me.vetovius.regionbattle.miniboss;

import me.vetovius.regionbattle.RegionBattle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.logging.Logger;

public class CommandSpawnMiniBoss implements CommandExecutor {

    private static final Logger LOGGER = Logger.getLogger( CommandSpawnMiniBoss.class.getName() );

    // This method is called, when somebody uses our command
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        RegionBattle plugin = RegionBattle.getPlugin(RegionBattle.class);

            LOGGER.info("--Creating MiniBoss--");
           new MiniBoss(plugin);


        return true;
    }


}

