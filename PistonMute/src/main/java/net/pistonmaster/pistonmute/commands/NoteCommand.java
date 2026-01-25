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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Command to add a note to a player.
 * Usage: /note <player> <text>
 */
@RequiredArgsConstructor
public final class NoteCommand implements CommandExecutor, TabExecutor {
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

    // Combine remaining args into note text
    String noteText = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
    String staffName = sender.getName();

    int noteId = NotesStorage.addNote(target, staffName, noteText);

    // Log the action
    String targetName = target.getName() != null ? target.getName() : args[0];
    PunishmentLogger.logNoteAdd(staffName, targetName, noteText);

    sender.spigot().sendMessage(new ComponentBuilder("----------------").color(ChatColor.DARK_BLUE).create());
    sender.spigot().sendMessage(new ComponentBuilder("PistonMute").color(ChatColor.GOLD).create());
    sender.spigot().sendMessage(new ComponentBuilder("Note #" + noteId + " added to " + targetName + "!").color(ChatColor.GREEN).create());
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
