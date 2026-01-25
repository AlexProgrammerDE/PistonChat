package net.pistonmaster.pistonmute.listeners;

import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import net.pistonmaster.pistonmute.PistonMute;
import net.pistonmaster.pistonmute.config.PistonMuteConfig;
import net.pistonmaster.pistonmute.utils.StorageTool;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.List;
import java.util.Locale;

/**
 * Listener that blocks muted players from using chat-related commands.
 */
@RequiredArgsConstructor
public final class CommandBlockListener implements Listener {
  private final PistonMute plugin;

  @EventHandler(priority = EventPriority.LOWEST)
  public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
    if (event.isCancelled()) {
      return;
    }

    PistonMuteConfig config = plugin.getPluginConfig();

    // Check if command blocking is enabled
    if (!config.blockCommands) {
      return;
    }

    Player player = event.getPlayer();

    // Check if player is muted
    if (!StorageTool.isMuted(player)) {
      return;
    }

    // Check if player has bypass permission
    if (player.hasPermission("pistonmute.bypass.commands")) {
      return;
    }

    // Extract the command name from the message
    String commandLine = event.getMessage().substring(1); // Remove leading /
    String commandName = extractCommandName(commandLine);

    // Check if this command is blocked
    if (isCommandBlocked(commandName, config.blockedCommands)) {
      event.setCancelled(true);

      // Send blocked message to player
      String message = ChatColor.translateAlternateColorCodes('&', config.blockedCommandMessage);
      player.sendMessage(message);
    }
  }

  /**
   * Extract the command name from a command line (handles plugin:command format).
   *
   * @param commandLine The command line without the leading slash.
   * @return The command name in lowercase.
   */
  private String extractCommandName(String commandLine) {
    // Split by space to get just the command part
    String command = commandLine.split(" ")[0].toLowerCase(Locale.ROOT);

    // Handle plugin:command format (e.g., "essentials:msg")
    if (command.contains(":")) {
      command = command.substring(command.indexOf(':') + 1);
    }

    return command;
  }

  /**
   * Check if a command is in the blocked list.
   *
   * @param commandName The command name to check.
   * @param blockedCommands The list of blocked commands.
   * @return true if the command should be blocked.
   */
  private boolean isCommandBlocked(String commandName, List<String> blockedCommands) {
    for (String blocked : blockedCommands) {
      if (blocked.equalsIgnoreCase(commandName)) {
        return true;
      }
    }
    return false;
  }
}
