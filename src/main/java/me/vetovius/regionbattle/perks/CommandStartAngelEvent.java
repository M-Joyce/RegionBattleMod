package me.vetovius.regionbattle.perks;

import me.vetovius.regionbattle.RegionBattle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;


//This class controls the duration of the The Great Angel Event
public class CommandStartAngelEvent implements CommandExecutor {

    private static final Logger LOGGER = Logger.getLogger( CommandStartAngelEvent.class.getName() );
    RegionBattle plugin = RegionBattle.getPlugin(RegionBattle.class);
    public static int flightBoostActiveHours = 48; //2 days
    public static long angelEventActiveEndTime;

    // This method is called, when somebody uses our command
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {


        LOGGER.info("Activating The Great Angel Event.");
        plugin.getConfig().set("perks.angelEvent.isActive", true);

        //Set when the flight boost should end.
        angelEventActiveEndTime = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(flightBoostActiveHours);
        plugin.getConfig().set("perks.angelEvent.endTime", angelEventActiveEndTime);

        plugin.saveConfig(); //save to apply

        return true;
    }

}