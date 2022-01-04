package me.vetovius.regionbattle.behemoth;

import me.vetovius.regionbattle.RegionBattle;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;

import java.util.Objects;
import java.util.logging.Logger;

public class Behemoth implements Listener {

    private static final Logger LOGGER = Logger.getLogger( Behemoth.class.getName() );
    private RegionBattle pluginInstance;
    private Player player; //player who summoned the Behemoth

    LivingEntity behemoth;

    public static final String behemothName = "BEHEMOTH";

    private ArrayList<Player> behemothHealthBarPlayersList = new ArrayList<Player>();
    private BossBar behemothHealthBar;

    private int checkForBossBarPlayersId=0;

    private final int behemothHealth = 2000;
    private final int behemothDamage = 20;
    private final int behemothArmor = 25;
    private final double behemothSpeed = 0.3;

    public Behemoth(RegionBattle pluginInstance, Player player){
        this.pluginInstance = pluginInstance;
        this.player = player;

        Bukkit.getPluginManager().registerEvents(this, pluginInstance); //register events

        this.behemothHealthBar = Bukkit.createBossBar(ChatColor.GRAY+behemothName, BarColor.RED, BarStyle.SEGMENTED_10);
        behemothHealthBar.setProgress(1);

        startBehemothEvent(); //start the event

    }


    protected void startBehemothEvent(){



            behemoth = (LivingEntity) player.getWorld().spawnEntity(player.getLocation(), EntityType.IRON_GOLEM); //spawn a Giant to defeat.
            LOGGER.info("behemoth spawned at X: " + behemoth.getLocation().getBlockX() + " Z: " + behemoth.getLocation().getBlockZ());
            behemoth.setCustomName(behemothName);
            AttributeInstance behemothMaxHealthInstance = behemoth.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            behemothMaxHealthInstance.setBaseValue(behemothHealth);
            behemoth.setHealth(behemothHealth);
            AttributeInstance behemothDamageInstance = behemoth.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
            behemothDamageInstance.setBaseValue(behemothDamage);
            AttributeInstance behemothArmorInstance = behemoth.getAttribute(Attribute.GENERIC_ARMOR);
            behemothArmorInstance.setBaseValue(behemothArmor);
            AttributeInstance behemothSpeedInstance = behemoth.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
            behemothSpeedInstance.setBaseValue(behemothSpeed);
            behemoth.setRemoveWhenFarAway(false);
            behemoth.setPersistent(true);

        for(Player p : player.getWorld().getPlayers()){ //broadcast location of Behemoth
            p.sendMessage(ChatColor.LIGHT_PURPLE + "The [" + ChatColor.DARK_RED + behemothName + ChatColor.LIGHT_PURPLE +"] has just appeared at X: " + player.getLocation().getBlockX() + " Z: "+ player.getLocation().getBlockZ());
        }

        checkForBossBarPlayersId = Bukkit.getScheduler().scheduleSyncRepeatingTask(pluginInstance, new Runnable() {
            public void run() {
                behemothHealthBarPlayersList = (ArrayList<Player>) behemoth.getLocation().getNearbyPlayers(50);

                for(Player p : behemothHealthBar.getPlayers()){
                    if(!behemothHealthBarPlayersList.contains(p)){
                        behemothHealthBar.removePlayer(p);
                    }
                }
                for(Player p : behemothHealthBarPlayersList){
                    if(!behemothHealthBar.getPlayers().contains(p)){
                        behemothHealthBar.addPlayer(p);
                    }
                }

                if(behemoth.isDead()){

                    LOGGER.info("Behemoth is dead, cancelling the instance.");
                    behemothHealthBar.removeAll();
                    Bukkit.getScheduler().cancelTask(checkForBossBarPlayersId);
                    behemoth.remove();

                    //F this deprecation, I'm using it anyway because this must be manually spawned by an admin
                    Bukkit.broadcastMessage(ChatColor.LIGHT_PURPLE + "The [" + ChatColor.DARK_RED + behemothName + ChatColor.LIGHT_PURPLE +"] has perished!");

                }

            }}, 20, 20*5); //repeat task every 5 seconds, assuming 20TPS.


    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent e) {
        if(e.getEntity().getWorld() == behemoth.getWorld()){
            if(Objects.equals(e.getEntity().getCustomName(), behemothName)){

                if(e.getEntity().getKiller() != null){
                    //announce
                    for(Player p : behemoth.getWorld().getPlayers()){
                        p.sendMessage(ChatColor.LIGHT_PURPLE + "The [" + ChatColor.DARK_RED + behemothName + ChatColor.LIGHT_PURPLE +"]" + " has been slain by " + e.getEntity().getKiller().getName() + "!");
                    }
                     //TODO could add loot here if wanted
                }
                else{
                    e.getDrops().clear();
                }

                //End tasks only if the location was within the right zone, else this could be other minibosses
                behemothHealthBar.removeAll();
                Bukkit.getScheduler().cancelTask(checkForBossBarPlayersId);

            }
        }

    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) { //keep inv near behemoth boss, because its OP
        if(e.getPlayer().getWorld() == behemoth.getWorld()) {
            if(behemoth.getLocation().distanceSquared(e.getPlayer().getLocation()) < (50*50)){ //if player is "near" behemoth
                e.setKeepInventory(true);
                e.getDrops().clear(); //clear player drops, keep inv is on.
            }
        }

    }


    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if(Objects.equals(e.getEntity().getCustomName(), behemothName)){
            if (e.getEntity() instanceof LivingEntity livingEntity) {
                if(e.getDamager() instanceof Player){
                    ((Player) e.getDamager()).addPotionEffect(new PotionEffect(PotionEffectType.POISON,20*1,1));
                }
                behemothHealthBar.setProgress(livingEntity.getHealth() / behemothHealth);
            }
        }

    }



}
