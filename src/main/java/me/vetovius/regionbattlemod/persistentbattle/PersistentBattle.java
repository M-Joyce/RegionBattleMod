package me.vetovius.regionbattlemod.persistentbattle;

import me.vetovius.regionbattlemod.ChestLoot;
import me.vetovius.regionbattlemod.RegionBattleMod;
import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.*;

import javax.swing.plaf.synth.Region;
import java.awt.*;
import java.util.*;
import java.util.logging.Logger;

public class PersistentBattle implements Listener {

    private static final Logger LOGGER = Logger.getLogger( PersistentBattle.class.getName() );

    private ScoreboardManager manager;
    private Scoreboard board;
    private Team teamRed;
    private Team teamBlue;
    private Objective objective;

    protected ArrayList<Player> redPlayers;
    protected ArrayList<Player> bluePlayers;

    private RegionBattleMod plugin;
    Location spawn;
    Location redSpawn;
    Location blueSpawn;

    public static World world = Bukkit.getWorld("Battle");

    private static final int max = 5800; //max coordinate
    private static final int min = 600; //min coordinate
    private static final int minDistance = 400; //minimum distance between region centers.
    private static final int maxDistance = 1200; //maximum distance between region centers.

    private long hourTime; //system time when its been 1 hour from battle start.
    private long startTime;

    private BossBar battleTimerBar;
    protected int timerTaskId;


    public PersistentBattle(RegionBattleMod pluginInstance){

        LOGGER.info("--Creating Battle--");

        this.plugin = pluginInstance;

        Bukkit.getPluginManager().registerEvents(this, plugin); //register events

        this.manager = Bukkit.getScoreboardManager();
        this.board = manager.getNewScoreboard();
        this.teamRed = board.registerNewTeam("Team_Red");
        this.teamBlue = board.registerNewTeam("Team_Blue");

        //create objective for score board
        objective = board.registerNewObjective("battleObjective", "playerKillCount","Player Kills");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        teamBlue.setColor(ChatColor.BLUE); //Set Colors for teams
        teamRed.setColor(ChatColor.RED);

        teamBlue.setAllowFriendlyFire(false);
        teamRed.setAllowFriendlyFire(false);


        this.redPlayers = new ArrayList<>();
        this.bluePlayers = new ArrayList<>();

        spawn = new Location(world, -72, 89, 3103); //location of battle spawn

        world.setTime(23200); //set time to Morning

        Point[] spawnPoints = findTeamSpawns();
        int redX = (int)spawnPoints[0].getX();
        int redZ = (int)spawnPoints[0].getY();
        int blueX = (int)spawnPoints[1].getX();
        int blueZ = (int)spawnPoints[1].getY();

        redSpawn = new Location(world,redX,world.getHighestBlockYAt(redX,redZ)+1,redZ);
        blueSpawn = new Location(world,blueX,world.getHighestBlockYAt(blueX,blueZ)+1,blueZ);


        ChestLoot chestLoot = new ChestLoot(4000, new Location(world, 3000, 80, 3000), plugin); //init chestLoot feature to generate loot in unopened chestLoot chests each game (only if pdc value is set)

        //1 hour = 3600000 miliseconds
        startTime = System.currentTimeMillis();
        hourTime =  System.currentTimeMillis() + 3600000; //1 hour from now

        this.battleTimerBar = Bukkit.createBossBar(ChatColor.GRAY+"The battle is on!", BarColor.RED, BarStyle.SEGMENTED_10);
        battleTimerBar.setProgress(1);
        battleTimer(); //keep track of time


    }

    protected void battleTimer(){

        RegionBattleMod plugin = RegionBattleMod.getPlugin(RegionBattleMod.class);
        timerTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            public void run() {

                if (System.currentTimeMillis() >= hourTime){ //Hour is up, end the battle and start a new one.

                    Bukkit.getScheduler().cancelTask(timerTaskId);
                    //remove players, avoiding comodification exception
                    ArrayList<Player> toRemove = new ArrayList<>();
                    for(Player p : redPlayers){
                        toRemove.add(p);
                    }
                    for(Player p : bluePlayers){
                        toRemove.add(p);
                    }
                    for(Player p : toRemove){
                        p.sendMessage("The battle has ended!"); //TODO log announce winning team, maybe reward winning players?
                        removePlayerFromBattle(p);
                    }

                    CommandStartPersistentBattle.battle = null; //Tell CommandStartRegionBattle that the battle is over and another can be started.
                    CommandPSeek.battle = null;
                    CommandSendTeamChatPersistentBattle.battle = null;

                    //Trigger a new battle to start
                    for(Player p : world.getPlayers()){
                        p.sendMessage(ChatColor.GREEN + "A new battle will begin in " + 3 + " minutes!");
                    }
                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

                        public void run() {
                            //Start a new battle.
                            ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
                            Bukkit.dispatchCommand(console, "startpersistentbattle");
                        }
                    }, 20*60L*3); //20 ticks per second * 60 seconds * # Minutes

                }

                double currPerc = (100 - ((System.currentTimeMillis() - startTime) * 100) / (hourTime - startTime)) * 0.01;

                battleTimerBar.setProgress(currPerc);
                battleTimerBar.setVisible(true);

            }}, 20, 20*30); //repeat task every 30 seconds
    }

    protected void assignPlayerToTeam(Player player){

        if(redPlayers.contains(player) || bluePlayers.contains(player)){
            player.sendMessage("You are already in a battle! You can't join twice!");
        }
        else {

            String team = "";

            //Figure out which team to add player to
            if (redPlayers.size() == bluePlayers.size()) {
                //Teams are same size, pick one at random
                int randInt = (Math.random() <= 0.5) ? 1 : 2;

                if (randInt == 1) {
                    team = "red";
                } else {
                    team = "blue";
                }
            } else if (redPlayers.size() > bluePlayers.size()) { //red bigger, assign player to blue
                team = "blue";
            } else if (redPlayers.size() < bluePlayers.size()) { //blue bigger, assign player to red
                team = "red";
            }
            LOGGER.info("Assigning " + player.getDisplayName() + " to Team " + team + ".");

            battleTimerBar.addPlayer(player); //display battle timer to player

            ItemStack compass = new ItemStack(Material.COMPASS, 1);

            //TODO assign teams a random location as "home base" and send them there
            if (team.equals("red")) {
                player.teleport(redSpawn, PlayerTeleportEvent.TeleportCause.END_GATEWAY); //set the cause the end gateway, since this likely will never naturally happen
                player.getInventory().clear(); //clear inventory
                player.getInventory().addItem(compass); //give player a compass for seek
                redPlayers.add(player);
                teamRed.addEntry(player.getDisplayName()); //add player to team
                player.setScoreboard(board); //set player scoreboard
                Score score = objective.getScore(player.getDisplayName());
                score.setScore(score.getScore());

                for(Player p : redPlayers){
                    p.sendMessage(ChatColor.RED+player.getDisplayName() + " has joined the Red Team!");
                }
                for(Player p : bluePlayers){
                    p.sendMessage(ChatColor.RED+player.getDisplayName() + " has joined the Red Team!");
                }
            }

            if (team.equals("blue")) {
                player.teleport(blueSpawn, PlayerTeleportEvent.TeleportCause.END_GATEWAY); //set the cause the end gateway, since this likely will never naturally happen
                player.getInventory().clear(); //clear inventory
                player.getInventory().addItem(compass); //give player a compass for seek
                bluePlayers.add(player);
                teamBlue.addEntry(player.getDisplayName()); //add player to team
                player.setScoreboard(board); //set player scoreboard
                Score score = objective.getScore(player.getDisplayName());
                score.setScore(score.getScore());

                for(Player p : redPlayers){
                    p.sendMessage(ChatColor.BLUE+player.getDisplayName() + " has joined the Blue Team!");
                }
                for(Player p : bluePlayers){
                    p.sendMessage(ChatColor.BLUE+player.getDisplayName() + " has joined the Blue Team!");
                }
            }
        }

    }

    public void seekPlayers(UUID uuid){

        if(Bukkit.getPlayer(uuid).getWorld() == world){
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
    public void onPlayerRespawn (PlayerRespawnEvent event) { //handle when die and need to respawn to their team
        Player p = event.getPlayer();
        if(redPlayers.contains(p)){
            event.setRespawnLocation(redSpawn);
        }
        if(bluePlayers.contains(p)){
            event.setRespawnLocation(blueSpawn);
        }
    }

    @EventHandler
    public void onPlayerLogOff (PlayerQuitEvent event) { //handle when players log off in the middle of battle
        Player p = event.getPlayer();
        if(redPlayers.contains(p) || bluePlayers.contains(p)){
            removePlayerFromBattle(p);
        }
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) { //remove player from battle if they teleport.

        if(event.getCause() != PlayerTeleportEvent.TeleportCause.END_GATEWAY){ //the specific cause we set earlier for a valid TP by this plugin.
            Player p = event.getPlayer();
            if(redPlayers.contains(p) || bluePlayers.contains(p)){
                p.sendMessage("You were removed from battle for teleporting.");
                LOGGER.info("Removing player from battle because they teleported: " + p.getDisplayName() +" Teleport Cause: " + event.getCause());
                removePlayerFromBattle(p);
            }
        }
    }

    private Point[] findTeamSpawns(){ //find team spawns

        int rX = 0; //red points
        int rZ = 0;

        int bX = 0; //blue points
        int bZ = 0;

        Point r = new Point(rX,rZ); //create center points for red and blue
        Point b = new Point(bX,bZ);

        //get new points while the regions are too close together as is defined by minDistance/maxDistance or if block is liquid
        while((minDistance >= r.distance(b)) || (r.distance(b) >= maxDistance) || !world.getHighestBlockAt(bX,bZ).getType().isSolid() || !world.getHighestBlockAt(rX,rZ).getType().isSolid()){ //get new points while the regions are too close together as is defined by minDistance/maxDistance or if block is liquid
            rX = new Random().nextInt(max - min + 1) + min;
            rZ = new Random().nextInt(max - min + 1) + min;

            bX = new Random().nextInt(max - min + 1) + min;
            bZ = new Random().nextInt(max - min + 1) + min;

            r = new Point(rX,rZ);
            b = new Point(bX,bZ);
        }

        Point teamPointArray[]; //declaring array
        teamPointArray = new Point[2]; // allocating memory to array

        teamPointArray[0] = r;
        teamPointArray[1] = b;

        return teamPointArray;
    }

    protected void removePlayerFromBattle(Player player){

        LOGGER.info("Removing from battle: " + player.getDisplayName());

        battleTimerBar.removePlayer(player); //display prep timer
        if(redPlayers.contains(player)){
            redPlayers.remove(player);
            teamRed.removeEntry(player.getDisplayName());
            player.setScoreboard(manager.getNewScoreboard()); //manager.getNewScoreboard() will return a blank scoreboard
            player.teleport(spawn);
            player.getInventory().clear(); //clear inventory

            for(Player p : redPlayers){
                p.sendMessage(ChatColor.RED+player.getDisplayName() + " has been removed from the Red Team!");
            }
            for(Player p : bluePlayers){
                p.sendMessage(ChatColor.RED+player.getDisplayName() + " has been removed from the Red Team!");
            }
        }
        else if(bluePlayers.contains(player)){
            bluePlayers.remove(player);
            teamBlue.removeEntry(player.getDisplayName());
            player.setScoreboard(manager.getNewScoreboard()); //manager.getNewScoreboard() will return a blank scoreboard
            player.teleport(spawn);
            player.getInventory().clear(); //clear inventory

            for(Player p : redPlayers){
                p.sendMessage(ChatColor.BLUE+player.getDisplayName() + " has been removed from the Blue Team!");
            }
            for(Player p : bluePlayers){
                p.sendMessage(ChatColor.BLUE+player.getDisplayName() + " has been removed from the Blue Team!");
            }
        }
        else{
            LOGGER.info("Something went wrong, player needed to be removed from battle but was not on a team!");
        }

    }

}
