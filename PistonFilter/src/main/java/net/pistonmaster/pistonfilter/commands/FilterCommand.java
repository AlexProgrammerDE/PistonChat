package net.pistonmaster.pistonfilter.commands;

import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import net.pistonmaster.pistonfilter.PistonFilter;
import net.pistonmaster.pistonfilter.config.PistonFilterConfig;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class FilterCommand implements CommandExecutor, TabExecutor {
  private final PistonFilter plugin;

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (sender.hasPermission("pistonfilter.admin") && args.length > 0) {
      if ("reload".equalsIgnoreCase(args[0])) {
        plugin.loadConfig();
        sender.sendMessage(ChatColor.GOLD + "Reloaded the config!");
      }

      if ("add".equalsIgnoreCase(args[0]) && args.length > 1) {
        PistonFilterConfig config = plugin.getPluginConfig();

        List<String> newBannedText = Stream.concat(config.bannedText.stream(), Stream.of(args[1])).toList();
        config.bannedText = newBannedText;

        plugin.saveConfig(config);

        sender.sendMessage(ChatColor.GOLD + "Successfully added the config entry!");
      }
    }

    return false;
  }

  @Override
  public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
    if (args.length == 1) {
      List<String> suggestion = new ArrayList<>();

      suggestion.add("add");
      suggestion.add("reload");

      List<String> completions = new ArrayList<>();

      StringUtil.copyPartialMatches(args[0], suggestion, completions);

      Collections.sort(completions);

      return completions;
    } else {
      return new ArrayList<>();
    }
  }
}
