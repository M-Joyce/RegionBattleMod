package me.vetovius.regionbattle;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import java.util.logging.Logger;

public class CommandMap implements CommandExecutor {

    private static final Logger LOGGER = Logger.getLogger( CommandMap.class.getName() );

    // This method is called, when somebody uses our command
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        //send vote info
        Bukkit.getPlayer(sender.getName()).sendMessage(ChatColor.DARK_AQUA + "Dynmap Link: " + ChatColor.AQUA +" http://play.regionbattle.com:8123/");

        return true;
    }

}