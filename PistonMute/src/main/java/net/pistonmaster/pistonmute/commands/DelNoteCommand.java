package net.pistonmaster.pistonmute.commands;

import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.pistonmaster.pistonmute.PistonMute;
import net.pistonmaster.pistonmute.utils.NotesStorage;
import net.pistonmaster.pistonmute.utils.PunishmentLogger;
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
 * Command to delete a note from a player.
 * Usage: /delnote <player> <id>
 */
@RequiredArgsConstructor
public final class DelNoteCommand implements CommandExecutor, TabExecutor {
  private final PistonMute plugin;

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (!plugin.getPluginConfig().notesEnabled) {
      sender.spigot().sendMessage(new ComponentBuilder("Notes system is disabled!").color(ChatColor.RED).create());
      return true;
    }

    if (args.length < 2) {
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

    // Parse note ID
    int noteId;
    try {
      noteId = Integer.parseInt(args[1]);
    } catch (NumberFormatException e) {
      sender.spigot().sendMessage(new ComponentBuilder("----------------").color(ChatColor.DARK_BLUE).create());
      sender.spigot().sendMessage(new ComponentBuilder("PistonMute").color(ChatColor.GOLD).create());
      sender.spigot().sendMessage(new ComponentBuilder("Invalid note ID! Must be a number.").color(ChatColor.RED).create());
      sender.spigot().sendMessage(new ComponentBuilder("----------------").color(ChatColor.DARK_BLUE).create());
      return true;
    }

    String targetName = target.getName() != null ? target.getName() : args[0];
    String staffName = sender.getName();

    if (NotesStorage.deleteNote(target, noteId)) {
      // Log the action
      PunishmentLogger.logNoteDelete(staffName, targetName, noteId);

      sender.spigot().sendMessage(new ComponentBuilder("----------------").color(ChatColor.DARK_BLUE).create());
      sender.spigot().sendMessage(new ComponentBuilder("PistonMute").color(ChatColor.GOLD).create());
      sender.spigot().sendMessage(new ComponentBuilder("Note #" + noteId + " deleted from " + targetName + "!").color(ChatColor.GREEN).create());
      sender.spigot().sendMessage(new ComponentBuilder("----------------").color(ChatColor.DARK_BLUE).create());
    } else {
      sender.spigot().sendMessage(new ComponentBuilder("----------------").color(ChatColor.DARK_BLUE).create());
      sender.spigot().sendMessage(new ComponentBuilder("PistonMute").color(ChatColor.GOLD).create());
      sender.spigot().sendMessage(new ComponentBuilder("Note #" + noteId + " not found for " + targetName + "!").color(ChatColor.RED).create());
      sender.spigot().sendMessage(new ComponentBuilder("----------------").color(ChatColor.DARK_BLUE).create());
    }

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
    } else if (args.length == 2) {
      // Try to provide note ID completions
      OfflinePlayer target = findPlayer(args[0]);
      if (target != null) {
        List<NotesStorage.PlayerNote> notes = NotesStorage.getNotes(target);
        List<String> ids = new ArrayList<>();

        for (NotesStorage.PlayerNote note : notes) {
          ids.add(String.valueOf(note.id()));
        }

        List<String> completions = new ArrayList<>();
        StringUtil.copyPartialMatches(args[1], ids, completions);
        Collections.sort(completions);

        return completions;
      }
    }

    return new ArrayList<>();
  }
}
