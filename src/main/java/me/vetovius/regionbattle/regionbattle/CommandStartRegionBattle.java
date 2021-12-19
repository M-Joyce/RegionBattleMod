package me.vetovius.regionbattle.regionbattle;

import me.vetovius.regionbattle.CommandSeek;
import me.vetovius.regionbattle.CommandSendTeamChat;
import me.vetovius.regionbattle.RegionBattle;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.logging.Logger;

public class CommandStartRegionBattle implements CommandExecutor {

    private static final Logger LOGGER = Logger.getLogger( CommandStartRegionBattle.class.getName() );
    private static int checkIfStartNewBattle;
    public static Battle battle;

    // This method is called, when somebody uses our command
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        RegionBattle plugin = RegionBattle.getPlugin(RegionBattle.class);
        checkIfStartNewBattle = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            public void run() {

                for(Player p : Battle.optOutPlayersList){
                    if(p.isOnline()){
                        if(p.getWorld() == Regions.world){

                        }
                        else{
                            Battle.optOutPlayersList.remove(p);
                        }
                    }
                }

                if(Regions.world.getPlayers().size() - Battle.optOutPlayersList.size() > 1){
                    if(battle == null){
                        LOGGER.info("--Creating Battle--");
                        battle = new Battle(RegionBattle.getPlugin(RegionBattle.class));
                        CommandSeek.battle = battle;
                        CommandAddPlayerToRegionBattle.battle = battle;
                        CommandSendTeamChat.battle = battle;
                        Bukkit.getScheduler().cancelTask(checkIfStartNewBattle);
                    }
                    else{
                        Bukkit.getPlayer(sender.getName()).sendMessage(ChatColor.RED + "There is already an ongoing battle! Wait until it ends!");
                    }
                }
                else{
                    //Do things if a game doesn't start due to not enough players
                    for(Player p : Regions.world.getPlayers()){
                        p.sendMessage(ChatColor.GREEN + "Not enough players! A new battle will begin in " + Battle.newBattleDelay + " minutes!");
                    }


                }

            }}, 20, 20*60*Battle.newBattleDelay); //repeat task, delay = 1s, repeat every 5 minutes.

        return true;
    }


}
