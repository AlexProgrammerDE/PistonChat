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

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
public final class WarningsCommand implements CommandExecutor, TabExecutor {
  private final PistonMute plugin;

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (!plugin.getPluginConfig().warnings.enabled) {
      sender.sendMessage(ChatColor.RED + "Warning system is disabled.");
      return true;
    }

    if (args.length < 1) {
      sender.sendMessage(ChatColor.RED + "Usage: /warnings <player>");
      return true;
    }

    // Try to find the player
    OfflinePlayer target = Bukkit.getPlayer(args[0]);
    if (target == null) {
      // Try offline player
      target = Bukkit.getOfflinePlayer(args[0]);
      if (!target.hasPlayedBefore() && !target.isOnline()) {
        sender.sendMessage(ChatColor.RED + "Player not found.");
        return true;
      }
    }

    List<Warning> warnings = WarningStorage.getWarnings(target.getUniqueId());
    List<Warning> activeWarnings = WarningStorage.getActiveWarnings(target.getUniqueId());

    String playerName = target.getName() != null ? target.getName() : args[0];

    sender.spigot().sendMessage(new ComponentBuilder("----------------").color(ChatColor.DARK_BLUE).create());
    sender.spigot().sendMessage(new ComponentBuilder("Warnings for " + playerName).color(ChatColor.GOLD).create());
    sender.spigot().sendMessage(new ComponentBuilder("Active: " + activeWarnings.size() + " | Total: " + warnings.size()).color(ChatColor.YELLOW).create());
    sender.spigot().sendMessage(new ComponentBuilder("----------------").color(ChatColor.DARK_BLUE).create());

    if (warnings.isEmpty()) {
      sender.spigot().sendMessage(new ComponentBuilder("No warnings found.").color(ChatColor.GREEN).create());
    } else {
      for (Warning warning : warnings) {
        String timeAgo = formatTimeAgo(warning.getIssuedAt());
        String status = warning.isActive() ? ChatColor.GREEN + "[Active]" : ChatColor.GRAY + "[Expired]";

        String format = plugin.getPluginConfig().warnings.messages.listFormat
            .replace("%id%", warning.getId())
            .replace("%reason%", warning.getReason())
            .replace("%issuer%", warning.getIssuerName())
            .replace("%time_ago%", timeAgo)
            .replace("&", "\u00A7");

        sender.sendMessage(status + " " + format);

        // Show expiry info for active warnings
        if (warning.isActive() && warning.getExpiresAt() != null) {
          String expiresIn = formatTimeRemaining(warning.getExpiresAt());
          String expiryNote = plugin.getPluginConfig().warnings.messages.expiryNote
              .replace("%time%", expiresIn)
              .replace("&", "\u00A7");
          sender.sendMessage("  " + expiryNote);
        }
      }
    }

    sender.spigot().sendMessage(new ComponentBuilder("----------------").color(ChatColor.DARK_BLUE).create());

    return true;
  }

  private String formatTimeAgo(Instant instant) {
    if (instant == null) {
      return "unknown";
    }

    Duration duration = Duration.between(instant, Instant.now());
    long seconds = duration.getSeconds();

    if (seconds < 60) {
      return seconds + "s ago";
    } else if (seconds < 3600) {
      return (seconds / 60) + "m ago";
    } else if (seconds < 86400) {
      return (seconds / 3600) + "h ago";
    } else {
      return (seconds / 86400) + "d ago";
    }
  }

  private String formatTimeRemaining(Instant expiresAt) {
    if (expiresAt == null) {
      return "never";
    }

    Duration duration = Duration.between(Instant.now(), expiresAt);
    if (duration.isNegative()) {
      return "expired";
    }

    long seconds = duration.getSeconds();

    if (seconds < 60) {
      return seconds + " seconds";
    } else if (seconds < 3600) {
      return (seconds / 60) + " minutes";
    } else if (seconds < 86400) {
      return (seconds / 3600) + " hours";
    } else {
      return (seconds / 86400) + " days";
    }
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
    }

    return new ArrayList<>();
  }
}
