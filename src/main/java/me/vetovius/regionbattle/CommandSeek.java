package me.vetovius.regionbattle;

import me.vetovius.regionbattle.persistentbattle.PersistentBattle;
import me.vetovius.regionbattle.regionbattle.Battle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.logging.Logger;

public class CommandSeek implements CommandExecutor {

    private static final Logger LOGGER = Logger.getLogger( CommandSeek.class.getName() );
    public static Battle battle;
    public static PersistentBattle pbattle;

    // This method is called, when somebody uses our command
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(sender instanceof Player) {
            Player player = (Player) sender;

            if(battle != null) {
                if (battle.redPlayers.contains(player) || battle.bluePlayers.contains(player)) {
                    if (battle != null) {
                        LOGGER.info("--Seek Command Called in Region Battle--");
                        UUID uuid = player.getUniqueId(); // this should work
                        battle.seekPlayers(uuid);
                    }
                }
            }
            if(pbattle != null) {
                if (pbattle.redPlayers.contains(player) || pbattle.bluePlayers.contains(player)) {
                    if (pbattle != null) {
                        LOGGER.info("--Seek Command Called in Persistent Battle--");
                        UUID uuid = player.getUniqueId(); // this should work
                        pbattle.seekPlayers(uuid);
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
