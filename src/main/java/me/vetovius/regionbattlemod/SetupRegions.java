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
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.logging.Logger;

public class SetupRegions implements Listener
{

    private static final Logger LOGGER = Logger.getLogger( SetupRegions.class.getName() );

    private static int regionSize = 300; //dimension region should be (a square at y=0 to max height)

    //for findRegionZones()
    //TODO X,Z(min/max) MUST BE POSITIVE FOR PARTICLES TO WORK, May be worth fixing (Negative messes up the > in particle location loop)
    private static int max = 2800; //max coordinate
    private static int min = 800; //min coordinate
    private static int minDistance = 500; //minimum distance between region centers.
    private static int maxDistance = 1200; //maximum distance between region centers.

    private static int prepareMinutes = 5;

    private static RegionManager regions;

    private static ProtectedRegion regionRed;
    private static ProtectedRegion regionBlue;

    private static Location spawn = new Location(Bukkit.getWorld("RegionBattle"), 0,79,0);

    private static ArrayList<Player> redPlayers = new ArrayList<>();
    private static ArrayList<Player> bluePlayers = new ArrayList<>();

    private static int battleTimerID;

//TODO use UUIDs throughout instead of names, particularly the toLowerCase() may cause issues if there can be players of the same name with different case letters?
    protected static void setup(){

        //Getting World
        World world = Bukkit.getWorld("RegionBattle");

        //Setting up RegionContainer and RegionManager
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        regions = container.get(BukkitAdapter.adapt(world));

        //Get Points for random region locations
        Point[] redBlueRegionArr = findRegionZones();
        int redX = (int)redBlueRegionArr[0].getX();
        int redZ = (int)redBlueRegionArr[0].getY();
        int blueX = (int)redBlueRegionArr[1].getX();
        int blueZ = (int)redBlueRegionArr[1].getY();

        //Red Team Region////////////
        BlockVector3 minRed = BlockVector3.at(redX, 0, redZ);
        BlockVector3 maxRed = BlockVector3.at(redX+regionSize, 255, redZ + regionSize);
        regionRed = new ProtectedCuboidRegion("Team_Red", minRed, maxRed);

        //Red Team DefaultDomain for owners/members //TODO should server be owner?
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
        BlockVector3 minBlue = BlockVector3.at(blueX, 0, blueZ);
        BlockVector3 maxBlue = BlockVector3.at(blueX+regionSize, 255, blueZ+regionSize);

        regionBlue = new ProtectedCuboidRegion("Team_Blue", minBlue, maxBlue);


        //Blue Team DefaultDomain for owners/members //TODO should server be owner?
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
        World world = Bukkit.getWorld("RegionBattle");

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
            particleLocations.add(new Location(world,maxRedX,world.getHighestBlockYAt(maxRedX,maxRed.getBlockZ())+1,maxRed.getBlockZ()));
            particleLocations.add(new Location(world,maxRedX,world.getHighestBlockYAt(maxRedX,maxRed.getBlockZ())+1,maxRed.getBlockZ()));
            particleLocations.add(new Location(world,maxRedX,world.getHighestBlockYAt(maxRedX,minRed.getBlockZ())+1,minRed.getBlockZ()));
            particleLocations.add(new Location(world,maxRedX,world.getHighestBlockYAt(maxRedX,minRed.getBlockZ())+1,minRed.getBlockZ()));
        }
        for(int maxBlueX=maxBlue.getBlockX();maxBlueX>minBlue.getBlockX();maxBlueX--) { //Blue Region Particles
            //Ground
            particleLocations.add(new Location(world,maxBlueX,world.getHighestBlockYAt(maxBlueX,maxBlue.getBlockZ())+1,maxBlue.getBlockZ()));
            particleLocations.add(new Location(world,maxBlueX,world.getHighestBlockYAt(maxBlueX,maxBlue.getBlockZ())+1,maxBlue.getBlockZ()));
            particleLocations.add(new Location(world,maxBlueX,world.getHighestBlockYAt(maxBlueX,minBlue.getBlockZ())+1,minBlue.getBlockZ()));
            particleLocations.add(new Location(world,maxBlueX,world.getHighestBlockYAt(maxBlueX,minBlue.getBlockZ())+1,minBlue.getBlockZ()));
        }
        for(int maxRedZ=maxRed.getBlockZ();maxRedZ>minRed.getBlockZ();maxRedZ--) { //Red Region Particles
            //Ground
            particleLocations.add(new Location(world,maxRed.getBlockX(),world.getHighestBlockYAt(maxRed.getBlockX(),maxRedZ)+1,maxRedZ));
            particleLocations.add(new Location(world,maxRed.getBlockX(),world.getHighestBlockYAt(maxRed.getBlockX(),maxRedZ)+1,maxRedZ));
            particleLocations.add(new Location(world,minRed.getBlockX(),world.getHighestBlockYAt(minRed.getBlockX(),maxRedZ)+1,maxRedZ));
            particleLocations.add(new Location(world,minRed.getBlockX(),world.getHighestBlockYAt(minRed.getBlockX(),maxRedZ)+1,maxRedZ));
        }
        for(int maxBlueZ=maxBlue.getBlockZ();maxBlueZ>minBlue.getBlockZ();maxBlueZ--) { //Blue Region Particles
            //Ground
            particleLocations.add(new Location(world,maxBlue.getBlockX(),world.getHighestBlockYAt(maxBlue.getBlockX(),maxBlueZ)+1,maxBlueZ));
            particleLocations.add(new Location(world,maxBlue.getBlockX(),world.getHighestBlockYAt(maxBlue.getBlockX(),maxBlueZ)+1,maxBlueZ));
            particleLocations.add(new Location(world,minBlue.getBlockX(),world.getHighestBlockYAt(maxBlue.getBlockX(),maxBlueZ)+1,maxBlueZ));
            particleLocations.add(new Location(world,minBlue.getBlockX(),world.getHighestBlockYAt(maxBlue.getBlockX(),maxBlueZ)+1,maxBlueZ));
        }

        RegionBattleMod plugin = RegionBattleMod.getPlugin(RegionBattleMod.class);
        int id = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            public void run() {
                for(Location location : particleLocations){ //Set Particle Border by looping through particleLocations List
                    world.spawnParticle(Particle.COMPOSTER,location.getBlockX(), location.getBlockY(), location.getBlockZ(), 10);
                }
            }}, 0, 60); //second parameter is the frequency in ticks of the flash, 100 = flash every 100 ticks(5 seconds).
        return id;
    }

    public static void assignRegionMembers(ProtectedRegion regionRed, ProtectedRegion regionBlue){

        LOGGER.info("Assigning Teams..");

        DefaultDomain membersBlue = regionBlue.getMembers(); //blue team members
        DefaultDomain membersRed = regionRed.getMembers(); //red team members

        List<Player> players = new ArrayList<>(Bukkit.getWorld("RegionBattle").getPlayers());
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
            redPlayers.add(player);
        }

        for(Player player : bluePlayersList){ //add blue players
            membersBlue.addPlayer(player.getDisplayName());
            bluePlayers.add(player);
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

        Location redMidPoint = new Location(Bukkit.getWorld("RegionBattle"),(rmaxX+rminX)/2,(rmaxY+rminY )/2,(rmaxZ+rminZ )/2);
        redMidPoint.setY(Bukkit.getWorld("RegionBattle").getHighestBlockYAt(redMidPoint)+1);
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

        Location blueMidPoint = new Location(Bukkit.getWorld("RegionBattle"),(bmaxX+bminX)/2,(bmaxY+bminY )/2,(bmaxZ+bminZ )/2);
        blueMidPoint.setY(Bukkit.getWorld("RegionBattle").getHighestBlockYAt(blueMidPoint)+1);
        LOGGER.info("Teleporting Blue Team to: "+blueMidPoint);

        for(String name : regionBlue.getMembers().getPlayers()){
            Bukkit.getPlayer(name).teleport(blueMidPoint);
        }

        Bukkit.broadcastMessage(ChatColor.RED+"The Red Team is at: X="+redMidPoint.getBlockX()+" Z="+redMidPoint.getBlockZ());
        Bukkit.broadcastMessage(ChatColor.BLUE+"The Blue Team is at: X="+blueMidPoint.getBlockX()+" Z="+blueMidPoint.getBlockZ());

    }

    public static void battleTimer(ProtectedRegion regionRed, ProtectedRegion regionBlue, int particleRunnerID){
        RegionBattleMod plugin = RegionBattleMod.getPlugin(RegionBattleMod.class);

        Bukkit.broadcastMessage(ChatColor.GREEN + "Prepare for Battle! Gather Supplies!");
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
                        if(regions.getRegion("Team_Blue") != null) {
                            LOGGER.info("Removing Team_Blue");
                            regions.removeRegion("Team_Blue");
                        }
                        if(regions.getRegion("Team_Red") != null) {
                            LOGGER.info("Removing Team_Red");
                            regions.removeRegion("Team_Red");
                        }

                        //send players back
                        for(Player p : redPlayers){
                            p.teleport(spawn);
                        }
                        for(Player p : bluePlayers){
                            p.teleport(spawn);
                        }

                        //cancel tasks
                        Bukkit.getScheduler().cancelTask(battleTimerID);
                        Bukkit.getScheduler().cancelTask(checkIfGameIsOverID);
                    }

                }}, 20*30, 200); //check if game should end every 10 seconds, delay start by 30 seconds.

            public void run() {
                Bukkit.broadcastMessage(ChatColor.RED + "Let the Battle Begin! Force fields have come down! Last team standing wins!");

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

            }}, 20*60L*prepareMinutes); //20 ticks per second * 60 seconds * # Minutes wanted from prepareMinutes
    }

    private static Point[] findRegionZones(){ //find zones for region

        int rX = 0; //red points
        int rZ = 0;

        int bX = 0; //blue points
        int bZ = 0;

        Point r = new Point(rX,rZ); //create center points for red and blue
        Point b = new Point(bX,bZ);

        //get new points while the regions are too close together as is defined by minDistance/maxDistance or if block is liquid
        while((minDistance >= r.distance(b)) || (r.distance(b) >= maxDistance) || !Bukkit.getWorld("RegionBattle").getHighestBlockAt(((bX+(bX+regionSize))/2),((bZ+(bZ+regionSize))/2)).getType().isSolid() || !Bukkit.getWorld("RegionBattle").getHighestBlockAt(((rX+(rX+regionSize))/2),((rZ+(rZ+regionSize))/2)).getType().isSolid()){ //get new points while the regions are too close together as is defined by minDistance/maxDistance or if block is liquid
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

    @EventHandler
    public void onPlayerDeath (PlayerDeathEvent event){ // Send message of players death.
        Player p = event.getEntity().getPlayer();
        if(p.getWorld() == Bukkit.getWorld("RegionBattle")) {
            if(regions.getRegion("Team_Red") != null){
                if(regions.getRegion("Team_Blue") != null){
                    LOGGER.info(regionRed.getMembers().getPlayers().toString());
                    LOGGER.info(p.getDisplayName());
                    if(regionRed.getMembers().getPlayers().contains(p.getDisplayName().toLowerCase())){
                        redPlayers.remove(event.getEntity().getPlayer());
                        Bukkit.broadcastMessage(ChatColor.RED+""+redPlayers.size()+" players remain on Team Red.");
                    }
                    if(regionBlue.getMembers().getPlayers().contains(p.getDisplayName().toLowerCase())){
                        bluePlayers.remove(event.getEntity().getPlayer());
                        Bukkit.broadcastMessage(ChatColor.BLUE+""+ bluePlayers.size()+" players remain on Team Blue.");
                    }
                }
            }
            Bukkit.broadcastMessage(ChatColor.DARK_RED + p.getDisplayName() + " has died!");

        }
    }

    @EventHandler
    public void onPlayerLogOff (PlayerQuitEvent event) { //handle when players log off in the middle of battle
        if(event.getPlayer().getWorld() == Bukkit.getWorld("RegionBattle")){
            if(regionRed.getMembers().getPlayers().contains(event.getPlayer().getDisplayName().toLowerCase())){
                redPlayers.remove(event.getPlayer());
            }
            if(regionBlue.getMembers().getPlayers().contains(event.getPlayer().getDisplayName().toLowerCase())){
                bluePlayers.remove(event.getPlayer());
            }
        }
    }

    @EventHandler
    public void onPlayerLogOn (PlayerJoinEvent event) { //handle when players log off in the middle of battle, and then join back.
        if(event.getPlayer().getWorld() == Bukkit.getWorld("RegionBattle")){
            if(regionRed.getMembers().getPlayers().contains(event.getPlayer().getDisplayName().toLowerCase())){
                redPlayers.add(event.getPlayer());
            }
            if(regionBlue.getMembers().getPlayers().contains(event.getPlayer().getDisplayName().toLowerCase())){
                bluePlayers.add(event.getPlayer());
            }
        }
    }

}
