package net.pistonmaster.pistonmute.commands;

import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.pistonmaster.pistonmute.PistonMute;
import net.pistonmaster.pistonmute.utils.StorageTool;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
public final class UnMuteCommand implements CommandExecutor, TabExecutor {
    private final PistonMute plugin;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0) {
            Player player = plugin.getServer().getPlayer(args[0]);

            if (player != null) {
                if (player != sender) {
                    if (StorageTool.unMutePlayer(player)) {
                        sender.spigot().sendMessage(new ComponentBuilder("----------------").color(ChatColor.DARK_BLUE).create());
                        sender.spigot().sendMessage(new ComponentBuilder("PistonMute").color(ChatColor.GOLD).create());
                        sender.spigot().sendMessage(new ComponentBuilder("Successfully unmuted " + player.getName() + "!").color(ChatColor.GREEN).create());
                        sender.spigot().sendMessage(new ComponentBuilder("----------------").color(ChatColor.DARK_BLUE).create());
                    } else {
                        sender.spigot().sendMessage(new ComponentBuilder("----------------").color(ChatColor.DARK_BLUE).create());
                        sender.spigot().sendMessage(new ComponentBuilder("PistonMute").color(ChatColor.GOLD).create());
                        sender.spigot().sendMessage(new ComponentBuilder(player.getName() + " wasn't muted!").color(ChatColor.RED).create());
                        sender.spigot().sendMessage(new ComponentBuilder("----------------").color(ChatColor.DARK_BLUE).create());
                    }
                } else {
                    sender.sendMessage("Please don't mute yourself!");
                }
            }
        } else {
            return false;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> players = new ArrayList<>();

            for (Player player : Bukkit.getOnlinePlayers()) {
                players.add(player.getName());
            }

            List<String> completions = new ArrayList<>();

            StringUtil.copyPartialMatches(args[0], players, completions);

            Collections.sort(completions);

            return completions;
        } else {
            return new ArrayList<>();
        }
    }
}
