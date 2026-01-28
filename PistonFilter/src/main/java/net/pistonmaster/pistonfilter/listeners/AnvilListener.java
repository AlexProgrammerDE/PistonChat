package net.pistonmaster.pistonfilter.listeners;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import net.md_5.bungee.api.ChatColor;
import net.pistonmaster.pistonfilter.PistonFilter;
import net.pistonmaster.pistonfilter.config.PistonFilterConfig;
import net.pistonmaster.pistonfilter.utils.FilterLogic;
import net.pistonmaster.pistonfilter.utils.MessageInfo;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.Instant;
import java.util.List;

/**
 * Listener for filtering anvil rename text.
 */
public class AnvilListener implements Listener {
  private final PistonFilter plugin;

  @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Plugin instance is intentionally shared")
  public AnvilListener(PistonFilter plugin) {
    this.plugin = plugin;
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onPrepareAnvil(PrepareAnvilEvent event) {
    PistonFilterConfig config = plugin.getPluginConfig();

    if (!config.anvil.enabled) {
      return;
    }

    ItemStack result = event.getResult();
    if (result == null || !result.hasItemMeta()) {
      return;
    }

    ItemMeta meta = result.getItemMeta();
    if (meta == null || !meta.hasDisplayName()) {
      return;
    }

    // Check if any viewer has bypass permission
    List<HumanEntity> viewers = event.getViewers();
    for (HumanEntity viewer : viewers) {
      if (viewer.hasPermission("pistonfilter.bypass") || viewer.hasPermission("pistonfilter.bypass.anvil")) {
        return;
      }
    }

    String displayName = meta.getDisplayName();
    String filterResult = checkContent(displayName, config);

    if (filterResult != null) {
      if (config.verbose) {
        String playerNames = viewers.stream()
            .map(HumanEntity::getName)
            .reduce((a, b) -> a + ", " + b)
            .orElse("Unknown");
        plugin.getLogger().info(ChatColor.RED + "[AnvilFilter] <" + playerNames + "> Rename: " + displayName + " (" + filterResult + ")");
      }

      if (config.anvil.cancelOnFilter) {
        // Clear the result to prevent the rename
        event.setResult(null);
      }
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onInventoryClick(InventoryClickEvent event) {
    PistonFilterConfig config = plugin.getPluginConfig();

    if (!config.anvil.enabled) {
      return;
    }

    if (!(event.getInventory() instanceof AnvilInventory)) {
      return;
    }

    // Only check when clicking the result slot
    if (event.getSlotType() != InventoryType.SlotType.RESULT) {
      return;
    }

    if (!(event.getWhoClicked() instanceof Player player)) {
      return;
    }

    if (player.hasPermission("pistonfilter.bypass") || player.hasPermission("pistonfilter.bypass.anvil")) {
      return;
    }

    ItemStack result = event.getCurrentItem();
    if (result == null || !result.hasItemMeta()) {
      return;
    }

    ItemMeta meta = result.getItemMeta();
    if (meta == null || !meta.hasDisplayName()) {
      return;
    }

    String displayName = meta.getDisplayName();
    String filterResult = checkContent(displayName, config);

    if (filterResult != null) {
      if (config.verbose) {
        plugin.getLogger().info(ChatColor.RED + "[AnvilFilter] <" + player.getName() + "> Attempted rename: " + displayName + " (" + filterResult + ")");
      }

      if (config.anvil.cancelOnFilter) {
        event.setCancelled(true);
        player.sendMessage(ChatColor.RED + "That item name contains filtered content.");
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
