package me.vetovius.regionbattle;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import java.util.logging.Logger;

public class CommandDiscord implements CommandExecutor {

    private static final Logger LOGGER = Logger.getLogger( CommandDiscord.class.getName() );

    // This method is called, when somebody uses our command
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        //send discord
        Bukkit.getPlayer(sender.getName()).sendMessage(ChatColor.BLUE + "Discord Invite Link: " + ChatColor.AQUA +"https://discord.regionbattle.com/");

        return true;
    }

}