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
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.inventory.meta.BookMeta;

import java.time.Instant;
import java.util.List;

/**
 * Listener for filtering content in books.
 */
public class BookListener implements Listener {
  private final PistonFilter plugin;

  @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Plugin instance is intentionally shared")
  public BookListener(PistonFilter plugin) {
    this.plugin = plugin;
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onBookEdit(PlayerEditBookEvent event) {
    PistonFilterConfig config = plugin.getPluginConfig();

    if (!config.books.enabled) {
      return;
    }

    Player player = event.getPlayer();
    if (player.hasPermission("pistonfilter.bypass") || player.hasPermission("pistonfilter.bypass.books")) {
      return;
    }

    BookMeta newBookMeta = event.getNewBookMeta();

    // Check the title if the book is being signed
    if (event.isSigning() && newBookMeta.hasTitle()) {
      String title = newBookMeta.getTitle();
      if (title != null) {
        String filterResult = checkContent(title, config);
        if (filterResult != null) {
          if (config.verbose) {
            plugin.getLogger().info(ChatColor.RED + "[BookFilter] <" + player.getName() + "> Title: " + title + " (" + filterResult + ")");
          }
          if (config.books.cancelOnFilter) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "Your book title contains filtered content.");
            return;
          }
        }
      }
    }

    // Check all pages
    List<String> pages = newBookMeta.getPages();
    for (int i = 0; i < pages.size(); i++) {
      String page = pages.get(i);
      if (page == null || page.trim().isEmpty()) {
        continue;
      }

      // Split page into lines and check each
      String[] lines = page.split("\n");
      for (String line : lines) {
        if (line.trim().isEmpty()) {
          continue;
        }

        String filterResult = checkContent(line, config);
        if (filterResult != null) {
          if (config.verbose) {
            plugin.getLogger().info(ChatColor.RED + "[BookFilter] <" + player.getName() + "> Page " + (i + 1) + ": " + line + " (" + filterResult + ")");
          }
          if (config.books.cancelOnFilter) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "Your book contains filtered content on page " + (i + 1) + ".");
            return;
          }
        }
      }
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

    // Check word length
    for (String word : messageInfo.getWords()) {
      if (FilterLogic.isWordTooLong(word, config.content.maxWordLength)) {
        return "Contains a word that is too long: " + word;
      }
      if (FilterLogic.hasInvalidSeparators(word, config.content.maxSeparatedNumbers)) {
        return "Has a word with invalid separators: " + word;
      }
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
