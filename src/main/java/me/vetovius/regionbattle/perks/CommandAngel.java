package me.vetovius.regionbattle.perks;

import me.vetovius.regionbattle.RegionBattle;
import me.vetovius.regionbattle.persistentbattle.PersistentBattle;
import me.vetovius.regionbattle.regionbattle.Regions;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

//Class used for donators to purchase a server-wide fly perk
//Command will be called /angel
public class CommandAngel implements CommandExecutor {

    private static final Logger LOGGER = Logger.getLogger( CommandAngel.class.getName() );
    RegionBattle plugin = RegionBattle.getPlugin(RegionBattle.class);
    private int flightTimer;
    private static final int flyMinutes = 30;
    private static final int flyCooldownHours = 6;

    // This method is called, when somebody uses our command
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(sender instanceof Player) {

            Player player = (Player) sender;

            if(plugin.getConfig().getBoolean("perks.angelEvent.isActive")){ //if true in config, boost is active

                if(System.currentTimeMillis() >= plugin.getConfig().getLong("perks.angelEvent.endTime") ){ //end flight time if needed
                    plugin.getConfig().set("perks.angelEvent.isActive", false); //set angel even to false if times up
                    plugin.getConfig().set("perks.angelEvent.endTime" , 0); // set end time to 0
                    plugin.saveConfig();

                    player.sendMessage("The Great Angel Event has ended! You can purchase it at store.regionbattle.com");
                    return true;
                }


                if(player.getWorld() == Regions.world || player.getWorld() == PersistentBattle.world){

                    player.sendMessage("You can't use fly here.");
                    return true;
                }

                if (player.getAllowFlight()) {
                    player.sendMessage("You are already flying.");
                }
                else if(!player.getAllowFlight()){

                    long serverFlyBoostEndTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(flyMinutes);
                    long serverFlyBoostCooldown = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(flyCooldownHours);


                    PersistentDataContainer playerFlyPDC = player.getPersistentDataContainer();

                    if(playerFlyPDC.has(new NamespacedKey(plugin,"serverFlyBoostCooldown"), PersistentDataType.LONG)){
                        long flightCooldownTime = playerFlyPDC.get(new NamespacedKey(plugin,"serverFlyBoostCooldown"), PersistentDataType.LONG);
                        if(System.currentTimeMillis() < flightCooldownTime){
                            player.sendMessage("You can use /angel again in " +TimeUnit.MILLISECONDS.toHours(flightCooldownTime - System.currentTimeMillis()) + " hours");
                            return true;
                        }
                        else if(System.currentTimeMillis() >= flightCooldownTime){
                            playerFlyPDC.remove(new NamespacedKey(plugin,"serverFlyBoostCooldown")); //cooldown over
                        }

                    }

                    if(!playerFlyPDC.has(new NamespacedKey(plugin,"serverFlyBoostEndTime"), PersistentDataType.LONG)){
                        playerFlyPDC.set(new NamespacedKey(plugin,"serverFlyBoostEndTime"), PersistentDataType.LONG, serverFlyBoostEndTime);
                    }
                    if(!playerFlyPDC.has(new NamespacedKey(plugin,"serverFlyBoostCooldown"), PersistentDataType.LONG)){
                        playerFlyPDC.set(new NamespacedKey(plugin,"serverFlyBoostCooldown"), PersistentDataType.LONG, serverFlyBoostCooldown);
                    }


                    LOGGER.info("Starting angel event flight time for: " + player.getName());
                    player.setAllowFlight(true);
                    player.sendMessage("You are now able to fly for " + flyMinutes + " minutes! Teleporting or changing worlds will cancel this effect!"+ChatColor.RED+" Do not use this for PVP!");

                    flightTimer = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
                        public void run() {

                            if (System.currentTimeMillis() >= serverFlyBoostEndTime){ //Time is up, end the battle region.
                                player.setAllowFlight(false);

                                if(playerFlyPDC.has(new NamespacedKey(plugin,"serverFlyBoostEndTime"), PersistentDataType.LONG)){
                                    playerFlyPDC.remove(new NamespacedKey(plugin,"serverFlyBoostEndTime"));
                                }
                                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING,20*20,1)); //slow fall so they don't die.
                                LOGGER.info("Ending angel event fly time naturally for: " + player.getName());
                                Bukkit.getScheduler().cancelTask(flightTimer);
                            }

                        }}, 20, 20*30); //repeat task every 30 seconds


                }

            }
            else{ //Boost isnt active
                player.sendMessage("The Great Angel Event is not active! You can purchase it at store.regionbattle.com");
            }


        }

        return true;
    }

}
