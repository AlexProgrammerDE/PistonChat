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

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public final class MuteListCommand implements CommandExecutor, TabExecutor {
  private static final int PAGE_SIZE = 10;

  private final PistonMute plugin;

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    int page = 1;
    if (args.length >= 1) {
      try {
        page = Integer.parseInt(args[0]);
        if (page < 1) {
          page = 1;
        }
      } catch (NumberFormatException e) {
        sender.sendMessage(ChatColor.RED + "Invalid page number.");
        return true;
      }
    }

    List<UUID> mutedPlayers = StorageTool.getMutedPlayers();
    int totalPages = (int) Math.ceil((double) mutedPlayers.size() / PAGE_SIZE);
    if (totalPages == 0) {
      totalPages = 1;
    }

    if (page > totalPages) {
      page = totalPages;
    }

    int startIndex = (page - 1) * PAGE_SIZE;
    int endIndex = Math.min(startIndex + PAGE_SIZE, mutedPlayers.size());

    // Send header
    String header = plugin.getPluginConfig().muteList.header
        .replace("%page%", String.valueOf(page))
        .replace("%total%", String.valueOf(totalPages))
        .replace("&", "\u00A7");
    sender.sendMessage(header);

    if (mutedPlayers.isEmpty()) {
      sender.sendMessage(ChatColor.GREEN + "No players are currently muted.");
    } else {
      sender.sendMessage(ChatColor.GRAY + "Total muted: " + mutedPlayers.size());
      sender.sendMessage("");

      for (int i = startIndex; i < endIndex; i++) {
        UUID uuid = mutedPlayers.get(i);
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        String playerName = player.getName() != null ? player.getName() : uuid.toString().substring(0, 8);

        Optional<MuteRecord> recordOpt = StorageTool.getMuteRecord(uuid);
        String duration;
        String reason;

        if (recordOpt.isPresent()) {
          MuteRecord record = recordOpt.get();
          duration = formatDuration(record);
          reason = record.getReason() != null ? record.getReason() : "No reason";
        } else {
          // Legacy mute without details
          if (StorageTool.isPermanentlyMuted(uuid)) {
            duration = ChatColor.RED + "Permanent";
          } else {
            Optional<Instant> expiresOpt = StorageTool.getMuteExpiration(uuid);
            duration = expiresOpt.map(this::formatTimeRemaining).orElse("Unknown");
          }
          reason = StorageTool.getMuteReason(uuid);
          if (reason == null) {
            reason = "No reason";
          }
        }

        String entry = plugin.getPluginConfig().muteList.entryFormat
            .replace("%player%", playerName)
            .replace("%duration%", duration)
            .replace("%reason%", reason)
            .replace("&", "\u00A7");
        sender.sendMessage(entry);
      }
    }

    // Send footer
    if (totalPages > 1) {
      String footer = plugin.getPluginConfig().muteList.footer
          .replace("&", "\u00A7");
      sender.sendMessage(footer);
    }

    return true;
  }

  private String formatDuration(MuteRecord record) {
    if (record.isPermanent()) {
      return ChatColor.RED + "Permanent";
    }

    if (record.getExpiresAt() == null) {
      return "Unknown";
    }

    return formatTimeRemaining(record.getExpiresAt());
  }

  private String formatTimeRemaining(Instant expiresAt) {
    Duration duration = Duration.between(Instant.now(), expiresAt);
    if (duration.isNegative()) {
      return "Expired";
    }

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
    return new ArrayList<>();
  }
}
