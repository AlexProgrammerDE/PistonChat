package net.pistonmaster.pistonmute.commands;

import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import net.pistonmaster.pistonmute.PistonMute;
import net.pistonmaster.pistonmute.data.MuteRecord;
import net.pistonmaster.pistonmute.utils.StorageTool;
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
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public final class MuteInfoCommand implements CommandExecutor, TabExecutor {
  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
      .withZone(ZoneId.systemDefault());

  private final PistonMute plugin;

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (args.length < 1) {
      sender.sendMessage(ChatColor.RED + "Usage: /muteinfo <player>");
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

    if (!StorageTool.isMuted(target.getUniqueId())) {
      sender.sendMessage(ChatColor.RED + playerName + " is not currently muted.");
      return true;
    }

    Optional<MuteRecord> recordOpt = StorageTool.getMuteRecord(target.getUniqueId());

    // Send header
    String header = plugin.getPluginConfig().muteInfo.header
        .replace("%player%", playerName)
        .replace("&", "\u00A7");
    sender.sendMessage(header);

    if (recordOpt.isPresent()) {
      MuteRecord record = recordOpt.get();

      // Reason
      String reason = record.getReason() != null ? record.getReason() : "No reason specified";
      String reasonMsg = plugin.getPluginConfig().muteInfo.reason
          .replace("%reason%", reason)
          .replace("&", "\u00A7");
      sender.sendMessage(reasonMsg);

      // Issuer
      String issuer = record.getIssuerName() != null ? record.getIssuerName() : "Console";
      String issuerMsg = plugin.getPluginConfig().muteInfo.issuer
          .replace("%issuer%", issuer)
          .replace("&", "\u00A7");
      sender.sendMessage(issuerMsg);

      // Duration / Permanent
      if (record.isPermanent()) {
        String permanentMsg = plugin.getPluginConfig().muteInfo.permanent
            .replace("&", "\u00A7");
        sender.sendMessage(permanentMsg);
      } else if (record.getExpiresAt() != null) {
        String duration = formatDuration(record.getIssuedAt(), record.getExpiresAt());
        String durationMsg = plugin.getPluginConfig().muteInfo.duration
            .replace("%duration%", duration)
            .replace("&", "\u00A7");
        sender.sendMessage(durationMsg);

        String expires = formatExpires(record.getExpiresAt());
        String expiresMsg = plugin.getPluginConfig().muteInfo.expires
            .replace("%expires%", expires)
            .replace("&", "\u00A7");
        sender.sendMessage(expiresMsg);
      }

      // Template
      if (record.getTemplate() != null) {
        String templateMsg = plugin.getPluginConfig().muteInfo.template
            .replace("%template%", record.getTemplate())
            .replace("&", "\u00A7");
        sender.sendMessage(templateMsg);
      }

      // Issued at
      if (record.getIssuedAt() != null) {
        sender.sendMessage(ChatColor.GRAY + "Issued: " + ChatColor.YELLOW + DATE_FORMATTER.format(record.getIssuedAt()));
      }

    } else {
      // Legacy mute - limited info
      String reason = StorageTool.getMuteReason(target.getUniqueId());
      if (reason != null) {
        String reasonMsg = plugin.getPluginConfig().muteInfo.reason
            .replace("%reason%", reason)
            .replace("&", "\u00A7");
        sender.sendMessage(reasonMsg);
      } else {
        sender.sendMessage(ChatColor.GRAY + "Reason: " + ChatColor.YELLOW + "No reason specified");
      }

      if (StorageTool.isPermanentlyMuted(target.getUniqueId())) {
        String permanentMsg = plugin.getPluginConfig().muteInfo.permanent
            .replace("&", "\u00A7");
        sender.sendMessage(permanentMsg);
      } else {
        Optional<Instant> expiresOpt = StorageTool.getMuteExpiration(target.getUniqueId());
        if (expiresOpt.isPresent()) {
          String expires = formatExpires(expiresOpt.get());
          String expiresMsg = plugin.getPluginConfig().muteInfo.expires
              .replace("%expires%", expires)
              .replace("&", "\u00A7");
          sender.sendMessage(expiresMsg);
        }
      }
    }

    return true;
  }

  private String formatDuration(Instant start, Instant end) {
    if (start == null || end == null) {
      return "Unknown";
    }

    Duration duration = Duration.between(start, end);
    long seconds = duration.getSeconds();

    if (seconds < 60) {
      return seconds + " seconds";
    } else if (seconds < 3600) {
      return (seconds / 60) + " minutes";
    } else if (seconds < 86400) {
      return (seconds / 3600) + " hours";
    } else if (seconds < 604800) {
      return (seconds / 86400) + " days";
    } else {
      return (seconds / 604800) + " weeks";
    }
  }

  private String formatExpires(Instant expiresAt) {
    Duration remaining = Duration.between(Instant.now(), expiresAt);
    if (remaining.isNegative()) {
      return "Expired";
    }

    long seconds = remaining.getSeconds();
    String timeRemaining;

    if (seconds < 60) {
      timeRemaining = seconds + "s";
    } else if (seconds < 3600) {
      timeRemaining = (seconds / 60) + "m";
    } else if (seconds < 86400) {
      timeRemaining = (seconds / 3600) + "h";
    } else {
      timeRemaining = (seconds / 86400) + "d";
    }

    return DATE_FORMATTER.format(expiresAt) + " (" + timeRemaining + " remaining)";
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
