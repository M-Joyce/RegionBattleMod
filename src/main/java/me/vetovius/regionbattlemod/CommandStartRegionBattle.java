package me.vetovius.regionbattlemod;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import java.util.logging.Logger;

public class CommandStartRegionBattle implements CommandExecutor {

    private static final Logger LOGGER = Logger.getLogger( CommandStartRegionBattle.class.getName() );


    // This method is called, when somebody uses our command
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        LOGGER.info("--Creating Battle--");
        //Battle battle = new Battle(); Use this and remove the registerEvent and arg in the constructor in Battle.java to go back to using main for registering event
        Battle battle = new Battle(RegionBattleMod.getPlugin(RegionBattleMod.class));
        CommandSeek.battle = battle;

        return true;
    }

}
