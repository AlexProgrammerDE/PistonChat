package net.pistonmaster.pistonchat.events;

import lombok.RequiredArgsConstructor;
import net.pistonmaster.pistonchat.PistonChat;
import net.pistonmaster.pistonchat.api.PistonChatEvent;
import net.pistonmaster.pistonchat.api.PistonChatReceiveEvent;
import net.pistonmaster.pistonchat.tools.CommonTool;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Set;

@RequiredArgsConstructor
public class ChatEvent implements Listener {
    private final PistonChat plugin;

    // Mute plugins should have a lower priority to work!
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        Player chatter = event.getPlayer();
        PistonChatEvent pistonChatEvent = new PistonChatEvent(chatter, event.getMessage(), event.isAsynchronous());

        Set<Player> recipients = Set.copyOf(event.getRecipients());
        event.getRecipients().clear();

        Bukkit.getPluginManager().callEvent(pistonChatEvent);

        event.setCancelled(pistonChatEvent.isCancelled());

        if (pistonChatEvent.isCancelled()) {
            return;
        }

        if (!plugin.getTempDataTool().isChatEnabled(chatter)) {
            plugin.getCommonTool().sendLanguageMessage(plugin.getAdventure(), chatter, "chatisoff");
            event.setCancelled(true);
            return;
        }

        for (Player receiver : recipients) {
            if (plugin.getIgnoreTool().isIgnored(chatter, receiver) || !plugin.getTempDataTool().isChatEnabled(receiver)) {
                continue;
            }

            PistonChatReceiveEvent perPlayerEvent = new PistonChatReceiveEvent(chatter, receiver, pistonChatEvent.getMessage(), event.isAsynchronous());

            Bukkit.getPluginManager().callEvent(perPlayerEvent);

            if (perPlayerEvent.isCancelled()) {
                continue;
            }

            plugin.getCommonTool().sendChatMessage(chatter, perPlayerEvent.getMessage(), receiver);
        }
    }
}
