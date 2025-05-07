package net.pistonmaster.pistonchat.events;

import lombok.RequiredArgsConstructor;
import net.pistonmaster.pistonchat.PistonChat;
import net.pistonmaster.pistonchat.api.PistonChatEvent;
import net.pistonmaster.pistonchat.api.PistonChatReceiveEvent;
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

        plugin.getServer().getPluginManager().callEvent(pistonChatEvent);

        event.setCancelled(pistonChatEvent.isCancelled());

        if (pistonChatEvent.isCancelled()) {
            return;
        }

        plugin.runAsync(() -> {
            if (!plugin.getTempDataTool().isChatEnabled(chatter)) {
                plugin.getCommonTool().sendLanguageMessage(chatter, "chatisoff");
                event.setCancelled(true);
                return;
            }

            for (Player receiver : recipients) {
                if (plugin.getIgnoreTool().isIgnored(chatter, receiver)
                    || !plugin.getTempDataTool().isChatEnabled(receiver)) {
                    continue;
                }

                PistonChatReceiveEvent perPlayerEvent = new PistonChatReceiveEvent(chatter, receiver, pistonChatEvent.getMessage(), event.isAsynchronous());

                plugin.getServer().getPluginManager().callEvent(perPlayerEvent);

                if (perPlayerEvent.isCancelled()) {
                    continue;
                }

                plugin.getCommonTool().sendChatMessage(chatter, perPlayerEvent.getMessage(), receiver);
            }
        });
    }
}
