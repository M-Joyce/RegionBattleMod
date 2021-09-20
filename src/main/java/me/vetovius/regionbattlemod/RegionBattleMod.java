package me.vetovius.regionbattlemod;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
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

}
