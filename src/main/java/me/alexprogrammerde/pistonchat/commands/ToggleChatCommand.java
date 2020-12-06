package me.alexprogrammerde.pistonchat.commands;

import me.alexprogrammerde.pistonchat.utils.ConfigTool;
import me.alexprogrammerde.pistonchat.utils.TempDataTool;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ToggleChatCommand implements CommandExecutor, TabExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            TempDataTool.setChatEnabled(player, !TempDataTool.isChatEnabled(player));

            if (TempDataTool.isChatEnabled(player)) {
                player.sendMessage("Enabled chat messages!");
            } else {
                player.sendMessage("Disabled chat messages! (Will be reset on rejoin!)");
            }
        } else {
            sender.sendMessage("You need to be a player to do that!");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return new ArrayList<>();
    }
}
