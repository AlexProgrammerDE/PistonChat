package net.pistonmaster.pistonmute.commands;

import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import net.pistonmaster.pistonmute.PistonMute;
import net.pistonmaster.pistonmute.data.PunishmentRecord;
import net.pistonmaster.pistonmute.utils.PunishmentHistoryStorage;
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

@RequiredArgsConstructor
public final class HistoryCommand implements CommandExecutor, TabExecutor {
  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
      .withZone(ZoneId.systemDefault());

  private final PistonMute plugin;

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (args.length < 1) {
      sender.sendMessage(ChatColor.RED + "Usage: /history <player> [page]");
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

    int page = 1;
    if (args.length >= 2) {
      try {
        page = Integer.parseInt(args[1]);
        if (page < 1) {
          page = 1;
        }
      } catch (NumberFormatException e) {
        sender.sendMessage(ChatColor.RED + "Invalid page number.");
        return true;
      }
    }

    int pageSize = plugin.getPluginConfig().historyPageSize;
    int totalPages = PunishmentHistoryStorage.getTotalPages(target.getUniqueId(), pageSize);
    int totalRecords = PunishmentHistoryStorage.getTotalRecords(target.getUniqueId());

    if (totalPages == 0) {
      totalPages = 1;
    }

    if (page > totalPages) {
      page = totalPages;
    }

    String playerName = target.getName() != null ? target.getName() : args[0];

    List<PunishmentRecord> records = PunishmentHistoryStorage.getHistoryPaginated(target.getUniqueId(), page, pageSize);

    // Send header
    String header = plugin.getPluginConfig().historyHeader
        .replace("%player%", playerName)
        .replace("%page%", String.valueOf(page))
        .replace("%total%", String.valueOf(totalPages))
        .replace("&", "\u00A7");
    sender.sendMessage(header);

    if (records.isEmpty()) {
      sender.sendMessage(ChatColor.GREEN + "No punishment history found.");
    } else {
      sender.sendMessage(ChatColor.GRAY + "Total records: " + totalRecords);
      sender.sendMessage("");

      for (PunishmentRecord record : records) {
        String reason = record.getReason() != null ? record.getReason() : "No reason specified";
        String duration = formatDuration(record);
        String issuer = record.getIssuerName() != null ? record.getIssuerName() : "Console";
        String date = record.getIssuedAt() != null ? DATE_FORMATTER.format(record.getIssuedAt()) : "Unknown";
        String type = record.getType().getDisplayName();

        String entry = plugin.getPluginConfig().historyEntryFormat
            .replace("%type%", type)
            .replace("%reason%", reason)
            .replace("%duration%", duration)
            .replace("%issuer%", issuer)
            .replace("%date%", date)
            .replace("&", "\u00A7");
        sender.sendMessage(entry);

        // Show template if used
        if (record.getTemplate() != null) {
          sender.sendMessage(ChatColor.GRAY + "  Template: " + ChatColor.AQUA + record.getTemplate());
        }
      }
    }

    // Send footer
    if (totalPages > 1) {
      String footer = plugin.getPluginConfig().historyFooter
          .replace("%player%", playerName)
          .replace("&", "\u00A7");
      sender.sendMessage(footer);
    }

    return true;
  }

  private String formatDuration(PunishmentRecord record) {
    if (record.isPermanent()) {
      return ChatColor.RED + "Permanent";
    }

    if (record.getExpiresAt() == null || record.getIssuedAt() == null) {
      return "Unknown";
    }

    Duration duration = Duration.between(record.getIssuedAt(), record.getExpiresAt());
    long seconds = duration.getSeconds();

    if (seconds < 60) {
      return seconds + "s";
    } else if (seconds < 3600) {
      return (seconds / 60) + "m";
    } else if (seconds < 86400) {
      return (seconds / 3600) + "h";
    } else if (seconds < 604800) {
      return (seconds / 86400) + "d";
    } else {
      return (seconds / 604800) + "w";
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
