package net.pistonmaster.pistonfilter.listeners;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import net.md_5.bungee.api.ChatColor;
import net.pistonmaster.pistonfilter.PistonFilter;
import net.pistonmaster.pistonfilter.config.PistonFilterConfig;
import net.pistonmaster.pistonfilter.utils.FilterLogic;
import net.pistonmaster.pistonfilter.utils.MessageInfo;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.time.Instant;
import java.util.List;
import java.util.Locale;

/**
 * Listener for filtering commands that contain chat-like messages.
 * Filters commands like /me, /say, /msg, /tell, /whisper, /reply, etc.
 */
public class CommandListener implements Listener {
  private final PistonFilter plugin;

  @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Plugin instance is intentionally shared")
  public CommandListener(PistonFilter plugin) {
    this.plugin = plugin;
  }

  @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
  public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
    PistonFilterConfig config = plugin.getPluginConfig();

    if (!config.commands.enabled) {
      return;
    }

    Player player = event.getPlayer();
    if (player.hasPermission("pistonfilter.bypass") || player.hasPermission("pistonfilter.bypass.commands")) {
      return;
    }

    String message = event.getMessage();
    if (message.length() <= 1) {
      return;
    }

    // Remove the leading slash
    String commandString = message.substring(1);

    // Split into command and arguments
    String[] parts = commandString.split("\\s+", 2);
    if (parts.length == 0) {
      return;
    }

    String rawCommand = parts[0].toLowerCase(Locale.ROOT);

    // Strip plugin prefix if present (e.g., "essentials:me" -> "me")
    String command;
    if (rawCommand.contains(":")) {
      command = rawCommand.substring(rawCommand.indexOf(':') + 1);
    } else {
      command = rawCommand;
    }

    // Check if this command should be filtered
    List<String> filteredCommands = config.commands.filtered;
    boolean shouldFilter = false;
    for (String filteredCommand : filteredCommands) {
      if (command.equalsIgnoreCase(filteredCommand)) {
        shouldFilter = true;
        break;
      }
    }

    if (!shouldFilter) {
      return;
    }

    // Get the message part of the command
    if (parts.length < 2) {
      return; // No message content to filter
    }

    String messageContent = parts[1];

    // For commands with a target player, skip the first argument
    if (config.commands.withTarget.stream().anyMatch(cmd -> cmd.equalsIgnoreCase(command))) {
      String[] messageParts = messageContent.split("\\s+", 2);
      if (messageParts.length < 2) {
        return; // No message after the target player
      }
      messageContent = messageParts[1];
    }

    // Now filter the message content
    String filterResult = checkContent(messageContent, config);
    if (filterResult != null) {
      if (config.verbose) {
        plugin.getLogger().info(ChatColor.RED + "[CommandFilter] <" + player.getName() + "> /" + command + ": " + messageContent + " (" + filterResult + ")");
      }

      event.setCancelled(true);
      player.sendMessage(ChatColor.RED + "Your message contains filtered content.");
    }
  }

  /**
   * Check content for filter violations.
   *
   * @param content the content to check
   * @param config  the plugin configuration
   * @return the reason for blocking, or null if allowed
   */
  private String checkContent(String content, PistonFilterConfig config) {
    MessageInfo messageInfo = MessageInfo.of(Instant.now(), content);

    // Check banned text
    String bannedText = FilterLogic.findBannedText(messageInfo.getStrippedMessage(), config.content.bannedPatterns, config.content.bannedTextMatchRatio);
    if (bannedText != null) {
      return "Contains banned text: " + bannedText;
    }

    // Check banned regex patterns
    String bannedRegex = FilterLogic.findBannedRegex(messageInfo.getStrippedMessage(), config.content.bannedRegexPatterns);
    if (bannedRegex != null) {
      return "Matches banned pattern: " + bannedRegex;
    }

    // Check word length
    for (String word : messageInfo.getWords()) {
      if (FilterLogic.isWordTooLong(word, config.content.maxWordLength)) {
        return "Contains a word that is too long: " + word;
      }
      if (FilterLogic.hasInvalidSeparators(word, config.content.maxSeparatedNumbers)) {
        return "Has a word with invalid separators: " + word;
      }
    }

    // Check words with numbers
    int wordsWithNumbers = FilterLogic.countWordsWithNumbers(messageInfo.getWords());
    if (wordsWithNumbers > config.content.maxWordsWithNumbers) {
      return "Used " + wordsWithNumbers + " words with numbers";
    }

    // Check Unicode filtering
    if (config.unicode.enabled) {
      List<FilterLogic.UnicodeRange> allowedRanges = FilterLogic.parseUnicodeRanges(config.unicode.allowedRanges);
      FilterLogic.UnicodeFilterResult unicodeResult = FilterLogic.checkUnicodeCharacters(
          content, config.unicode.blockNonAscii, config.unicode.blockMathAlphanumeric, config.unicode.blockHackedClientFonts, allowedRanges);
      if (unicodeResult.isBlocked()) {
        return unicodeResult.getReason();
      }
    }

    return null;
  }
}
