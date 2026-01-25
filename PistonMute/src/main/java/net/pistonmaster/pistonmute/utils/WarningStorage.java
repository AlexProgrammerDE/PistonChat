package net.pistonmaster.pistonmute.utils;

import net.pistonmaster.pistonmute.PistonMute;
import net.pistonmaster.pistonmute.data.Warning;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Storage utility for managing player warnings.
 */
public final class WarningStorage {
  private static final AtomicReference<PistonMute> PLUGIN = new AtomicReference<>();
  private static final Object LOCK = new Object();
  private static FileConfiguration warningsConfig;
  private static File warningsFile;

  private WarningStorage() {
  }

  /**
   * Add a warning to a player.
   *
   * @param playerUuid  The UUID of the player receiving the warning
   * @param issuerUuid  The UUID of the issuer (null for console)
   * @param issuerName  The name of the issuer
   * @param reason      The reason for the warning
   * @param expiresAt   When the warning expires (null for never)
   * @return The created warning
   */
  public static Warning addWarning(UUID playerUuid, UUID issuerUuid, String issuerName, String reason, Instant expiresAt) {
    synchronized (LOCK) {
      ensureLoaded();

      String warningId = UUID.randomUUID().toString().substring(0, 8);
      Instant issuedAt = Instant.now();

      String playerKey = playerUuid.toString();
      String warningPath = playerKey + "." + warningId;

      warningsConfig.set(warningPath + ".issuerUuid", issuerUuid != null ? issuerUuid.toString() : null);
      warningsConfig.set(warningPath + ".issuerName", issuerName);
      warningsConfig.set(warningPath + ".reason", reason);
      warningsConfig.set(warningPath + ".issuedAt", issuedAt.toString());
      warningsConfig.set(warningPath + ".expiresAt", expiresAt != null ? expiresAt.toString() : null);

      saveData();

      return new Warning(warningId, playerUuid, issuerUuid, issuerName, reason, issuedAt, expiresAt);
    }
  }

  /**
   * Remove a specific warning from a player.
   *
   * @param playerUuid The UUID of the player
   * @param warningId  The ID of the warning to remove
   * @return true if the warning was removed, false if not found
   */
  public static boolean removeWarning(UUID playerUuid, String warningId) {
    synchronized (LOCK) {
      ensureLoaded();

      String warningPath = playerUuid.toString() + "." + warningId;

      if (warningsConfig.contains(warningPath)) {
        warningsConfig.set(warningPath, null);
        saveData();
        return true;
      }

      return false;
    }
  }

  /**
   * Remove all warnings from a player.
   *
   * @param playerUuid The UUID of the player
   * @return The number of warnings removed
   */
  public static int removeAllWarnings(UUID playerUuid) {
    synchronized (LOCK) {
      ensureLoaded();

      String playerKey = playerUuid.toString();
      ConfigurationSection playerSection = warningsConfig.getConfigurationSection(playerKey);

      if (playerSection == null) {
        return 0;
      }

      int count = playerSection.getKeys(false).size();
      warningsConfig.set(playerKey, null);
      saveData();

      return count;
    }
  }

  /**
   * Get all warnings for a player.
   *
   * @param playerUuid The UUID of the player
   * @return List of warnings, sorted by issued date (newest first)
   */
  public static List<Warning> getWarnings(UUID playerUuid) {
    synchronized (LOCK) {
      ensureLoaded();

      String playerKey = playerUuid.toString();
      ConfigurationSection playerSection = warningsConfig.getConfigurationSection(playerKey);

      if (playerSection == null) {
        return Collections.emptyList();
      }

      List<Warning> warnings = new ArrayList<>();

      for (String warningId : playerSection.getKeys(false)) {
        String warningPath = playerKey + "." + warningId;

        String issuerUuidStr = warningsConfig.getString(warningPath + ".issuerUuid");
        UUID issuerUuid = issuerUuidStr != null ? UUID.fromString(issuerUuidStr) : null;
        String issuerName = warningsConfig.getString(warningPath + ".issuerName", "Console");
        String reason = warningsConfig.getString(warningPath + ".reason", "No reason specified");

        Instant issuedAt = parseInstant(warningsConfig.getString(warningPath + ".issuedAt"));
        Instant expiresAt = parseInstant(warningsConfig.getString(warningPath + ".expiresAt"));

        if (issuedAt != null) {
          warnings.add(new Warning(warningId, playerUuid, issuerUuid, issuerName, reason, issuedAt, expiresAt));
        }
      }

      // Sort by issued date, newest first
      warnings.sort((a, b) -> b.getIssuedAt().compareTo(a.getIssuedAt()));

      return warnings;
    }
  }

  /**
   * Get active (non-expired) warnings for a player.
   *
   * @param playerUuid The UUID of the player
   * @return List of active warnings
   */
  public static List<Warning> getActiveWarnings(UUID playerUuid) {
    return getWarnings(playerUuid).stream()
        .filter(Warning::isActive)
        .collect(Collectors.toList());
  }

  /**
   * Get the count of active warnings for a player.
   *
   * @param playerUuid The UUID of the player
   * @return Number of active warnings
   */
  public static int getActiveWarningCount(UUID playerUuid) {
    return getActiveWarnings(playerUuid).size();
  }

  /**
   * Get a specific warning by ID.
   *
   * @param playerUuid The UUID of the player
   * @param warningId  The ID of the warning
   * @return Optional containing the warning, or empty if not found
   */
  public static Optional<Warning> getWarning(UUID playerUuid, String warningId) {
    return getWarnings(playerUuid).stream()
        .filter(w -> w.getId().equals(warningId))
        .findFirst();
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

      warningsFile = new File(plugin.getDataFolder(), "warnings.yml");
      loadData();
    }
  }

  private static PistonMute plugin() {
    return Objects.requireNonNull(PLUGIN.get(), "WarningStorage has not been initialized");
  }

  private static void logWarning(String message) {
    plugin().getLogger().warning(message);
  }

  private static void ensureLoaded() {
    plugin();
    if (warningsConfig == null) {
      loadData();
    }
  }

  private static void loadData() {
    generateFile();

    if (warningsFile == null) {
      logWarning("Warnings data file not initialized.");
      return;
    }

    warningsConfig = YamlConfiguration.loadConfiguration(warningsFile);
  }

  private static void saveData() {
    generateFile();

    try {
      warningsConfig.save(warningsFile);
    } catch (IOException e) {
      logWarning("Failed to save warnings data: " + e.getMessage());
    }
  }

  private static void generateFile() {
    File dataFolder = plugin().getDataFolder();
    if (!dataFolder.exists() && !dataFolder.mkdir()) {
      logWarning("Failed to create plugin data folder.");
    }

    if (warningsFile == null) {
      logWarning("Warnings data file not initialized.");
      return;
    }

    if (!warningsFile.exists()) {
      try {
        if (!warningsFile.createNewFile()) {
          logWarning("Warnings data file already exists.");
        }
      } catch (IOException e) {
        logWarning("Failed to create warnings data file: " + e.getMessage());
      }
    }
  }
}
