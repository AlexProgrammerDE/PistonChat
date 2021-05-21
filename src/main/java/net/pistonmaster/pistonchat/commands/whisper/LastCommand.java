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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class LastCommand implements CommandExecutor, TabExecutor {
    private final PistonChat plugin;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Optional<CommandSender> lastSentTo = plugin.getCacheTool().getLastSentTo(new UniqueSender(sender));
        Optional<CommandSender> lastMessagedOf = plugin.getCacheTool().getLastMessagedOf(new UniqueSender(sender));

        if (lastSentTo.isPresent()) {
            if (plugin.getIgnoreTool().isIgnored(new UniqueSender(sender), new UniqueSender(lastSentTo.get()))) {
                if (plugin.getConfig().getBoolean("onlyhidepms")) {
                    CommonTool.sendSender(new UniqueSender(sender), CommonTool.mergeArgs(args, 0), new UniqueSender(lastSentTo.get()));
                } else {
                    sender.sendMessage(CommonTool.getPrefix() + "This person ignores you!");
                }
            } else if (!plugin.getConfig().getBoolean("allowpmignored") && plugin.getIgnoreTool().isIgnored(new UniqueSender(lastSentTo.get()), new UniqueSender(sender))) {
                sender.sendMessage(CommonTool.getPrefix() + "You ignore this person!");
            } else {
                if (args.length > 0) {
                    CommonTool.sendWhisperTo(new UniqueSender(sender), CommonTool.mergeArgs(args, 0), new UniqueSender(lastSentTo.get()));
                } else {
                    return false;
                }
            }
        } else if (lastMessagedOf.isPresent()) {
            if (plugin.getIgnoreTool().isIgnored(new UniqueSender(sender), new UniqueSender(lastMessagedOf.get()))) {
                if (plugin.getConfig().getBoolean("onlyhidepms")) {
                    CommonTool.sendSender(new UniqueSender(sender), CommonTool.mergeArgs(args, 0), new UniqueSender(lastMessagedOf.get()));
                } else {
                    sender.sendMessage(CommonTool.getPrefix() + "This person ignores you!");
                }
            } else if (!plugin.getConfig().getBoolean("allowpmignored") && plugin.getIgnoreTool().isIgnored(new UniqueSender(lastMessagedOf.get()), new UniqueSender(sender))) {
                sender.sendMessage(CommonTool.getPrefix() + "You ignore this person!");
            } else {
                if (args.length > 0) {
                    CommonTool.sendWhisperTo(new UniqueSender(sender), CommonTool.mergeArgs(args, 0), new UniqueSender(lastMessagedOf.get()));
                } else {
                    return false;
                }
            }
        } else {
            sender.sendMessage(LanguageTool.getMessage("notonline"));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return new ArrayList<>();
    }
}
