package me.alexprogrammerde.pistonchat.commands;

import me.alexprogrammerde.pistonchat.utils.CacheTool;
import me.alexprogrammerde.pistonchat.utils.CommonTool;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ReplyCommand implements CommandExecutor, TabExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            Optional<Player> lastMessagedOf = CacheTool.getLastMessagedOf(player);

            if (lastMessagedOf.isPresent()) {
                CommonTool.sendWhisperTo(player, CommonTool.mergeArgs(args, 0), lastMessagedOf.get());
                return true;
            } else {
                return false;
            }
        } else {
            sender.sendMessage("You need to be a player to do this!");
            return false;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return new ArrayList<>();
    }
}
