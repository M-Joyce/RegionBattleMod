package me.vetovius.regionbattlemod;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import java.util.logging.Logger;

public class CommandStartRegionBattle implements CommandExecutor {

    private static final Logger LOGGER = Logger.getLogger( CommandStartRegionBattle.class.getName() );
    public static Battle battle;

    // This method is called, when somebody uses our command
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(battle == null){
            LOGGER.info("--Creating Battle--");
            battle = new Battle(RegionBattleMod.getPlugin(RegionBattleMod.class));
            CommandSeek.battle = battle;
        }
        else{
            Bukkit.getPlayer(sender.getName()).sendMessage(ChatColor.RED + "There is already an ongoing battle! Wait until it ends!");
        }

        return true;
    }

}
