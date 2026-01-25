package net.pistonmaster.pistonmute.listeners;

import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import net.pistonmaster.pistonmute.PistonMute;
import net.pistonmaster.pistonmute.config.PistonMuteConfig;
import net.pistonmaster.pistonmute.utils.StorageTool;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Set;
import java.util.UUID;

/**
 * Listener for handling player join events.
 * Handles mute notifications on join and IP tracking for alt detection.
 */
@RequiredArgsConstructor
public final class PlayerJoinListener implements Listener {
  private final PistonMute plugin;

  @EventHandler(priority = EventPriority.MONITOR)
  public void onPlayerJoin(PlayerJoinEvent event) {
    Player player = event.getPlayer();
    PistonMuteConfig config = plugin.getPluginConfig();

    // Track player IP for alt detection
    if (config.altDetection.enabled) {
      StorageTool.trackPlayerIP(player);

      // Check if this player might be an alt of a muted player
      checkForMutedAlts(player);
    }

    // Notify player if they are muted
    if (config.muteNotification.notifyOnJoin && StorageTool.isMuted(player)) {
      notifyMutedPlayer(player);
    }
  }

  /**
   * Check if the joining player is an alt of a muted player and notify staff.
   */
  private void checkForMutedAlts(Player player) {
    PistonMuteConfig config = plugin.getPluginConfig();

    // Don't notify if the player themselves is muted
    if (StorageTool.isMuted(player)) {
      return;
    }

    Set<UUID> mutedAlts = StorageTool.getMutedAlts(player);
    if (mutedAlts.isEmpty()) {
      return;
    }

    // Auto-mute this player if enabled
    if (config.altDetection.autoMuteAlts) {
      StorageTool.hardMutePlayer(player.getUniqueId(), "Auto-muted: alt of muted player");
      notifyMutedPlayer(player);
    }

    // Notify staff about the alt
    if (config.altDetection.notifyStaffOnJoin) {
      // Get one of the muted alt names for the notification
      UUID mutedAltUuid = mutedAlts.iterator().next();
      String mutedAltName = StorageTool.getStoredPlayerName(mutedAltUuid);
      if (mutedAltName == null) {
        mutedAltName = mutedAltUuid.toString().substring(0, 8);
      }

      String ip = StorageTool.getStoredIP(player.getUniqueId());
      if (ip == null) {
        ip = "unknown";
      }

      String message = config.altDetection.joinNotifyMessage
          .replace("%player%", player.getName())
          .replace("%alt%", mutedAltName)
          .replace("%ip%", ip);

      message = ChatColor.translateAlternateColorCodes('&', message);

      for (Player staff : Bukkit.getOnlinePlayers()) {
        if (staff.hasPermission("pistonmute.notify")) {
          staff.sendMessage(message);
        }
      }
    }
  }

  /**
   * Notify a player that they are muted.
   */
  private void notifyMutedPlayer(Player player) {
    PistonMuteConfig config = plugin.getPluginConfig();

    // Send main mute notification
    String message = ChatColor.translateAlternateColorCodes('&', config.muteNotification.joinMessage);
    player.sendMessage(message);

    // Show reason if enabled and available
    if (config.muteNotification.showReason) {
      String reason = StorageTool.getMuteReason(player.getUniqueId());
      if (reason != null && !reason.isEmpty()) {
        String reasonMessage = config.muteNotification.reasonFormat.replace("%reason%", reason);
        reasonMessage = ChatColor.translateAlternateColorCodes('&', reasonMessage);
        player.sendMessage(reasonMessage);
      }
    }
  }
}
