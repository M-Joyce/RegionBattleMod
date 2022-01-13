package me.vetovius.regionbattle.regionbattle;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.RegionGroup;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import me.vetovius.regionbattle.RegionBattle;
import org.bukkit.*;
import org.bukkit.Color;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.logging.Logger;

public class Regions {

    private static final Logger LOGGER = Logger.getLogger( Regions.class.getName() );


    //TODO X,Z(min/max) MUST BE POSITIVE FOR PARTICLES TO WORK, May be worth fixing (Negative messes up the > in particle location loop)
    //for findRegionZones()
    private static final int regionSize = 300; //dimension region should be (a square at y=0 to max height)
    private static final int max = 2800; //max coordinate
    private static final int min = 800; //min coordinate
    private static final int minDistance = 400; //minimum distance between region centers.
    private static final int maxDistance = 800; //maximum distance between region centers.

    //Getting World
    public static final World world = Bukkit.getWorld("RegionBattle");
    protected static final Location spawn = new Location(world, 0,79,0);

    private RegionManager regionManager;

    protected ProtectedRegion regionRed;
    protected ProtectedRegion regionBlue;

    private DefaultDomain membersBlue; //blue team members
    private DefaultDomain membersRed; //red team members

    protected BukkitTask particleRunnerTask;


    public Regions(){

        LOGGER.info("--Creating Regions...--");
        //Setting up RegionContainer and RegionManager
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        regionManager = container.get(BukkitAdapter.adapt(world));

        setup();

    }

    //TODO use UUIDs throughout instead of names, particularly the toLowerCase() may cause issues if there can be players of the same name with different case letters?
    protected void setup(){

        //Get Points for random region locations
        Point[] redBlueRegionArr = findRegionZones();
        int redX = (int)redBlueRegionArr[0].getX();
        int redZ = (int)redBlueRegionArr[0].getY();
        int blueX = (int)redBlueRegionArr[1].getX();
        int blueZ = (int)redBlueRegionArr[1].getY();

        //Red Team Region////////////
        BlockVector3 minRed = BlockVector3.at(redX, 0, redZ);
        BlockVector3 maxRed = BlockVector3.at(redX+regionSize, 320, redZ + regionSize);
        regionRed = new ProtectedCuboidRegion("Team_Red", minRed, maxRed);

        //Red Team DefaultDomain for owners/members //TODO should server be owner?
        DefaultDomain ownersRed = regionRed.getOwners();
        ownersRed.addPlayer("server");
        regionRed.setOwners(ownersRed);

        //set region flags
        regionRed.setFlag(Flags.ENTRY, StateFlag.State.DENY);
        regionRed.setFlag(Flags.EXIT, StateFlag.State.DENY);
        regionRed.setFlag(Flags.EXIT.getRegionGroupFlag(), RegionGroup.ALL); //don't let members leave the region for now
        regionRed.setFlag(Flags.ENDERPEARL, StateFlag.State.DENY);
        regionRed.setFlag(Flags.ENDERPEARL.getRegionGroupFlag(), RegionGroup.ALL);

        regionManager.addRegion(regionRed); //add region to RegionManager (saves it)

        //Blue Team Region//////////////
        BlockVector3 minBlue = BlockVector3.at(blueX, 0, blueZ);
        BlockVector3 maxBlue = BlockVector3.at(blueX+regionSize, 320, blueZ+regionSize);

        regionBlue = new ProtectedCuboidRegion("Team_Blue", minBlue, maxBlue);


        //Blue Team DefaultDomain for owners/members //TODO should server be owner?
        DefaultDomain ownersBlue = regionBlue.getOwners();
        ownersBlue.addPlayer("server");
        regionBlue.setOwners(ownersBlue);

        //set region flags
        regionBlue.setFlag(Flags.ENTRY, StateFlag.State.DENY);
        regionBlue.setFlag(Flags.EXIT, StateFlag.State.DENY);
        regionBlue.setFlag(Flags.EXIT.getRegionGroupFlag(), RegionGroup.ALL); //don't let members leave the region for now
        regionBlue.setFlag(Flags.ENDERPEARL, StateFlag.State.DENY);
        regionBlue.setFlag(Flags.ENDERPEARL.getRegionGroupFlag(), RegionGroup.ALL);

        regionManager.addRegion(regionBlue); //add region to RegionManager (saves it)
        particleRunnerTask = regionParticles(minBlue,maxBlue,minRed,maxRed);

        ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
        Bukkit.dispatchCommand(console,"wg flushstates"); //flushstates needed as players are in region during flag change.
    }

    //Generate the region particles on a schedule as a way of showing the border of the region.
    public BukkitTask regionParticles(BlockVector3 minBlue, BlockVector3 maxBlue, BlockVector3 minRed, BlockVector3 maxRed)
    {

        LOGGER.info("Initiating Particles..");

        //Create List of points for vertical particles at corners of region
        List<Location> particleLocations = new ArrayList<>();
        for(int maxBlueY=maxBlue.getBlockY();maxBlueY>minBlue.getBlockY();maxBlueY--){ //Blue Region Particles
            //Vertical corners
            particleLocations.add(new Location(world,maxBlue.getBlockX(),maxBlueY,maxBlue.getBlockZ()));
            particleLocations.add(new Location(world,minBlue.getBlockX(),maxBlueY,maxBlue.getBlockZ()));
            particleLocations.add(new Location(world,maxBlue.getBlockX(),maxBlueY,minBlue.getBlockZ()));
            particleLocations.add(new Location(world,minBlue.getBlockX(),maxBlueY,minBlue.getBlockZ()));
        }
        for(int maxRedY=maxRed.getBlockY();maxRedY>minRed.getBlockY();maxRedY--) { //Red Region Particles
            //vertical corners
            particleLocations.add(new Location(world,maxRed.getBlockX(),maxRedY,maxRed.getBlockZ()));
            particleLocations.add(new Location(world,minRed.getBlockX(),maxRedY,maxRed.getBlockZ()));
            particleLocations.add(new Location(world,maxRed.getBlockX(),maxRedY,minRed.getBlockZ()));
            particleLocations.add(new Location(world,minRed.getBlockX(),maxRedY,minRed.getBlockZ()));
        }
        for(int maxRedX=maxRed.getBlockX();maxRedX>minRed.getBlockX();maxRedX--) { //Red Region Particles
            //Ground
            for(int y=62; y<=172 ; y+=10) {
                particleLocations.add(new Location(world,maxRedX,y,maxRed.getBlockZ()));
                particleLocations.add(new Location(world,maxRedX,y,minRed.getBlockZ()));
            }

        }
        for(int maxBlueX=maxBlue.getBlockX();maxBlueX>minBlue.getBlockX();maxBlueX--) { //Blue Region Particles - North and South Boundary
            //Ground
            for(int y=62; y<=172 ; y+=10) {
                particleLocations.add(new Location(world,maxBlueX,y,maxBlue.getBlockZ()));
                particleLocations.add(new Location(world,maxBlueX,y,minBlue.getBlockZ()));
            }
        }
        for(int maxRedZ=maxRed.getBlockZ();maxRedZ>minRed.getBlockZ();maxRedZ--) { //Red Region Particles
            //Ground
            for(int y=62; y<=172 ; y+=10) {
                particleLocations.add(new Location(world,maxRed.getBlockX(),y,maxRedZ));
                particleLocations.add(new Location(world,minRed.getBlockX(),y,maxRedZ));
            }
        }
        for(int maxBlueZ=maxBlue.getBlockZ();maxBlueZ>minBlue.getBlockZ();maxBlueZ--) { //Blue Region Particles - West and East Boundary
            //Ground
            for(int y=62; y<=172 ; y+=10) {
                particleLocations.add(new Location(world,maxBlue.getBlockX(),y,maxBlueZ));
                particleLocations.add(new Location(world,minBlue.getBlockX(),y,maxBlueZ));
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

    protected void assignRegionMembers(Battle battle){

        membersRed = regionRed.getMembers();
        membersBlue = regionBlue.getMembers();

        for(Player player : battle.redPlayers){ //add red players
            membersRed.addPlayer(player.getUniqueId());
        }

        for(Player player : battle.bluePlayers){ //add blue players
            membersBlue.addPlayer(player.getUniqueId());
        }

        regionRed.setMembers(membersRed);
        regionBlue.setMembers(membersBlue);
    }

    private Point[] findRegionZones(){ //find zones for region

        Random rand = new Random();

        int rX = 0; //red points
        int rZ = 0;

        int bX = 0; //blue points
        int bZ = 0;

        Point r = new Point(rX,rZ); //create center points for red and blue
        Point b = new Point(bX,bZ);

        //get new points while the regions are too close together as is defined by minDistance/maxDistance or if block is liquid
        while((minDistance >= r.distance(b)) || (r.distance(b) >= maxDistance) || !world.getHighestBlockAt(((bX+(bX+regionSize))/2),((bZ+(bZ+regionSize))/2)).getType().isSolid() || !world.getHighestBlockAt(((rX+(rX+regionSize))/2),((rZ+(rZ+regionSize))/2)).getType().isSolid()){ //get new points while the regions are too close together as is defined by minDistance/maxDistance or if block is liquid
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

    protected void removeFlagsForBattle(){
        //Remove Flags
        regionBlue.setFlag(Flags.EXIT, StateFlag.State.ALLOW);
        regionBlue.setFlag(Flags.ENTRY, StateFlag.State.ALLOW);
        regionBlue.setFlag(Flags.EXIT.getRegionGroupFlag(), RegionGroup.ALL);
        regionBlue.setFlag(Flags.ENTRY.getRegionGroupFlag(), RegionGroup.ALL);
        regionBlue.setFlag(Flags.PVP, StateFlag.State.ALLOW);
        regionBlue.setFlag(Flags.ENDERPEARL, StateFlag.State.ALLOW);
        regionBlue.setFlag(Flags.ENDERPEARL.getRegionGroupFlag(), RegionGroup.ALL);


        regionRed.setFlag(Flags.EXIT, StateFlag.State.ALLOW);
        regionRed.setFlag(Flags.ENTRY, StateFlag.State.ALLOW);
        regionRed.setFlag(Flags.EXIT.getRegionGroupFlag(), RegionGroup.ALL);
        regionRed.setFlag(Flags.ENTRY.getRegionGroupFlag(), RegionGroup.ALL);
        regionRed.setFlag(Flags.PVP, StateFlag.State.ALLOW);
        regionRed.setFlag(Flags.ENDERPEARL, StateFlag.State.ALLOW);
        regionRed.setFlag(Flags.ENDERPEARL.getRegionGroupFlag(), RegionGroup.ALL);

        ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
        Bukkit.dispatchCommand(console,"wg flushstates"); //flushstates needed as players are in region during flag change.
    }

    protected void removeRegions(){
        LOGGER.info("Removing Teams");
        if(regionManager.getRegion("Team_Blue") != null) {
            regionManager.removeRegion("Team_Blue");
        }
        if(regionManager.getRegion("Team_Red") != null) {
            regionManager.removeRegion("Team_Red");
        }
        ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
        Bukkit.dispatchCommand(console,"wg flushstates"); //flushstates needed as players are in region during flag change.
    }

}
