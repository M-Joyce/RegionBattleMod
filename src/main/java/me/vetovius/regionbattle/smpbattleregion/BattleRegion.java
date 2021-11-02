package me.vetovius.regionbattle.smpbattleregion;

import me.vetovius.regionbattle.RegionBattle;
import me.vetovius.regionbattle.persistentbattle.PersistentBattle;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
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

    private Location battleRegionCenter;


    public BattleRegion(RegionBattle pluginInstance){
        this.pluginInstance = pluginInstance;
        findLocationForBattleRegion(); //find a suitable location for the battle region
        initBattleRegionParticles();


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
    }

    private void initBattleRegionParticles(){

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

        int id = Bukkit.getScheduler().scheduleSyncRepeatingTask(pluginInstance, new Runnable() {
            public void run() {
                for(Location location : particleLocations){ //Set Particle Border by looping through particleLocations List
                    smpWorld.spawnParticle(Particle.DRIPPING_OBSIDIAN_TEAR,location, 3, 0, 0, 0, 1, null, true);
                }
            }}, 0, 60); //second parameter is the frequency in ticks of the flash, 100 = flash every 100 ticks(5 seconds).


    }

}
