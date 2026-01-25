package net.pistonmaster.pistonmute.utils;

import net.pistonmaster.pistonmute.PistonMute;
import net.pistonmaster.pistonmute.data.PunishmentRecord;
import net.pistonmaster.pistonmute.data.PunishmentType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Storage utility for managing punishment history.
 */
public final class PunishmentHistoryStorage {
  private static final AtomicReference<PistonMute> PLUGIN = new AtomicReference<>();
  private static final Object LOCK = new Object();
  private static FileConfiguration historyConfig;
  private static File historyFile;

  private PunishmentHistoryStorage() {
  }

  /**
   * Record a punishment in history.
   *
   * @param type        The type of punishment
   * @param playerUuid  The UUID of the punished player
   * @param issuerUuid  The UUID of the issuer (null for console)
   * @param issuerName  The name of the issuer
   * @param reason      The reason for the punishment
   * @param expiresAt   When the punishment expires (null for permanent)
   * @param permanent   Whether the punishment is permanent
   * @param template    The escalation template used (null if none)
   * @return The created punishment record
   */
  public static PunishmentRecord recordPunishment(PunishmentType type, UUID playerUuid, UUID issuerUuid,
                                                   String issuerName, String reason, Instant expiresAt,
                                                   boolean permanent, String template) {
    synchronized (LOCK) {
      ensureLoaded();

      String recordId = UUID.randomUUID().toString().substring(0, 8);
      Instant issuedAt = Instant.now();

      String playerKey = playerUuid.toString();
      String recordPath = playerKey + "." + recordId;

      historyConfig.set(recordPath + ".type", type.name());
      historyConfig.set(recordPath + ".issuerUuid", issuerUuid != null ? issuerUuid.toString() : null);
      historyConfig.set(recordPath + ".issuerName", issuerName);
      historyConfig.set(recordPath + ".reason", reason);
      historyConfig.set(recordPath + ".issuedAt", issuedAt.toString());
      historyConfig.set(recordPath + ".expiresAt", expiresAt != null ? expiresAt.toString() : null);
      historyConfig.set(recordPath + ".permanent", permanent);
      historyConfig.set(recordPath + ".template", template);

      saveData();

      return new PunishmentRecord(recordId, type, playerUuid, issuerUuid, issuerName, reason,
          issuedAt, expiresAt, permanent, template);
    }
  }

  /**
   * Get all punishment records for a player.
   *
   * @param playerUuid The UUID of the player
   * @return List of punishment records, sorted by issued date (newest first)
   */
  public static List<PunishmentRecord> getHistory(UUID playerUuid) {
    synchronized (LOCK) {
      ensureLoaded();

      String playerKey = playerUuid.toString();
      ConfigurationSection playerSection = historyConfig.getConfigurationSection(playerKey);

      if (playerSection == null) {
        return Collections.emptyList();
      }

      List<PunishmentRecord> records = new ArrayList<>();

      for (String recordId : playerSection.getKeys(false)) {
        String recordPath = playerKey + "." + recordId;

        String typeStr = historyConfig.getString(recordPath + ".type");
        PunishmentType type;
        try {
          type = PunishmentType.valueOf(typeStr);
        } catch (Exception e) {
          continue; // Skip invalid records
        }

        String issuerUuidStr = historyConfig.getString(recordPath + ".issuerUuid");
        UUID issuerUuid = issuerUuidStr != null ? UUID.fromString(issuerUuidStr) : null;
        String issuerName = historyConfig.getString(recordPath + ".issuerName", "Console");
        String reason = historyConfig.getString(recordPath + ".reason", "No reason specified");

        Instant issuedAt = parseInstant(historyConfig.getString(recordPath + ".issuedAt"));
        Instant expiresAt = parseInstant(historyConfig.getString(recordPath + ".expiresAt"));
        boolean permanent = historyConfig.getBoolean(recordPath + ".permanent", false);
        String template = historyConfig.getString(recordPath + ".template");

        if (issuedAt != null) {
          records.add(new PunishmentRecord(recordId, type, playerUuid, issuerUuid, issuerName,
              reason, issuedAt, expiresAt, permanent, template));
        }
      }

      // Sort by issued date, newest first
      records.sort((a, b) -> b.getIssuedAt().compareTo(a.getIssuedAt()));

      return records;
    }
  }

  /**
   * Get punishment records of a specific type for a player.
   *
   * @param playerUuid The UUID of the player
   * @param type       The type of punishment to filter by
   * @return List of punishment records of the specified type
   */
  public static List<PunishmentRecord> getHistoryByType(UUID playerUuid, PunishmentType type) {
    List<PunishmentRecord> filtered = new ArrayList<>();
    for (PunishmentRecord record : getHistory(playerUuid)) {
      if (record.getType() == type) {
        filtered.add(record);
      }
    }
    return filtered;
  }

  /**
   * Get the count of punishments for a specific template.
   *
   * @param playerUuid The UUID of the player
   * @param template   The template name to count
   * @return Number of punishments using this template
   */
  public static int getTemplateViolationCount(UUID playerUuid, String template) {
    int count = 0;
    for (PunishmentRecord record : getHistory(playerUuid)) {
      if (template.equals(record.getTemplate())) {
        count++;
      }
    }
    return count;
  }

  /**
   * Get paginated history for a player.
   *
   * @param playerUuid The UUID of the player
   * @param page       The page number (1-indexed)
   * @param pageSize   The number of records per page
   * @return List of punishment records for the specified page
   */
  public static List<PunishmentRecord> getHistoryPaginated(UUID playerUuid, int page, int pageSize) {
    List<PunishmentRecord> allRecords = getHistory(playerUuid);

    int startIndex = (page - 1) * pageSize;
    int endIndex = Math.min(startIndex + pageSize, allRecords.size());

    if (startIndex >= allRecords.size()) {
      return Collections.emptyList();
    }

    return allRecords.subList(startIndex, endIndex);
  }

  /**
   * Get the total number of pages for a player's history.
   *
   * @param playerUuid The UUID of the player
   * @param pageSize   The number of records per page
   * @return Total number of pages
   */
  public static int getTotalPages(UUID playerUuid, int pageSize) {
    int total = getHistory(playerUuid).size();
    return (int) Math.ceil((double) total / pageSize);
  }

  /**
   * Get the total count of punishment records for a player.
   *
   * @param playerUuid The UUID of the player
   * @return Total number of punishment records
   */
  public static int getTotalRecords(UUID playerUuid) {
    return getHistory(playerUuid).size();
  }

  private static Instant parseInstant(String str) {
    if (str == null || str.isEmpty()) {
      return null;
    }
    try {
      return Instant.parse(str);
    } catch (Exception e) {
      return null;
    }
  }

  public static void setupStorage(PistonMute plugin) {
    if (plugin == null) {
      return;
    }

    synchronized (LOCK) {
      if (!PLUGIN.compareAndSet(null, plugin)) {
        return;
      }

      historyFile = new File(plugin.getDataFolder(), "history.yml");
      loadData();
    }
  }

  private static PistonMute plugin() {
    return Objects.requireNonNull(PLUGIN.get(), "PunishmentHistoryStorage has not been initialized");
  }

  private static void logWarning(String message) {
    plugin().getLogger().warning(message);
  }

  private static void ensureLoaded() {
    plugin();
    if (historyConfig == null) {
      loadData();
    }
  }

  private static void loadData() {
    generateFile();

    if (historyFile == null) {
      logWarning("History data file not initialized.");
      return;
    }

    historyConfig = YamlConfiguration.loadConfiguration(historyFile);
  }

  private static void saveData() {
    generateFile();

    try {
      historyConfig.save(historyFile);
    } catch (IOException e) {
      logWarning("Failed to save history data: " + e.getMessage());
    }
  }

  private static void generateFile() {
    File dataFolder = plugin().getDataFolder();
    if (!dataFolder.exists() && !dataFolder.mkdir()) {
      logWarning("Failed to create plugin data folder.");
    }

    if (historyFile == null) {
      logWarning("History data file not initialized.");
      return;
    }

    if (!historyFile.exists()) {
      try {
        if (!historyFile.createNewFile()) {
          logWarning("History data file already exists.");
        }
      } catch (IOException e) {
        logWarning("Failed to create history data file: " + e.getMessage());
      }
    }
  }
}
