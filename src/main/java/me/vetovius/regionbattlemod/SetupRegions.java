package me.vetovius.regionbattlemod;

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
import org.bukkit.*;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public class SetupRegions implements Listener
{

    private static final Logger LOGGER = Logger.getLogger( SetupRegions.class.getName() );

    protected static void setup(){
        //TODO Maybe randomize region locations?
        //TODO Parameterizable/Configurable Region Sizes, announcement at game start of region size
        //TODO Do something about ocean region spawns. Not ideal for a team to start entirely in the ocean.

        //Getting World
        World world = Bukkit.getWorld("world");

        //Setting up RegionContainer and RegionManager
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regions = container.get(BukkitAdapter.adapt(world));

        //Red Team Region////////////
        //TODO XYZ MUST BE POSITIVE FOR PARTICLES TO WORK, May be worth fixing (Negative messes up the >)
        BlockVector3 minRed = BlockVector3.at(50, 0, 50);
        BlockVector3 maxRed = BlockVector3.at(75, 80, 75);
        ProtectedRegion regionRed = new ProtectedCuboidRegion("Team_Red", minRed, maxRed);

        //Red Team DefaultDomain for owners/members //TODO Make this dynamic based on logged in players, should server be owner?
        DefaultDomain ownersRed = regionRed.getOwners();
        DefaultDomain membersRed = regionRed.getMembers();
        ownersRed.addPlayer("server");
        regionRed.setOwners(ownersRed);

        //set region flags
        regionRed.setFlag(Flags.ENTRY, StateFlag.State.DENY);
        regionRed.setFlag(Flags.EXIT, StateFlag.State.DENY);
        regionRed.setFlag(Flags.EXIT.getRegionGroupFlag(), RegionGroup.ALL); //don't let members leave the region for now

        regions.addRegion(regionRed); //add region to RegionManager (saves it)

        //Blue Team Region//////////////
        //TODO XYZ MUST BE POSITIVE FOR PARTICLES TO WORK, May be worth fixing (Negative messes up the >)
        BlockVector3 minBlue = BlockVector3.at(100, 0, 100);
        BlockVector3 maxBlue = BlockVector3.at(125, 80, 125);
        ProtectedRegion regionBlue = new ProtectedCuboidRegion("Team_Blue", minBlue, maxBlue);

        //Blue Team DefaultDomain for owners/members //TODO Make this dynamic based on logged in players, should server be owner?
        DefaultDomain ownersBlue = regionBlue.getOwners();
        DefaultDomain membersBlue = regionBlue.getMembers();
        ownersBlue.addPlayer("server");
        regionBlue.setOwners(ownersBlue);

        //set region flags
        regionBlue.setFlag(Flags.ENTRY, StateFlag.State.DENY);
        regionBlue.setFlag(Flags.EXIT, StateFlag.State.DENY);
        regionBlue.setFlag(Flags.EXIT.getRegionGroupFlag(), RegionGroup.ALL); //don't let members leave the region for now

        regions.addRegion(regionBlue); //add region to RegionManager (saves it)
        int particleRunnerID = regionParticles(minBlue,maxBlue,minRed,maxRed);

        assignRegionMembers(regionRed,regionBlue);
        teleportTeamsToRegions(regionRed,regionBlue);
        battleTimer(regionRed,regionBlue,particleRunnerID);
    }

    //Generate the region particles on a schedule as a way of showing the border of the region.
    public static int regionParticles(BlockVector3 minBlue, BlockVector3 maxBlue, BlockVector3 minRed, BlockVector3 maxRed)
    {

        LOGGER.info("Initiating Particles..");
        World world = Bukkit.getWorld("world");

        //Create List of points for vertical particles at corners of region
        List<Location> particleLocations = new ArrayList<Location>();
        for(int maxBlueY=maxBlue.getBlockY();maxBlueY>minBlue.getBlockY();maxBlueY--){ //Blue Region Particles
            particleLocations.add(new Location(world,maxBlue.getBlockX(),maxBlueY,maxBlue.getBlockZ()));
            particleLocations.add(new Location(world,minBlue.getBlockX(),maxBlueY,maxBlue.getBlockZ()));
            particleLocations.add(new Location(world,maxBlue.getBlockX(),maxBlueY,minBlue.getBlockZ()));
            particleLocations.add(new Location(world,minBlue.getBlockX(),maxBlueY,minBlue.getBlockZ()));
        }
        for(int maxRedY=maxRed.getBlockY();maxRedY>minRed.getBlockY();maxRedY--) { //Red Region Particles
            particleLocations.add(new Location(world,maxRed.getBlockX(),maxRedY,maxRed.getBlockZ()));
            particleLocations.add(new Location(world,minRed.getBlockX(),maxRedY,maxRed.getBlockZ()));
            particleLocations.add(new Location(world,maxRed.getBlockX(),maxRedY,minRed.getBlockZ()));
            particleLocations.add(new Location(world,minRed.getBlockX(),maxRedY,minRed.getBlockZ()));
        }
        for(int maxRedX=maxRed.getBlockX();maxRedX>minRed.getBlockX();maxRedX--) { //Red Region Particles
            particleLocations.add(new Location(world,maxRedX,maxRed.getBlockY(),maxRed.getBlockZ()));
            particleLocations.add(new Location(world,maxRedX,minRed.getBlockY(),maxRed.getBlockZ()));
            particleLocations.add(new Location(world,maxRedX,maxRed.getBlockY(),minRed.getBlockZ()));
            particleLocations.add(new Location(world,maxRedX,minRed.getBlockY(),minRed.getBlockZ()));
        }
        for(int maxBlueX=maxBlue.getBlockX();maxBlueX>minBlue.getBlockX();maxBlueX--) { //Blue Region Particles
            particleLocations.add(new Location(world,maxBlueX,maxBlue.getBlockY(),maxBlue.getBlockZ()));
            particleLocations.add(new Location(world,maxBlueX,minBlue.getBlockY(),maxBlue.getBlockZ()));
            particleLocations.add(new Location(world,maxBlueX,maxBlue.getBlockY(),minBlue.getBlockZ()));
            particleLocations.add(new Location(world,maxBlueX,minBlue.getBlockY(),minBlue.getBlockZ()));
        }
        for(int maxRedZ=maxRed.getBlockZ();maxRedZ>minRed.getBlockZ();maxRedZ--) { //Red Region Particles
            particleLocations.add(new Location(world,maxRed.getBlockX(),maxRed.getBlockY(),maxRedZ));
            particleLocations.add(new Location(world,maxRed.getBlockX(),minRed.getBlockY(),maxRedZ));
            particleLocations.add(new Location(world,minRed.getBlockX(),maxRed.getBlockY(),maxRedZ));
            particleLocations.add(new Location(world,minRed.getBlockX(),minRed.getBlockY(),maxRedZ));
        }
        for(int maxBlueZ=maxBlue.getBlockZ();maxBlueZ>minBlue.getBlockZ();maxBlueZ--) { //Blue Region Particles
            particleLocations.add(new Location(world,maxBlue.getBlockX(),maxBlue.getBlockY(),maxBlueZ));
            particleLocations.add(new Location(world,maxBlue.getBlockX(),minBlue.getBlockY(),maxBlueZ));
            particleLocations.add(new Location(world,minBlue.getBlockX(),maxBlue.getBlockY(),maxBlueZ));
            particleLocations.add(new Location(world,minBlue.getBlockX(),minBlue.getBlockY(),maxBlueZ));
        }

        RegionBattleMod plugin = RegionBattleMod.getPlugin(RegionBattleMod.class);
        int id = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            public void run() {
                for(Location location : particleLocations){ //Set Particle Border by looping through particleLocations List
                    world.spawnParticle(Particle.COMPOSTER,location.getBlockX(), location.getBlockY(), location.getBlockZ(), 10);
                }
            }}, 0, 100);
        return id;
    }

    public static void assignRegionMembers(ProtectedRegion regionRed, ProtectedRegion regionBlue){

        LOGGER.info("Assigning Teams..");

        DefaultDomain membersBlue = regionBlue.getMembers(); //blue team members
        DefaultDomain membersRed = regionRed.getMembers(); //red team members

        List<Player> players = new ArrayList<>(Bukkit.getWorld("world").getPlayers());
        Collections.shuffle(players); //randomize

        //Create two lists to hold the team members
        ArrayList<Player> redPlayersList = new ArrayList<>();
        ArrayList<Player> bluePlayersList = new ArrayList<>();
        // add the first half of players to Red
        redPlayersList.addAll(players.subList(0, players.size() / 2 + players.size()%2));
        // and the second half to Blue
        bluePlayersList.addAll(players.subList(players.size() / 2 + players.size()%2, players.size()));

        for(Player player : redPlayersList){ //add red players
            membersRed.addPlayer(player.getDisplayName());
        }

        for(Player player : bluePlayersList){ //add blue players
            membersBlue.addPlayer(player.getDisplayName());
        }

    }

    public static void teleportTeamsToRegions(ProtectedRegion regionRed, ProtectedRegion regionBlue) {

        LOGGER.info("Teleporting players..");
        //Red Team
        int rmaxX = regionRed.getMaximumPoint().getBlockX();
        int rmaxY = regionRed.getMaximumPoint().getBlockY();
        int rmaxZ = regionRed.getMaximumPoint().getBlockZ();

        int rminX = regionRed.getMinimumPoint().getBlockX();
        int rminY = regionRed.getMinimumPoint().getBlockY();
        int rminZ = regionRed.getMinimumPoint().getBlockZ();

        Location redMidPoint = new Location(Bukkit.getWorld("world"),(rmaxX+rminX)/2,(rmaxY+rminY )/2,(rmaxZ+rminZ )/2);
        redMidPoint.setY(Bukkit.getWorld("world").getHighestBlockYAt(redMidPoint)+1);
        LOGGER.info("Teleporting Red Team to: "+redMidPoint);
        for(String name : regionRed.getMembers().getPlayers()){
            Bukkit.getPlayer(name).teleport(redMidPoint);
        }

        //Blue Team
        int bmaxX = regionBlue.getMaximumPoint().getBlockX();
        int bmaxY = regionBlue.getMaximumPoint().getBlockY();
        int bmaxZ = regionBlue.getMaximumPoint().getBlockZ();

        int bminX = regionBlue.getMinimumPoint().getBlockX();
        int bminY = regionBlue.getMinimumPoint().getBlockY();
        int bminZ = regionBlue.getMinimumPoint().getBlockZ();

        Location blueMidPoint = new Location(Bukkit.getWorld("world"),(bmaxX+bminX)/2,(bmaxY+bminY )/2,(bmaxZ+bminZ )/2);
        blueMidPoint.setY(Bukkit.getWorld("world").getHighestBlockYAt(blueMidPoint)+1);
        LOGGER.info("Teleporting Blue Team to: "+blueMidPoint);

        for(String name : regionBlue.getMembers().getPlayers()){
            Bukkit.getPlayer(name).teleport(blueMidPoint);
        }

    }

    public static void battleTimer(ProtectedRegion regionRed, ProtectedRegion regionBlue, int particleRunnerID){
        RegionBattleMod plugin = RegionBattleMod.getPlugin(RegionBattleMod.class);
        Bukkit.broadcastMessage("Prepare for Battle! Gather Supplies!");
        int id = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            public void run() {
                Bukkit.broadcastMessage("Let the Battle Begin! Force fields have come down!");

                //Remove Flags
                regionBlue.setFlag(Flags.EXIT, StateFlag.State.ALLOW);
                regionBlue.setFlag(Flags.ENTRY, StateFlag.State.ALLOW);
                regionBlue.setFlag(Flags.EXIT.getRegionGroupFlag(), RegionGroup.ALL);
                regionBlue.setFlag(Flags.ENTRY.getRegionGroupFlag(), RegionGroup.ALL);

                regionRed.setFlag(Flags.EXIT, StateFlag.State.ALLOW);
                regionRed.setFlag(Flags.ENTRY, StateFlag.State.ALLOW);
                regionRed.setFlag(Flags.EXIT.getRegionGroupFlag(), RegionGroup.ALL);
                regionRed.setFlag(Flags.ENTRY.getRegionGroupFlag(), RegionGroup.ALL);

                ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
                Bukkit.dispatchCommand(console,"wg flushstates"); //flushstates needed as players are in region during flag change.

                Bukkit.getScheduler().cancelTask(particleRunnerID); //Cancel Particle Effects at boundary

            }}, 20*60*1L);
    }


}
