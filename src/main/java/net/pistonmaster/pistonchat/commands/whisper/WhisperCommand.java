package net.pistonmaster.pistonchat.commands.whisper;

import net.pistonmaster.pistonchat.utils.*;
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
        if (args.length > 0) {
            Optional<Player> receiver = CommonTool.getPlayer(args[0]);

            if (receiver.isPresent()) {
                if (IgnoreTool.isIgnored(new UniqueSender(sender), new UniqueSender(receiver.get()))) {
                    if (ConfigTool.getConfig().getBoolean("onlyhidepms")) {
                        CommonTool.sendSender(new UniqueSender(sender), CommonTool.mergeArgs(args, 0), new UniqueSender(receiver.get()));
                    } else {
                        sender.sendMessage(CommonTool.getPrefix() + "This person ignores you!");
                    }
                } else if (!ConfigTool.getConfig().getBoolean("allowpmignored") && IgnoreTool.isIgnored(new UniqueSender(receiver.get()), new UniqueSender(sender))) {
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
