package me.vetovius.regionbattle.smpbattleregion;

import io.papermc.paper.event.entity.EntityMoveEvent;
import me.vetovius.regionbattle.RegionBattle;
import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class BattleRegion implements Listener {

    private static final Logger LOGGER = Logger.getLogger( BattleRegion.class.getName() );
    private RegionBattle pluginInstance;

    public static World smpWorld = Bukkit.getWorld("world");
    private static final int range = 4000; //how far can the zone be from spawn + or -
    private static final int minDistanceFromSpawn = 550; //can't be closer than this to spawn
    private static final int radius = 10; //how big should the battle region square radius be in blocks?
    private static final int captureBarRadius = 100;

    public static final String witherCustomName = "Battle Region Guardian";

    private long startTime = 0;
    private long duration = 0; //how long battle region lasts

    private long timeToCapture = 300000; //5 minutes = 300000ms
    private long captureStartTime;
    private long currentTimeToCapture; //used internally, to keep track of timestamp when captured.

    private BossBar captureProgressBar;

    private int particleTaskId = 0;
    private int timerTaskId = 0;
    private int checkForPlayers = 0;

    private Location battleRegionCenter;

    private ArrayList<Player> playersInZone = new ArrayList<Player>();
    private ArrayList<Player> captureProgressBarPlayersList = new ArrayList<Player>();
    private Player capturingPlayer;


    public BattleRegion(RegionBattle pluginInstance){
        this.pluginInstance = pluginInstance;

        Bukkit.getPluginManager().registerEvents(this, pluginInstance); //register events

        findLocationForBattleRegion(); //find a suitable location for the battle region
        particleTaskId=initBattleRegionParticles();

        //1 hour = 3600000 miliseconds
        startTime = System.currentTimeMillis();
        duration =  System.currentTimeMillis() + 3600000; //1 hour from now

        this.captureProgressBar = Bukkit.createBossBar(ChatColor.GRAY+"Battle Region", BarColor.RED, BarStyle.SEGMENTED_10);
        captureProgressBar.setProgress(1);
        startBattleRegionTimer();
    }

    protected void startBattleRegionTimer(){

        Wither wither = (Wither) smpWorld.spawnEntity(battleRegionCenter, EntityType.WITHER); //spawn a boss to defeat.
        wither.setCustomName(witherCustomName);

        PersistentDataContainer witherPDC = wither.getPersistentDataContainer();
        if(!witherPDC.has(new NamespacedKey(pluginInstance,"maxAllowedAge"), PersistentDataType.LONG)) {
            witherPDC.set(new NamespacedKey(pluginInstance, "maxAllowedAge"), PersistentDataType.LONG, duration);
        }


        int broadcastLocationTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(pluginInstance, new Runnable() {
            public void run() {
                for(Player p : smpWorld.getPlayers()){
                    p.sendMessage(ChatColor.AQUA + "The [" + ChatColor.BLUE + "Battle Region" + ChatColor.AQUA + "] at X: " + battleRegionCenter.getBlockX() + " Z: "+ battleRegionCenter.getBlockZ() +" will vanish in " + TimeUnit.MILLISECONDS.toMinutes(duration - System.currentTimeMillis()) + " minutes. Capture it for a reward!");
                }
            }}, 6000, 18000); //second parameter is the frequency in ticks of the flash, 100 = flash every 100 ticks(5 seconds).

        timerTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(pluginInstance, new Runnable() {
            public void run() {

                if (System.currentTimeMillis() >= duration){ //Time is up, end the battle region.

                    Bukkit.getScheduler().cancelTask(timerTaskId);
                    Bukkit.getScheduler().cancelTask(particleTaskId);
                    Bukkit.getScheduler().cancelTask(checkForPlayers);
                    Bukkit.getScheduler().cancelTask(broadcastLocationTaskId);
                    captureProgressBar.removeAll();

                    for(LivingEntity e : battleRegionCenter.getNearbyLivingEntities(200)){
                            if(e.getType() == EntityType.WITHER){
                                if(Objects.equals(e.getCustomName(), witherCustomName)){
                                    e.remove();
                                }
                            }
                    }

                    for(Player p : smpWorld.getPlayers()){
                        p.sendMessage(ChatColor.AQUA+"The [" + ChatColor.BLUE + "Battle Region" + ChatColor.AQUA + "] has vanished!");
                    }


                }

            }}, 20, 20*30); //repeat task every 30 seconds



        //Actual Battle Region Logic goes here.
        checkForPlayers = Bukkit.getScheduler().scheduleSyncRepeatingTask(pluginInstance, new Runnable() {
            public void run() {
                playersInZone = (ArrayList<Player>) battleRegionCenter.getNearbyPlayers(radius+3); //updating the playersInZone list if zone is empty or contains players. // + 3 to make the capturable area slightly bigger
                captureProgressBarPlayersList = (ArrayList<Player>) battleRegionCenter.getNearbyPlayers(captureBarRadius);

                for(Player p : captureProgressBar.getPlayers()){
                    if(!captureProgressBarPlayersList.contains(p)){
                        captureProgressBar.removePlayer(p);
                    }
                }
                for(Player p : captureProgressBarPlayersList){
                    if(!captureProgressBar.getPlayers().contains(p)){
                        captureProgressBar.addPlayer(p);
                    }
                }

                if(playersInZone.isEmpty()){
                    captureProgressBar.setTitle("Battle Region is Empty!");
                    captureProgressBar.setProgress(1);
                    captureProgressBar.setColor(BarColor.RED);
                    capturingPlayer = null;
                }
                else if(playersInZone.size() == 1){
                    Boolean witherAlive = false;

                    for(LivingEntity e : battleRegionCenter.getNearbyLivingEntities(100)){
                        if(e.getType() == EntityType.WITHER){
                            if(Objects.equals(e.getCustomName(), witherCustomName)){
                                witherAlive = true;
                            }
                        }
                    }

                    if(witherAlive){
                        captureProgressBar.setTitle("You must slay the Guardian!");
                        capturingPlayer = null;
                        captureProgressBar.setColor(BarColor.PURPLE);
                    }
                    else if(capturingPlayer == null){
                        captureProgressBar.setTitle(playersInZone.get(0).getName() + " is capturing the Battle Region!");
                        captureProgressBar.setColor(BarColor.GREEN);
                        capturingPlayer = playersInZone.get(0);
                        captureStartTime = System.currentTimeMillis();
                        currentTimeToCapture = timeToCapture + System.currentTimeMillis();
                    }
                    else if(playersInZone.get(0) != capturingPlayer){ //New player is in control
                        capturingPlayer = playersInZone.get(0);
                    }else if(playersInZone.get(0) == capturingPlayer){
                        if(System.currentTimeMillis() > currentTimeToCapture){
                            //Zone is captured
                            Bukkit.getScheduler().cancelTask(checkForPlayers);

                            for(Player p : smpWorld.getPlayers()){
                                p.sendMessage(ChatColor.AQUA + "The [" + ChatColor.BLUE + "Battle Region" + ChatColor.AQUA + "] has been captured by " + capturingPlayer.getName() +"!");
                            }
                            capturingPlayer.sendMessage(ChatColor.AQUA + "You have captured the zone! Check the center of the battle region for your reward!");

                            int captureParticleTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(pluginInstance, new Runnable() {
                                public void run() {
                                    smpWorld.spawnParticle(Particle.COMPOSTER,battleRegionCenter, 50, 1, 1, 1, 2, null, true);
                                }}, 0, 60); //second parameter is the frequency in ticks of the flash, 100 = flash every 100 ticks(5 seconds).

                            //Give some Loot!
                            for(ItemStack itemStack : BattleRegionLootItem.getRandomItemStacks()){
                                smpWorld.dropItem(battleRegionCenter,itemStack);
                            }
                            ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
                            Bukkit.dispatchCommand(console, "eco give "+capturingPlayer.getName()+" 1000"); //give $1000 for capturing!

                            capturingPlayer.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION,1200,1));
                            capturingPlayer.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE,1200,1));
                            capturingPlayer.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE,1200,1));

                            //after 1 minute cancel all the RegionBattle Stuff.
                            Bukkit.getScheduler().scheduleSyncDelayedTask(pluginInstance, new Runnable() {
                                @Override
                                public void run() {
                                    Bukkit.getScheduler().cancelTask(timerTaskId);
                                    Bukkit.getScheduler().cancelTask(particleTaskId);
                                    Bukkit.getScheduler().cancelTask(captureParticleTaskId);
                                    Bukkit.getScheduler().cancelTask(broadcastLocationTaskId);
                                    captureProgressBar.removeAll();

                                    for(LivingEntity e : battleRegionCenter.getNearbyLivingEntities(200)){
                                        if(e.getType() == EntityType.WITHER){
                                            if(Objects.equals(e.getCustomName(), witherCustomName)){
                                                e.remove();
                                            }
                                        }
                                    }
                                }
                            }, 20*120); //1 minute delay


                            captureProgressBar.setProgress(0);
                            captureProgressBar.setTitle("Battle Region has been captured by: " + capturingPlayer.getName());

                        }
                        else{
                            double currPerc = (100 - ((System.currentTimeMillis() - captureStartTime) * 100) / (currentTimeToCapture - captureStartTime)) * 0.01;
                            captureProgressBar.setProgress(currPerc);
                        }

                    }

                }
                else if(playersInZone.size() > 1){
                    captureProgressBar.setTitle("Battle Region is Contested!");
                    capturingPlayer = null;
                    captureProgressBar.setColor(BarColor.YELLOW);
                }


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
            p.sendMessage(ChatColor.AQUA + "A [" + ChatColor.BLUE + "Battle Region" + ChatColor.AQUA + "] has just appeared at X: " + x + " Z: "+ z +". Capture it for a reward!");
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

    @EventHandler
    public void onEntityExplodeEvent(EntityExplodeEvent e) {
        if(e.getEntity().getLocation().getWorld() == smpWorld){
            if(e.getEntity().getType() == EntityType.WITHER_SKULL){
                if (e.getLocation().distanceSquared(battleRegionCenter) < (250*250)){ //TODO this method for distance is best practice, refactor others to use it
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onEntityMove(EntityMoveEvent e) {
        if(e.getEntity().getLocation().getWorld() == smpWorld){
            if(e.getEntity() instanceof Wither){
                if(Objects.equals(e.getEntity().getCustomName(), witherCustomName)){
                    if(e.getEntity().getLocation().distanceSquared(battleRegionCenter) < (250*250)){
                        if (e.getEntity().getLocation().distanceSquared(battleRegionCenter) > (50*50)){ //cant leave beyond this radius
                            e.getEntity().teleport(battleRegionCenter);
                        }
                    }
                }
            }
        }

    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent e) {
        if(e.getEntity().getLocation().getWorld() == smpWorld){
            if(e.getEntity() instanceof Wither){
                if(Objects.equals(e.getEntity().getCustomName(), witherCustomName)) {
                    if (e.getEntity().getLocation().distanceSquared(battleRegionCenter) < (75 * 75)) { //cant leave beyond this radius
                        e.getDrops().clear();
                    }
                }
            }
        }

    }

}
