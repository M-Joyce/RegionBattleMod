package me.vetovius.regionbattlemod;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.logging.Logger;

public class CommandStartPersistentBattle implements CommandExecutor {

    private static final Logger LOGGER = Logger.getLogger( CommandStartPersistentBattle.class.getName() );
    public static PersistentBattle battle;

    // This method is called, when somebody uses our command
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(battle == null){
            LOGGER.info("--Creating Persistent Battle--");
            battle = new PersistentBattle(RegionBattleMod.getPlugin(RegionBattleMod.class));
            CommandLeaveBattle.battle = battle;
            CommandJoinBattle.battle = battle;
            CommandPSeek.battle = battle;
            CommandSendTeamChatPersistentBattle.battle = battle;
        }
        else{
            Bukkit.getPlayer(sender.getName()).sendMessage(ChatColor.RED + "There is already an ongoing battle! Wait until it ends!");
        }

        return true;
    }


}
