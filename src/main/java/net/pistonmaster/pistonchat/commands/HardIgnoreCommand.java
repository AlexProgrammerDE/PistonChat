package net.pistonmaster.pistonchat.commands;

import net.pistonmaster.pistonchat.utils.CommonTool;
import net.pistonmaster.pistonchat.utils.ConfigTool;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HardIgnoreCommand implements CommandExecutor, TabExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (args.length > 0) {
                Optional<Player> ignored = CommonTool.getPlayer(args[0]);

                if (ignored.isPresent()) {
                    ConfigTool.HardReturn type = ConfigTool.hardIgnorePlayer(player, ignored.get());

                    if (type == ConfigTool.HardReturn.IGNORE) {
                        player.sendMessage(ConfigTool.getPreparedString("ignorehard", ignored.get()));
                    } else if (type == ConfigTool.HardReturn.UNIGNORE) {
                        player.sendMessage(ConfigTool.getPreparedString("unignorehard", ignored.get()));
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
