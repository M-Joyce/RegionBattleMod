package me.vetovius.regionbattle.smpbattleregion;

import me.vetovius.regionbattle.CommandSeek;
import me.vetovius.regionbattle.CommandSendTeamChat;
import me.vetovius.regionbattle.RegionBattle;
import me.vetovius.regionbattle.persistentbattle.CommandStartPersistentBattle;
import me.vetovius.regionbattle.persistentbattle.PersistentBattle;
import org.bukkit.*;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

public class BattleRegion {

    private static final Logger LOGGER = Logger.getLogger( BattleRegion.class.getName() );
    private RegionBattle pluginInstance;

    public static World smpWorld = Bukkit.getWorld("world");
    private static final int range = 4000; //how far can the zone be from spawn + or -
    private static final int minDistanceFromSpawn = 550; //can't be closer than this to spawn
    private static final int radius = 10; //how big should the battle region square radius be in blocks?

    private long startTime = 0;
    private long duration = 0;

    private int particleTaskId = 0;
    private int timerTaskId = 0;
    private int checkForPlayers = 0;

    private Location battleRegionCenter;

    private ArrayList<Player> playersInZone = new ArrayList<Player>();


    public BattleRegion(RegionBattle pluginInstance){
        this.pluginInstance = pluginInstance;
        findLocationForBattleRegion(); //find a suitable location for the battle region
        particleTaskId=initBattleRegionParticles();

        //1 hour = 3600000 miliseconds
        startTime = System.currentTimeMillis();
        duration =  System.currentTimeMillis() + 3600000; //1 hour from now

        startBattleRegionTimer();
    }

    protected void startBattleRegionTimer(){

        timerTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(pluginInstance, new Runnable() {
            public void run() {

                if (System.currentTimeMillis() >= duration){ //Time is up, end the battle region.

                    Bukkit.getScheduler().cancelTask(timerTaskId);
                    Bukkit.getScheduler().cancelTask(particleTaskId);
                    Bukkit.getScheduler().cancelTask(checkForPlayers);

                    for(Player p : smpWorld.getPlayers()){
                        p.sendMessage("The battle region has vanished!");
                    }


                }

            }}, 20, 20*30); //repeat task every 30 seconds

        //Actual Battle Region Logic goes here.
        checkForPlayers = Bukkit.getScheduler().scheduleSyncRepeatingTask(pluginInstance, new Runnable() {
            public void run() {
                playersInZone = (ArrayList<Player>) battleRegionCenter.getNearbyPlayers(radius); //updating the playersInZone list if zone is empty or contains players.
                //For Debugging LOGGER.info(playersInZone.toString());

            }}, 20, 20*5); //repeat task every 5 seconds, assuming 20TPS.

    }

    private void findLocationForBattleRegion(){
        int x = 0; //initial points
        int z = 0;

        Point spawn = new Point(0,0);
        Random r = new Random();

        Point point = new Point(x,z); //create initial center point
        //This will be a bit confusing, in the context of Point, z is y.

        //get new points while the regions are not in requirements as is defined by range or if block is liquid
        while((minDistanceFromSpawn > point.distance(spawn)) || (Math.abs(point.x) > range) || (Math.abs(point.y) > range) || !smpWorld.getHighestBlockAt(x,z).getType().isSolid()){ //get new points while the regions are too close together as is defined by minDistance/maxDistance or if block is liquid
            x = r.nextInt((range*2))-range;
            z = r.nextInt((range*2))-range;

            point = new Point(x,z);

        }

        LOGGER.info("battleRegionCenter Found. X: " + x + " Z: " + z);
        battleRegionCenter = new Location(smpWorld,x,smpWorld.getHighestBlockYAt(x,z)+1,z); //set battleRegionCenter
        for(Player p : smpWorld.getPlayers()){
            p.sendMessage("A BattleRegion has appeared at X: " + x + " Z: "+ z +". Control it for a reward!");
        }
    }

    private int initBattleRegionParticles(){

        //Generate max and min points of a sqaure based on the battleRegionCenter
        Point max = new Point(battleRegionCenter.getBlockX()+radius,battleRegionCenter.getBlockZ()+radius);
        Point min = new Point(battleRegionCenter.getBlockX()-radius,battleRegionCenter.getBlockZ()-radius);

        //store the locations of our particles in this list
        List<Location> particleLocations = new ArrayList<>();

        for(int maxX=max.x;maxX>min.x;maxX--) { //North South Boundary
            //Ground
            particleLocations.add(new Location(smpWorld,maxX,smpWorld.getHighestBlockYAt(maxX,max.y)+1,max.y));
            particleLocations.add(new Location(smpWorld,maxX,smpWorld.getHighestBlockYAt(maxX,min.y)+1,min.y));
        }
        for(int maxZ=max.y;maxZ>min.y;maxZ--) { //West and East Boundary
            //Ground
            particleLocations.add(new Location(smpWorld,max.x,smpWorld.getHighestBlockYAt(max.x,maxZ)+1,maxZ));
            particleLocations.add(new Location(smpWorld,min.x,smpWorld.getHighestBlockYAt(min.x,maxZ)+1,maxZ));
        }

        int particleTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(pluginInstance, new Runnable() {
            public void run() {
                for(Location location : particleLocations){ //Set Particle Border by looping through particleLocations List
                    smpWorld.spawnParticle(Particle.SPELL_WITCH,location, 10, 0, 0.5, 0, 0, null, true);
                }
            }}, 0, 60); //second parameter is the frequency in ticks of the flash, 100 = flash every 100 ticks(5 seconds).

        return particleTaskId;
    }

}
