package net.pistonmaster.pistonchat.commands.whisper;

import lombok.RequiredArgsConstructor;
import net.pistonmaster.pistonchat.PistonChat;
import net.pistonmaster.pistonchat.utils.CommonTool;
import net.pistonmaster.pistonchat.utils.LanguageTool;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class WhisperCommand implements CommandExecutor, TabExecutor {
    private final PistonChat plugin;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0) {
            Optional<Player> receiver = CommonTool.getPlayer(args[0]);

            if (receiver.isPresent()) {
                if (plugin.getIgnoreTool().isIgnored(sender, receiver.get())) {
                    if (plugin.getConfig().getBoolean("onlyhidepms")) {
                        CommonTool.sendSender(sender, CommonTool.mergeArgs(args, 0), receiver.get());
                    } else {
                        sender.sendMessage(CommonTool.getPrefix() + "This person ignores you!");
                    }
                } else if (!plugin.getConfig().getBoolean("allowpmignored") && plugin.getIgnoreTool().isIgnored(receiver.get(), sender)) {
                    sender.sendMessage(CommonTool.getPrefix() + "You ignore this person!");
                } else {
                    if (args.length > 1) {
                        CommonTool.sendWhisperTo(sender, CommonTool.mergeArgs(args, 1), receiver.get());
                    } else {
                        return false;
                    }
                }
            } else {
                sender.sendMessage(LanguageTool.getMessage("notonline"));
            }
        } else {
            return false;
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
