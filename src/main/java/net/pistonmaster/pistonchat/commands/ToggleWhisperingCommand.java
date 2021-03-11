package net.pistonmaster.pistonchat.commands;

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
                player.sendMessage(LanguageTool.getMessage("pmson"));
            } else {
                player.sendMessage(LanguageTool.getMessage("pmsoff"));
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
