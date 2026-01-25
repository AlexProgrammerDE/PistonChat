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
import org.bukkit.event.block.SignChangeEvent;

import java.time.Instant;
import java.util.List;

/**
 * Listener for filtering content on signs.
 */
public class SignListener implements Listener {
  private final PistonFilter plugin;

  @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Plugin instance is intentionally shared")
  public SignListener(PistonFilter plugin) {
    this.plugin = plugin;
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onSignChange(SignChangeEvent event) {
    PistonFilterConfig config = plugin.getPluginConfig();

    if (!config.filterSigns) {
      return;
    }

    Player player = event.getPlayer();
    if (player.hasPermission("pistonfilter.bypass") || player.hasPermission("pistonfilter.bypass.signs")) {
      return;
    }

    String[] lines = event.getLines();
    boolean foundViolation = false;

    for (int i = 0; i < lines.length; i++) {
      String line = lines[i];
      if (line == null || line.trim().isEmpty()) {
        continue;
      }

      String filterResult = checkLine(line, config);
      if (filterResult != null) {
        foundViolation = true;

        if (config.verbose) {
          plugin.getLogger().info(ChatColor.RED + "[SignFilter] <" + player.getName() + "> Line " + (i + 1) + ": " + line + " (" + filterResult + ")");
        }

        if (config.cancelSignOnFilter) {
          event.setCancelled(true);
          player.sendMessage(ChatColor.RED + "Your sign contains filtered content.");
          return;
        } else {
          // Clear the offending line
          event.setLine(i, "");
        }
      }
    }

    if (foundViolation && !config.cancelSignOnFilter) {
      player.sendMessage(ChatColor.RED + "Some text on your sign was filtered.");
    }
  }

  /**
   * Check a single line for filter violations.
   *
   * @param line   the line to check
   * @param config the plugin configuration
   * @return the reason for blocking, or null if allowed
   */
  private String checkLine(String line, PistonFilterConfig config) {
    MessageInfo messageInfo = MessageInfo.of(Instant.now(), line);

    // Check banned text
    String bannedText = FilterLogic.findBannedText(messageInfo.getStrippedMessage(), config.bannedText, config.bannedTextPartialRatio);
    if (bannedText != null) {
      return "Contains banned text: " + bannedText;
    }

    // Check word length
    for (String word : messageInfo.getWords()) {
      if (FilterLogic.isWordTooLong(word, config.maxWordLength)) {
        return "Contains a word that is too long: " + word;
      }
      if (FilterLogic.hasInvalidSeparators(word, config.maxSeparatedNumbers)) {
        return "Has a word with invalid separators: " + word;
      }
    }

    // Check Unicode filtering
    if (config.filterUnicode) {
      List<FilterLogic.UnicodeRange> allowedRanges = FilterLogic.parseUnicodeRanges(config.allowedUnicodeRanges);
      FilterLogic.UnicodeFilterResult unicodeResult = FilterLogic.checkUnicodeCharacters(
          line, config.blockNonAscii, config.blockMathAlphanumeric, config.blockHackedClientFonts, allowedRanges);
      if (unicodeResult.isBlocked()) {
        return unicodeResult.getReason();
      }
    }

    return null;
  }
}
