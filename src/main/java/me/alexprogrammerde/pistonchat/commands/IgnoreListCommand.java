package me.alexprogrammerde.pistonchat.commands;

import com.google.common.math.IntMath;
import me.alexprogrammerde.pistonchat.utils.CommonTool;
import me.alexprogrammerde.pistonchat.utils.ConfigTool;
import me.alexprogrammerde.pistonchat.utils.IgnoreTool;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class IgnoreListCommand implements CommandExecutor, TabExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            List<String> list = new ArrayList<>();

            for (OfflinePlayer offlinePlayer : IgnoreTool.getIgnoredPlayers(player).keySet()) {
                list.add(offlinePlayer.getName());
            }

            if (list.isEmpty()) {
                player.sendMessage(CommonTool.getPrefix() + "You have no players ignored!");
            } else {
                if (args.length > 0) {
                    showList(Integer.parseInt(args[0]), player);
                } else {
                    showList(1, player);
                }
            }
        } else {
            sender.sendMessage(CommonTool.getPrefix() + "You need to be a player to do that!");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return new ArrayList<>();
    }

    private void showList(int page, Player player) {
        int maxValue = page * ConfigTool.getPageSize();
        int minValue = maxValue - ConfigTool.getPageSize();

        HashMap<OfflinePlayer, IgnoreTool.IgnoreType> map = IgnoreTool.getIgnoredPlayers(player);

        int allPages = IntMath.divide(map.size(), ConfigTool.getPageSize(), RoundingMode.CEILING);

        ComponentBuilder navigation = new ComponentBuilder("[ Ignored players ").color(ChatColor.GOLD);

        navigation.append("[<]").color(page > 1 ? ChatColor.AQUA : ChatColor.GRAY);

        if (page > 1) {
            navigation.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ignorelist " + (page - 1)));
        }

        navigation.append(" " + page + "/" + allPages + " ").reset().color(ChatColor.GOLD);

        navigation.append("[>]").color(allPages > page ? ChatColor.AQUA : ChatColor.GRAY);

        if (allPages > page) {
            navigation.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ignorelist " + (page + 1)));
        }

        navigation.append(" ]").reset().color(ChatColor.GOLD);

        player.spigot().sendMessage(navigation.create());

        int i = 0;

        for (OfflinePlayer ignored : map.keySet()) {
            if (i >= minValue && i < maxValue) {
                ComponentBuilder playerBuilder = new ComponentBuilder(ChatColor.stripColor(ignored.getName()));

                playerBuilder.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(player.getUniqueId().toString())));

                playerBuilder.append(" ").reset();

                playerBuilder.append("[");

                playerBuilder.color(ChatColor.GRAY);

                if (map.get(ignored) == IgnoreTool.IgnoreType.HARD) {
                    playerBuilder.append("hard");

                    playerBuilder.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to remove the permanent ignore").color(ChatColor.GOLD).create()));
                    playerBuilder.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ignorehard " + ChatColor.stripColor(ignored.getName())));
                } else {
                    playerBuilder.append("soft");
                    playerBuilder.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to remove the temporary ignore").color(ChatColor.GOLD).create()));
                    playerBuilder.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ignore " + ChatColor.stripColor(ignored.getName())));
                }

                playerBuilder.color(ChatColor.GOLD);

                playerBuilder.append("]").reset();

                playerBuilder.color(ChatColor.GRAY);

                player.spigot().sendMessage(playerBuilder.create());
            }

            i++;
        }
    }
}
