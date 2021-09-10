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

        Bukkit.getPluginManager().registerEvents(this, this); //register events
        Bukkit.getPluginManager().registerEvents(new SetupRegions(), this);
        this.getCommand("startregionbattle").setExecutor(new CommandStartRegionBattle()); //register command
        this.getCommand("seek").setExecutor(new CommandSeek()); //register command

    }
    @Override
    public void onDisable() {
        getLogger().info("onDisable is called!");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Bukkit.broadcastMessage("Welcome to the server, "+event.getPlayer().getDisplayName()+"!");
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event){
        //Bukkit.broadcastMessage("A block was placed!");
    }

}
