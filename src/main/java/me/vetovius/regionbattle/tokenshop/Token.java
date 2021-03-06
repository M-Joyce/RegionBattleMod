package me.vetovius.regionbattle.tokenshop;

import me.vetovius.regionbattle.RegionBattle;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;

public class Token {

    public static RegionBattle plugin = RegionBattle.getPlugin(RegionBattle.class);

    public static ItemStack getTokenItemStack(){

        ItemStack tokenItemStack = new ItemStack(Material.AMETHYST_SHARD,1);
        ItemMeta tokenMeta = tokenItemStack.getItemMeta();

        TextComponent tokenName = Component.text("Token").color(TextColor.color(0xC29D));
        tokenMeta.displayName(tokenName);

        ArrayList loreList = new ArrayList<Component>();
        loreList.add(Component.text("A special token.").color(TextColor.color(0xFFC900)));
        loreList.add(Component.text("Spend in the Token shop at SMP spawn.").color(TextColor.color(0xFFC900)));

        tokenMeta.lore(loreList);

        PersistentDataContainer tokenPDC = tokenMeta.getPersistentDataContainer();

        byte t = 1;
        if(!tokenPDC.has(new NamespacedKey(plugin,"isToken"), PersistentDataType.BYTE)) {
            tokenPDC.set(new NamespacedKey(plugin, "isToken"), PersistentDataType.BYTE, t);
        }
        tokenItemStack.setItemMeta(tokenMeta);

        return tokenItemStack;
    }






}
