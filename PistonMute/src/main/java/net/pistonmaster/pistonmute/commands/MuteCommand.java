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

import java.util.*;

@RequiredArgsConstructor
public final class MuteCommand implements CommandExecutor, TabExecutor {
    private final PistonMute plugin;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0) {
            Player player = plugin.getServer().getPlayer(args[0]);

            if (player != null) {
                if (player != sender) {
                    if (args.length > 1) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(new Date());

                        if (args[1].toLowerCase().endsWith("y")) {
                            int d = Integer.parseInt(args[1].toLowerCase().replace("y", ""));

                            calendar.add(Calendar.YEAR, d);
                        } else if (args[1].toLowerCase().endsWith("d")) {
                            int d = Integer.parseInt(args[1].toLowerCase().replace("d", ""));

                            calendar.add(Calendar.DAY_OF_WEEK, d);
                        } else if (args[1].toLowerCase().endsWith("h")) {
                            int h = Integer.parseInt(args[1].toLowerCase().replace("h", ""));

                            calendar.add(Calendar.HOUR_OF_DAY, h);
                        } else if (args[1].toLowerCase().endsWith("m")) {
                            int m = Integer.parseInt(args[1].toLowerCase().replace("m", ""));

                            calendar.add(Calendar.MINUTE, m);
                        } else if (args[1].toLowerCase().endsWith("s")) {
                            int s = Integer.parseInt(args[1].toLowerCase().replace("s", ""));

                            calendar.add(Calendar.SECOND, s);
                        } else {
                            return false;
                        }

                        if (StorageTool.tempMutePlayer(player, calendar.getTime())) {
                            successMessage(sender, player);
                        } else {
                            alreadyMutedMessage(sender, player);
                        }
                    } else {
                        if (StorageTool.hardMutePlayer(player)) {
                            successMessage(sender, player);
                        } else {
                            alreadyMutedMessage(sender, player);
                        }
                    }
                } else {
                    sender.sendMessage("You can't mute yourself!");
                }
            } else {
                return false;
            }
        } else {
            return false;
        }

        return true;
    }

    private void alreadyMutedMessage(CommandSender sender, Player player) {
        sender.spigot().sendMessage(new ComponentBuilder("----------------").color(ChatColor.DARK_BLUE).create());
        sender.spigot().sendMessage(new ComponentBuilder("PistonMute").color(ChatColor.GOLD).create());
        sender.spigot().sendMessage(new ComponentBuilder(player.getName() + " is already muted!").color(ChatColor.RED).create());
        sender.spigot().sendMessage(new ComponentBuilder("----------------").color(ChatColor.DARK_BLUE).create());
    }

    private void successMessage(CommandSender sender, Player player) {
        sender.spigot().sendMessage(new ComponentBuilder("----------------").color(ChatColor.DARK_BLUE).create());
        sender.spigot().sendMessage(new ComponentBuilder("PistonMute").color(ChatColor.GOLD).create());
        sender.spigot().sendMessage(new ComponentBuilder("Successfully muted " + player.getName() + "!").color(ChatColor.GREEN).create());
        sender.spigot().sendMessage(new ComponentBuilder("----------------").color(ChatColor.DARK_BLUE).create());
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
