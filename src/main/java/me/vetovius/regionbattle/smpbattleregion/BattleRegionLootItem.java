package me.vetovius.regionbattle.smpbattleregion;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.util.ArrayList;
import java.util.Random;

public enum BattleRegionLootItem{

    Netherite_Ingot(Material.NETHERITE_INGOT,50,5),
    Enchanted_Golden_Apple(Material.ENCHANTED_GOLDEN_APPLE,25,3),
    Diamond(Material.DIAMOND,65,20),
    Token(Material.AMETHYST_SHARD,50,7),
    Netherite_Axe(Material.NETHERITE_AXE,10,1),
    Netherite_Shovel(Material.NETHERITE_SHOVEL,10,1),
    Netherite_Pickaxe(Material.NETHERITE_PICKAXE,10,1),
    EnchantedBook_Efficiency5(Material.ENCHANTED_BOOK,Enchantment.DIG_SPEED,5,2,1),
    EnchantedBook_Efficiency4(Material.ENCHANTED_BOOK,Enchantment.DIG_SPEED,4,3,1),
    EnchantedBook_Mending(Material.ENCHANTED_BOOK,Enchantment.MENDING,1,2,1),
    EnchantedBook_Looting3(Material.ENCHANTED_BOOK,Enchantment.LOOT_BONUS_BLOCKS,3,2,1),
    EnchantedBook_Protection4(Material.ENCHANTED_BOOK,Enchantment.PROTECTION_ENVIRONMENTAL,4,2,1),
    EnchantedBook_Protection3(Material.ENCHANTED_BOOK,Enchantment.PROTECTION_ENVIRONMENTAL,3,3,1),
    EnchantedBook_Unbreaking3(Material.ENCHANTED_BOOK,Enchantment.DURABILITY,3,2,1),
    EnchantedBook_Sharpness5(Material.ENCHANTED_BOOK,Enchantment.DAMAGE_ALL,5,2,1);

    private Material material;
    private double chance;
    private int maxQuantity;
    private static Random random = new Random();
    private int enchantLevel;
    private Enchantment enchantment;

    BattleRegionLootItem(Material material, double chance, int maxQuantity) {
        this.material = material;
        this.chance = chance;
        this.maxQuantity = maxQuantity;
    }

    BattleRegionLootItem(Material material, Enchantment enchantment, int enchantLevel, double chance, int maxQuantity) {
        this.material = material;
        this.chance = chance;
        this.maxQuantity = maxQuantity;
        this.enchantLevel = enchantLevel;
        this.enchantment = enchantment;
    }

    public static ArrayList<ItemStack> getRandomItemStacks(){

        //This arraylist will hold all the item stacks to add to a chest.
        ArrayList<ItemStack> itemStacks = new ArrayList();


        while(itemStacks.size() == 0){ //dont return an empty itemStacks, chest should always have at least 1 thing.
            for(BattleRegionLootItem lootItem : BattleRegionLootItem.values()){
                //Determine if itemStack will be present in itemStacks

                int i = random.nextInt(100);
                if(i < lootItem.chance){
                    int quantity = 1 + random.nextInt(lootItem.maxQuantity);
                    if(lootItem.name().equals("Token")) {
                        for (int j = 0; j < quantity; j++){
                            itemStacks.add(me.vetovius.regionbattle.tokenshop.Token.getTokenItemStack());
                        }
                    }
                    else if(lootItem.material == Material.ENCHANTED_BOOK){
                        ItemStack enchantedBookItemStack = new ItemStack(lootItem.material);
                        EnchantmentStorageMeta enchantmentStorageMeta = (EnchantmentStorageMeta) enchantedBookItemStack.getItemMeta();
                        enchantmentStorageMeta.addStoredEnchant(lootItem.enchantment, lootItem.enchantLevel, true);
                        enchantedBookItemStack.setItemMeta(enchantmentStorageMeta);

                        itemStacks.add(enchantedBookItemStack);
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
