package net.pistonmaster.pistonchat.commands.toggle;

import lombok.RequiredArgsConstructor;
import net.pistonmaster.pistonchat.PistonChat;
import net.pistonmaster.pistonchat.utils.LanguageTool;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class ToggleChatCommand implements CommandExecutor, TabExecutor {
    private final PistonChat plugin;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            plugin.getTempDataTool().setChatEnabled(player, !plugin.getTempDataTool().isChatEnabled(player));

            if (plugin.getTempDataTool().isChatEnabled(player)) {
                player.sendMessage(LanguageTool.getMessage("chaton"));
            } else {
                player.sendMessage(LanguageTool.getMessage("chatoff"));
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
