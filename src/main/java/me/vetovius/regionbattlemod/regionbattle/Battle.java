package me.vetovius.regionbattlemod.regionbattle;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.vetovius.regionbattlemod.CommandSeek;
import me.vetovius.regionbattlemod.CommandSendTeamChat;
import me.vetovius.regionbattlemod.RegionBattleMod;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.*;

import java.util.*;
import java.util.logging.Logger;

public class Battle implements Listener {

    private static final int prepareMinutes = 15;
    public static final int newBattleDelay = 5;

    private static final Logger LOGGER = Logger.getLogger( Battle.class.getName() );

    private ScoreboardManager manager;
    private Scoreboard board;
    private Team teamRed;
    private Team teamBlue;

    private Regions battleRegions; //Regions object containing region for each team, and region related functionality

    public static ArrayList<Player> optOutPlayersList = new ArrayList<Player>();
    public ArrayList<Player> redPlayers;
    public ArrayList<Player> bluePlayers;

    private int battleTimerID;

    private BukkitTask prepCountdownTask;

    private BossBar prepPhaseBossBar;

    private RegionBattleMod plugin;



    public Battle(RegionBattleMod pluginInstance){

        LOGGER.info("--Creating Battle--");

        this.plugin = pluginInstance;

        Bukkit.getPluginManager().registerEvents(this, plugin); //register events

        this.battleRegions = new Regions();

        this.manager = Bukkit.getScoreboardManager();
        this.board = manager.getNewScoreboard();
        this.teamRed = board.registerNewTeam("Team_Red");
        this.teamBlue = board.registerNewTeam("Team_Blue");

        this.redPlayers = new ArrayList<>();
        this.bluePlayers = new ArrayList<>();

        this.prepPhaseBossBar = Bukkit.createBossBar(ChatColor.GRAY+"Prepare for Battle!", BarColor.RED, BarStyle.SEGMENTED_10);

        this.assignTeams();
        this.battleRegions.assignRegionMembers(this);
        this.teleportTeams(battleRegions.regionRed,battleRegions.regionBlue);
        this.startPrepPhaseBossBarTimer();
        this.battleTimer(battleRegions.particleRunnerID);

    }

    private void assignTeams(){
        LOGGER.info("Assigning Teams..");

        List<Player> players = new ArrayList<>(Regions.world.getPlayers());

        //remove opted out players
        for(Player p : optOutPlayersList){
            players.remove(p);
        }

        Collections.shuffle(players); //randomize

        // add the first half of players to Red
        redPlayers.addAll(players.subList(0, players.size() / 2 + players.size()%2));
        // and the second half to Blue
        bluePlayers.addAll(players.subList(players.size() / 2 + players.size()%2, players.size()));

        teamBlue.setColor(ChatColor.BLUE); //Set Colors for teams
        teamRed.setColor(ChatColor.RED);

        teamBlue.setAllowFriendlyFire(false);
        teamRed.setAllowFriendlyFire(false);

        //create objective for score board
        Objective objective = board.registerNewObjective("battleObjective", "playerKillCount","Player Kills");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        //LOGGER.info("assignTeams() RED:" + redPlayers.toString());
        //LOGGER.info("assignTeams() BLUE:" + bluePlayers.toString());

        ItemStack compass = new ItemStack(Material.COMPASS,1);


        for(Player player : redPlayers){ //add red players
            player.getInventory().clear(); //clear inventory
            player.setHealth(20); //heal
            player.setFoodLevel(20); //feed
            player.getInventory().addItem(compass); //give player a compass for seek
            teamRed.addEntry(player.getDisplayName()); //add player to team
            player.setScoreboard(board); //set player scoreboard
            prepPhaseBossBar.addPlayer(player); //display prep timer
            Score score = objective.getScore(player.getDisplayName());
            score.setScore(score.getScore());
        }

        for(Player player : bluePlayers){ //add blue players
            player.getInventory().clear(); //clear inventory
            player.setHealth(20); //heal
            player.setFoodLevel(20); //feed
            player.getInventory().addItem(compass); //give player a compass for seek
            teamBlue.addEntry(player.getDisplayName()); //add player to team
            player.setScoreboard(board); //set player scoreboard
            prepPhaseBossBar.addPlayer(player); //display prep timer
            Score score = objective.getScore(player.getDisplayName());
            score.setScore(score.getScore());
        }


    }

    public void teleportTeams(ProtectedRegion regionRed, ProtectedRegion regionBlue) {

        LOGGER.info("Teleporting players..");
        //Red Team
        int rmaxX = regionRed.getMaximumPoint().getBlockX();
        int rmaxY = regionRed.getMaximumPoint().getBlockY();
        int rmaxZ = regionRed.getMaximumPoint().getBlockZ();

        int rminX = regionRed.getMinimumPoint().getBlockX();
        int rminY = regionRed.getMinimumPoint().getBlockY();
        int rminZ = regionRed.getMinimumPoint().getBlockZ();

        Location redMidPoint = new Location(Regions.world,(rmaxX+rminX)/2,(rmaxY+rminY )/2,(rmaxZ+rminZ )/2);
        redMidPoint.setY(Regions.world.getHighestBlockYAt(redMidPoint)+1);
        //LOGGER.info("Teleporting Red Team to: "+redMidPoint);
        for(UUID uuid : regionRed.getMembers().getUniqueIds()){
            Bukkit.getPlayer(uuid).teleport(redMidPoint);
        }

        //Blue Team
        int bmaxX = regionBlue.getMaximumPoint().getBlockX();
        int bmaxY = regionBlue.getMaximumPoint().getBlockY();
        int bmaxZ = regionBlue.getMaximumPoint().getBlockZ();

        int bminX = regionBlue.getMinimumPoint().getBlockX();
        int bminY = regionBlue.getMinimumPoint().getBlockY();
        int bminZ = regionBlue.getMinimumPoint().getBlockZ();

        Location blueMidPoint = new Location(Regions.world,(bmaxX+bminX)/2,(bmaxY+bminY )/2,(bmaxZ+bminZ )/2);
        blueMidPoint.setY(Regions.world.getHighestBlockYAt(blueMidPoint)+1);
        //LOGGER.info("Teleporting Blue Team to: "+blueMidPoint);

        for(UUID uuid : regionBlue.getMembers().getUniqueIds()){
            Bukkit.getPlayer(uuid).teleport(blueMidPoint);
        }

        for(Player p : redPlayers){
            p.sendMessage(ChatColor.BLUE+"The Blue Team is at: X="+blueMidPoint.getBlockX()+" Z="+blueMidPoint.getBlockZ());
        }
        for(Player p : bluePlayers){
            p.sendMessage(ChatColor.RED+"The Red Team is at: X="+redMidPoint.getBlockX()+" Z="+redMidPoint.getBlockZ());
        }



    }

    public void battleTimer(int particleRunnerID){

        Regions.world.setTime(23200); //set time to Morning

        for(Player p : Regions.world.getPlayers()){
            p.sendMessage(ChatColor.GREEN + "Prepare for Battle! Gather Supplies! You have "+prepareMinutes + " minutes!");
            p.sendMessage(ChatColor.RED+""+redPlayers.size()+" players remain on Team Red.");
            p.sendMessage(ChatColor.BLUE+""+bluePlayers.size()+" players remain on Team Blue.");
        }


        battleTimerID = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

            int checkIfGameIsOverID = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
                public void run() {

                    if((redPlayers.size() <= 0 || bluePlayers.size() <= 0)){ //End game check

                        if(redPlayers.size() <= 0){
                            for(Player p : Regions.world.getPlayers()){
                                p.sendMessage(ChatColor.BLUE+"The Blue Team has won the battle! No players remain on the Red Team!");
                            }
                        }

                        if(bluePlayers.size() <= 0) {
                            for(Player p : Regions.world.getPlayers()) {
                                p.sendMessage(ChatColor.RED + "The Red Team has won the battle! No players remain on the Blue Team!");
                            }
                        }

                        //Do Tasks to end the game
                        prepPhaseBossBar.removeAll();

                        battleRegions.removeRegions(); //remove regions at game end

                        teamBlue.unregister(); //unregister teams at game end
                        teamRed.unregister();

                        //send players back
                        for(Player p : redPlayers){
                            p.setScoreboard(manager.getNewScoreboard()); //manager.getNewScoreboard() will return a blank scoreboard
                            p.teleport(Regions.spawn);
                            p.getInventory().clear();
                        }
                        for(Player p : bluePlayers){
                            p.setScoreboard(manager.getNewScoreboard()); //manager.getNewScoreboard() will return a blank scoreboard
                            p.teleport(Regions.spawn);
                            p.getInventory().clear();
                        }

                        //cancel tasks
                        Bukkit.getScheduler().cancelTask(battleTimerID);
                        Bukkit.getScheduler().cancelTask(checkIfGameIsOverID);

                        if (prepCountdownTask != null) {
                            prepCountdownTask.cancel();
                        }

                        Bukkit.getScheduler().cancelTask(particleRunnerID); //Cancel Particle Effects at boundary

                        CommandStartRegionBattle.battle = null; //Tell CommandStartRegionBattle that the battle is over and another can be started.
                        CommandSeek.battle = null;
                        CommandSendTeamChat.battle = null;
                        battleRegions = null;

                        //Trigger a new battle to start
                        for(Player p : Regions.world.getPlayers()){
                            p.sendMessage(ChatColor.GREEN + "A new battle will begin in " + newBattleDelay + " minutes!");
                        }
                        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

                            public void run() {
                                //Start a new battle.
                                ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
                                Bukkit.dispatchCommand(console, "startregionbattle");
                            }
                        }, 20*60L*newBattleDelay); //20 ticks per second * 60 seconds * # Minutes

                    }

                }}, 20*30, 200); //check if game should end every 10 seconds, delay start by 30 seconds.

            public void run() {

                for(Player p : Regions.world.getPlayers()){
                    p.sendMessage(ChatColor.RED + "Let the Battle Begin! Force fields have come down! Last team standing wins!");
                }

                battleRegions.removeFlagsForBattle(); //remove flags in regions to allow for battle

                Bukkit.getScheduler().cancelTask(particleRunnerID); //Cancel Particle Effects at boundary

            }}, 20*60L*prepareMinutes); //20 ticks per second * 60 seconds * # Minutes wanted from prepareMinutes
    }

    public void seekPlayers(UUID uuid){

        if(Bukkit.getPlayer(uuid).getWorld() == Regions.world){
            Random rand = new Random();

            if(bluePlayers.size() > 0 && redPlayers.size() > 0) {
                if (redPlayers.contains(Bukkit.getPlayer(uuid))) {
                    Player playerToSeek = bluePlayers.get(rand.nextInt(bluePlayers.size()));
                    Location loc = playerToSeek.getLocation();
                    Bukkit.getPlayer(uuid).sendMessage("There is an enemy player " + (int) Math.round(Bukkit.getPlayer(uuid).getLocation().distance(loc)) + " blocks away.");
                    Bukkit.getPlayer(uuid).setCompassTarget(playerToSeek.getLocation());
                    Bukkit.getPlayer(uuid).sendMessage("Your compass is now pointing at their last known location.");
                }

                if (bluePlayers.contains(Bukkit.getPlayer(uuid))) {

                    Player playerToSeek = redPlayers.get(rand.nextInt(redPlayers.size()));
                    Location loc = playerToSeek.getLocation();
                    Bukkit.getPlayer(uuid).sendMessage("There is an enemy player " + (int) Math.round(Bukkit.getPlayer(uuid).getLocation().distance(loc)) + " blocks away.");
                    Bukkit.getPlayer(uuid).setCompassTarget(playerToSeek.getLocation());
                    Bukkit.getPlayer(uuid).sendMessage("Your compass is now pointing at their last known location.");
                }
            }
            else{
                Bukkit.getPlayer(uuid).sendMessage("There are no players to seek!");
            }
        }
        else{
            Bukkit.getPlayer(uuid).sendMessage("You aren't in the battle world.");
        }
    }

    @EventHandler
    public void onPlayerDeath (PlayerDeathEvent event){ // Send message of players death.

        Player p = event.getEntity().getPlayer();
        if(p.getWorld() == Regions.world) {

            if(battleRegions != null){

                for(Player player : Regions.world.getPlayers()){
                    player.sendMessage(ChatColor.DARK_RED + p.getDisplayName() + " has died!");
                }

                prepPhaseBossBar.removePlayer(p);

                //search teams for dead player.
                //TODO there is definitely a more efficient way to do this if needed
                ArrayList<Player> toRemove = new ArrayList<Player>();
                for(Player player : redPlayers ){
                    if(player == p){
                        p.setScoreboard(manager.getNewScoreboard()); //manager.getNewScoreboard() will return a blank scoreboard
                        toRemove.add(event.getEntity().getPlayer());
                        teamRed.removeEntry(event.getEntity().getDisplayName());
                        for(Player worldPlayer : Regions.world.getPlayers()){
                            worldPlayer.sendMessage(ChatColor.RED+""+redPlayers.size()+" players remain on Team Red.");
                        }
                    }
                }
                redPlayers.removeAll(toRemove);
                toRemove.clear();

                for(Player player : bluePlayers){
                    if(player == p){
                        p.setScoreboard(manager.getNewScoreboard()); //manager.getNewScoreboard() will return a blank scoreboard
                        toRemove.add(event.getEntity().getPlayer());
                        teamBlue.removeEntry(event.getEntity().getDisplayName());
                        for(Player worldPlayer : Regions.world.getPlayers()){
                            worldPlayer.sendMessage(ChatColor.BLUE+""+ bluePlayers.size()+" players remain on Team Blue.");
                        }
                    }
                }
                bluePlayers.removeAll(toRemove);
                toRemove.clear();

            }
        }
    }

    @EventHandler
    public void onPlayerLogOff (PlayerQuitEvent event) { //handle when players log off in the middle of battle
        if(battleRegions != null){
            Player p = event.getPlayer();

            //TODO again, more efficient way to do this than for loop if needed
            if(p.getWorld() == Regions.world){
                for(Player player : redPlayers ) {
                    if (player == p) {
                        p.setScoreboard(manager.getNewScoreboard()); //manager.getNewScoreboard() will return a blank scoreboard
                        redPlayers.remove(p);
                        teamRed.removeEntry(p.getDisplayName());
                        prepPhaseBossBar.removePlayer(p);
                    }
                }
                for(Player player : bluePlayers ) {
                    if (player == p) {
                        p.setScoreboard(manager.getNewScoreboard()); //manager.getNewScoreboard() will return a blank scoreboard
                        bluePlayers.remove(p);
                        teamBlue.removeEntry(p.getDisplayName());
                        prepPhaseBossBar.removePlayer(p);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerLogOn (PlayerJoinEvent event) { //handle when players log off in the middle of battle, and then join back.
        if(battleRegions != null){

            Player p = event.getPlayer();

            if(p.getWorld() == Regions.world){
                if(battleRegions.regionRed.getMembers().getPlayers().contains(p.getUniqueId().toString().toLowerCase())){
                    redPlayers.add(p);
                    teamRed.addEntry(p.getDisplayName()); //add player to team
                    p.setScoreboard(board); //set player scoreboard
                    prepPhaseBossBar.addPlayer(p);
                }
                if(battleRegions.regionBlue.getMembers().getPlayers().contains(p.getUniqueId().toString().toLowerCase())){
                    bluePlayers.add(p);
                    teamBlue.addEntry(p.getDisplayName()); //add player to team
                    p.setScoreboard(board); //set player scoreboard
                    prepPhaseBossBar.addPlayer(p);
                }
            }
        }
    }
    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) { //remove player from battle if the teleport.

        if (battleRegions != null) {
            Player p = event.getPlayer();
            if (p.getWorld() == Regions.world) {

                if (battleRegions.regionRed.getMembers().getPlayers().contains(p.getUniqueId().toString().toLowerCase())) {
                    redPlayers.remove(p);
                    teamRed.removeEntry(p.getDisplayName());
                    prepPhaseBossBar.removePlayer(p);
                }
                if (battleRegions.regionBlue.getMembers().getPlayers().contains(p.getUniqueId().toString().toLowerCase())) {
                    bluePlayers.remove(p);
                    teamBlue.removeEntry(p.getDisplayName());
                    prepPhaseBossBar.removePlayer(p);
                }
            }
        }
    }


    private void startPrepPhaseBossBarTimer(){
        //Timer for preparation phase

        if (prepCountdownTask == null) {
            this.prepCountdownTask = new BukkitRunnable() {
                int seconds = 60*prepareMinutes;
                @Override
                public void run() {
                    if ((seconds -= 1) == 0) {
                        prepCountdownTask.cancel();
                        prepPhaseBossBar.removeAll();
                    } else {
                        prepPhaseBossBar.setProgress(seconds / (60D*prepareMinutes));
                    }
                }
            }.runTaskTimer(plugin, 0, 20);
        }
        prepPhaseBossBar.setVisible(true);
    }

}
