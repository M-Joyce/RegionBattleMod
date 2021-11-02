package me.vetovius.regionbattle.smpbattleregion;

import me.vetovius.regionbattle.RegionBattle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.logging.Logger;

public class CommandSpawnBattleRegion implements CommandExecutor {

    private static final Logger LOGGER = Logger.getLogger( CommandSpawnBattleRegion.class.getName() );
    public static BattleRegion battleRegion;

    // This method is called, when somebody uses our command
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        RegionBattle plugin = RegionBattle.getPlugin(RegionBattle.class);

        if(battleRegion == null){
            LOGGER.info("--Creating SMP Battle Region--");
            battleRegion = new BattleRegion(RegionBattle.getPlugin(RegionBattle.class));
        }

        return true;
    }


}

