package net.pistonmaster.pistonchat.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.pistonmaster.pistonchat.PistonChat;
import net.pistonmaster.pistonchat.utils.CommonTool;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainCommand implements CommandExecutor, TabExecutor {
    private final PistonChat plugin;

    public MainCommand(PistonChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0) {
            switch (args[0].toLowerCase()) {
                case "help":
                    if (sender.hasPermission("pistonchat.help")) {
                        ComponentBuilder builder = new ComponentBuilder("---" + CommonTool.getPrefix() + "---").color(ChatColor.GOLD);

                        plugin.getDescription().getCommands().forEach((name, info) ->
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
                                        .append(info.get("description").toString()));

                        sender.spigot().sendMessage(builder.create());
                    } else {
                        sender.sendMessage(command.getPermissionMessage());
                    }

                    break;
                case "version":
                    if (sender.hasPermission("pistonchat.version")) {
                        sender.sendMessage(ChatColor.GOLD + "Currently running: " + plugin.getDescription().getFullName());
                    } else {
                        sender.sendMessage(command.getPermissionMessage());
                    }

                    break;
                case "reload":
                    if (sender.hasPermission("pistonchat.reload")) {
                        plugin.reloadConfig();
                        sender.sendMessage("Reloaded the config!");
                    } else {
                        sender.sendMessage(command.getPermissionMessage());
                    }

                    break;
                default:
                    return false;
            }
        } else {
            return false;
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
            return new ArrayList<>();
        }
    }
}
