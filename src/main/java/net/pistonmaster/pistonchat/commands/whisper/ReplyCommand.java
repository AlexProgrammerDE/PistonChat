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

public class ReplyCommand implements CommandExecutor, TabExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            Optional<Player> lastMessagedOf = CacheTool.getLastMessagedOf(player);

            if (lastMessagedOf.isPresent()) {
                if (IgnoreTool.isIgnored(player, lastMessagedOf.get())) {
                    if (ConfigTool.getConfig().getBoolean("onlyhidepms")) {
                        CommonTool.sendSender(player, CommonTool.mergeArgs(args, 0), lastMessagedOf.get());
                    } else {
                        player.sendMessage(CommonTool.getPrefix() + "This person ignores you!");
                    }
                } else if (!ConfigTool.getConfig().getBoolean("allowpmignored") && IgnoreTool.isIgnored(lastMessagedOf.get(), player)) {
                    player.sendMessage(CommonTool.getPrefix() + "You ignore this person!");
                } else {
                    if (args.length > 0) {
                        CommonTool.sendWhisperTo(player, CommonTool.mergeArgs(args, 0), lastMessagedOf.get());
                    } else {
                        return false;
                    }
                }
            } else {
                player.sendMessage(LanguageTool.getMessage("notonline"));
            }
        } else {
            sender.sendMessage(LanguageTool.getMessage("playeronly"));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return new ArrayList<>();
    }
}
