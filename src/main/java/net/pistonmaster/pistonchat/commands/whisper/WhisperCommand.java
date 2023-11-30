package net.pistonmaster.pistonchat.commands.whisper;

import lombok.RequiredArgsConstructor;
import net.pistonmaster.pistonchat.PistonChat;
import net.pistonmaster.pistonchat.tools.CommonTool;
import net.pistonmaster.pistonchat.utils.PlatformUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class WhisperCommand implements CommandExecutor, TabExecutor {
    private final PistonChat plugin;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0) {
            Optional<Player> receiver = PlatformUtils.getPlayer(args[0]);

            if (receiver.isPresent()) {
                if (plugin.getIgnoreTool().isIgnored(sender, receiver.get())) {
                    if (plugin.getConfig().getBoolean("only-hide-pms")) {
                        plugin.getCommonTool().sendSender(sender, CommonTool.mergeArgs(args, 0), receiver.get());
                    } else {
                        plugin.getCommonTool().sendLanguageMessage(sender, "source-ignored");
                    }
                } else if (!plugin.getConfig().getBoolean("allow-pm-ignored") && plugin.getIgnoreTool().isIgnored(receiver.get(), sender)) {
                    plugin.getCommonTool().sendLanguageMessage(sender, "target-ignored");
                } else {
                    if (args.length > 1) {
                        plugin.getCommonTool().sendWhisperTo(sender, CommonTool.mergeArgs(args, 1), receiver.get());
                    } else {
                        return false;
                    }
                }
            } else {
                plugin.getCommonTool().sendLanguageMessage(sender, "notonline");
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
            return Collections.emptyList();
        }
    }
}
