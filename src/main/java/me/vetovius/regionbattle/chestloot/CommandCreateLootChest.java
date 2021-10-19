package me.vetovius.regionbattle.chestloot;

import me.vetovius.regionbattle.RegionBattle;
import me.vetovius.regionbattle.persistentbattle.PersistentBattle;
import me.vetovius.regionbattle.regionbattle.Regions;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.logging.Logger;

public class CommandCreateLootChest implements CommandExecutor {

    private static final Logger LOGGER = Logger.getLogger(CommandCreateLootChest.class.getName());

    // This method is called, when somebody uses our command
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(sender instanceof Player){
            Player player = (Player) sender;
            if(player.getWorld() == Regions.world || player.getWorld() == PersistentBattle.world){

                //create a chest at players feet
                 player.getLocation().getBlock().setType(Material.CHEST);

                 //get BlockState of chest
                 BlockState blockState = player.getLocation().getBlock().getState();
                 if(blockState instanceof Chest chest){

                     RegionBattle plugin = RegionBattle.getPlugin(RegionBattle.class);

                     //add chestLoot tag to PDC of the chest
                     PersistentDataContainer chestPersistentDataContainer = chest.getPersistentDataContainer();
                     //have to do NamespacedKey.fromString("regionbattlemod:chestloot",plugin because PDC chests were created when plugin was named regionbattlemod
                     if(!chestPersistentDataContainer.has(NamespacedKey.fromString("regionbattlemod:chestloot",plugin), PersistentDataType.INTEGER)) {
                         chestPersistentDataContainer.set(NamespacedKey.fromString("regionbattlemod:chestloot",plugin), PersistentDataType.INTEGER, 1);
                     }

                     //update blockstate
                     blockState.update();
                 }
            }
        }
        return true;
    }

}
