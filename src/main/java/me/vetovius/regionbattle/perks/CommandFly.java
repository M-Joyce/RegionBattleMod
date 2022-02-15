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

public class CommandFly implements CommandExecutor {

    private static final Logger LOGGER = Logger.getLogger( CommandFly.class.getName() );
    RegionBattle plugin = RegionBattle.getPlugin(RegionBattle.class);
    private int flightTimer;
    private static final int flyMinutes = 60;
    private static final int flyCooldownHours = 8;

    // This method is called, when somebody uses our command
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(sender instanceof Player) {
            Player player = (Player) sender;

            if(player.getWorld() == Regions.world || player.getWorld() == PersistentBattle.world){

                player.sendMessage("You can't use vipfly here.");
                return true;
            }

            if (player.getAllowFlight()) {
                player.sendMessage("You are already flying.");
            }
            else if(!player.getAllowFlight()){

                long flightEndTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(flyMinutes);
                long vipFlightCooldown = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(flyCooldownHours);


                PersistentDataContainer playerFlyPDC = player.getPersistentDataContainer();

                if(playerFlyPDC.has(new NamespacedKey(plugin,"vipFlightCooldown"), PersistentDataType.LONG)){
                    long flightCooldownTime = playerFlyPDC.get(new NamespacedKey(plugin,"vipFlightCooldown"), PersistentDataType.LONG);
                    if(System.currentTimeMillis() < flightCooldownTime){
                        player.sendMessage("You can use vipflight again in " +TimeUnit.MILLISECONDS.toHours(flightCooldownTime - System.currentTimeMillis()) + " hours");
                        return true;
                    }
                    else if(System.currentTimeMillis() >= flightCooldownTime){
                        playerFlyPDC.remove(new NamespacedKey(plugin,"vipFlightCooldown")); //cooldown over
                    }

                }

                if(!playerFlyPDC.has(new NamespacedKey(plugin,"vipFlightEndTime"), PersistentDataType.LONG)){
                    playerFlyPDC.set(new NamespacedKey(plugin,"vipFlightEndTime"), PersistentDataType.LONG, flightEndTime);
                }
                if(!playerFlyPDC.has(new NamespacedKey(plugin,"vipFlightCooldown"), PersistentDataType.LONG)){
                    playerFlyPDC.set(new NamespacedKey(plugin,"vipFlightCooldown"), PersistentDataType.LONG, vipFlightCooldown);
                }


                LOGGER.info("Starting vip flight time for: " + player.getName());
                player.setAllowFlight(true);
                player.sendMessage("You are now able to fly for " + flyMinutes + " minutes! Teleporting or changing worlds will cancel this effect!"+ChatColor.RED+" Do not use this for PVP!");

                flightTimer = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
                    public void run() {

                        if (System.currentTimeMillis() >= flightEndTime){ //Time is up, end the battle region.
                            player.setAllowFlight(false);

                            if(playerFlyPDC.has(new NamespacedKey(plugin,"vipFlightEndTime"), PersistentDataType.LONG)){
                                playerFlyPDC.remove(new NamespacedKey(plugin,"vipFlightEndTime"));
                            }
                            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING,20*20,1)); //slow fall so they don't die.
                            LOGGER.info("Ending vip flight time naturally for: " + player.getName());
                            Bukkit.getScheduler().cancelTask(flightTimer);
                        }

                    }}, 20, 20*30); //repeat task every 30 seconds


            }

        }

        return true;
    }

}
