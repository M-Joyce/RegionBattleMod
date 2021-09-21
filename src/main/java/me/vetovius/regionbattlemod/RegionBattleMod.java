package me.vetovius.regionbattlemod;

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


public class RegionBattleMod extends JavaPlugin implements Listener {



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

        //Start first battle.
        ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
        Bukkit.dispatchCommand(console, "startregionbattle");
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
            //Chat to survival chat
            for(Player player : e.getRecipients()){ //modify recipients
                if(!CommandChat.survivalChatPlayers.contains(player)){
                    e.getRecipients().remove(player);
                }
            }
            e.setMessage(ChatColor.YELLOW +"[Survival] "+ChatColor.WHITE+e.getMessage());
        }
        else if(CommandChat.battleChatPlayers.contains(p)){
            //Chat to battle chat
            for(Player player : e.getRecipients()){ //modify recipients
                if(!CommandChat.battleChatPlayers.contains(player)){
                    e.getRecipients().remove(player);
                }
            }
            e.setMessage(ChatColor.RED+"[Battle] "+ChatColor.WHITE+e.getMessage());

        }
        else {
            //do nothing, continue regular global chat.
            e.setMessage("[Global] "+e.getMessage());
        }


    }

}
