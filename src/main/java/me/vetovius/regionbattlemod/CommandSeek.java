package me.vetovius.regionbattlemod;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.logging.Logger;

public class CommandSeek implements CommandExecutor {

    private static final Logger LOGGER = Logger.getLogger( CommandSeek.class.getName() );


    // This method is called, when somebody uses our command
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        LOGGER.info("--Seek Command Called...--");
        SetupRegions.seekPlayers(sender.getName());

        return true;
    }

}
