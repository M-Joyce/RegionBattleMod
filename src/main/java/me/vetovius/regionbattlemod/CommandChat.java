package me.vetovius.regionbattlemod;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.logging.Logger;

public class CommandChat implements CommandExecutor {

    protected static ArrayList<Player> battleChatPlayers = new ArrayList<Player>();
    protected static ArrayList<Player> survivalChatPlayers = new ArrayList<Player>();

    private static final Logger LOGGER = Logger.getLogger( CommandChat.class.getName() );

    // This method is called, when somebody uses our command
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        Player p = Bukkit.getPlayer(sender.getName());

        if(args[0].toLowerCase() == "s"){
            if(battleChatPlayers.contains(p)){ //remove players from other chat, only allow one chat at a time.
                battleChatPlayers.remove(p);
            }
            if(survivalChatPlayers.contains(p)){
                survivalChatPlayers.remove(p);
                p.sendMessage("You have been removed from [Survival]");
            }
            else{
                survivalChatPlayers.add(p);
                p.sendMessage("You are now chatting in [Survival]");
            }
        }
        else if(args[0].toLowerCase() == "b"){
            if(survivalChatPlayers.contains(p)){ //remove players from other chat, only allow one chat at a time.
                survivalChatPlayers.remove(p);
            }
            if(battleChatPlayers.contains(p)){
                battleChatPlayers.remove(p);
                p.sendMessage("You have been removed from [Battle]");
            }
            else{
                battleChatPlayers.add(p);
                p.sendMessage("You are now chatting in [Battle]");
            }
        }
        else{
            p.sendMessage("Invalid chat! use /chat then s or b.");
        }


        return true;
    }

}
