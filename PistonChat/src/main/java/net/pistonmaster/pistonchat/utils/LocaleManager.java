package net.pistonmaster.pistonchat.utils;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import net.pistonmaster.pistonchat.config.PistonChatConfig;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * Manages per-player locale translations.
 * Loads translation files from plugins/PistonChat/lang/<locale>.yml
 */
public final class LocaleManager {
  private final Logger logger;
  private final File langFolder;
  private final Supplier<PistonChatConfig> configSupplier;
  private final Map<String, YamlConfiguration> localeCache = new ConcurrentHashMap<>();

  @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Logger is intentionally shared and immutable in practice")
  public LocaleManager(Logger logger, File langFolder, Supplier<PistonChatConfig> configSupplier) {
    this.logger = logger;
    this.langFolder = langFolder;
    this.configSupplier = configSupplier;
    loadLocales();
  }

  /**
   * Load all locale files from the lang folder.
   */
  public void loadLocales() {
    localeCache.clear();

    if (!langFolder.exists()) {
      if (!langFolder.mkdirs()) {
        logger.warning("Failed to create lang folder: " + langFolder.getAbsolutePath());
      }
      return;
    }

    File[] files = langFolder.listFiles((dir, name) -> name.endsWith(".yml"));
    if (files == null) {
      return;
    }

    for (File file : files) {
      String localeName = file.getName().replace(".yml", "");
      YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
      localeCache.put(localeName.toLowerCase(Locale.ROOT), config);
      logger.info("Loaded locale: " + localeName);
    }
  }

  /**
   * Get a localized message for a player.
   *
   * @param player     the player (or null for default)
   * @param messageKey the message key
   * @return the localized message, or null if not found
   */
  public String getLocalizedMessage(Player player, String messageKey) {
    if (player == null || !configSupplier.get().perIssuerLocale) {
      return null;
    }

    String playerLocale = player.getLocale();
    if (playerLocale == null || playerLocale.isEmpty()) {
      return null;
    }

    // Try exact locale match first (e.g., "de_de")
    String normalizedLocale = playerLocale.toLowerCase(Locale.ROOT);
    YamlConfiguration localeConfig = localeCache.get(normalizedLocale);

    // Try language-only match (e.g., "de" from "de_de")
    if (localeConfig == null && normalizedLocale.contains("_")) {
      String languageOnly = normalizedLocale.split("_")[0];
      localeConfig = localeCache.get(languageOnly);
    }

    if (localeConfig == null) {
      return null;
    }

    return localeConfig.getString(messageKey);
  }

  /**
   * Check if any locales are loaded.
   *
   * @return true if at least one locale is loaded
   */
  public boolean hasLocales() {
    return !localeCache.isEmpty();
  }

  /**
   * Get the number of loaded locales.
   *
   * @return the number of loaded locales
   */
  public int getLocaleCount() {
    return localeCache.size();
  }
}
