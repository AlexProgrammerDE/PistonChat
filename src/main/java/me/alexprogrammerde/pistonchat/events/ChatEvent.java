package me.alexprogrammerde.pistonchat.events;

import me.alexprogrammerde.pistonchat.utils.ConfigTool;
import me.alexprogrammerde.pistonchat.utils.TempDataTool;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatEvent implements Listener {
    // Mute plugins should have a lower priority to work!
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent event) {
        if (!event.isCancelled()) {
            Player chatter = event.getPlayer();

            event.setCancelled(true);

            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!ConfigTool.isIgnored(chatter, player) && TempDataTool.isChatEnabled(player)) {
                    ComponentBuilder builder = new ComponentBuilder("<" + chatter.getDisplayName() + ChatColor.RESET + "> ");

                    if (player.hasPermission("pistonchat.playernamereply")) {
                        builder.event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + ChatColor.stripColor(chatter.getDisplayName()) + " "));
                        builder.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                new ComponentBuilder("Message ")
                                .color(ChatColor.DARK_AQUA)
                                .append(ChatColor.stripColor(chatter.getDisplayName()))
                                .color(ChatColor.GOLD)
                                .create()
                        ));
                    }

                    builder.append(event.getMessage());

                    if (player.hasPermission("pistonchat.greenprefix") && event.getMessage().startsWith(">")) {
                        builder.color(ChatColor.GREEN);
                    }

                    player.spigot().sendMessage(builder.create());
                }
            }
        }
    }
}
