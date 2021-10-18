//package me.vetovius.regionbattle.regionbattle;
//
//import org.bukkit.Bukkit;
//import org.bukkit.ChatColor;
//import org.bukkit.command.Command;
//import org.bukkit.command.CommandExecutor;
//import org.bukkit.command.CommandSender;
//import org.bukkit.entity.Player;
//
//import java.util.HashMap;
//import java.util.logging.Logger;
//
//public class CommandChat implements CommandExecutor {
//
//    public static HashMap<Player,String> playerChatChannels = new HashMap();
//
//
//    private static final Logger LOGGER = Logger.getLogger( CommandChat.class.getName() );
//
//    // This method is called, when somebody uses our command
//    @Override
//    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
//
//        Player p = Bukkit.getPlayer(sender.getName());
//
//        if(args.length > 0) {
//
//            if (args[0].equalsIgnoreCase("g")) { //global chat
//                CommandChat.playerChatChannels.put(p,"g");
//                p.sendMessage("You are now chatting in [Global]. Everyone can hear you.");
//            }
//            else if (args[0].equalsIgnoreCase("s")) { //survival chat
//                CommandChat.playerChatChannels.put(p,"s");
//                p.sendMessage("You are now chatting in [" + ChatColor.YELLOW + "SMP" + ChatColor.WHITE + "]. Only the SMP worlds can hear you.");
//            }
//            else if (args[0].equalsIgnoreCase("b")) { //battle chat
//                CommandChat.playerChatChannels.put(p,"b");
//                p.sendMessage("You are now chatting in [" + ChatColor.RED + "Battle" + ChatColor.WHITE + "]. Only the RegionBattle world can hear you.");
//            }
//            else { //global chat
//                p.sendMessage("Invalid chat! use /chat then g, s, or b.");
//            }
//        }
//        else{
//            p.sendMessage("Invalid chat! use /chat then g, s, or b.");
//        }
//
//        return true;
//    }
//
//}
