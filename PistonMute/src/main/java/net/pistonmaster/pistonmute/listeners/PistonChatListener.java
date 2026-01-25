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
    Player player = event.getPlayer();
    if (StorageTool.isMuted(player)) {
      if (plugin.getPluginConfig().shadowMute) {
        pistonChat.getCommonTool().sendChatMessage(player, event.getMessage(), player);
      } else {
        // Show mute reason if not shadow mute
        showMuteReason(player);
      }

      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onChat(PistonWhisperEvent event) {
    if (event.getSender() == event.getReceiver()) {
      return;
    }

    if (event.getSender() instanceof Player) {
      Player sender = (Player) event.getSender();
      if (StorageTool.isMuted(sender)) {
        if (plugin.getPluginConfig().shadowMute) {
          pistonChat.getCommonTool().sendSender(sender, event.getMessage(), event.getReceiver());
        } else {
          // Show mute reason if not shadow mute
          showMuteReason(sender);
        }

        event.setCancelled(true);
      }
    }
  }

  /**
   * Show the mute reason to the player if configured to do so.
   */
  private void showMuteReason(Player player) {
    if (!plugin.getPluginConfig().muteNotification.showReason) {
      return;
    }

    String reason = StorageTool.getMuteReason(player.getUniqueId());
    if (reason != null && !reason.isEmpty()) {
      String muteNotify = plugin.getPluginConfig().muteNotification.joinMessage
          .replace("&", "\u00A7");
      String reasonFormat = plugin.getPluginConfig().muteNotification.reasonFormat
          .replace("%reason%", reason)
          .replace("&", "\u00A7");
      player.sendMessage(muteNotify);
      player.sendMessage(reasonFormat);
    } else {
      String muteNotify = plugin.getPluginConfig().muteNotification.joinMessage
          .replace("&", "\u00A7");
      player.sendMessage(muteNotify);
    }
  }
}
