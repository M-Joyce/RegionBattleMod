package me.vetovius.regionbattle.tokenshop;

import me.vetovius.regionbattle.RegionBattle;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

public class TokenShop implements Listener {
    private final Inventory inv;
    private RegionBattle plugin = RegionBattle.getPlugin(RegionBattle.class);
    private static final Logger LOGGER = Logger.getLogger( TokenShop.class.getName() );


    public TokenShop() {


        Bukkit.getPluginManager().registerEvents(this, plugin); //register events


        TextComponent inventoryName = Component.text("RegionBattle Token Shop").color(TextColor.color(0xC29D));
        // Create a new inventory, with no owner (as this isn't a real inventory)
        inv = Bukkit.createInventory(null, 27, inventoryName);

        // Put the items into the inventory
        initializeItems();
    }

    // You can call this whenever you want to put the items in
    public void initializeItems() {

        TextComponent spawnerName = Component.text("Spawner").color(TextColor.color(0xC29D));
        ArrayList spawnerLoreList = new ArrayList<Component>();
        spawnerLoreList.add(Component.text("A mob spawner.").color(TextColor.color(0xFFFF)));
        spawnerLoreList.add(Component.text("Right click with a spawn egg once placed to determine type.").color(TextColor.color(0xFFFF)));
        spawnerLoreList.add(Component.text("Cost: 30 Tokens").color(TextColor.color(0xFFC900)));
        inv.setItem(0,createGuiItem(Material.SPAWNER, spawnerName, spawnerLoreList));

        TextComponent zombieEggName = Component.text("Zombie Spawn Egg").color(TextColor.color(0xC29D));
        ArrayList zombieEggLoreList = new ArrayList<Component>();
        zombieEggLoreList.add(Component.text("A zombie spawn egg.").color(TextColor.color(0xFFFF)));
        zombieEggLoreList.add(Component.text("Right click your spawner with this to change its type.").color(TextColor.color(0xFFFF)));
        zombieEggLoreList.add(Component.text("Cost: 30 Tokens").color(TextColor.color(0xFFC900)));
        inv.setItem(1,createGuiItem(Material.ZOMBIE_SPAWN_EGG, zombieEggName, zombieEggLoreList));

        TextComponent skeletonEggName = Component.text("Skeleton Spawn Egg").color(TextColor.color(0xC29D));
        ArrayList skeletonEggLoreList = new ArrayList<Component>();
        skeletonEggLoreList.add(Component.text("A skeleton spawn egg.").color(TextColor.color(0xFFFF)));
        skeletonEggLoreList.add(Component.text("Right click your spawner with this to change its type.").color(TextColor.color(0xFFFF)));
        skeletonEggLoreList.add(Component.text("Cost: 35 Tokens").color(TextColor.color(0xFFC900)));
        inv.setItem(2,createGuiItem(Material.SKELETON_SPAWN_EGG, skeletonEggName, skeletonEggLoreList));

        TextComponent creeperEggName = Component.text("Creeper Spawn Egg").color(TextColor.color(0xC29D));
        ArrayList creeperEggLoreList = new ArrayList<Component>();
        creeperEggLoreList.add(Component.text("A creeper spawn egg.").color(TextColor.color(0xFFFF)));
        creeperEggLoreList.add(Component.text("Right click your spawner with this to change its type.").color(TextColor.color(0xFFFF)));
        creeperEggLoreList.add(Component.text("Cost: 40 Tokens").color(TextColor.color(0xFFC900)));
        inv.setItem(3,createGuiItem(Material.CREEPER_SPAWN_EGG, creeperEggName, creeperEggLoreList));

        TextComponent TokenPickaxeName = Component.text("Token Pickaxe").color(TextColor.color(0xC29D));
        ArrayList TokenPickaxeLoreList = new ArrayList<Component>();
        TokenPickaxeLoreList.add(Component.text("A very good pickaxe.").color(TextColor.color(0xFFFF)));
        TokenPickaxeLoreList.add(Component.text("Efficiency 7").color(TextColor.color(0xFFFF)));
        TokenPickaxeLoreList.add(Component.text("Unbreaking 4").color(TextColor.color(0xFFFF)));
        TokenPickaxeLoreList.add(Component.text("Fortune 3").color(TextColor.color(0xFFFF)));
        TokenPickaxeLoreList.add(Component.text("Cost: 50 Tokens").color(TextColor.color(0xFFC900)));
        inv.setItem(4,createGuiItem(Material.NETHERITE_PICKAXE, TokenPickaxeName, TokenPickaxeLoreList));

        TextComponent Fortune4BookName = Component.text("Fortune 4 Enchanted Book").color(TextColor.color(0xC29D));
        ArrayList Fortune4BookLoreList = new ArrayList<Component>();
        Fortune4BookLoreList.add(Component.text("A very good pickaxe.").color(TextColor.color(0xFFFF)));
        Fortune4BookLoreList.add(Component.text("Fortune 4").color(TextColor.color(0xFFFF)));
        Fortune4BookLoreList.add(Component.text("Cost: 20 Tokens").color(TextColor.color(0xFFC900)));
        inv.setItem(5,createGuiItem(Material.ENCHANTED_BOOK, Fortune4BookName, Fortune4BookLoreList));

    }

    // Nice little method to create a gui item with a custom name, and description
    protected ItemStack createGuiItem(final Material material, final Component name, final ArrayList<Component> loreComponentList) {
        final ItemStack item = new ItemStack(material, 1);
        final ItemMeta meta = item.getItemMeta();

        // Set the name of the item
        meta.displayName(name);

        // Set the lore of the item
        meta.lore(loreComponentList);

        item.setItemMeta(meta);

        return item;
    }

    // You can open the inventory with this
    public void openInventory(final HumanEntity ent) {
        ent.openInventory(inv);
    }

    // Check for clicks on items
    @EventHandler
    public void onInventoryClick(final InventoryClickEvent e) {
        if (e.getInventory() != inv) return;

        e.setCancelled(true);

        final ItemStack clickedItem = e.getCurrentItem();

        // verify current item is not null
        if (clickedItem == null || clickedItem.getType().isAir()) return;

        final Player p = (Player) e.getWhoClicked();

        // Using slots click is a best option for your inventory click's
        int slotClicked = e.getRawSlot();
        //p.sendMessage("You clicked at slot " + slotClicked);

        //Switch for what to do based on slotClicked
        switch (slotClicked) {
            case 0: purchaseItem(p, clickedItem,30); //spawner
                break;
            case 1: purchaseItem(p, clickedItem,30); //zomb egg
                break;
            case 2: purchaseItem(p, clickedItem,35); //skelly egg
                break;
            case 3: purchaseItem(p, clickedItem,40); //creeper egg
                break;
            case 4:  //Token voter pickaxe
                ItemStack TokenPickAxe = new ItemStack(Material.NETHERITE_PICKAXE);
                ItemMeta TokenPickMeta = TokenPickAxe.getItemMeta();
                TextComponent tokenPickName = Component.text("Token Pickaxe").color(TextColor.color(0xC29D));
                TokenPickMeta.displayName(tokenPickName);
                TokenPickMeta.addEnchant(Enchantment.DIG_SPEED, 7, true);
                TokenPickMeta.addEnchant(Enchantment.DURABILITY, 4, true);
                TokenPickMeta.addEnchant(Enchantment.LOOT_BONUS_BLOCKS, 3, true);
                TokenPickAxe.setItemMeta(TokenPickMeta);
                purchaseItem(p, TokenPickAxe,50);
                break;
            case 5:  //Fortune 4 Book
                ItemStack Fortune4Book = new ItemStack(Material.ENCHANTED_BOOK);
                EnchantmentStorageMeta Fortune4BookMeta = (EnchantmentStorageMeta) Fortune4Book.getItemMeta();
                TextComponent Fortune4Bookname = Component.text("Enchanted Book: Fortune 4").color(TextColor.color(0xC29D));
                Fortune4BookMeta.displayName(Fortune4Bookname);
                Fortune4BookMeta.addStoredEnchant(Enchantment.LOOT_BONUS_BLOCKS, 4, true);
                Fortune4Book.setItemMeta(Fortune4BookMeta);
                purchaseItem(p, Fortune4Book,20);
                break;
        }


    }

    //determine if item can be purchased, and purchase it if possible, deducting tokens.
    private void purchaseItem(Player player, ItemStack itemStack, int tokenCost){

        HashMap<Integer, ? extends ItemStack> tokenHashMap = player.getInventory().all(Material.AMETHYST_SHARD);

        ArrayList<ItemStack> tokenItemStacks = new ArrayList<>();

        tokenHashMap.forEach((k, v) -> {
            if(v.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(plugin,"isToken"), PersistentDataType.BYTE)) {
                if (v.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(plugin, "isToken"), PersistentDataType.BYTE) == 1) {
                    tokenItemStacks.add(v);
                }
            }
        });

        int totalTokensInPlayerInventory = 0;
        for(ItemStack tokenStacks : tokenItemStacks){
            totalTokensInPlayerInventory += tokenStacks.getAmount();
        }

        if(totalTokensInPlayerInventory >= tokenCost){
            player.sendMessage(Component.text("You have purchased: ").append(itemStack.getItemMeta().displayName()));

            ItemStack tokensToRemove = Token.getTokenItemStack();
            tokensToRemove.setAmount(tokenCost);
            player.getInventory().removeItemAnySlot(tokensToRemove);
            player.getInventory().addItem(itemStack);

            LOGGER.info(player.getName() + " purchased an item from the token shop for tokens: " + tokenCost);
        }
        else{
            player.sendMessage("You do not have enough tokens!");
        }




    }


    // Cancel dragging in our inventory
    @EventHandler
    public void onInventoryClick(final InventoryDragEvent e) {
        if (e.getInventory().equals(inv)) {
            e.setCancelled(true);
        }
    }

}
