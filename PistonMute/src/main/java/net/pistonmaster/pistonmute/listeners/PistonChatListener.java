package net.pistonmaster.pistonmute.listeners;

import lombok.RequiredArgsConstructor;
import net.pistonmaster.pistonchat.PistonChat;
import net.pistonmaster.pistonchat.api.PistonChatEvent;
import net.pistonmaster.pistonchat.api.PistonWhisperEvent;
import net.pistonmaster.pistonmute.PistonMute;
import net.pistonmaster.pistonmute.utils.StorageTool;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

@RequiredArgsConstructor
public final class PistonChatListener implements Listener {
    private final PistonMute plugin;
    private final PistonChat pistonChat = PistonChat.getPlugin(PistonChat.class);

    @EventHandler
    public void onChat(PistonChatEvent event) {
        if (StorageTool.isMuted(event.getPlayer())) {
            if (plugin.getConfig().getBoolean("shadowMute")) {
                pistonChat.getCommonTool().sendChatMessage(event.getPlayer(), event.getMessage(), event.getPlayer());
            }

            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onChat(PistonWhisperEvent event) {
        if (event.getSender() == event.getReceiver()) return;

        if (event.getSender() instanceof Player && StorageTool.isMuted((Player) event.getSender())) {
            if (plugin.getConfig().getBoolean("shadowMute")) {
                pistonChat.getCommonTool().sendSender(event.getSender(), event.getMessage(), event.getReceiver());
            }

            event.setCancelled(true);
        }
    }
}
