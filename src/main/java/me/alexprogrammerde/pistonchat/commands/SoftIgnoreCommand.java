package me.alexprogrammerde.pistonchat.commands;

import me.alexprogrammerde.pistonchat.utils.CommonTool;
import me.alexprogrammerde.pistonchat.utils.ConfigTool;
import me.alexprogrammerde.pistonchat.utils.SoftIgnoreTool;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SoftIgnoreCommand implements CommandExecutor, TabExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (args.length > 0) {
                Optional<Player> ignored = CommonTool.getPlayer(args[0]);

                if (ignored.isPresent()) {
                    SoftIgnoreTool.IgnoreType type = SoftIgnoreTool.softIgnorePlayer(player, ignored.get());

                    if (type == SoftIgnoreTool.IgnoreType.IGNORE) {
                        player.sendMessage(ConfigTool.getPreparedString("ignore"));
                    } else if (type == SoftIgnoreTool.IgnoreType.UNIGNORE) {
                        player.sendMessage(ConfigTool.getPreparedString("unignore"));
                    }
                } else {
                    player.sendMessage(CommonTool.getPrefix() + "This player doesn't exist!");
                }
            } else {
                return false;
            }
        } else {
            sender.sendMessage(CommonTool.getPrefix() + "You need to be a player to do that!");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return null;
        } else {
            return new ArrayList<>();
        }
    }
}
