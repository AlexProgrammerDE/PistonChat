package net.pistonmaster.pistonchat.commands.toggle;

import lombok.RequiredArgsConstructor;
import net.pistonmaster.pistonchat.PistonChat;
import net.pistonmaster.pistonchat.tools.CommonTool;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
public class ToggleWhisperingCommand implements CommandExecutor, TabExecutor {
    private final PistonChat plugin;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player player) {
            plugin.getTempDataTool().setWhisperingEnabled(player, !plugin.getTempDataTool().isWhisperingEnabled(player));

            if (plugin.getTempDataTool().isWhisperingEnabled(player)) {
                plugin.getCommonTool().sendLanguageMessage(plugin.getAdventure(), player, "pmson");
            } else {
                plugin.getCommonTool().sendLanguageMessage(plugin.getAdventure(), player, "pmsoff");
            }
        } else {
            plugin.getCommonTool().sendLanguageMessage(plugin.getAdventure(), sender, "playeronly");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }
}
