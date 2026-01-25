package net.pistonmaster.pistonchat.utils;

import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;

import java.util.List;

/**
 * Utility class for player-related operations.
 */
public final class PlayerUtils {
  private PlayerUtils() {
  }

  /**
   * Check if a player is vanished based on their metadata.
   *
   * @param player The player to check
   * @return true if the player has a "vanished" metadata value set to true
   */
  public static boolean isVanished(Player player) {
    return isVanished(player.getMetadata("vanished"));
  }

  /**
   * Check if a list of metadata values indicates vanished state.
   * This is separated for easier testing.
   *
   * @param metadataValues The list of metadata values to check
   * @return true if any metadata value is true
   */
  public static boolean isVanished(List<MetadataValue> metadataValues) {
    for (MetadataValue meta : metadataValues) {
      if (meta.asBoolean()) {
        return true;
      }
    }
    return false;
  }
}
