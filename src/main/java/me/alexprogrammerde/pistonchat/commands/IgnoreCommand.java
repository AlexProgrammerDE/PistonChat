package me.alexprogrammerde.pistonchat.commands;

import me.alexprogrammerde.pistonchat.utils.ConfigTool;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class IgnoreCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (args.length > 0) {
                Player ignored = Bukkit.getPlayer(args[0]);

                ConfigTool.ignorePlayer(player, ignored);
                return true;
            } else {
                return false;
            }
        } else {
            sender.sendMessage("You need to be a player to do that!");
            return false;
        }
    }
}
