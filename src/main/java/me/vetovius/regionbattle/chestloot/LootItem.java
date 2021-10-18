package me.vetovius.regionbattle.chestloot;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Random;

public enum LootItem{

    Iron_Ingot(Material.IRON_INGOT,3,5),
    Cooked_Beef(Material.COOKED_BEEF,10,4),
    Golden_Apple(Material.GOLDEN_APPLE,1,2),
    Stone_Axe(Material.STONE_AXE,7,1),
    Stone_Sword(Material.STONE_SWORD,7,1),
    Diamond(Material.DIAMOND,1,2),
    Oak_Log(Material.OAK_LOG,12,2),
    Leather_Chestplate(Material.LEATHER_CHESTPLATE,5,1),
    Leather_Boots(Material.LEATHER_BOOTS,7,1),
    Leather_Leggings(Material.LEATHER_LEGGINGS,5,1),
    Leather_Helmet(Material.LEATHER_HELMET,7,1),
    Iron_Sword(Material.IRON_SWORD,3,1),
    TNT(Material.TNT,1,3),
    Lighter(Material.FLINT_AND_STEEL,1,1),
    Iron_Chestplate(Material.IRON_CHESTPLATE,3,1),
    Iron_Leggings(Material.IRON_LEGGINGS,3,1),
    Bow(Material.BOW,3,1),
    Arrow(Material.ARROW,5,12),
    Diamond_Axe(Material.DIAMOND_AXE,0.8,1),
    Shield(Material.SHIELD,2,1),
    Bread(Material.BREAD,10,4),
    Cooked_Porkchop(Material.COOKED_PORKCHOP,10,4),
    Golden_Axe(Material.GOLDEN_AXE,2,1),
    Raw_Iron(Material.RAW_IRON,7,4),
    Bookshelf(Material.BOOKSHELF,5,3),
    Enchanting_Table(Material.ENCHANTING_TABLE,3,1);

    private Material material;
    private double chance;
    private int maxQuantity;
    private static Random random = new Random();

    LootItem(Material material, double chance, int maxQuantity) {
        this.material = material;
        this.chance = chance;
        this.maxQuantity = maxQuantity;
    }

    public static ArrayList<ItemStack> getRandomItemStacks(){

        //This arraylist will hold all the item stacks to add to a chest.
        ArrayList<ItemStack> itemStacks = new ArrayList();


        while(itemStacks.size() == 0){ //dont return an empty itemStacks, chest should always have at least 1 thing.
            for(LootItem lootItem : LootItem.values()){
                //Determine if itemStack will be present in itemStacks

                int i = random.nextInt(100);
                if(i < lootItem.chance){
                    int quantity = 1 + random.nextInt(lootItem.maxQuantity);
                    itemStacks.add(new ItemStack(lootItem.material,quantity));
                }
            }
        }

        return itemStacks;
    }

}
