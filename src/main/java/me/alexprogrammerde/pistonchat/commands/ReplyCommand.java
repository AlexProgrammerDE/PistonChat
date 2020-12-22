package me.alexprogrammerde.pistonchat.commands;

import me.alexprogrammerde.pistonchat.utils.CacheTool;
import me.alexprogrammerde.pistonchat.utils.CommonTool;
import me.alexprogrammerde.pistonchat.utils.ConfigTool;
import me.alexprogrammerde.pistonchat.utils.IgnoreTool;
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
                if (IgnoreTool.isIgnored(player, lastMessagedOf.get())) {
                    player.sendMessage(CommonTool.getPrefix() + "This person blocked you!");
                } else if (IgnoreTool.isIgnored(lastMessagedOf.get(), player)) {
                    player.sendMessage(CommonTool.getPrefix() + "You block this person!");
                } else {
                    if (args.length > 0) {
                        CommonTool.sendWhisperTo(player, CommonTool.mergeArgs(args, 0), lastMessagedOf.get());
                    } else {
                        return false;
                    }
                }
            } else {
                player.sendMessage(CommonTool.getPrefix() + "Player not found/online!");
            }
        } else {
            sender.sendMessage(CommonTool.getPrefix() + "You need to be a player to do this!");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return new ArrayList<>();
    }
}
