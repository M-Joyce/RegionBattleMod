package me.vetovius.regionbattle.persistentbattle;

import me.vetovius.regionbattle.CommandSeek;
import me.vetovius.regionbattle.CommandSendTeamChat;
import me.vetovius.regionbattle.RegionBattle;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.logging.Logger;

public class CommandStartPersistentBattle implements CommandExecutor {

    private static final Logger LOGGER = Logger.getLogger( CommandStartPersistentBattle.class.getName() );
    public static PersistentBattle battle;
    private static int checkIfStartNewBattle;

    // This method is called, when somebody uses our command
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        RegionBattle plugin = RegionBattle.getPlugin(RegionBattle.class);
        checkIfStartNewBattle = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            public void run() {

                if(battle == null){
                    LOGGER.info("--Creating Persistent Battle--");
                    battle = new PersistentBattle(RegionBattle.getPlugin(RegionBattle.class));
                    CommandLeaveBattle.battle = battle;
                    CommandJoinBattle.battle = battle;
                    CommandSeek.pbattle = battle;
                    CommandSendTeamChat.pbattle = battle;
                }

            }}, 20, 20*60*3); //repeat task, delay = 1s, repeat every 3 minutes.

        return true;
    }


}
