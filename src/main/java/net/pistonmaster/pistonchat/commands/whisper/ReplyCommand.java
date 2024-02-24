package net.pistonmaster.pistonchat.commands.whisper;

import lombok.RequiredArgsConstructor;
import net.pistonmaster.pistonchat.PistonChat;
import net.pistonmaster.pistonchat.tools.CommonTool;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class ReplyCommand extends MessageCommandHelper implements CommandExecutor, TabExecutor {
    private final PistonChat plugin;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            return false;
        }

        Optional<CommandSender> lastMessagedOf = plugin.getCacheTool().getLastMessagedOf(sender);

        if (lastMessagedOf.isEmpty()) {
            plugin.getCommonTool().sendLanguageMessage(sender, "notonline");
            return true;
        }

        MessageCommandHelper.sendWhisper(plugin, sender, lastMessagedOf.get(), CommonTool.mergeArgs(args, 0));

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }
}
