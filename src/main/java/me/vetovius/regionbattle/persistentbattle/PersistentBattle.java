package me.vetovius.regionbattle.persistentbattle;

import me.vetovius.regionbattle.chestloot.ChestLoot;
import me.vetovius.regionbattle.CommandSeek;
import me.vetovius.regionbattle.CommandSendTeamChat;
import me.vetovius.regionbattle.RegionBattle;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.*;
import org.bukkit.Color;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.*;
import org.bukkit.util.BoundingBox;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.logging.Logger;

public class PersistentBattle implements Listener {

    private static final Logger LOGGER = Logger.getLogger( PersistentBattle.class.getName() );

    private ScoreboardManager manager;
    private Scoreboard board;

    private Team teamRed;
    private Team teamBlue;
    private Objective objective;

    public ArrayList<Player> redPlayers;
    public ArrayList<Player> bluePlayers;

    private RegionBattle plugin;
    Location spawn;
    Location redSpawn;
    Location blueSpawn;

    private BoundingBox border;

    public static World world = Bukkit.getWorld("Battle");

    private static final int max = 5000; //max coordinate
    private static final int min = 1000; //min coordinate
    private static final int minDistance = 400; //minimum distance between region centers.
    private static final int maxDistance = 850; //maximum distance between region centers.
    private static final int borderRadius = 1000; //radius of the dynamic border.

    private long hourTime; //system time when its been 1 hour from battle start.
    private long startTime;

    private BossBar battleTimerBar;
    protected int timerTaskId;
    protected BukkitTask spawnTeamParticlesTask;
    protected BukkitTask borderParticlesTask;


    public PersistentBattle(RegionBattle pluginInstance){

        LOGGER.info("--Creating Battle--");

        this.plugin = pluginInstance;

        Bukkit.getPluginManager().registerEvents(this, plugin); //register events

        this.manager = Bukkit.getScoreboardManager();
        this.board = manager.getNewScoreboard();


        this.teamRed = board.registerNewTeam("Team_Red");
        this.teamBlue = board.registerNewTeam("Team_Blue");

        TextComponent objectiveName = Component.text("Player Kills").color(TextColor.color(0x9C8A));

        //create objective for score board
        objective = board.registerNewObjective("battleObjective", "dummy",objectiveName);
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        teamBlue.color(NamedTextColor.BLUE); //Set Colors for teams
        teamRed.color(NamedTextColor.RED);

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

        //Create BoundingBox Border

        int midX = Math.round(((redX + blueX)/2));
        int midZ = Math.round(((redZ + blueZ)/2));


        //create a new border using min/max corners
        border = new BoundingBox(midX-borderRadius, 0, midZ-borderRadius , midX+borderRadius, 320, midZ+borderRadius);

        LOGGER.info("Battle Border created. Center: " + border.getCenter());

        borderParticlesTask = borderParticles();



        ChestLoot chestLoot = new ChestLoot(4000, new Location(world, 3000, 80, 3000), plugin); //init chestLoot feature to generate loot in unopened chestLoot chests each game (only if pdc value is set)

        //1 hour = 3600000 miliseconds
        startTime = System.currentTimeMillis();
        hourTime =  System.currentTimeMillis() + 3600000 * 2; //1 hour from now * 2

        spawnTeamParticlesTask = startTeamParticles();

        this.battleTimerBar = Bukkit.createBossBar(ChatColor.GRAY+"The battle is on!", BarColor.RED, BarStyle.SEGMENTED_10);
        battleTimerBar.setProgress(1);
        battleTimer(); //keep track of time


    }

    protected void battleTimer(){

        RegionBattle plugin = RegionBattle.getPlugin(RegionBattle.class);
        timerTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            public void run() {

                if (System.currentTimeMillis() >= hourTime){ //Hour is up, end the battle and start a new one.

                    Bukkit.getScheduler().cancelTask(timerTaskId);
                    spawnTeamParticlesTask.cancel();
                    borderParticlesTask.cancel();
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
                    CommandSeek.pbattle = null;
                    CommandSendTeamChat.pbattle = null;

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
            } else { //blue bigger, assign player to red
                team = "red";
            }
            LOGGER.info("Assigning " + PlainTextComponentSerializer.plainText().serialize(player.displayName()) + " to Team " + team + ".");

            battleTimerBar.addPlayer(player); //display battle timer to player

            ItemStack compass = new ItemStack(Material.COMPASS, 1);

            //heal and feed the player
            player.setHealth(20);
            player.setFoodLevel(20);

            //TODO assign teams a random location as "home base" and send them there
            if (team.equals("red")) {
                player.teleport(redSpawn, PlayerTeleportEvent.TeleportCause.END_GATEWAY); //set the cause the end gateway, since this likely will never naturally happen
                player.getInventory().clear(); //clear inventory
                player.getInventory().addItem(compass); //give player a compass for seek
                redPlayers.add(player);
                teamRed.addEntry(PlainTextComponentSerializer.plainText().serialize(player.displayName())); //add player to team
                player.setScoreboard(board); //set player scoreboard
                Score score = objective.getScore(PlainTextComponentSerializer.plainText().serialize(player.displayName()));
                score.setScore(0);

                for(Player p : redPlayers){
                    p.sendMessage(ChatColor.RED+PlainTextComponentSerializer.plainText().serialize(player.displayName()) + " has joined the Red Team!");
                }
                for(Player p : bluePlayers){
                    p.sendMessage(ChatColor.RED+PlainTextComponentSerializer.plainText().serialize(player.displayName()) + " has joined the Red Team!");
                }
            }

            if (team.equals("blue")) {
                player.teleport(blueSpawn, PlayerTeleportEvent.TeleportCause.END_GATEWAY); //set the cause the end gateway, since this likely will never naturally happen
                player.getInventory().clear(); //clear inventory
                player.getInventory().addItem(compass); //give player a compass for seek
                bluePlayers.add(player);
                teamBlue.addEntry(PlainTextComponentSerializer.plainText().serialize(player.displayName())); //add player to team
                player.setScoreboard(board); //set player scoreboard
                Score score = objective.getScore(PlainTextComponentSerializer.plainText().serialize(player.displayName()));
                score.setScore(0);

                for(Player p : redPlayers){
                    p.sendMessage(ChatColor.BLUE+PlainTextComponentSerializer.plainText().serialize(player.displayName()) + " has joined the Blue Team!");
                }
                for(Player p : bluePlayers){
                    p.sendMessage(ChatColor.BLUE+PlainTextComponentSerializer.plainText().serialize(player.displayName()) + " has joined the Blue Team!");
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

    private BukkitTask startTeamParticles(){

        BukkitTask task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            for(Player p: bluePlayers){
                world.spawnParticle(Particle.REDSTONE,p.getLocation(), 1, 0, 0, 0, 0, new Particle.DustOptions(Color.BLUE, 8), true);
            }

            for(Player p: redPlayers){
                world.spawnParticle(Particle.REDSTONE,p.getLocation(), 1, 0, 0, 0, 0, new Particle.DustOptions(Color.RED, 8), true);
            }
        }, 0, 20*2); //20 ticks = 1 sec

        return task;
    }

    //Generate the border particles on a schedule as a way of showing the border of the battle.
    public BukkitTask borderParticles()
    {

        LOGGER.info("Initiating Border Particles..");


        Location minBorder = border.getMin().toLocation(world);
        Location maxBorder = border.getMax().toLocation(world);

        //Create List of points for vertical particles at corners of region
        List<Location> particleLocations = new ArrayList<>();

            for(int maxRedY=maxBorder.getBlockY();maxRedY>minBorder.getBlockY();maxRedY--) {
                //vertical corners
                particleLocations.add(new Location(world,maxBorder.getBlockX(),maxRedY,maxBorder.getBlockZ()));
                particleLocations.add(new Location(world,minBorder.getBlockX(),maxRedY,maxBorder.getBlockZ()));
                particleLocations.add(new Location(world,maxBorder.getBlockX(),maxRedY,minBorder.getBlockZ()));
                particleLocations.add(new Location(world,minBorder.getBlockX(),maxRedY,minBorder.getBlockZ()));
            }
            for(int maxRedX=maxBorder.getBlockX();maxRedX>minBorder.getBlockX();maxRedX--) {
                //Ground
                for(int y=62; y<=172 ; y+=10) {
                    particleLocations.add(new Location(world, maxRedX, y, maxBorder.getBlockZ()));
                    particleLocations.add(new Location(world, maxRedX, y, minBorder.getBlockZ()));
                }
            }
            for(int maxRedZ=maxBorder.getBlockZ();maxRedZ>minBorder.getBlockZ();maxRedZ--) {
                //Ground
                for(int y=62; y<=172 ; y+=10){
                    particleLocations.add(new Location(world,maxBorder.getBlockX(),y,maxRedZ));
                    particleLocations.add(new Location(world,minBorder.getBlockX(),y,maxRedZ));
                }

            }



        RegionBattle plugin = RegionBattle.getPlugin(RegionBattle.class);

        BukkitTask task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            for(Location location : particleLocations){ //Set Particle Border by looping through particleLocations List
                world.spawnParticle(Particle.REDSTONE,location, 0, 0, 0, 0, 0, new Particle.DustOptions(Color.GRAY, 3), true);
            }
        }, 0, 100);

        return task;

    }

    @EventHandler
    public void onPlayerRespawn (PlayerRespawnEvent event) { //handle when die and need to respawn to their team
        Player p = event.getPlayer();
        if(redPlayers.contains(p)){
            event.setRespawnLocation(redSpawn);
            ItemStack compass = new ItemStack(Material.COMPASS, 1);
            p.getInventory().addItem(compass); //give player a compass for seek
        }
        if(bluePlayers.contains(p)){
            event.setRespawnLocation(blueSpawn);
            ItemStack compass = new ItemStack(Material.COMPASS, 1);
            p.getInventory().addItem(compass); //give player a compass for seek
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

        if(event.getFrom().getWorld() == world){

            if(event.getCause() == PlayerTeleportEvent.TeleportCause.END_GATEWAY || event.getCause() == PlayerTeleportEvent.TeleportCause.UNKNOWN || event.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL){ //the specific cause we set earlier for a valid TP by this plugin.
                //Do nothing
            }
            else{

                if(event.getTo().getWorld() != world){
                    //Do nothing here, continue if to kick player.
                }
                else if (event.getFrom().distance(event.getTo()) < 80){ //This wont run unless they aren't tping in the same world
                    //Do nothing, they were teleported such a small distance that this is likely just anticheat or worldguard
                    return;
                }

                //kick player for teleporting
                Player p = event.getPlayer();
                if(redPlayers.contains(p) || bluePlayers.contains(p)){
                    p.sendMessage("You were removed from battle for teleporting.");
                    LOGGER.info("Removing player from battle because they teleported: " + PlainTextComponentSerializer.plainText().serialize(p.displayName()) +" Teleport Cause: " + event.getCause());
                    removePlayerFromBattle(p);
                }
            }

        }
    }

    private Point[] findTeamSpawns(){ //find team spawns

        Random rand = new Random();

        int rX = 0; //red points
        int rZ = 0;

        int bX = 0; //blue points
        int bZ = 0;

        Point r = new Point(rX,rZ); //create center points for red and blue
        Point b = new Point(bX,bZ);

        //get new points while the regions are too close together as is defined by minDistance/maxDistance or if block is liquid
        while((minDistance >= r.distance(b)) || (r.distance(b) >= maxDistance) || !world.getHighestBlockAt(bX,bZ).getType().isSolid() || !world.getHighestBlockAt(rX,rZ).getType().isSolid()){ //get new points while the regions are too close together as is defined by minDistance/maxDistance or if block is liquid
            rX = rand.nextInt(max - min + 1) + min;
            rZ = rand.nextInt(max - min + 1) + min;

            bX = rand.nextInt(max - min + 1) + min;
            bZ = rand.nextInt(max - min + 1) + min;

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

        LOGGER.info("Removing from battle: " + PlainTextComponentSerializer.plainText().serialize(player.displayName()));

        battleTimerBar.removePlayer(player); //display prep timer
        if(redPlayers.contains(player)){
            redPlayers.remove(player);
            teamRed.removeEntry(PlainTextComponentSerializer.plainText().serialize(player.displayName()));
            player.setScoreboard(manager.getNewScoreboard()); //manager.getNewScoreboard() will return a blank scoreboard
            player.teleport(spawn);
            player.getInventory().clear(); //clear inventory

            for(Player p : redPlayers){
                p.sendMessage(ChatColor.RED+PlainTextComponentSerializer.plainText().serialize(player.displayName()) + " has been removed from the Red Team!");
            }
            for(Player p : bluePlayers){
                p.sendMessage(ChatColor.RED+PlainTextComponentSerializer.plainText().serialize(player.displayName()) + " has been removed from the Red Team!");
            }
        }
        else if(bluePlayers.contains(player)){
            bluePlayers.remove(player);
            teamBlue.removeEntry(PlainTextComponentSerializer.plainText().serialize(player.displayName()));
            player.setScoreboard(manager.getNewScoreboard()); //manager.getNewScoreboard() will return a blank scoreboard
            player.teleport(spawn);
            player.getInventory().clear(); //clear inventory

            for(Player p : redPlayers){
                p.sendMessage(ChatColor.BLUE+PlainTextComponentSerializer.plainText().serialize(player.displayName()) + " has been removed from the Blue Team!");
            }
            for(Player p : bluePlayers){
                p.sendMessage(ChatColor.BLUE+PlainTextComponentSerializer.plainText().serialize(player.displayName()) + " has been removed from the Blue Team!");
            }
        }
        else{
            LOGGER.info("Something went wrong, player needed to be removed from battle but was not on a team!");
        }

        Score score = objective.getScore(player.getName()); //remove score if player leaves
        score.resetScore();

    }

    @EventHandler
    private void onPlayerDeath(PlayerDeathEvent e){

        if(e.getPlayer().getWorld() == world){ //manually track scores
            if(e.getPlayer().getKiller() instanceof Player){
                Player killer = e.getPlayer().getKiller();
                if(bluePlayers.contains(killer) || redPlayers.contains(killer)) {
                    Score score = objective.getScore(PlainTextComponentSerializer.plainText().serialize(killer.displayName()));
                    score.setScore(score.getScore() + 1);
                }
            }
        }
    }

    @EventHandler
    private void onPlayerMove(PlayerMoveEvent e){ //don't allow the player out of the border

        if(e.getPlayer().getWorld() == world){
            if(e.hasChangedBlock()){
                if(bluePlayers.contains(e.getPlayer()) || redPlayers.contains(e.getPlayer())){
                    if(!border.contains(e.getTo().toVector())){
                        e.setCancelled(true);
                        e.getPlayer().sendMessage("You can't leave the border!");
                    }
                }
            }
        }
    }

}
