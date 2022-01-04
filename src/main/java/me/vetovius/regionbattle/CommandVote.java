package me.vetovius.regionbattle;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import java.util.logging.Logger;

public class CommandVote implements CommandExecutor {

    private static final Logger LOGGER = Logger.getLogger( CommandVote.class.getName() );

    // This method is called, when somebody uses our command
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        //send vote info
        Bukkit.getPlayer(sender.getName()).sendMessage(ChatColor.AQUA + "Vote for us to get a reward! Voting daily helps our server grow!\n" +
                "All vote sites are listed at: " + ChatColor.YELLOW + "[" + ChatColor.GOLD + "https://regionbattle.com/vote" + ChatColor.YELLOW + "]\n" +
                ChatColor.AQUA + "Make sure you are online while voting, and bedrock users should include the '-' before their name.");
        return true;
    }

}