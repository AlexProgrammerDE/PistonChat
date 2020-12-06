package me.alexprogrammerde.pistonchat.commands;

import me.alexprogrammerde.pistonchat.utils.CacheTool;
import me.alexprogrammerde.pistonchat.utils.CommonTool;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

public class WhisperCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            Optional<Player> lastMessagedOf = CacheTool.getLastMessagedOf(player);

            if (lastMessagedOf.isPresent()) {
                CommonTool.sendWhisperTo(player, "", lastMessagedOf.get());
                return true;
            } else {
                return false;
            }
        } else {
            sender.sendMessage("You need to be a player to do this!");
            return false;
        }
    }
}
