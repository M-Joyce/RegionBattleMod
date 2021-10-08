package me.vetovius.regionbattlemod.persistentbattle;

import me.vetovius.regionbattlemod.RegionBattleMod;
import me.vetovius.regionbattlemod.regionbattle.Battle;
import me.vetovius.regionbattlemod.regionbattle.CommandSeek;
import me.vetovius.regionbattlemod.regionbattle.CommandSendTeamChat;
import me.vetovius.regionbattlemod.regionbattle.Regions;
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
    private static int checkIfStartNewBattle;

    // This method is called, when somebody uses our command
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        RegionBattleMod plugin = RegionBattleMod.getPlugin(RegionBattleMod.class);
        checkIfStartNewBattle = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            public void run() {

                if(battle == null){
                    LOGGER.info("--Creating Persistent Battle--");
                    battle = new PersistentBattle(RegionBattleMod.getPlugin(RegionBattleMod.class));
                    CommandLeaveBattle.battle = battle;
                    CommandJoinBattle.battle = battle;
                    CommandPSeek.battle = battle;
                    CommandSendTeamChatPersistentBattle.battle = battle;
                }

            }}, 20, 20*60*3); //repeat task, delay = 1s, repeat every 3 minutes.

        return true;
    }


}
