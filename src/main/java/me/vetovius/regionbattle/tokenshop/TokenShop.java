package me.vetovius.regionbattle.tokenshop;

import me.vetovius.regionbattle.RegionBattle;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashMap;

public class TokenShop implements Listener {
    private final Inventory inv;
    private RegionBattle plugin = RegionBattle.getPlugin(RegionBattle.class);


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
        spawnerLoreList.add(Component.text("Cost: 50 Tokens").color(TextColor.color(0xFFC900)));
        inv.setItem(0,createGuiItem(Material.SPAWNER, spawnerName, spawnerLoreList));


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
            case 0: purchaseItem(p, clickedItem,50);
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
