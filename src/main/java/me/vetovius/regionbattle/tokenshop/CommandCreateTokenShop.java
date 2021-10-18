package me.vetovius.regionbattle.tokenshop;

import me.vetovius.regionbattle.RegionBattle;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockState;
import org.bukkit.block.ShulkerBox;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.logging.Logger;

public class CommandCreateTokenShop implements CommandExecutor {

    private static final Logger LOGGER = Logger.getLogger(CommandCreateTokenShop.class.getName());

    // This method is called, when somebody uses our command
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(sender instanceof Player){
            Player player = (Player) sender;
                //create a chest at players feet
                player.getLocation().getBlock().setType(Material.PURPLE_SHULKER_BOX);

                //get BlockState of chest
                BlockState blockState = player.getLocation().getBlock().getState();
                if(blockState instanceof ShulkerBox sbox){
                    RegionBattle plugin = RegionBattle.getPlugin(RegionBattle.class);

                    //add chestLoot tag to PDC of the chest
                    PersistentDataContainer chestPersistentDataContainer = sbox.getPersistentDataContainer();
                    if(!chestPersistentDataContainer.has(new NamespacedKey(plugin,"isTokenShop"), PersistentDataType.BYTE)) {
                        Byte b = 1;
                        chestPersistentDataContainer.set(new NamespacedKey(plugin, "isTokenShop"), PersistentDataType.BYTE, b);
                    }

                    //update blockstate
                    blockState.update();
                }

        }
        return true;
    }

}
