package net.pistonmaster.pistonchat.commands.whisper;

import lombok.RequiredArgsConstructor;
import net.pistonmaster.pistonchat.PistonChat;
import net.pistonmaster.pistonchat.utils.CommonTool;
import net.pistonmaster.pistonchat.utils.LanguageTool;
import net.pistonmaster.pistonchat.utils.UniqueSender;
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
                if (plugin.getIgnoreTool().isIgnored(new UniqueSender(sender), new UniqueSender(receiver.get()))) {
                    if (plugin.getConfig().getBoolean("onlyhidepms")) {
                        CommonTool.sendSender(new UniqueSender(sender), CommonTool.mergeArgs(args, 0), new UniqueSender(receiver.get()));
                    } else {
                        sender.sendMessage(CommonTool.getPrefix() + "This person ignores you!");
                    }
                } else if (!plugin.getConfig().getBoolean("allowpmignored") && plugin.getIgnoreTool().isIgnored(new UniqueSender(receiver.get()), new UniqueSender(sender))) {
                    sender.sendMessage(CommonTool.getPrefix() + "You ignore this person!");
                } else {
                    if (args.length > 1) {
                        CommonTool.sendWhisperTo(new UniqueSender(sender), CommonTool.mergeArgs(args, 1), new UniqueSender(receiver.get()));
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
