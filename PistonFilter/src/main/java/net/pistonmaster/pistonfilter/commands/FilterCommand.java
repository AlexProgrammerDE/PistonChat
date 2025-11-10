package net.pistonmaster.pistonfilter.commands;

import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import net.pistonmaster.pistonfilter.PistonFilter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.util.StringUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class FilterCommand implements CommandExecutor, TabExecutor {
  private final PistonFilter plugin;

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (sender.hasPermission("pistonfilter.admin") && args.length > 0) {
      if (args[0].equalsIgnoreCase("reload")) {
        plugin.reloadConfig();
        sender.sendMessage(ChatColor.GOLD + "Reloaded the config!");
      }

      if (args[0].equalsIgnoreCase("add") && args.length > 1) {
        FileConfiguration config = plugin.getConfig();

        config.set("banned-text", Stream.concat(plugin.getConfig().getStringList("banned-text").stream(), Stream.of(args[1])).collect(Collectors.toList()));

        try {
          config.save(new File(plugin.getDataFolder(), "config.yml"));
        } catch (IOException e) {
          plugin.getLogger().warning("Failed to save config: " + e.getMessage());
        }

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
