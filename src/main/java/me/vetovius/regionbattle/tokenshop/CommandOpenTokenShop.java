package me.vetovius.regionbattle.tokenshop;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.logging.Logger;

public class CommandOpenTokenShop implements CommandExecutor {

        private static final Logger LOGGER = Logger.getLogger( CommandOpenTokenShop.class.getName() );

        // This method is called, when somebody uses our command
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

            if(sender instanceof Player) {
                TokenShop tokenShop = new TokenShop();
                tokenShop.openInventory((Player)sender);
            }


            return true;
        }

}
