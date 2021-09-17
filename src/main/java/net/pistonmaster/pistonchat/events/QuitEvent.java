package net.pistonmaster.pistonchat.events;

import lombok.RequiredArgsConstructor;
import net.pistonmaster.pistonchat.PistonChat;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

@RequiredArgsConstructor
public class QuitEvent implements Listener {
    private final PistonChat plugin;

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getTempDataTool().onQuit(event.getPlayer());
    }
}
