package net.pistonmaster.pistonmute.commands;

import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.pistonmaster.pistonmute.PistonMute;
import net.pistonmaster.pistonmute.utils.StorageTool;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.*;

/**
 * Command to view known alt accounts for a player.
 * Uses IP tracking data to identify potential alt accounts.
 */
@RequiredArgsConstructor
public final class AltsCommand implements CommandExecutor, TabExecutor {
  private final PistonMute plugin;

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (args.length == 0) {
      return false;
    }

    // Check if alt detection is enabled
    if (!plugin.getPluginConfig().enableAltDetection) {
      sender.spigot().sendMessage(new ComponentBuilder("----------------").color(ChatColor.DARK_BLUE).create());
      sender.spigot().sendMessage(new ComponentBuilder("PistonMute").color(ChatColor.GOLD).create());
      sender.spigot().sendMessage(new ComponentBuilder("Alt detection is disabled in the config!").color(ChatColor.RED).create());
      sender.spigot().sendMessage(new ComponentBuilder("----------------").color(ChatColor.DARK_BLUE).create());
      return true;
    }

    // Try to find the player
    String targetName = args[0];
    OfflinePlayer target = findPlayer(targetName);

    if (target == null) {
      sender.spigot().sendMessage(new ComponentBuilder("----------------").color(ChatColor.DARK_BLUE).create());
      sender.spigot().sendMessage(new ComponentBuilder("PistonMute").color(ChatColor.GOLD).create());
      sender.spigot().sendMessage(new ComponentBuilder("Player '" + targetName + "' not found!").color(ChatColor.RED).create());
      sender.spigot().sendMessage(new ComponentBuilder("----------------").color(ChatColor.DARK_BLUE).create());
      return true;
    }

    String playerName = target.getName() != null ? target.getName() : targetName;

    // Get alt accounts
    Set<UUID> alts = StorageTool.getAltAccounts(target);
    String storedIP = StorageTool.getStoredIP(target.getUniqueId());

    // Display header
    sender.spigot().sendMessage(new ComponentBuilder("----------------").color(ChatColor.DARK_BLUE).create());
    sender.spigot().sendMessage(new ComponentBuilder("PistonMute - Alt Accounts").color(ChatColor.GOLD).create());
    sender.spigot().sendMessage(new ComponentBuilder("Player: ").color(ChatColor.GRAY)
        .append(playerName).color(ChatColor.WHITE).create());

    // Show IP if sender has permission
    if (sender.hasPermission("pistonmute.alts.showip") && storedIP != null) {
      sender.spigot().sendMessage(new ComponentBuilder("Last IP: ").color(ChatColor.GRAY)
          .append(storedIP).color(ChatColor.WHITE).create());
    }

    // Show mute status
    boolean isMuted = StorageTool.isMuted(target);
    sender.spigot().sendMessage(new ComponentBuilder("Mute Status: ").color(ChatColor.GRAY)
        .append(isMuted ? "MUTED" : "Not muted").color(isMuted ? ChatColor.RED : ChatColor.GREEN).create());

    sender.spigot().sendMessage(new ComponentBuilder("").create());

    // Show alt accounts
    if (alts.isEmpty()) {
      sender.spigot().sendMessage(new ComponentBuilder("No known alt accounts found.").color(ChatColor.YELLOW).create());
    } else {
      sender.spigot().sendMessage(new ComponentBuilder("Known Alt Accounts (" + alts.size() + "):").color(ChatColor.YELLOW).create());

      for (UUID altUuid : alts) {
        String altName = StorageTool.getStoredPlayerName(altUuid);
        if (altName == null) {
          OfflinePlayer altPlayer = Bukkit.getOfflinePlayer(altUuid);
          altName = altPlayer.getName() != null ? altPlayer.getName() : altUuid.toString().substring(0, 8);
        }

        boolean altMuted = StorageTool.isMuted(altUuid);
        OfflinePlayer altPlayer = Bukkit.getOfflinePlayer(altUuid);
        boolean altOnline = altPlayer.isOnline();

        // Build the alt entry with hover information
        TextComponent altEntry = new TextComponent(" - ");
        altEntry.setColor(ChatColor.GRAY);

        TextComponent nameComponent = new TextComponent(altName);
        nameComponent.setColor(altOnline ? ChatColor.GREEN : ChatColor.WHITE);

        // Add hover with more details
        String hoverText = "UUID: " + altUuid.toString() + "\n" +
            "Status: " + (altOnline ? "Online" : "Offline") + "\n" +
            "Muted: " + (altMuted ? "Yes" : "No");
        BaseComponent[] hoverComponents = new ComponentBuilder(hoverText).create();
        nameComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverComponents));

        altEntry.addExtra(nameComponent);

        // Add muted indicator
        if (altMuted) {
          TextComponent mutedIndicator = new TextComponent(" [MUTED]");
          mutedIndicator.setColor(ChatColor.RED);
          altEntry.addExtra(mutedIndicator);
        }

        // Add online indicator
        if (altOnline) {
          TextComponent onlineIndicator = new TextComponent(" (online)");
          onlineIndicator.setColor(ChatColor.GREEN);
          altEntry.addExtra(onlineIndicator);
        }

        sender.spigot().sendMessage(altEntry);
      }
    }

    sender.spigot().sendMessage(new ComponentBuilder("----------------").color(ChatColor.DARK_BLUE).create());
    return true;
  }

  /**
   * Find a player by name (online or offline).
   */
  @SuppressWarnings("deprecation")
  private OfflinePlayer findPlayer(String name) {
    // First try to find online player
    Player online = Bukkit.getPlayer(name);
    if (online != null) {
      return online;
    }

    // Try to find offline player
    OfflinePlayer offline = Bukkit.getOfflinePlayer(name);
    if (offline.hasPlayedBefore()) {
      return offline;
    }

    return null;
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
