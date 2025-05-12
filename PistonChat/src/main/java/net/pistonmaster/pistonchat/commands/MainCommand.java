package net.pistonmaster.pistonchat.commands;

import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.pistonmaster.pistonchat.PistonChat;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.util.StringUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class MainCommand implements CommandExecutor, TabExecutor {
  private final PistonChat plugin;

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (args.length == 0) {
      return false;
    }

    switch (args[0].toLowerCase()) {
      case "help" -> {
        if (!sender.hasPermission("pistonchat.help")) {
          sender.sendMessage(command.getPermissionMessage());
        }

        String headerText = LegacyComponentSerializer.legacySection().serialize(plugin.getCommonTool().getLanguageMessage("help-header", false));
        ComponentBuilder builder = new ComponentBuilder(headerText).color(ChatColor.GOLD);

        for (Map.Entry<String, Map<String, Object>> entry : plugin.getDescription().getCommands().entrySet()) {
          String name = entry.getKey();
          Map<String, Object> info = entry.getValue();
          if (!sender.hasPermission(info.get("permission").toString())) {
            continue;
          }

          builder.append("\n/" + name)
              .color(ChatColor.GOLD)
              .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/" + name + " "))
              .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                  new ComponentBuilder("Click me!")
                      .color(ChatColor.GOLD)
                      .create()
              ))
              .append(" - ")
              .color(ChatColor.GOLD)
              .append(info.get("description").toString());
        }

        sender.spigot().sendMessage(builder.create());
      }
      case "version" -> {
        if (!sender.hasPermission("pistonchat.version")) {
          sender.sendMessage(command.getPermissionMessage());
        }

        sender.sendMessage(ChatColor.GOLD + "Currently running: " + plugin.getDescription().getFullName());
      }
      case "reload" -> {
        if (!sender.hasPermission("pistonchat.reload")) {
          sender.sendMessage(command.getPermissionMessage());
        }

        try {
          plugin.getConfigManager().create();
          plugin.getLanguageManager().create();
        } catch (IOException e) {
          plugin.getLogger().severe("Could not create config!");
          e.printStackTrace();
        }
        sender.sendMessage("Reloaded the config!");
      }
      default -> {
        return false;
      }
    }

    return true;
  }

  @Override
  public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
    if (args.length == 1) {
      List<String> possibleCommands = new ArrayList<>();
      List<String> completions = new ArrayList<>();

      if (sender.hasPermission("pistonchat.help")) {
        possibleCommands.add("help");
      }

      if (sender.hasPermission("pistonchat.reload")) {
        possibleCommands.add("reload");
      }

      StringUtil.copyPartialMatches(args[0], possibleCommands, completions);
      Collections.sort(completions);

      return completions;
    } else {
      return Collections.emptyList();
    }
  }
}
