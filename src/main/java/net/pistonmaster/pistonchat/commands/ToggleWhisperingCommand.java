package net.pistonmaster.pistonchat.commands;

import net.pistonmaster.pistonchat.utils.CommonTool;
import net.pistonmaster.pistonchat.utils.LanguageTool;
import net.pistonmaster.pistonchat.utils.TempDataTool;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ToggleWhisperingCommand implements CommandExecutor, TabExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            TempDataTool.setWhisperingEnabled(player, !TempDataTool.isWhisperingEnabled(player));

            if (TempDataTool.isWhisperingEnabled(player)) {
                player.sendMessage(CommonTool.getPrefix() + "Enabled whispering!");
            } else {
                player.sendMessage(CommonTool.getPrefix() + "Disabled whispering! (Will be reset on rejoin!)");
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
