package net.pistonmaster.pistonchat.events;

import lombok.RequiredArgsConstructor;
import net.pistonmaster.pistonchat.PistonChat;
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

        Set<Player> recipients = Set.copyOf(event.getRecipients());
        event.getRecipients().clear();

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

            plugin.getCommonTool().sendChatMessage(chatter, event.getMessage(), receiver);
        }
    }
}
