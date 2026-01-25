package net.pistonmaster.pistonmute.commands;

import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.pistonmaster.pistonmute.PistonMute;
import net.pistonmaster.pistonmute.data.PunishmentType;
import net.pistonmaster.pistonmute.data.Warning;
import net.pistonmaster.pistonmute.utils.PunishmentHistoryStorage;
import net.pistonmaster.pistonmute.utils.WarningStorage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@RequiredArgsConstructor
public final class WarnCommand implements CommandExecutor, TabExecutor {
  private final PistonMute plugin;

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (!plugin.getPluginConfig().warningsEnabled) {
      sender.sendMessage(ChatColor.RED + "Warning system is disabled.");
      return true;
    }

    if (args.length < 2) {
      sender.sendMessage(ChatColor.RED + "Usage: /warn <player> <reason>");
      return true;
    }

    Player target = plugin.getServer().getPlayer(args[0]);
    if (target == null) {
      sender.sendMessage(ChatColor.RED + "Player not found or not online.");
      return true;
    }

    if (target == sender) {
      sender.sendMessage(ChatColor.RED + "You cannot warn yourself!");
      return true;
    }

    // Build reason from remaining args
    StringBuilder reasonBuilder = new StringBuilder();
    for (int i = 1; i < args.length; i++) {
      if (i > 1) {
        reasonBuilder.append(" ");
      }
      reasonBuilder.append(args[i]);
    }
    String reason = reasonBuilder.toString();

    // Calculate expiry time
    Instant expiresAt = null;
    int expiryDays = plugin.getPluginConfig().warningExpiryDays;
    if (expiryDays > 0) {
      expiresAt = Instant.now().plus(expiryDays, ChronoUnit.DAYS);
    }

    // Get issuer info
    UUID issuerUuid = sender instanceof Player ? ((Player) sender).getUniqueId() : null;
    String issuerName = sender instanceof Player ? sender.getName() : "Console";

    // Add the warning
    Warning warning = WarningStorage.addWarning(
        target.getUniqueId(),
        issuerUuid,
        issuerName,
        reason,
        expiresAt
    );

    // Record in punishment history
    PunishmentHistoryStorage.recordPunishment(
        PunishmentType.WARNING,
        target.getUniqueId(),
        issuerUuid,
        issuerName,
        reason,
        expiresAt,
        false,
        null
    );

    // Notify the target
    String warnedMessage = plugin.getPluginConfig().warnedMessage
        .replace("%reason%", reason)
        .replace("&", "\u00A7");
    String warnedByMessage = plugin.getPluginConfig().warnedByMessage
        .replace("%issuer%", issuerName)
        .replace("&", "\u00A7");

    target.sendMessage(warnedMessage);
    target.sendMessage(warnedByMessage);

    // Get current warning count
    int activeWarnings = WarningStorage.getActiveWarningCount(target.getUniqueId());

    // Send success message to sender
    sender.spigot().sendMessage(new ComponentBuilder("----------------").color(ChatColor.DARK_BLUE).create());
    sender.spigot().sendMessage(new ComponentBuilder("PistonMute").color(ChatColor.GOLD).create());
    sender.spigot().sendMessage(new ComponentBuilder("Successfully warned " + target.getName() + "!").color(ChatColor.GREEN).create());
    sender.spigot().sendMessage(new ComponentBuilder("Reason: " + reason).color(ChatColor.GRAY).create());
    sender.spigot().sendMessage(new ComponentBuilder("Active warnings: " + activeWarnings).color(ChatColor.YELLOW).create());
    sender.spigot().sendMessage(new ComponentBuilder("----------------").color(ChatColor.DARK_BLUE).create());

    // Check if max warnings reached
    int maxWarnings = plugin.getPluginConfig().maxWarningsBeforeAction;
    if (maxWarnings > 0 && activeWarnings >= maxWarnings) {
      handleMaxWarningsReached(sender, target, activeWarnings);
    }

    return true;
  }

  private void handleMaxWarningsReached(CommandSender sender, Player target, int warningCount) {
    String action = plugin.getPluginConfig().warningMaxAction.toLowerCase();

    sender.sendMessage(ChatColor.YELLOW + target.getName() + " has reached " + warningCount + " warnings!");

    switch (action) {
      case "mute":
        // Execute mute command
        Bukkit.dispatchCommand(sender, "mute " + target.getName());
        break;
      case "tempmute":
        String duration = plugin.getPluginConfig().warningMaxActionDuration;
        Bukkit.dispatchCommand(sender, "mute " + target.getName() + " " + duration);
        break;
      default:
        // No action
        break;
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
