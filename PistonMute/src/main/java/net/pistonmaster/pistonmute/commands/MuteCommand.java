package net.pistonmaster.pistonmute.commands;

import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.pistonmaster.pistonmute.PistonMute;
import net.pistonmaster.pistonmute.config.PistonMuteConfig;
import net.pistonmaster.pistonmute.data.PunishmentType;
import net.pistonmaster.pistonmute.utils.MuteDateUtils;
import net.pistonmaster.pistonmute.utils.PunishmentHistoryStorage;
import net.pistonmaster.pistonmute.utils.PunishmentLogger;
import net.pistonmaster.pistonmute.utils.StaffUtils;
import net.pistonmaster.pistonmute.utils.StorageTool;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.time.Instant;
import java.util.*;

@RequiredArgsConstructor
public final class MuteCommand implements CommandExecutor, TabExecutor {
  private final PistonMute plugin;

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (args.length == 0) {
      return false;
    }

    // Parse for template option
    String template = null;
    List<String> argList = new ArrayList<>(Arrays.asList(args));

    for (int i = 0; i < argList.size(); i++) {
      if (argList.get(i).equalsIgnoreCase("--template") && i + 1 < argList.size()) {
        template = argList.get(i + 1);
        argList.remove(i + 1);
        argList.remove(i);
        break;
      } else if (argList.get(i).startsWith("--template=")) {
        template = argList.get(i).substring("--template=".length());
        argList.remove(i);
        break;
      }
    }

    args = argList.toArray(new String[0]);

    if (args.length == 0) {
      return false;
    }

    // Try to find online player first
    Player onlinePlayer = plugin.getServer().getPlayer(args[0]);

    if (onlinePlayer != null) {
      return handleOnlinePlayerMute(sender, onlinePlayer, args, template);
    } else {
      // Try to handle offline player
      return handleOfflinePlayerMute(sender, args, template);
    }
  }

  /**
   * Handle muting an online player.
   */
  private boolean handleOnlinePlayerMute(CommandSender sender, Player player, String[] args, String template) {
    if (player == sender) {
      sender.sendMessage("You can't mute yourself!");
      return true;
    }

    // Check staff hierarchy
    if (!StaffUtils.canTarget(sender, player)) {
      cannotTargetMessage(sender, player);
      return true;
    }

    // Get issuer info
    UUID issuerUuid = sender instanceof Player ? ((Player) sender).getUniqueId() : null;
    String issuerName = sender instanceof Player ? sender.getName() : "Console";

    // Check if using escalation template
    if (template != null) {
      return handleTemplateMute(sender, player.getUniqueId(), player.getName(), template, issuerUuid, issuerName, args);
    }

    // Parse duration and reason
    Instant muteUntil = null;
    String reason = null;
    int reasonStartIndex = 1;

    if (args.length > 1) {
      Optional<Instant> muteUntilOpt = MuteDateUtils.parseTimeSuffix(args[1]);
      if (muteUntilOpt.isPresent()) {
        muteUntil = muteUntilOpt.get();
        reasonStartIndex = 2;
      }
    }

    // Collect reason from remaining arguments
    if (args.length > reasonStartIndex) {
      reason = String.join(" ", Arrays.copyOfRange(args, reasonStartIndex, args.length));
    }

    // Mute the player
    boolean success;
    if (muteUntil != null) {
      success = StorageTool.tempMutePlayer(player.getUniqueId(), muteUntil, issuerUuid, issuerName, reason, null);
      if (success) {
        successMessage(sender, player.getName());
        PunishmentLogger.logMute(sender.getName(), player.getName(), args[1] + (reason != null ? " - " + reason : ""));

        // Record in history
        PunishmentHistoryStorage.recordPunishment(
            PunishmentType.MUTE,
            player.getUniqueId(),
            issuerUuid,
            issuerName,
            reason,
            muteUntil,
            false,
            null
        );
      } else {
        alreadyMutedMessage(sender, player.getName());
      }
    } else {
      success = StorageTool.hardMutePlayer(player.getUniqueId(), issuerUuid, issuerName, reason, null);
      if (success) {
        successMessage(sender, player.getName());
        PunishmentLogger.logMute(sender.getName(), player.getName(), "permanent" + (reason != null ? " - " + reason : ""));

        // Record in history
        PunishmentHistoryStorage.recordPunishment(
            PunishmentType.MUTE,
            player.getUniqueId(),
            issuerUuid,
            issuerName,
            reason,
            null,
            true,
            null
        );
      } else {
        alreadyMutedMessage(sender, player.getName());
      }
    }

    // Handle alt muting if enabled
    if (success) {
      handleAltMuting(sender, player, muteUntil, reason, issuerUuid, issuerName);
    }

    return true;
  }

  /**
   * Handle muting an offline player.
   */
  @SuppressWarnings("deprecation")
  private boolean handleOfflinePlayerMute(CommandSender sender, String[] args, String template) {
    // Get the offline player
    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[0]);

    // Check if the player has ever played on this server
    if (!offlinePlayer.hasPlayedBefore() && !offlinePlayer.isOnline()) {
      playerNotFoundMessage(sender, args[0]);
      return true;
    }

    // Check if trying to mute self
    if (sender instanceof Player && offlinePlayer.getUniqueId().equals(((Player) sender).getUniqueId())) {
      sender.sendMessage("You can't mute yourself!");
      return true;
    }

    String playerName = offlinePlayer.getName() != null ? offlinePlayer.getName() : args[0];

    // Get issuer info
    UUID issuerUuid = sender instanceof Player ? ((Player) sender).getUniqueId() : null;
    String issuerName = sender instanceof Player ? sender.getName() : "Console";

    // Check if using escalation template
    if (template != null) {
      return handleTemplateMute(sender, offlinePlayer.getUniqueId(), playerName, template, issuerUuid, issuerName, args);
    }

    // Parse duration and reason
    Instant muteUntil = null;
    String reason = null;
    int reasonStartIndex = 1;

    if (args.length > 1) {
      Optional<Instant> muteUntilOpt = MuteDateUtils.parseTimeSuffix(args[1]);
      if (muteUntilOpt.isPresent()) {
        muteUntil = muteUntilOpt.get();
        reasonStartIndex = 2;
      }
    }

    // Collect reason from remaining arguments
    if (args.length > reasonStartIndex) {
      reason = String.join(" ", Arrays.copyOfRange(args, reasonStartIndex, args.length));
    }

    // Mute the player
    boolean success;
    if (muteUntil != null) {
      success = StorageTool.tempMutePlayer(offlinePlayer.getUniqueId(), muteUntil, issuerUuid, issuerName, reason, null);
      if (success) {
        successMessageOffline(sender, playerName);
        PunishmentLogger.logMute(sender.getName(), playerName, args[1] + (reason != null ? " - " + reason : ""));

        // Record in history
        PunishmentHistoryStorage.recordPunishment(
            PunishmentType.MUTE,
            offlinePlayer.getUniqueId(),
            issuerUuid,
            issuerName,
            reason,
            muteUntil,
            false,
            null
        );
      } else {
        alreadyMutedMessage(sender, playerName);
      }
    } else {
      success = StorageTool.hardMutePlayer(offlinePlayer.getUniqueId(), issuerUuid, issuerName, reason, null);
      if (success) {
        successMessageOffline(sender, playerName);
        PunishmentLogger.logMute(sender.getName(), playerName, "permanent" + (reason != null ? " - " + reason : ""));

        // Record in history
        PunishmentHistoryStorage.recordPunishment(
            PunishmentType.MUTE,
            offlinePlayer.getUniqueId(),
            issuerUuid,
            issuerName,
            reason,
            null,
            true,
            null
        );
      } else {
        alreadyMutedMessage(sender, playerName);
      }
    }

    // Handle alt muting if enabled
    if (success) {
      handleAltMutingOffline(sender, offlinePlayer, muteUntil, reason, issuerUuid, issuerName);
    }

    return true;
  }

  /**
   * Handle muting using an escalation template.
   */
  private boolean handleTemplateMute(CommandSender sender, UUID playerUuid, String playerName,
                                      String template, UUID issuerUuid, String issuerName, String[] args) {
    Map<String, Map<Integer, String>> escalation = plugin.getPluginConfig().escalation;

    if (!escalation.containsKey(template)) {
      sender.sendMessage(ChatColor.RED + "Unknown escalation template: " + template);
      sender.sendMessage(ChatColor.GRAY + "Available templates: " + String.join(", ", escalation.keySet()));
      return true;
    }

    // Get violation count for this template
    int violationCount = PunishmentHistoryStorage.getTemplateViolationCount(playerUuid, template) + 1;

    Map<Integer, String> levels = escalation.get(template);

    // Find the appropriate duration for this violation level
    String duration = null;
    int maxLevel = 0;
    for (Map.Entry<Integer, String> entry : levels.entrySet()) {
      int level = entry.getKey();
      if (level > maxLevel) {
        maxLevel = level;
      }
      if (level == violationCount) {
        duration = entry.getValue();
        break;
      }
    }

    // If violation count exceeds max level, use the max level duration
    if (duration == null && violationCount > maxLevel) {
      duration = levels.get(maxLevel);
      violationCount = maxLevel;
    }

    if (duration == null) {
      sender.sendMessage(ChatColor.RED + "No punishment defined for offense #" + violationCount + " in template " + template);
      return true;
    }

    // Collect reason from remaining arguments
    String reason = template + " (offense #" + violationCount + ")";
    if (args.length > 1) {
      int reasonStartIndex = 1;
      // Skip duration arg if present
      if (MuteDateUtils.parseTimeSuffix(args[1]).isPresent()) {
        reasonStartIndex = 2;
      }
      if (args.length > reasonStartIndex) {
        reason = template + " (offense #" + violationCount + "): " + String.join(" ", Arrays.copyOfRange(args, reasonStartIndex, args.length));
      }
    }

    // Apply the mute
    boolean success;
    boolean permanent = duration.equalsIgnoreCase("permanent");

    if (permanent) {
      success = StorageTool.hardMutePlayer(playerUuid, issuerUuid, issuerName, reason, template);
      if (success) {
        successMessageTemplate(sender, playerName, template, violationCount, "permanent");
        PunishmentLogger.logMute(sender.getName(), playerName, "permanent (template: " + template + " #" + violationCount + ")");

        // Record in history
        PunishmentHistoryStorage.recordPunishment(
            PunishmentType.MUTE,
            playerUuid,
            issuerUuid,
            issuerName,
            reason,
            null,
            true,
            template
        );
      } else {
        alreadyMutedMessage(sender, playerName);
      }
    } else {
      Optional<Instant> muteUntilOpt = MuteDateUtils.parseTimeSuffix(duration);
      if (muteUntilOpt.isEmpty()) {
        sender.sendMessage(ChatColor.RED + "Invalid duration in template: " + duration);
        return true;
      }

      Instant muteUntil = muteUntilOpt.get();
      success = StorageTool.tempMutePlayer(playerUuid, muteUntil, issuerUuid, issuerName, reason, template);
      if (success) {
        successMessageTemplate(sender, playerName, template, violationCount, duration);
        PunishmentLogger.logMute(sender.getName(), playerName, duration + " (template: " + template + " #" + violationCount + ")");

        // Record in history
        PunishmentHistoryStorage.recordPunishment(
            PunishmentType.MUTE,
            playerUuid,
            issuerUuid,
            issuerName,
            reason,
            muteUntil,
            false,
            template
        );
      } else {
        alreadyMutedMessage(sender, playerName);
      }
    }

    return true;
  }

  /**
   * Handle auto-muting of alt accounts for an online player.
   */
  private void handleAltMuting(CommandSender sender, Player player, Instant muteUntil, String reason,
                                UUID issuerUuid, String issuerName) {
    PistonMuteConfig config = plugin.getPluginConfig();

    if (!config.altDetection.enabled || !config.altDetection.autoMuteAlts) {
      return;
    }

    Set<UUID> alts = StorageTool.getAltAccounts(player);
    if (alts.isEmpty()) {
      return;
    }

    int mutedCount = 0;
    for (UUID altUuid : alts) {
      if (!StorageTool.isMuted(altUuid)) {
        String altReason = reason != null ? reason + " (alt of " + player.getName() + ")" : "Alt of " + player.getName();
        if (muteUntil != null) {
          if (StorageTool.tempMutePlayer(altUuid, muteUntil, issuerUuid, issuerName, altReason, null)) {
            mutedCount++;
            String altName = StorageTool.getStoredPlayerName(altUuid);
            if (altName != null) {
              PunishmentLogger.logMute(sender.getName(), altName, "auto-muted as alt of " + player.getName());
            }
            PunishmentHistoryStorage.recordPunishment(
                PunishmentType.MUTE,
                altUuid,
                issuerUuid,
                issuerName,
                altReason,
                muteUntil,
                false,
                null
            );
          }
        } else {
          if (StorageTool.hardMutePlayer(altUuid, issuerUuid, issuerName, altReason, null)) {
            mutedCount++;
            String altName = StorageTool.getStoredPlayerName(altUuid);
            if (altName != null) {
              PunishmentLogger.logMute(sender.getName(), altName, "auto-muted as alt of " + player.getName());
            }
            PunishmentHistoryStorage.recordPunishment(
                PunishmentType.MUTE,
                altUuid,
                issuerUuid,
                issuerName,
                altReason,
                null,
                true,
                null
            );
          }
        }
      }
    }

    if (mutedCount > 0) {
      altsMutedMessage(sender, mutedCount);
    }
  }

  /**
   * Handle auto-muting of alt accounts for an offline player.
   */
  private void handleAltMutingOffline(CommandSender sender, OfflinePlayer player, Instant muteUntil, String reason,
                                       UUID issuerUuid, String issuerName) {
    PistonMuteConfig config = plugin.getPluginConfig();

    if (!config.altDetection.enabled || !config.altDetection.autoMuteAlts) {
      return;
    }

    Set<UUID> alts = StorageTool.getAltAccounts(player);
    if (alts.isEmpty()) {
      return;
    }

    String playerName = player.getName() != null ? player.getName() : player.getUniqueId().toString().substring(0, 8);
    int mutedCount = 0;
    for (UUID altUuid : alts) {
      if (!StorageTool.isMuted(altUuid)) {
        String altReason = reason != null ? reason + " (alt of " + playerName + ")" : "Alt of " + playerName;
        if (muteUntil != null) {
          if (StorageTool.tempMutePlayer(altUuid, muteUntil, issuerUuid, issuerName, altReason, null)) {
            mutedCount++;
            String altName = StorageTool.getStoredPlayerName(altUuid);
            if (altName != null) {
              PunishmentLogger.logMute(sender.getName(), altName, "auto-muted as alt of " + playerName);
            }
            PunishmentHistoryStorage.recordPunishment(
                PunishmentType.MUTE,
                altUuid,
                issuerUuid,
                issuerName,
                altReason,
                muteUntil,
                false,
                null
            );
          }
        } else {
          if (StorageTool.hardMutePlayer(altUuid, issuerUuid, issuerName, altReason, null)) {
            mutedCount++;
            String altName = StorageTool.getStoredPlayerName(altUuid);
            if (altName != null) {
              PunishmentLogger.logMute(sender.getName(), altName, "auto-muted as alt of " + playerName);
            }
            PunishmentHistoryStorage.recordPunishment(
                PunishmentType.MUTE,
                altUuid,
                issuerUuid,
                issuerName,
                altReason,
                null,
                true,
                null
            );
          }
        }
      }
    }

    if (mutedCount > 0) {
      altsMutedMessage(sender, mutedCount);
    }
  }

  private void alreadyMutedMessage(CommandSender sender, String playerName) {
    sender.spigot().sendMessage(new ComponentBuilder("----------------").color(ChatColor.DARK_BLUE).create());
    sender.spigot().sendMessage(new ComponentBuilder("PistonMute").color(ChatColor.GOLD).create());
    sender.spigot().sendMessage(new ComponentBuilder(playerName + " is already muted!").color(ChatColor.RED).create());
    sender.spigot().sendMessage(new ComponentBuilder("----------------").color(ChatColor.DARK_BLUE).create());
  }

  private void successMessage(CommandSender sender, String playerName) {
    sender.spigot().sendMessage(new ComponentBuilder("----------------").color(ChatColor.DARK_BLUE).create());
    sender.spigot().sendMessage(new ComponentBuilder("PistonMute").color(ChatColor.GOLD).create());
    sender.spigot().sendMessage(new ComponentBuilder("Successfully muted " + playerName + "!").color(ChatColor.GREEN).create());
    sender.spigot().sendMessage(new ComponentBuilder("----------------").color(ChatColor.DARK_BLUE).create());
  }

  private void successMessageOffline(CommandSender sender, String playerName) {
    sender.spigot().sendMessage(new ComponentBuilder("----------------").color(ChatColor.DARK_BLUE).create());
    sender.spigot().sendMessage(new ComponentBuilder("PistonMute").color(ChatColor.GOLD).create());
    sender.spigot().sendMessage(new ComponentBuilder("Successfully muted " + playerName + " (offline)!").color(ChatColor.GREEN).create());
    sender.spigot().sendMessage(new ComponentBuilder("Mute will take effect when they next join.").color(ChatColor.YELLOW).create());
    sender.spigot().sendMessage(new ComponentBuilder("----------------").color(ChatColor.DARK_BLUE).create());
  }

  private void successMessageTemplate(CommandSender sender, String playerName, String template, int offense, String duration) {
    sender.spigot().sendMessage(new ComponentBuilder("----------------").color(ChatColor.DARK_BLUE).create());
    sender.spigot().sendMessage(new ComponentBuilder("PistonMute").color(ChatColor.GOLD).create());
    sender.spigot().sendMessage(new ComponentBuilder("Successfully muted " + playerName + "!").color(ChatColor.GREEN).create());
    sender.spigot().sendMessage(new ComponentBuilder("Template: " + template + " | Offense #" + offense).color(ChatColor.AQUA).create());
    sender.spigot().sendMessage(new ComponentBuilder("Duration: " + duration).color(ChatColor.YELLOW).create());
    sender.spigot().sendMessage(new ComponentBuilder("----------------").color(ChatColor.DARK_BLUE).create());
  }

  private void cannotTargetMessage(CommandSender sender, Player player) {
    sender.spigot().sendMessage(new ComponentBuilder("----------------").color(ChatColor.DARK_BLUE).create());
    sender.spigot().sendMessage(new ComponentBuilder("PistonMute").color(ChatColor.GOLD).create());
    sender.spigot().sendMessage(new ComponentBuilder(StaffUtils.getCannotTargetReason(player)).color(ChatColor.RED).create());
    sender.spigot().sendMessage(new ComponentBuilder("----------------").color(ChatColor.DARK_BLUE).create());
  }

  private void playerNotFoundMessage(CommandSender sender, String playerName) {
    sender.spigot().sendMessage(new ComponentBuilder("----------------").color(ChatColor.DARK_BLUE).create());
    sender.spigot().sendMessage(new ComponentBuilder("PistonMute").color(ChatColor.GOLD).create());
    sender.spigot().sendMessage(new ComponentBuilder("Player '" + playerName + "' has never played on this server!").color(ChatColor.RED).create());
    sender.spigot().sendMessage(new ComponentBuilder("----------------").color(ChatColor.DARK_BLUE).create());
  }

  private void altsMutedMessage(CommandSender sender, int count) {
    sender.spigot().sendMessage(new ComponentBuilder("Also muted " + count + " alt account(s).").color(ChatColor.YELLOW).create());
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
      // Suggest time durations and --template
      List<String> suggestions = new ArrayList<>();
      suggestions.add("--template");
      suggestions.add("10m");
      suggestions.add("30m");
      suggestions.add("1h");
      suggestions.add("6h");
      suggestions.add("12h");
      suggestions.add("1d");
      suggestions.add("3d");
      suggestions.add("7d");
      suggestions.add("30d");

      List<String> completions = new ArrayList<>();
      StringUtil.copyPartialMatches(args[1], suggestions, completions);
      return completions;
    } else if (args.length == 3 && args[1].equalsIgnoreCase("--template")) {
      // Suggest template names
      List<String> templates = new ArrayList<>(plugin.getPluginConfig().escalation.keySet());
      List<String> completions = new ArrayList<>();
      StringUtil.copyPartialMatches(args[2], templates, completions);
      return completions;
    }

    return new ArrayList<>();
  }
}
