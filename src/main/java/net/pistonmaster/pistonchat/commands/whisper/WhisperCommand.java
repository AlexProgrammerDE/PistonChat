package net.pistonmaster.pistonchat.commands.whisper;

import net.pistonmaster.pistonchat.utils.CommonTool;
import net.pistonmaster.pistonchat.utils.IgnoreTool;
import net.pistonmaster.pistonchat.utils.LanguageTool;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WhisperCommand implements CommandExecutor, TabExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (args.length > 0) {
                Optional<Player> receiver = CommonTool.getPlayer(args[0]);

                if (receiver.isPresent()) {
                    if (IgnoreTool.isIgnored(player, receiver.get())) {
                        player.sendMessage(CommonTool.getPrefix() + "This person blocked you!");
                    } else if (IgnoreTool.isIgnored(receiver.get(), player)) {
                        player.sendMessage(CommonTool.getPrefix() + "You block this person!");
                    } else {
                        if (args.length > 1) {
                            CommonTool.sendWhisperTo(player, CommonTool.mergeArgs(args, 1), receiver.get());
                        } else {
                            return false;
                        }
                    }
                } else {
                    player.sendMessage(LanguageTool.getMessage("notonline"));
                }
            } else {
                return false;
            }
        } else {
            sender.sendMessage(LanguageTool.getMessage("playeronly"));
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
