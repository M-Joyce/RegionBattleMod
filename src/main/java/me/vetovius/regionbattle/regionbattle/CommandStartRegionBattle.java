package me.vetovius.regionbattle.regionbattle;

import me.vetovius.regionbattle.CommandSeek;
import me.vetovius.regionbattle.CommandSendTeamChat;
import me.vetovius.regionbattle.RegionBattle;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.logging.Logger;

public class CommandStartRegionBattle implements CommandExecutor {

    private static final Logger LOGGER = Logger.getLogger( CommandStartRegionBattle.class.getName() );
    private static int checkIfStartNewBattle;
    public static Battle battle;
    private BukkitTask newBattleCountdownTask;
    private BossBar newBattleCountdownBossBar;

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
                    if (newBattleCountdownTask != null) {
                        newBattleCountdownTask.cancel();
                        newBattleCountdownTask = null;
                    }
                    if (newBattleCountdownBossBar != null){
                        newBattleCountdownBossBar.removeAll();
                        newBattleCountdownBossBar.setVisible(false);
                        newBattleCountdownBossBar = null;
                    }
                }
                else{
                    //Do things if a game doesn't start due to not enough players
                    for(Player p : Regions.world.getPlayers()){
                        p.sendMessage(ChatColor.GREEN + "Not enough players! A new battle will begin in " + Battle.newBattleDelay + " minutes if there are enough players!");
                    }

                    if (newBattleCountdownTask != null) {
                        newBattleCountdownTask.cancel();
                        newBattleCountdownTask = null;
                    }
                    if (newBattleCountdownBossBar != null){
                        newBattleCountdownBossBar.removeAll();
                        newBattleCountdownBossBar.setVisible(false);
                        newBattleCountdownBossBar = null;
                    }

                    if (newBattleCountdownTask == null) {
                        newBattleCountdownBossBar = Bukkit.createBossBar(ChatColor.GRAY+"Time until next battle.", BarColor.RED, BarStyle.SEGMENTED_10);
                        newBattleCountdownTask = new BukkitRunnable() {
                            int seconds = 60*Battle.newBattleDelay;
                            @Override
                            public void run() {
                                if ((seconds -= 1) <= 0) {
                                    newBattleCountdownTask.cancel();
                                    newBattleCountdownBossBar.removeAll();
                                } else {
                                    newBattleCountdownBossBar.setProgress(seconds / (60D*Battle.newBattleDelay));
                                }

                                for(Player p : Regions.world.getPlayers()) { //add players to boss bar.
                                    if(!newBattleCountdownBossBar.getPlayers().contains(p)){
                                        newBattleCountdownBossBar.addPlayer(p);
                                    }
                                }
                                for(Player p : newBattleCountdownBossBar.getPlayers()){ //remove player if not in battle world
                                    if(p.getWorld() != Regions.world){
                                        newBattleCountdownBossBar.removePlayer(p);
                                    }
                                }
                            }
                        }.runTaskTimer(plugin, 0, 20);
                    }


                }

            }}, 20, 20*60*Battle.newBattleDelay); //repeat task, delay = 1s, repeat every 5 minutes.

        return true;
    }


}
