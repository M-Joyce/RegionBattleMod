package me.vetovius.regionbattle.chestloot;

import me.vetovius.regionbattle.RegionBattle;
import org.bukkit.*;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.logging.Logger;

public class ChestLoot implements Listener {
    private static final Logger LOGGER = Logger.getLogger(ChestLoot.class.getName() );

    public World world;
    public Location center;
    public int radius;
    public ArrayList<InventoryHolder> openedChestsList;

    public ChestLoot(int radius, Location center, RegionBattle plugin){

        Bukkit.getPluginManager().registerEvents(this, plugin); //register events

        this.center = center;
        this.radius = radius;
        this.openedChestsList = new ArrayList<>();
        world = center.getWorld();

    }

    @EventHandler
    public void onChestOpenEvent(InventoryOpenEvent e){
        if (e.getPlayer().getWorld() == world) {
            if (e.getInventory().getHolder() instanceof Chest || e.getInventory().getHolder() instanceof DoubleChest){

                //Should I use LootTables?

                //Check if chest is in the right region
                if(e.getInventory().getLocation().distance(center) < radius){

                    //check if chest has already been opened ie openedChestsList doesn't have the chest
                    if(!openedChestsList.contains(e.getInventory().getHolder())){

                        BlockState blockState = ((BlockState) e.getInventory().getHolder()).getBlock().getState();
                        if(blockState instanceof Chest chest) {

                            RegionBattle plugin = RegionBattle.getPlugin(RegionBattle.class);

                            PersistentDataContainer chestPersistentDataContainer = chest.getPersistentDataContainer();

                            if(chestPersistentDataContainer.has(new NamespacedKey(plugin,"chestLoot"), PersistentDataType.INTEGER)){
                                if(chestPersistentDataContainer.get(new NamespacedKey(plugin,"chestLoot"), PersistentDataType.INTEGER) == 1){

                                    //Need to clear this chest of items so that they don't stack up with new items each match.
                                    e.getInventory().clear();

                                    //add random loot to chest
                                    LOGGER.info("First time chest has been opened this game, Adding loot");
                                    addRandomItemsToChestInventory(e.getInventory());

                                    //Mark chest as already opened
                                    openedChestsList.add(e.getInventory().getHolder());
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void addRandomItemsToChestInventory(Inventory inventory){
        ArrayList<ItemStack> itemStacks = LootItem.getRandomItemStacks(); //get item stacks to add based on probability

        for(ItemStack itemStack : itemStacks){
            inventory.addItem(itemStack);
        }
    }

}

