package me.vetovius.regionbattle;

import com.vexsoftware.votifier.model.VotifierEvent;
import me.vetovius.regionbattle.chestloot.CommandCreateLootChest;
import me.vetovius.regionbattle.persistentbattle.*;
import me.vetovius.regionbattle.regionbattle.*;
import me.vetovius.regionbattle.tokenshop.*;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.EnderChest;
import org.bukkit.block.ShulkerBox;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;


public class RegionBattle extends JavaPlugin implements Listener {

private static final Logger LOGGER = Logger.getLogger( RegionBattle.class.getName() );

    @Override
    public void onEnable() {
        getLogger().info("onEnable is called!");


        Bukkit.getPluginManager().registerEvents(this, this); //register events

        this.getCommand("startregionbattle").setExecutor(new CommandStartRegionBattle()); //register command
        this.getCommand("seek").setExecutor(new CommandSeek()); //register command
        this.getCommand("tc").setExecutor(new CommandSendTeamChat()); //register command
        this.getCommand("battleoptout").setExecutor(new CommandBattleOptOut()); //register command
        //this.getCommand("chat").setExecutor(new CommandChat()); //register command
        this.getCommand("vote").setExecutor(new CommandVote()); //register command

        this.getCommand("createlootchest").setExecutor(new CommandCreateLootChest()); //register command
        this.getCommand("giveplayertoken").setExecutor(new CommandGivePlayerToken()); //register command
        this.getCommand("opentokenshop").setExecutor(new CommandOpenTokenShop()); //register command
        this.getCommand("createtokenshop").setExecutor(new CommandCreateTokenShop()); //register command

        //persistent battle commands
        this.getCommand("startpersistentbattle").setExecutor(new CommandStartPersistentBattle()); //register command
        this.getCommand("joinbattle").setExecutor(new CommandJoinBattle()); //register command
        this.getCommand("leavebattle").setExecutor(new CommandLeaveBattle()); //register command


        Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            @Override
            public void run() {
                //Start first battle.
                ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
                Bukkit.dispatchCommand(console, "startregionbattle");
                Bukkit.dispatchCommand(console, "startpersistentbattle");
            }
        }, 20*60*2L); //20 Tick (1 Second) * 60 * 2 delay before run() is called

    }
    @Override
    public void onDisable() {
        getLogger().info("onDisable is called!");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {

    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event){
        //Bukkit.broadcastMessage("A block was placed!");
    }

    @EventHandler
    public void onChestOpenEvent(InventoryOpenEvent e){ //Event for tokenshop

        if (e.getInventory().getHolder() instanceof ShulkerBox){

            BlockState blockState = ((BlockState) e.getInventory().getHolder()).getBlock().getState();
            if(blockState instanceof ShulkerBox sbox) {

                PersistentDataContainer chestPersistentDataContainer = sbox.getPersistentDataContainer();

                if(chestPersistentDataContainer.has(new NamespacedKey(this,"isTokenShop"), PersistentDataType.BYTE)){
                    Byte b = 1;
                    if(chestPersistentDataContainer.get(new NamespacedKey(this,"isTokenShop"), PersistentDataType.BYTE) == b){
                        e.setCancelled(true); //cancel shulker open
                        TokenShop ts = new TokenShop();
                        ts.openInventory(e.getPlayer());
                    }
                }
            }
        }

    }

//    @EventHandler
//    public void onPlayerChat(AsyncPlayerChatEvent e){
//        Player p = e.getPlayer();
//        ArrayList<Player> toRemove = new ArrayList<Player>();
//
//        if(!CommandChat.playerChatChannels.containsKey(p)){ //always ensure player is default in global chat
//            CommandChat.playerChatChannels.put(p,"g");
//        }
//
//        if(CommandChat.playerChatChannels.get(p).equals("g")){ //global
//            //do nothing, continue regular global chat.
//            e.setMessage("[Global] "+e.getMessage());
//        }
//        else if(CommandChat.playerChatChannels.get(p).equals("s")){ //survival
//
//            for(Player player : Regions.world.getPlayers()){
//                toRemove.add(player); //add players not in SMP worlds to a removal list
//            }
//            e.getRecipients().removeAll(toRemove); //remove players not in an SMP world
//            toRemove.clear(); //clear list
//            e.setMessage(ChatColor.YELLOW +"[SMP] "+ChatColor.WHITE+e.getMessage());
//
//        }
//        else if(CommandChat.playerChatChannels.get(p).equals("b")){ //battle TODO FIX THIS FOR PERSISTENTBATTLES!
//
//            e.getRecipients().clear(); //remove all players
//            e.getRecipients().addAll(Regions.world.getPlayers()); //add all players in RB world.
//            e.setMessage(ChatColor.RED+"[Battle] "+ChatColor.WHITE+e.getMessage());
//
//        }
//        else{
//            p.sendMessage("Something went terribly wrong, tell an admin.");
//        }
//
//    }

    //VOTE HANDLING
    @EventHandler
    public void onVoteEvent(VotifierEvent event){
        ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();

        if(event.getVote().getUsername() != null){

            Player player = Bukkit.getPlayer(event.getVote().getUsername());

            if(player != null){
                player.sendMessage("Thanks for voting! Have a reward!");
                Bukkit.dispatchCommand(console, "eco give "+player.getName()+" 25"); //give $25 for voting!
                player.getInventory().addItem(Token.getTokenItemStack()); //give player a Token
            }
            else{
                LOGGER.info("VOTIFIER: INVALID PLAYER NAME!" + event.getVote().toString());
            }

        }

    }

    @EventHandler
    public void onPlayerLogOff (PlayerQuitEvent event) { //handle when players log off in the middle of battle

        if(Battle.optOutPlayersList.contains(event.getPlayer())){ //remove disconnected players from optout list.
            Battle.optOutPlayersList.remove(event.getPlayer());
        }

    }

}
