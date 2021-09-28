package me.vetovius.regionbattlemod;

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
        Bukkit.getPlayer(sender.getName()).sendMessage(ChatColor.LIGHT_PURPLE + "Vote for us to get a reward!\n" +
                "https://minecraftservers.org/server/624356\n" +
                "https://minecraft-server-list.com/server/481756/vote/\n" +
                "https://minecraft-mp.com/server/293346/vote/\n" +
                "https://www.planetminecraft.com/server/regionbattle/\n" +
                "https://topminecraftservers.org/server/21394\n" +
                "https://minebrowse.com/server/3550\n");

        return true;
    }

}