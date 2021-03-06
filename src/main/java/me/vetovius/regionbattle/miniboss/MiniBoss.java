package me.vetovius.regionbattle.miniboss;

import io.papermc.paper.event.entity.EntityMoveEvent;
import me.vetovius.regionbattle.RegionBattle;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.awt.*;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class MiniBoss implements Listener {

    private static final Logger LOGGER = Logger.getLogger( MiniBoss.class.getName() );
    private RegionBattle pluginInstance;

    public static World smpWorld = Bukkit.getWorld("world");
    private static final int range = 5500; //how far can the zone be from spawn + or -
    private static final int minDistanceFromSpawn = 550; //can't be closer than this to spawn
    private static final int miniBossHealthBarRadius = 100;

    public static String miniBossName = "MiniBoss";

    private long duration = 0; //how long miniboss lasts
    private long startTime = 0;


    private BossBar miniBossHealthBar;

    private int timerTaskId = 0;
    private int checkForBossBarPlayersId = 0;
    private int broadcastLocationTaskId = 0;

    private Location miniBossZoneCenter;

    private ArrayList<Player> miniBossHealthBarPlayersList = new ArrayList<Player>();

    private final int miniBossHealth = 250;
    private final int miniBossDamage = 8;
    private final int miniBossArmor = 15;
    private int randomValue;


    public MiniBoss(RegionBattle pluginInstance){
        this.pluginInstance = pluginInstance;

        Bukkit.getPluginManager().registerEvents(this, pluginInstance); //register events


        //Get Random value for miniboss type
        Random rand = new Random();
        randomValue = rand.nextInt((1) + 1);

        if(randomValue == 0){
            miniBossName = "Enraged Marauder";
        }
        else if(randomValue == 1){
            miniBossName = "Toxic Crawler";
        }

        findLocationForMiniBoss(); //find a suitable location for the battle region

        //1 hour = 3600000 miliseconds
        startTime = System.currentTimeMillis();
        duration =  System.currentTimeMillis() + 3600000; //1 hour from now

        this.miniBossHealthBar = Bukkit.createBossBar(ChatColor.GRAY+miniBossName, BarColor.RED, BarStyle.SEGMENTED_10);
        miniBossHealthBar.setProgress(1);
        startMiniBossTimer();
    }

    protected void startMiniBossTimer(){


        LivingEntity miniBoss = null; //initialize to null so compiler doesn't complain.

        //Determine which miniboss type should spawn
        if(randomValue == 0){
            miniBoss = (LivingEntity) smpWorld.spawnEntity(miniBossZoneCenter, EntityType.PILLAGER); //spawn a boss to defeat.
            LOGGER.info("miniBoss spawned at X: " + miniBoss.getLocation().getBlockX() + " Z: " + miniBoss.getLocation().getBlockZ());
            miniBoss.setCustomName(miniBossName);
            AttributeInstance miniBossMaxHealthInstance = miniBoss.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            miniBossMaxHealthInstance.setBaseValue(miniBossHealth);
            miniBoss.setHealth(miniBossHealth);
            AttributeInstance miniBossDamageInstance = miniBoss.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
            miniBossDamageInstance.setBaseValue(miniBossDamage);
            AttributeInstance miniBossArmorInstance = miniBoss.getAttribute(Attribute.GENERIC_ARMOR);
            miniBossArmorInstance.setBaseValue(miniBossArmor);
            miniBoss.setRemoveWhenFarAway(false);
            miniBoss.setPersistent(true);
        }
        else if(randomValue == 1){
            miniBoss = (LivingEntity) smpWorld.spawnEntity(miniBossZoneCenter, EntityType.CAVE_SPIDER); //spawn a boss to defeat.
            LOGGER.info("miniBoss spawned at X: " + miniBoss.getLocation().getBlockX() + " Z: " + miniBoss.getLocation().getBlockZ());
            miniBoss.setCustomName(miniBossName);
            AttributeInstance miniBossMaxHealthInstance = miniBoss.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            miniBossMaxHealthInstance.setBaseValue(miniBossHealth);
            miniBoss.setHealth(miniBossHealth);
            AttributeInstance miniBossDamageInstance = miniBoss.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
            miniBossDamageInstance.setBaseValue(miniBossDamage);
            AttributeInstance miniBossArmorInstance = miniBoss.getAttribute(Attribute.GENERIC_ARMOR);
            miniBossArmorInstance.setBaseValue(miniBossArmor);
            miniBoss.setRemoveWhenFarAway(false);
            miniBoss.setPersistent(true);
        }


        PersistentDataContainer miniBossPDC = miniBoss.getPersistentDataContainer();
        if(!miniBossPDC.has(new NamespacedKey(pluginInstance,"maxAllowedAge"), PersistentDataType.LONG)) {
            miniBossPDC.set(new NamespacedKey(pluginInstance, "maxAllowedAge"), PersistentDataType.LONG, duration);
        }


        broadcastLocationTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(pluginInstance, new Runnable() {
            public void run() {
                for(Player p : smpWorld.getPlayers()){
                    p.sendMessage(ChatColor.LIGHT_PURPLE + "The [" + ChatColor.DARK_RED + miniBossName + ChatColor.LIGHT_PURPLE +"] lurking at X: " + miniBossZoneCenter.getBlockX() + " Z: "+ miniBossZoneCenter.getBlockZ() + " will vanish in " + TimeUnit.MILLISECONDS.toMinutes(duration - System.currentTimeMillis()) + " minutes. Slay it for a reward!");
                }
            }}, 6000, 18000); //second parameter is the frequency in ticks of the flash, 100 = flash every 100 ticks(5 seconds).

        timerTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(pluginInstance, new Runnable() {
            public void run() {

                for(LivingEntity e : miniBossZoneCenter.getNearbyLivingEntities(200)){
                    if(e.getType() == EntityType.PILLAGER || e.getType() == EntityType.CAVE_SPIDER){
                        if(Objects.equals(e.getCustomName(), miniBossName)){
                            if(e.isDead()){
                                LOGGER.info("Miniboss is dead for some reason, cancelling the miniboss instance. " + miniBossZoneCenter.toString());
                                miniBossHealthBar.removeAll();
                                Bukkit.getScheduler().cancelTask(timerTaskId);
                                Bukkit.getScheduler().cancelTask(checkForBossBarPlayersId);
                                Bukkit.getScheduler().cancelTask(broadcastLocationTaskId);
                                e.remove();
                                for(Player p : smpWorld.getPlayers()){
                                    p.sendMessage(ChatColor.LIGHT_PURPLE + "The [" + ChatColor.DARK_RED + miniBossName + ChatColor.LIGHT_PURPLE +"] has vanished!");
                                }
                            }
                        }
                    }
                }

                if (System.currentTimeMillis() >= duration){ //Time is up, end the miniboss.

                    miniBossHealthBar.removeAll();
                    Bukkit.getScheduler().cancelTask(timerTaskId);
                    Bukkit.getScheduler().cancelTask(checkForBossBarPlayersId);
                    Bukkit.getScheduler().cancelTask(broadcastLocationTaskId);

                    for(LivingEntity e : miniBossZoneCenter.getNearbyLivingEntities(200)){
                        if(e.getType() == EntityType.PILLAGER || e.getType() == EntityType.CAVE_SPIDER){
                            if(Objects.equals(e.getCustomName(), miniBossName)){
                                e.remove();
                            }
                        }
                    }

                    for(Player p : smpWorld.getPlayers()){
                        p.sendMessage(ChatColor.LIGHT_PURPLE + "The [" + ChatColor.DARK_RED + miniBossName + ChatColor.LIGHT_PURPLE +"] has vanished!");
                    }

                }

            }}, 20, 20*30); //repeat task every 30 seconds


        checkForBossBarPlayersId = Bukkit.getScheduler().scheduleSyncRepeatingTask(pluginInstance, new Runnable() {
            public void run() {
                miniBossHealthBarPlayersList = (ArrayList<Player>) miniBossZoneCenter.getNearbyPlayers(miniBossHealthBarRadius);

                for(Player p : miniBossHealthBar.getPlayers()){
                    if(!miniBossHealthBarPlayersList.contains(p)){
                        miniBossHealthBar.removePlayer(p);
                    }
                }
                for(Player p : miniBossHealthBarPlayersList){
                    if(!miniBossHealthBar.getPlayers().contains(p)){
                        miniBossHealthBar.addPlayer(p);
                    }
                }

            }}, 20, 20*5); //repeat task every 5 seconds, assuming 20TPS.


    }

    private void findLocationForMiniBoss(){
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

        LOGGER.info("miniBossZoneCenter Found. X: " + x + " Z: " + z);
        miniBossZoneCenter = new Location(smpWorld,x,smpWorld.getHighestBlockYAt(x,z)+1,z); //set miniBossZoneCenter
        for(Player p : smpWorld.getPlayers()){
            p.sendMessage(ChatColor.LIGHT_PURPLE + "The [" + ChatColor.DARK_RED + miniBossName + ChatColor.LIGHT_PURPLE +"] has just appeared at X: " + x + " Z: "+ z +". Slay it for a reward!");
        }
    }


    @EventHandler
    public void onEntityMove(EntityMoveEvent e) {
        if(e.getEntity().getLocation().getWorld() == smpWorld){
            if(Objects.equals(e.getEntity().getCustomName(), miniBossName)){
                if(e.getEntity().getLocation().distanceSquared(miniBossZoneCenter) < (250*250)){
                    if (e.getEntity().getLocation().distanceSquared(miniBossZoneCenter) > (50*50)){ //cant leave beyond this radius
                        e.getEntity().teleport(miniBossZoneCenter);
                    }
                }
            }
        }

    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent e) {
        if(e.getEntity().getWorld() == smpWorld){
            if(Objects.equals(e.getEntity().getCustomName(), miniBossName)){
                if(e.getEntity().getLocation().distanceSquared(miniBossZoneCenter) < (100*100)){
                    if(e.getEntity().getKiller() != null){
                        //announce
                        for(Player p : smpWorld.getPlayers()){
                            p.sendMessage(ChatColor.LIGHT_PURPLE + "The [" + ChatColor.DARK_RED + miniBossName + ChatColor.LIGHT_PURPLE +"]" + " has been slain by " + e.getEntity().getKiller().getName() + "!");
                        }

                        //give loot
                        e.getDrops().addAll(MiniBossLootItem.getRandomItemStacks());
                    }
                    else{
                        e.getDrops().clear();
                    }

                    //End tasks only if the location was within the right zone, else this could be other minibosses
                    miniBossHealthBar.removeAll();
                    Bukkit.getScheduler().cancelTask(timerTaskId);
                    Bukkit.getScheduler().cancelTask(checkForBossBarPlayersId);
                    Bukkit.getScheduler().cancelTask(broadcastLocationTaskId);

                }
            }
        }

    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if(Objects.equals(e.getEntity().getCustomName(), miniBossName)){
            if (e.getEntity() instanceof LivingEntity livingEntity) {
                miniBossHealthBar.setProgress(livingEntity.getHealth() / miniBossHealth);
            }
        }

    }

    @EventHandler
    public void onShootBow(EntityShootBowEvent e) {

        if(e.getEntity().getWorld() == smpWorld) {
            if (Objects.equals(e.getEntity().getCustomName(), miniBossName)) {
                AbstractArrow projectile = (AbstractArrow) e.getProjectile();
                projectile.setDamage(miniBossDamage);
                projectile.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
                projectile.setKnockbackStrength(1);
                projectile.setPierceLevel(1); //Shields wont help too much now
            }
        }
    }

}
