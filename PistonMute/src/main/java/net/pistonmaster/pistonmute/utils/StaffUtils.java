package net.pistonmaster.pistonmute.utils;

import net.pistonmaster.pistonmute.PistonMute;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Utility class for staff hierarchy and immunity checks.
 */
public final class StaffUtils {
  private static final AtomicReference<PistonMute> PLUGIN = new AtomicReference<>();
  private static final String IMMUNE_PERMISSION = "pistonmute.immune";
  private static final String WEIGHT_PERMISSION_PREFIX = "pistonmute.weight.";

  private StaffUtils() {
  }

  /**
   * Initialize the staff utilities.
   *
   * @param plugin The plugin instance.
   */
  public static void setup(PistonMute plugin) {
    if (plugin == null) {
      return;
    }

    PLUGIN.compareAndSet(null, plugin);
  }

  private static PistonMute plugin() {
    return Objects.requireNonNull(PLUGIN.get(), "StaffUtils has not been initialized");
  }

  /**
   * Check if a staff member can target another player for punishment.
   *
   * @param staff  The staff member attempting the action.
   * @param target The target player.
   * @return true if the staff can target this player, false otherwise.
   */
  public static boolean canTarget(CommandSender staff, Player target) {
    PistonMute pluginInstance = plugin();

    // If hierarchy is disabled, anyone can target anyone
    if (!pluginInstance.getPluginConfig().staff.hierarchyEnabled) {
      return true;
    }

    String mode = pluginInstance.getPluginConfig().staff.hierarchyMode.toLowerCase();

    return switch (mode) {
      case "immune" -> !target.hasPermission(IMMUNE_PERMISSION);
      case "weight" -> checkWeightHierarchy(staff, target);
      default -> {
        pluginInstance.getLogger().warning("Unknown hierarchy mode: " + mode + ". Defaulting to allowing action.");
        yield true;
      }
    };
  }

  /**
   * Check weight-based hierarchy.
   * Staff with higher weight can target those with lower weight.
   * Console always has maximum weight.
   *
   * @param staff  The staff member.
   * @param target The target player.
   * @return true if staff's weight > target's weight.
   */
  private static boolean checkWeightHierarchy(CommandSender staff, Player target) {
    int staffWeight = getWeight(staff);
    int targetWeight = getWeight(target);

    return staffWeight > targetWeight;
  }

  /**
   * Get the weight of a command sender.
   * Console has Integer.MAX_VALUE weight.
   * Players get their weight from pistonmute.weight.<number> permission.
   * Default weight is 0 if no permission is found.
   *
   * @param sender The command sender.
   * @return The sender's weight.
   */
  public static int getWeight(CommandSender sender) {
    // Console always has maximum weight
    if (!(sender instanceof Player player)) {
      return Integer.MAX_VALUE;
    }

    int maxWeight = 0;

    // Check all permissions to find weight permissions
    for (PermissionAttachmentInfo perm : player.getEffectivePermissions()) {
      String permission = perm.getPermission();

      if (permission.startsWith(WEIGHT_PERMISSION_PREFIX) && perm.getValue()) {
        try {
          String weightStr = permission.substring(WEIGHT_PERMISSION_PREFIX.length());
          int weight = Integer.parseInt(weightStr);
          maxWeight = Math.max(maxWeight, weight);
        } catch (NumberFormatException ignored) {
          // Invalid weight format, skip
        }
      }
    }

    return maxWeight;
  }

  /**
   * Check if a player has immunity from being targeted.
   *
   * @param player The player to check.
   * @return true if the player is immune.
   */
  public static boolean isImmune(Player player) {
    return player.hasPermission(IMMUNE_PERMISSION);
  }

  /**
   * Get the reason why a staff member cannot target a player.
   *
   * @param target The target player.
   * @return A human-readable reason string.
   */
  public static String getCannotTargetReason(Player target) {
    PistonMute pluginInstance = plugin();
    String mode = pluginInstance.getPluginConfig().staff.hierarchyMode.toLowerCase();

    return switch (mode) {
      case "immune" -> target.getName() + " is immune to punishment!";
      case "weight" -> target.getName() + " has a higher or equal staff rank!";
      default -> target.getName() + " cannot be targeted!";
    };
  }
}
