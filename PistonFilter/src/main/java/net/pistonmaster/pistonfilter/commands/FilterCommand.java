package net.pistonmaster.pistonfilter.commands;

import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import net.pistonmaster.pistonfilter.PistonFilter;
import net.pistonmaster.pistonfilter.config.PistonFilterConfig;
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
import java.util.Locale;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class FilterCommand implements CommandExecutor, TabExecutor {
  private final PistonFilter plugin;

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (args.length == 0) {
      sendHelp(sender);
      return true;
    }

    String subCommand = args[0].toLowerCase(Locale.ROOT);

    switch (subCommand) {
      case "reload" -> {
        if (!sender.hasPermission("pistonfilter.admin")) {
          sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
          return true;
        }
        plugin.loadConfig();
        sender.sendMessage(ChatColor.GOLD + "Reloaded the config!");
      }
      case "add" -> {
        if (!sender.hasPermission("pistonfilter.admin")) {
          sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
          return true;
        }
        if (args.length < 2) {
          sender.sendMessage(ChatColor.RED + "Usage: /filter add <text>");
          return true;
        }
        PistonFilterConfig config = plugin.getPluginConfig();
        List<String> newBannedText = Stream.concat(config.bannedText.stream(), Stream.of(args[1])).toList();
        config.bannedText = newBannedText;
        plugin.saveConfig(config);
        sender.sendMessage(ChatColor.GOLD + "Successfully added the config entry!");
      }
      case "pause" -> {
        if (!sender.hasPermission("pistonfilter.pausechat")) {
          sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
          return true;
        }
        if (plugin.getChatPauseManager().pause()) {
          sender.sendMessage(ChatColor.GREEN + "Chat has been paused.");
          notifyStaffChatPaused(sender, true);
        } else {
          sender.sendMessage(ChatColor.YELLOW + "Chat is already paused.");
        }
      }
      case "unpause", "resume" -> {
        if (!sender.hasPermission("pistonfilter.pausechat")) {
          sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
          return true;
        }
        if (plugin.getChatPauseManager().unpause()) {
          sender.sendMessage(ChatColor.GREEN + "Chat has been resumed.");
          notifyStaffChatPaused(sender, false);
        } else {
          sender.sendMessage(ChatColor.YELLOW + "Chat is not paused.");
        }
      }
      case "clearchat", "clear" -> {
        if (!sender.hasPermission("pistonfilter.clearchat")) {
          sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
          return true;
        }
        clearChat();
        sender.sendMessage(ChatColor.GREEN + "Chat has been cleared.");
      }
      default -> sendHelp(sender);
    }

    return true;
  }

  private void sendHelp(CommandSender sender) {
    sender.sendMessage(ChatColor.GOLD + "PistonFilter Commands:");
    if (sender.hasPermission("pistonfilter.admin")) {
      sender.sendMessage(ChatColor.YELLOW + "/filter reload" + ChatColor.GRAY + " - Reload the config");
      sender.sendMessage(ChatColor.YELLOW + "/filter add <text>" + ChatColor.GRAY + " - Add banned text");
    }
    if (sender.hasPermission("pistonfilter.pausechat")) {
      sender.sendMessage(ChatColor.YELLOW + "/filter pause" + ChatColor.GRAY + " - Pause chat");
      sender.sendMessage(ChatColor.YELLOW + "/filter unpause" + ChatColor.GRAY + " - Resume chat");
    }
    if (sender.hasPermission("pistonfilter.clearchat")) {
      sender.sendMessage(ChatColor.YELLOW + "/filter clearchat" + ChatColor.GRAY + " - Clear chat");
    }
  }

  private void notifyStaffChatPaused(CommandSender executor, boolean paused) {
    PistonFilterConfig config = plugin.getPluginConfig();
    String message = paused ? config.chatPausedStaffMessage : config.chatUnpausedStaffMessage;
    message = ChatColor.translateAlternateColorCodes('&', message.replace("%player%", executor.getName()));

    for (Player player : Bukkit.getOnlinePlayers()) {
      if (player.hasPermission("pistonfilter.notify") && !player.equals(executor)) {
        player.sendMessage(message);
      }
    }
  }

  private void clearChat() {
    PistonFilterConfig config = plugin.getPluginConfig();
    int lines = config.clearChatLines;

    StringBuilder blankLines = new StringBuilder();
    for (int i = 0; i < lines; i++) {
      blankLines.append("\n");
    }
    String clearMessage = blankLines.toString();

    for (Player player : Bukkit.getOnlinePlayers()) {
      // Don't clear chat for staff who have the bypass permission
      if (!player.hasPermission("pistonfilter.clearchat.bypass")) {
        player.sendMessage(clearMessage);
      }
    }
  }

  @Override
  public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
    if (args.length == 1) {
      List<String> suggestions = new ArrayList<>();

      if (sender.hasPermission("pistonfilter.admin")) {
        suggestions.add("add");
        suggestions.add("reload");
      }
      if (sender.hasPermission("pistonfilter.pausechat")) {
        suggestions.add("pause");
        suggestions.add("unpause");
        suggestions.add("resume");
      }
      if (sender.hasPermission("pistonfilter.clearchat")) {
        suggestions.add("clearchat");
        suggestions.add("clear");
      }

      List<String> completions = new ArrayList<>();
      StringUtil.copyPartialMatches(args[0], suggestions, completions);
      Collections.sort(completions);

      return completions;
    }

    return new ArrayList<>();
  }
}
