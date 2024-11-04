package net.pistonmaster.pistonchat.commands.toggle;

import lombok.RequiredArgsConstructor;
import net.pistonmaster.pistonchat.PistonChat;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
public class ToggleChatCommand implements CommandExecutor, TabExecutor {
    private final PistonChat plugin;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getCommonTool().sendLanguageMessage(sender, "playeronly");
            return true;
        }

        boolean chatNowEnabled = !plugin.getTempDataTool().isChatEnabled(player);
        plugin.getTempDataTool().setChatEnabled(player, chatNowEnabled);

        if (chatNowEnabled) {
            plugin.getCommonTool().sendLanguageMessage(player, "chaton");
        } else {
            plugin.getCommonTool().sendLanguageMessage(player, "chatoff");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }
}
