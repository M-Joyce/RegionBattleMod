package me.vetovius.regionbattle.rankuptokenrequirements;

import me.vetovius.regionbattle.RegionBattle;
import me.vetovius.regionbattle.tokenshop.Token;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import sh.okx.rankup.RankupPlugin;
import sh.okx.rankup.requirements.DeductibleRequirement;
import sh.okx.rankup.requirements.ProgressiveRequirement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

public class RBTokenDeductibleRequirement extends ProgressiveRequirement implements DeductibleRequirement {

    public static RegionBattle rbPlugin = RegionBattle.getPlugin(RegionBattle.class);

    private static final Logger LOGGER = Logger.getLogger( RBTokenDeductibleRequirement.class.getName() );

    public RBTokenDeductibleRequirement(RankupPlugin plugin, String name) {
        super(plugin, name);
    }

    protected RBTokenDeductibleRequirement(RBTokenDeductibleRequirement clone) {
        super(clone);
    }

    @Override
    public double getProgress(Player player) {

        //get the number of tokens in the player inventory

        HashMap<Integer, ? extends ItemStack> tokenHashMap = player.getInventory().all(Material.AMETHYST_SHARD);

        ArrayList<ItemStack> tokenItemStacks = new ArrayList<>();

        tokenHashMap.forEach((k, v) -> {
            if(v.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(rbPlugin,"isToken"), PersistentDataType.BYTE)) {
                if (v.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(rbPlugin, "isToken"), PersistentDataType.BYTE) == 1) {
                    tokenItemStacks.add(v);
                }
            }
        });

        double totalTokensInPlayerInventory = 0L;
        for(ItemStack tokenStacks : tokenItemStacks){
            totalTokensInPlayerInventory += tokenStacks.getAmount();
        }

        return totalTokensInPlayerInventory;
    }

    @Override
    public void apply(Player player, double multiplier) {
        //remove tokens here

        HashMap<Integer, ? extends ItemStack> tokenHashMap = player.getInventory().all(Material.AMETHYST_SHARD);

        ArrayList<ItemStack> tokenItemStacks = new ArrayList<>();

        tokenHashMap.forEach((k, v) -> {
            if(v.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(rbPlugin,"isToken"), PersistentDataType.BYTE)) {
                if (v.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(rbPlugin, "isToken"), PersistentDataType.BYTE) == 1) {
                    tokenItemStacks.add(v);
                }
            }
        });

        int totalTokensInPlayerInventory = 0;
        for(ItemStack tokenStacks : tokenItemStacks){
            totalTokensInPlayerInventory += tokenStacks.getAmount();
        }

        if(totalTokensInPlayerInventory >= getValueInt()){
            player.sendMessage("You have used "+getValueInt() +" tokens to rank up.");
            LOGGER.info(player.getName() + "purchased a rank for # tokens:" + getValueInt());

            ItemStack tokensToRemove = Token.getTokenItemStack();
            tokensToRemove.setAmount(getValueInt()); //set the amount of the itemStack
            player.getInventory().removeItemAnySlot(tokensToRemove); //remove tokens
        }
        else{
            player.sendMessage("You do not have enough tokens!");
        }

    }

    @Override
    public ProgressiveRequirement clone() {
        return new RBTokenDeductibleRequirement(this);
    }

}
