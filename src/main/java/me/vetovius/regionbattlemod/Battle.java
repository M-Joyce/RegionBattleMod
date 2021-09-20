package me.vetovius.regionbattlemod;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
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
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;
import java.util.*;
import java.util.logging.Logger;

public class Battle implements Listener {

    private static final int prepareMinutes = 15;
    private static final int newBattleDelay = 3;

    private static final Logger LOGGER = Logger.getLogger( Battle.class.getName() );

    private ScoreboardManager manager;
    private Scoreboard board;
    private Team teamRed;
    private Team teamBlue;

    private Regions battleRegions; //Regions object containing region for each team, and region related functionality


    protected ArrayList<Player> redPlayers;
    protected ArrayList<Player> bluePlayers;

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

        this.prepPhaseBossBar = Bukkit.createBossBar("Prepare for Battle!", BarColor.GREEN, BarStyle.SEGMENTED_10);

        this.assignTeams();
        this.battleRegions.assignRegionMembers(this);
        this.teleportTeams(battleRegions.regionRed,battleRegions.regionBlue);
        this.startPrepPhaseBossBarTimer();
        this.battleTimer(battleRegions.particleRunnerID);

    }

    private void assignTeams(){
        LOGGER.info("Assigning Teams..");

        List<Player> players = new ArrayList<>(Regions.world.getPlayers());
        Collections.shuffle(players); //randomize

        // add the first half of players to Red
        redPlayers.addAll(players.subList(0, players.size() / 2 + players.size()%2));
        // and the second half to Blue
        bluePlayers.addAll(players.subList(players.size() / 2 + players.size()%2, players.size()));

        teamBlue.setColor(ChatColor.BLUE); //Set Colors for teams
        teamRed.setColor(ChatColor.RED);


        //LOGGER.info("assignTeams() RED:" + redPlayers.toString());
        //LOGGER.info("assignTeams() BLUE:" + bluePlayers.toString());

        for(Player player : redPlayers){ //add red players
            player.getInventory().clear(); //clear inventory
            teamRed.addEntry(player.getDisplayName()); //add player to team
            player.setScoreboard(board); //set player scoreboard
            prepPhaseBossBar.addPlayer(player); //display prep timer
        }

        for(Player player : bluePlayers){ //add blue players
            player.getInventory().clear(); //clear inventory
            teamBlue.addEntry(player.getDisplayName()); //add player to team
            player.setScoreboard(board); //set player scoreboard
            prepPhaseBossBar.addPlayer(player); //display prep timer
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

        Bukkit.broadcastMessage(ChatColor.RED+"The Red Team is at: X="+redMidPoint.getBlockX()+" Z="+redMidPoint.getBlockZ());
        Bukkit.broadcastMessage(ChatColor.BLUE+"The Blue Team is at: X="+blueMidPoint.getBlockX()+" Z="+blueMidPoint.getBlockZ());

    }

    public void battleTimer(int particleRunnerID){

        Bukkit.broadcastMessage(ChatColor.GREEN + "Prepare for Battle! Gather Supplies! You have "+prepareMinutes + " minutes!");
        Bukkit.broadcastMessage(ChatColor.RED+""+redPlayers.size()+" players remain on Team Red.");
        Bukkit.broadcastMessage(ChatColor.BLUE+""+bluePlayers.size()+" players remain on Team Blue.");

        battleTimerID = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

            int checkIfGameIsOverID = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
                public void run() {

                    if((redPlayers.size() <= 0 || bluePlayers.size() <= 0)){ //End game check

                        if(redPlayers.size() <= 0){
                            Bukkit.broadcastMessage(ChatColor.BLUE+"The Blue Team has won the battle! No players remain on the Red Team!");
                        }

                        if(bluePlayers.size() <= 0) {
                            Bukkit.broadcastMessage(ChatColor.RED+"The Red Team has won the battle! No players remain on the Blue Team!");
                        }

                        //Do Tasks to end the game
                        prepPhaseBossBar.removeAll();

                        battleRegions.removeRegions(); //remove regions at game end

                        teamBlue.unregister(); //unregister teams at game end
                        teamRed.unregister();

                        //send players back
                        for(Player p : redPlayers){
                            p.teleport(Regions.spawn);
                            p.getInventory().clear();
                        }
                        for(Player p : bluePlayers){
                            p.teleport(Regions.spawn);
                            p.getInventory().clear();
                        }

                        //cancel tasks
                        Bukkit.getScheduler().cancelTask(battleTimerID);
                        Bukkit.getScheduler().cancelTask(checkIfGameIsOverID);

                        Bukkit.getScheduler().cancelTask(particleRunnerID); //Cancel Particle Effects at boundary

                        CommandStartRegionBattle.battle = null; //Tell CommandStartRegionBattle that the battle is over and another can be started.
                        CommandSeek.battle = null;
                        CommandSendTeamChat.battle = null;
                        battleRegions = null;

                        //Trigger a new battle to start
                        Bukkit.broadcastMessage(ChatColor.GREEN + "A new battle will begin in " + newBattleDelay + " minutes!");
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
                Bukkit.broadcastMessage(ChatColor.RED + "Let the Battle Begin! Force fields have come down! Last team standing wins!");

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
                    Bukkit.getPlayer(uuid).sendMessage("There is an enemy player at X: " + loc.getBlockX() + " Y: " + loc.getBlockY() + " Z: " + loc.getBlockZ());
                }

                if (bluePlayers.contains(Bukkit.getPlayer(uuid))) {

                    Player playerToSeek = redPlayers.get(rand.nextInt(redPlayers.size()));
                    Location loc = playerToSeek.getLocation();
                    Bukkit.getPlayer(uuid).sendMessage("There is an enemy player at X: " + loc.getBlockX() + " Y: " + loc.getBlockY() + " Z: " + loc.getBlockZ());
                }
            }
            else{
                Bukkit.getPlayer(uuid).sendMessage("There are no players to seek!");
            }
        }
    }

    @EventHandler
    public void onPlayerDeath (PlayerDeathEvent event){ // Send message of players death.

        Player p = event.getEntity().getPlayer();
        if(p.getWorld() == Regions.world) {

            if(battleRegions != null){

                Bukkit.broadcastMessage(ChatColor.DARK_RED + p.getDisplayName() + " has died!");

                prepPhaseBossBar.removePlayer(p);

                //search teams for dead player.
                //TODO there is definitely a more efficient way to do this if needed
                for(Player player : redPlayers ){
                    if(player == p){
                        redPlayers.remove(event.getEntity().getPlayer());
                        teamRed.removeEntry(event.getEntity().getPlayer().getUniqueId().toString());
                        Bukkit.broadcastMessage(ChatColor.RED+""+redPlayers.size()+" players remain on Team Red.");
                    }
                }

                for(Player player : bluePlayers){
                    if(player == p){
                        bluePlayers.remove(event.getEntity().getPlayer());
                        teamBlue.removeEntry(event.getEntity().getPlayer().getUniqueId().toString());
                        Bukkit.broadcastMessage(ChatColor.BLUE+""+ bluePlayers.size()+" players remain on Team Blue.");
                    }
                }

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
                        redPlayers.remove(p);
                        prepPhaseBossBar.removePlayer(p);
                    }
                }
                for(Player player : bluePlayers ) {
                    if (player == p) {
                        bluePlayers.remove(p);
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
                    teamRed.addEntry(p.getUniqueId().toString()); //add player to team
                    p.setScoreboard(board); //set player scoreboard
                    prepPhaseBossBar.addPlayer(p);
                }
                if(battleRegions.regionBlue.getMembers().getPlayers().contains(p.getUniqueId().toString().toLowerCase())){
                    bluePlayers.add(p);
                    teamBlue.addEntry(p.getUniqueId().toString()); //add player to team
                    p.setScoreboard(board); //set player scoreboard
                    prepPhaseBossBar.addPlayer(p);
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
                        prepPhaseBossBar.setProgress(seconds / 10D);
                    }
                }
            }.runTaskTimer(plugin, 0, 20);
        }
        prepPhaseBossBar.setVisible(true);
    }

}
