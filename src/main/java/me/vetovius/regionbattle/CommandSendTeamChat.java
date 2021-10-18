package me.vetovius.regionbattle;

import me.vetovius.regionbattle.persistentbattle.PersistentBattle;
import me.vetovius.regionbattle.regionbattle.Battle;
import me.vetovius.regionbattle.regionbattle.Regions;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.logging.Logger;

public class CommandSendTeamChat implements CommandExecutor {

    private static final Logger LOGGER = Logger.getLogger( CommandSendTeamChat.class.getName() );
    public static Battle battle;
    public static PersistentBattle pbattle;

    // This method is called, when somebody uses our command
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {


        if(sender instanceof Player) {
            Player player = (Player) sender;

            if (battle != null) {
                if (battle.redPlayers.contains(player) || battle.bluePlayers.contains(player)) {
                    if (player.getWorld() == Regions.world) {

                        if (battle.redPlayers.contains(player)) {
                            for (Player p : battle.redPlayers) {
                                p.sendMessage(ChatColor.RED + "[Team Red] " + ChatColor.WHITE + PlainTextComponentSerializer.plainText().serialize(player.displayName()) + ": " + String.join(" ", args));
                            }
                        } else if (battle.bluePlayers.contains(player)) {
                            for (Player p : battle.bluePlayers) {
                                p.sendMessage(ChatColor.BLUE + "[Team Blue] " + ChatColor.WHITE + PlainTextComponentSerializer.plainText().serialize(player.displayName()) + ": " + String.join(" ", args));
                            }
                        }

                    }
                }
            }
            if (pbattle != null) {
                if (pbattle.redPlayers.contains(player) || pbattle.bluePlayers.contains(player)) {

                    if (player.getWorld() == PersistentBattle.world) {

                        if (pbattle.redPlayers.contains(player)) {
                            for (Player p : pbattle.redPlayers) {
                                p.sendMessage(ChatColor.RED + "[Team Red] " + ChatColor.WHITE + PlainTextComponentSerializer.plainText().serialize(player.displayName()) + ": " + String.join(" ", args));
                            }
                        } else if (pbattle.bluePlayers.contains(player)) {
                            for (Player p : pbattle.bluePlayers) {
                                p.sendMessage(ChatColor.BLUE + "[Team Blue] " + ChatColor.WHITE + PlainTextComponentSerializer.plainText().serialize(player.displayName()) + ": " + String.join(" ", args));
                            }
                        }
                    }
                }
            }

            if(pbattle != null && battle != null) {
                if (!pbattle.redPlayers.contains(player) && !pbattle.bluePlayers.contains(player) && !battle.redPlayers.contains(player) && !battle.bluePlayers.contains(player)) {
                    player.sendMessage("You are not in a battle.");
                }
            }

        }

        return true;
    }

}
