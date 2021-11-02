package me.vetovius.regionbattle.smpbattleregion;

import me.vetovius.regionbattle.RegionBattle;
import me.vetovius.regionbattle.persistentbattle.PersistentBattle;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.awt.*;
import java.util.Random;
import java.util.logging.Logger;

public class BattleRegion {

    private static final Logger LOGGER = Logger.getLogger( BattleRegion.class.getName() );
    private RegionBattle pluginInstance;

    public static World smpWorld = Bukkit.getWorld("world");
    private static final int range = 4000; //how far can the zone be from spawn + or -
    private static final int minDistanceFromSpawn = 550; //can't be closer than this to spawn

    private Location battleRegionCenter;


    public BattleRegion(RegionBattle pluginInstance){
        this.pluginInstance = pluginInstance;

        findLocationForBattleRegion();
    }

    private void findLocationForBattleRegion(){
        int x = 0; //initial points
        int z = 0;

        Point spawn = new Point(0,0);
        Random r = new Random();

        Point point = new Point(x,z); //create initial center point
        //This will be a bit confusing, in the context of Point, z is y.

        //get new points while the regions are too close together as is defined by minDistance/maxDistance or if block is liquid
        while((minDistanceFromSpawn > point.distance(spawn)) || (Math.abs(point.x) > range) || (Math.abs(point.y) > range) || !smpWorld.getHighestBlockAt(x,z).getType().isSolid()){ //get new points while the regions are too close together as is defined by minDistance/maxDistance or if block is liquid
            x = r.nextInt((range*2))-range;
            z = r.nextInt((range*2))-range;

            point = new Point(x,z);

        }

        LOGGER.info("battleRegionCenter Found. X: " + x + " Z: " + z);
        battleRegionCenter = new Location(smpWorld,x,smpWorld.getHighestBlockYAt(x,z)+1,z); //set battleRegionCenter
    }

}
