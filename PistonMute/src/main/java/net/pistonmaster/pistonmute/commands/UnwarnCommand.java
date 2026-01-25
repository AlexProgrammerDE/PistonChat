package net.pistonmaster.pistonmute.commands;

import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.pistonmaster.pistonmute.PistonMute;
import net.pistonmaster.pistonmute.data.Warning;
import net.pistonmaster.pistonmute.utils.WarningStorage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public final class UnwarnCommand implements CommandExecutor, TabExecutor {
  private final PistonMute plugin;

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (!plugin.getPluginConfig().warnings.enabled) {
      sender.sendMessage(ChatColor.RED + "Warning system is disabled.");
      return true;
    }

    if (args.length < 1) {
      sender.sendMessage(ChatColor.RED + "Usage: /unwarn <player> [warning_id]");
      sender.sendMessage(ChatColor.GRAY + "If no warning ID is specified, all warnings will be removed.");
      return true;
    }

    // Try to find the player
    OfflinePlayer target = Bukkit.getPlayer(args[0]);
    if (target == null) {
      target = Bukkit.getOfflinePlayer(args[0]);
      if (!target.hasPlayedBefore() && !target.isOnline()) {
        sender.sendMessage(ChatColor.RED + "Player not found.");
        return true;
      }
    }

    String playerName = target.getName() != null ? target.getName() : args[0];

    if (args.length >= 2) {
      // Remove specific warning
      String warningId = args[1];
      if (WarningStorage.removeWarning(target.getUniqueId(), warningId)) {
        sender.spigot().sendMessage(new ComponentBuilder("----------------").color(ChatColor.DARK_BLUE).create());
        sender.spigot().sendMessage(new ComponentBuilder("PistonMute").color(ChatColor.GOLD).create());
        sender.spigot().sendMessage(new ComponentBuilder("Removed warning " + warningId + " from " + playerName + "!").color(ChatColor.GREEN).create());
        sender.spigot().sendMessage(new ComponentBuilder("----------------").color(ChatColor.DARK_BLUE).create());
      } else {
        sender.sendMessage(ChatColor.RED + "Warning with ID '" + warningId + "' not found for " + playerName + ".");
      }
    } else {
      // Remove all warnings
      int count = WarningStorage.removeAllWarnings(target.getUniqueId());
      if (count > 0) {
        sender.spigot().sendMessage(new ComponentBuilder("----------------").color(ChatColor.DARK_BLUE).create());
        sender.spigot().sendMessage(new ComponentBuilder("PistonMute").color(ChatColor.GOLD).create());
        sender.spigot().sendMessage(new ComponentBuilder("Removed all " + count + " warnings from " + playerName + "!").color(ChatColor.GREEN).create());
        sender.spigot().sendMessage(new ComponentBuilder("----------------").color(ChatColor.DARK_BLUE).create());
      } else {
        sender.sendMessage(ChatColor.RED + playerName + " has no warnings to remove.");
      }
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
    } else if (args.length == 2) {
      // Try to get warning IDs for the player
      OfflinePlayer target = Bukkit.getPlayer(args[0]);
      if (target == null) {
        target = Bukkit.getOfflinePlayer(args[0]);
      }

      if (target != null) {
        List<Warning> warnings = WarningStorage.getWarnings(target.getUniqueId());
        List<String> warningIds = warnings.stream()
            .map(Warning::getId)
            .collect(Collectors.toList());

        List<String> completions = new ArrayList<>();
        StringUtil.copyPartialMatches(args[1], warningIds, completions);
        Collections.sort(completions);
        return completions;
      }
    }

    return new ArrayList<>();
  }
}
