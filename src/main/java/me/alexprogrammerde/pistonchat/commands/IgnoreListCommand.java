package me.alexprogrammerde.pistonchat.commands;

import me.alexprogrammerde.pistonchat.utils.CommonTool;
import me.alexprogrammerde.pistonchat.utils.ConfigTool;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class IgnoreListCommand implements CommandExecutor, TabExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            List<String> list = new ArrayList<>();

            for (OfflinePlayer offlinePlayer : ConfigTool.getHardIgnoredPlayers(player)) {
                list.add(offlinePlayer.getName());
            }

            if (list.isEmpty()) {
                player.sendMessage(CommonTool.getPrefix() + "You have no players ignored!");
            } else {
                player.sendMessage(CommonTool.getPrefix() + "All ignored players: " + String.join(", ", list));
            }
        } else {
            sender.sendMessage(CommonTool.getPrefix() + "You need to be a player to do that!");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return new ArrayList<>();
    }
}
