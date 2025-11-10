package net.pistonmaster.pistonmute.commands;

import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.pistonmaster.pistonmute.PistonMute;
import net.pistonmaster.pistonmute.utils.StorageTool;
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
public final class MuteCommand implements CommandExecutor, TabExecutor {
  private final PistonMute plugin;

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (args.length > 0) {
      Player player = plugin.getServer().getPlayer(args[0]);

      if (player != null) {
        if (player != sender) {
          if (args.length > 1) {
            String timeSuffix = args[1].toLowerCase(Locale.ROOT);
            Instant muteUntil = Instant.now();
            
            if (timeSuffix.endsWith("y")) {
              int years = Integer.parseInt(timeSuffix.replace("y", ""));
              muteUntil = muteUntil.plus(years * 365L, ChronoUnit.DAYS);
            } else if (timeSuffix.endsWith("d")) {
              int days = Integer.parseInt(timeSuffix.replace("d", ""));
              muteUntil = muteUntil.plus(days, ChronoUnit.DAYS);
            } else if (timeSuffix.endsWith("h")) {
              int hours = Integer.parseInt(timeSuffix.replace("h", ""));
              muteUntil = muteUntil.plus(hours, ChronoUnit.HOURS);
            } else if (timeSuffix.endsWith("m")) {
              int minutes = Integer.parseInt(timeSuffix.replace("m", ""));
              muteUntil = muteUntil.plus(minutes, ChronoUnit.MINUTES);
            } else if (timeSuffix.endsWith("s")) {
              int seconds = Integer.parseInt(timeSuffix.replace("s", ""));
              muteUntil = muteUntil.plus(seconds, ChronoUnit.SECONDS);
            } else {
              return false;
            }

            if (StorageTool.tempMutePlayer(player, Date.from(muteUntil))) {
              successMessage(sender, player);
            } else {
              alreadyMutedMessage(sender, player);
            }
          } else {
            if (StorageTool.hardMutePlayer(player)) {
              successMessage(sender, player);
            } else {
              alreadyMutedMessage(sender, player);
            }
          }
        } else {
          sender.sendMessage("You can't mute yourself!");
        }
      } else {
        return false;
      }
    } else {
      return false;
    }

    return true;
  }

  private void alreadyMutedMessage(CommandSender sender, Player player) {
    sender.spigot().sendMessage(new ComponentBuilder("----------------").color(ChatColor.DARK_BLUE).create());
    sender.spigot().sendMessage(new ComponentBuilder("PistonMute").color(ChatColor.GOLD).create());
    sender.spigot().sendMessage(new ComponentBuilder(player.getName() + " is already muted!").color(ChatColor.RED).create());
    sender.spigot().sendMessage(new ComponentBuilder("----------------").color(ChatColor.DARK_BLUE).create());
  }

  private void successMessage(CommandSender sender, Player player) {
    sender.spigot().sendMessage(new ComponentBuilder("----------------").color(ChatColor.DARK_BLUE).create());
    sender.spigot().sendMessage(new ComponentBuilder("PistonMute").color(ChatColor.GOLD).create());
    sender.spigot().sendMessage(new ComponentBuilder("Successfully muted " + player.getName() + "!").color(ChatColor.GREEN).create());
    sender.spigot().sendMessage(new ComponentBuilder("----------------").color(ChatColor.DARK_BLUE).create());
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
    } else {
      return new ArrayList<>();
    }
  }
}
