package me.vetovius.regionbattle.miniboss;

import me.vetovius.regionbattle.tokenshop.Token;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Random;

public enum MiniBossLootItem{

    Netherite_Ingot(Material.NETHERITE_INGOT,50,7),
    Enchanted_Golden_Apple(Material.ENCHANTED_GOLDEN_APPLE,50,5),
    Diamond(Material.DIAMOND,65,25),
    Token(Material.AMETHYST_SHARD,50,7),
    Netherite_Axe(Material.NETHERITE_AXE,10,1),
    Netherite_Shovel(Material.NETHERITE_SHOVEL,10,1),
    Netherite_Pickaxe(Material.NETHERITE_PICKAXE,10,1);

    private Material material;
    private double chance;
    private int maxQuantity;
    private static Random random = new Random();

    MiniBossLootItem(Material material, double chance, int maxQuantity) {
        this.material = material;
        this.chance = chance;
        this.maxQuantity = maxQuantity;
    }

    public static ArrayList<ItemStack> getRandomItemStacks(){

        //This arraylist will hold all the item stacks to add to a chest.
        ArrayList<ItemStack> itemStacks = new ArrayList();


        while(itemStacks.size() == 0){ //dont return an empty itemStacks, chest should always have at least 1 thing.
            for(MiniBossLootItem lootItem : MiniBossLootItem.values()){
                //Determine if itemStack will be present in itemStacks

                int i = random.nextInt(100);
                if(i < lootItem.chance){
                    int quantity = 1 + random.nextInt(lootItem.maxQuantity);
                    if(lootItem.name().equals("Token")) {
                        for (int j = 0; j < quantity; j++){
                            itemStacks.add(me.vetovius.regionbattle.tokenshop.Token.getTokenItemStack());
                        }
                    }
                    else {
                        itemStacks.add(new ItemStack(lootItem.material, quantity));
                    }
                }
            }
        }

        return itemStacks;
    }

}
