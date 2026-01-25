package net.pistonmaster.pistonmute.commands;

import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.pistonmaster.pistonmute.PistonMute;
import net.pistonmaster.pistonmute.utils.NotesStorage;
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

/**
 * Command to view all notes for a player.
 * Usage: /notes <player>
 */
@RequiredArgsConstructor
public final class NotesCommand implements CommandExecutor, TabExecutor {
  private final PistonMute plugin;

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (!plugin.getPluginConfig().notes.enabled) {
      sender.spigot().sendMessage(new ComponentBuilder("Notes system is disabled!").color(ChatColor.RED).create());
      return true;
    }

    if (args.length < 1) {
      return false;
    }

    // Try to find the player (online or offline)
    OfflinePlayer target = findPlayer(args[0]);

    if (target == null) {
      sender.spigot().sendMessage(new ComponentBuilder("----------------").color(ChatColor.DARK_BLUE).create());
      sender.spigot().sendMessage(new ComponentBuilder("PistonMute").color(ChatColor.GOLD).create());
      sender.spigot().sendMessage(new ComponentBuilder("Player not found!").color(ChatColor.RED).create());
      sender.spigot().sendMessage(new ComponentBuilder("----------------").color(ChatColor.DARK_BLUE).create());
      return true;
    }

    String targetName = target.getName() != null ? target.getName() : args[0];
    List<NotesStorage.PlayerNote> notes = NotesStorage.getNotes(target);

    sender.spigot().sendMessage(new ComponentBuilder("----------------").color(ChatColor.DARK_BLUE).create());
    sender.spigot().sendMessage(new ComponentBuilder("PistonMute - Notes").color(ChatColor.GOLD).create());
    sender.spigot().sendMessage(new ComponentBuilder("Notes for " + targetName + ":").color(ChatColor.YELLOW).create());

    if (notes.isEmpty()) {
      sender.spigot().sendMessage(new ComponentBuilder("No notes found.").color(ChatColor.GRAY).create());
    } else {
      for (NotesStorage.PlayerNote note : notes) {
        sender.spigot().sendMessage(new ComponentBuilder("")
            .append("#" + note.id()).color(ChatColor.AQUA)
            .append(" [" + NotesStorage.formatTimestamp(note.timestamp()) + "]").color(ChatColor.GRAY)
            .append(" by ").color(ChatColor.WHITE)
            .append(note.staff()).color(ChatColor.GOLD)
            .create());
        sender.spigot().sendMessage(new ComponentBuilder("  " + note.text()).color(ChatColor.WHITE).create());
      }
    }

    sender.spigot().sendMessage(new ComponentBuilder("Total: " + notes.size() + " note(s)").color(ChatColor.GRAY).create());
    sender.spigot().sendMessage(new ComponentBuilder("----------------").color(ChatColor.DARK_BLUE).create());

    return true;
  }

  @SuppressWarnings("deprecation")
  private OfflinePlayer findPlayer(String name) {
    // First try to find online player
    Player online = Bukkit.getPlayer(name);
    if (online != null) {
      return online;
    }

    // Try to find offline player by name
    OfflinePlayer offline = Bukkit.getOfflinePlayer(name);
    if (offline.hasPlayedBefore() || offline.isOnline()) {
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
