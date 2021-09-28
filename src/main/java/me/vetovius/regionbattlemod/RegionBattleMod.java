package me.vetovius.regionbattlemod;

import com.vexsoftware.votifier.model.VotifierEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.logging.Logger;


public class RegionBattleMod extends JavaPlugin implements Listener {

private static final Logger LOGGER = Logger.getLogger( RegionBattleMod.class.getName() );

    @Override
    public void onEnable() {
        getLogger().info("onEnable is called!");


        //TODO registering events with class() is calling the constructor, and I think not working for events since Battle gets instantiated elsewhere.
        Bukkit.getPluginManager().registerEvents(this, this); //register events
        //Bukkit.getPluginManager().registerEvents(new Battle(), this); //register events //Using an alternate method by passing instance of this class to Battle in the start battle command
        this.getCommand("startregionbattle").setExecutor(new CommandStartRegionBattle()); //register command
        this.getCommand("seek").setExecutor(new CommandSeek()); //register command
        this.getCommand("tc").setExecutor(new CommandSendTeamChat()); //register command
        this.getCommand("battleoptout").setExecutor(new CommandBattleOptOut()); //register command
        this.getCommand("chat").setExecutor(new CommandChat()); //register command
        this.getCommand("vote").setExecutor(new CommandVote()); //register command


        Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            @Override
            public void run() {
                //Start first battle.
                ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
                Bukkit.dispatchCommand(console, "startregionbattle");
            }
        }, 20*60*2L); //20 Tick (1 Second) delay before run() is called

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
    public void onPlayerChat(AsyncPlayerChatEvent e){
        Player p = e.getPlayer();
        if(CommandChat.survivalChatPlayers.contains(p)){
            ArrayList<Player> toRemove = new ArrayList<Player>();
            //Chat to survival chat
            for(Player player : e.getRecipients()){ //modify recipients
                if(!CommandChat.survivalChatPlayers.contains(player)){
                    toRemove.add(player);
                }
            }
            e.getRecipients().removeAll(toRemove);
            toRemove.clear();
            e.setMessage(ChatColor.YELLOW +"[Survival] "+ChatColor.WHITE+e.getMessage());
        }
        else if(CommandChat.battleChatPlayers.contains(p)){
            ArrayList<Player> toRemove = new ArrayList<Player>();
            //Chat to battle chat
            for(Player player : e.getRecipients()){ //modify recipients
                if(!CommandChat.battleChatPlayers.contains(player)){
                    toRemove.add(player);
                }
            }
            e.getRecipients().removeAll(toRemove);
            toRemove.clear();
            e.setMessage(ChatColor.RED+"[Battle] "+ChatColor.WHITE+e.getMessage());

        }
        else {
            //do nothing, continue regular global chat.
            e.setMessage("[Global] "+e.getMessage());
        }


    }

    //VOTE HANDLING
    @EventHandler
    public void onVoteEvent(VotifierEvent event){
        ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();

        if(event.getVote().getUsername() != null){

            Player player = Bukkit.getPlayer(event.getVote().getUsername());

            if(player != null){
                player.sendMessage("Thanks for voting!");
                Bukkit.dispatchCommand(console, "eco give "+player.getName()+" 25"); //give $25 for voting!
            }
            else{
                LOGGER.info("VOTIFIER: INVALID PLAYER NAME!" + event.getVote().toString());
            }

        }

    }

}
