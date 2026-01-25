package net.pistonmaster.pistonmute.utils;

import net.pistonmaster.pistonmute.PistonMute;
import net.pistonmaster.pistonmute.data.MuteRecord;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class StorageTool {
  private static final AtomicReference<PistonMute> PLUGIN = new AtomicReference<>();
  private static final Object LOCK = new Object();
  private static final String IP_DATA_SECTION = "ip-data";
  private static final String MUTE_REASONS_SECTION = "mute-reasons";
  private static final String MUTE_DETAILS_SECTION = "mute-details";
  private static FileConfiguration dataConfig;
  private static File dataFile;

  private StorageTool() {
  }

  /**
   * Mute a player temporarily!
   *
   * @param player The player to mute.
   * @param muteUntil The instant when the player will be unmuted.
   * @return true if player got muted and if already muted false.
   */
  public static boolean tempMutePlayer(Player player, Instant muteUntil) {
    synchronized (LOCK) {
      ensureLoaded();
      manageMute(player);

      String key = player.getUniqueId().toString();

      if (!dataConfig.contains(key)) {
        dataConfig.set(key, Objects.requireNonNull(muteUntil, "muteUntil").toString());

        saveData();

        return true;
      } else {
        return false;
      }
    }
  }

  /**
   * Mute a player!
   *
   * @param player The player to mute.
   * @return true if player got muted and if already muted false.
   */
  public static boolean hardMutePlayer(Player player) {
    synchronized (LOCK) {
      ensureLoaded();
      manageMute(player);

      if (!dataConfig.getStringList("hardmutes").contains(player.getUniqueId().toString())) {
        dataConfig.set("hardmutes", Stream.concat(dataConfig.getStringList("hardmutes").stream(), Stream.of(player.getUniqueId().toString())).collect(Collectors.toList()));

        saveData();

        return true;
      } else {
        return false;
      }
    }
  }

  /**
   * Unmute a player!
   *
   * @param player The player to unmute.
   * @return true if player got unmuted and false if not was muted.
   */
  public static boolean unMutePlayer(OfflinePlayer player) {
    synchronized (LOCK) {
      ensureLoaded();
      if (dataConfig.contains(player.getUniqueId().toString())) {
        dataConfig.set(player.getUniqueId().toString(), null);

        saveData();

        return true;
      } else if (dataConfig.getStringList("hardmutes").contains(player.getUniqueId().toString())) {
        dataConfig.set("hardmutes", dataConfig.getStringList("hardmutes").stream().filter(uuid -> !uuid.equals(player.getUniqueId().toString())).collect(Collectors.toList()));

        saveData();

        return true;
      } else {

        return false;
      }
    }
  }

  public static boolean isMuted(OfflinePlayer player) {
    synchronized (LOCK) {
      ensureLoaded();
      manageMute(player);

      return dataConfig.contains(player.getUniqueId().toString()) || dataConfig.getStringList("hardmutes").contains(player.getUniqueId().toString());
    }
  }

  /**
   * Check if a player UUID is muted.
   *
   * @param uuid The UUID to check.
   * @return true if the player is muted.
   */
  public static boolean isMuted(UUID uuid) {
    synchronized (LOCK) {
      ensureLoaded();
      OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
      manageMute(player);

      return dataConfig.contains(uuid.toString()) || dataConfig.getStringList("hardmutes").contains(uuid.toString());
    }
  }

  /**
   * Mute an offline player temporarily by UUID.
   *
   * @param uuid The UUID of the player to mute.
   * @param muteUntil The instant when the player will be unmuted.
   * @param reason The reason for the mute (can be null).
   * @return true if player got muted and if already muted false.
   */
  public static boolean tempMutePlayer(UUID uuid, Instant muteUntil, String reason) {
    synchronized (LOCK) {
      ensureLoaded();
      OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
      manageMute(player);

      String key = uuid.toString();

      if (!dataConfig.contains(key)) {
        dataConfig.set(key, Objects.requireNonNull(muteUntil, "muteUntil").toString());

        if (reason != null && !reason.isEmpty()) {
          dataConfig.set(MUTE_REASONS_SECTION + "." + key, reason);
        }

        saveData();

        return true;
      } else {
        return false;
      }
    }
  }

  /**
   * Mute an offline player permanently by UUID.
   *
   * @param uuid The UUID of the player to mute.
   * @param reason The reason for the mute (can be null).
   * @return true if player got muted and if already muted false.
   */
  public static boolean hardMutePlayer(UUID uuid, String reason) {
    synchronized (LOCK) {
      ensureLoaded();
      OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
      manageMute(player);

      if (!dataConfig.getStringList("hardmutes").contains(uuid.toString())) {
        dataConfig.set("hardmutes", Stream.concat(dataConfig.getStringList("hardmutes").stream(), Stream.of(uuid.toString())).collect(Collectors.toList()));

        if (reason != null && !reason.isEmpty()) {
          dataConfig.set(MUTE_REASONS_SECTION + "." + uuid, reason);
        }

        saveData();

        return true;
      } else {
        return false;
      }
    }
  }

  /**
   * Unmute a player by UUID.
   *
   * @param uuid The UUID of the player to unmute.
   * @return true if player got unmuted and false if not was muted.
   */
  public static boolean unMutePlayer(UUID uuid) {
    synchronized (LOCK) {
      ensureLoaded();
      if (dataConfig.contains(uuid.toString())) {
        dataConfig.set(uuid.toString(), null);
        dataConfig.set(MUTE_REASONS_SECTION + "." + uuid, null);

        saveData();

        return true;
      } else if (dataConfig.getStringList("hardmutes").contains(uuid.toString())) {
        dataConfig.set("hardmutes", dataConfig.getStringList("hardmutes").stream().filter(u -> !u.equals(uuid.toString())).collect(Collectors.toList()));
        dataConfig.set(MUTE_REASONS_SECTION + "." + uuid, null);

        saveData();

        return true;
      } else {

        return false;
      }
    }
  }

  /**
   * Get the reason for a player's mute.
   *
   * @param uuid The UUID of the player.
   * @return The mute reason, or null if not set.
   */
  public static String getMuteReason(UUID uuid) {
    synchronized (LOCK) {
      ensureLoaded();
      return dataConfig.getString(MUTE_REASONS_SECTION + "." + uuid.toString());
    }
  }

  /**
   * Mute a player temporarily with full details.
   *
   * @param uuid       The UUID of the player to mute.
   * @param muteUntil  The instant when the player will be unmuted.
   * @param issuerUuid The UUID of the issuer (null for console).
   * @param issuerName The name of the issuer.
   * @param reason     The reason for the mute (can be null).
   * @param template   The escalation template used (can be null).
   * @return true if player got muted and if already muted false.
   */
  public static boolean tempMutePlayer(UUID uuid, Instant muteUntil, UUID issuerUuid,
                                        String issuerName, String reason, String template) {
    synchronized (LOCK) {
      ensureLoaded();
      OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
      manageMute(player);

      String key = uuid.toString();

      if (!dataConfig.contains(key) && !dataConfig.getStringList("hardmutes").contains(key)) {
        dataConfig.set(key, Objects.requireNonNull(muteUntil, "muteUntil").toString());

        if (reason != null && !reason.isEmpty()) {
          dataConfig.set(MUTE_REASONS_SECTION + "." + key, reason);
        }

        // Store extended details
        String detailsPath = MUTE_DETAILS_SECTION + "." + key;
        dataConfig.set(detailsPath + ".issuerUuid", issuerUuid != null ? issuerUuid.toString() : null);
        dataConfig.set(detailsPath + ".issuerName", issuerName);
        dataConfig.set(detailsPath + ".template", template);
        dataConfig.set(detailsPath + ".issuedAt", Instant.now().toString());
        dataConfig.set(detailsPath + ".permanent", false);

        saveData();

        return true;
      } else {
        return false;
      }
    }
  }

  /**
   * Mute a player permanently with full details.
   *
   * @param uuid       The UUID of the player to mute.
   * @param issuerUuid The UUID of the issuer (null for console).
   * @param issuerName The name of the issuer.
   * @param reason     The reason for the mute (can be null).
   * @param template   The escalation template used (can be null).
   * @return true if player got muted and if already muted false.
   */
  public static boolean hardMutePlayer(UUID uuid, UUID issuerUuid, String issuerName,
                                        String reason, String template) {
    synchronized (LOCK) {
      ensureLoaded();
      OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
      manageMute(player);

      String key = uuid.toString();

      if (!dataConfig.getStringList("hardmutes").contains(key) && !dataConfig.contains(key)) {
        dataConfig.set("hardmutes", Stream.concat(dataConfig.getStringList("hardmutes").stream(), Stream.of(key)).collect(Collectors.toList()));

        if (reason != null && !reason.isEmpty()) {
          dataConfig.set(MUTE_REASONS_SECTION + "." + key, reason);
        }

        // Store extended details
        String detailsPath = MUTE_DETAILS_SECTION + "." + key;
        dataConfig.set(detailsPath + ".issuerUuid", issuerUuid != null ? issuerUuid.toString() : null);
        dataConfig.set(detailsPath + ".issuerName", issuerName);
        dataConfig.set(detailsPath + ".template", template);
        dataConfig.set(detailsPath + ".issuedAt", Instant.now().toString());
        dataConfig.set(detailsPath + ".permanent", true);

        saveData();

        return true;
      } else {
        return false;
      }
    }
  }

  /**
   * Get the mute record for a player.
   *
   * @param uuid The UUID of the player.
   * @return Optional containing the mute record, or empty if not muted.
   */
  public static Optional<MuteRecord> getMuteRecord(UUID uuid) {
    synchronized (LOCK) {
      ensureLoaded();

      String key = uuid.toString();
      String detailsPath = MUTE_DETAILS_SECTION + "." + key;

      // Check for temp mute
      if (dataConfig.contains(key) && !dataConfig.isConfigurationSection(key)) {
        String id = key.length() >= 8 ? key.substring(0, 8) : key;
        String issuerUuidStr = dataConfig.getString(detailsPath + ".issuerUuid");
        UUID issuerUuid = issuerUuidStr != null ? parseUUID(issuerUuidStr) : null;
        String issuerName = dataConfig.getString(detailsPath + ".issuerName", "Console");
        String reason = dataConfig.getString(MUTE_REASONS_SECTION + "." + key);
        String template = dataConfig.getString(detailsPath + ".template");
        Instant issuedAt = parseInstant(dataConfig.getString(detailsPath + ".issuedAt"));
        Instant expiresAt = MuteDateUtils.parseMuteUntil(dataConfig.getString(key)).orElse(null);
        boolean permanent = dataConfig.getBoolean(detailsPath + ".permanent", false);

        return Optional.of(new MuteRecord(id, uuid, issuerUuid, issuerName,
            reason, template, issuedAt, expiresAt, permanent));
      }

      // Check for permanent mute
      if (dataConfig.getStringList("hardmutes").contains(key)) {
        String id = key.length() >= 8 ? key.substring(0, 8) : key;
        String issuerUuidStr = dataConfig.getString(detailsPath + ".issuerUuid");
        UUID issuerUuid = issuerUuidStr != null ? parseUUID(issuerUuidStr) : null;
        String issuerName = dataConfig.getString(detailsPath + ".issuerName", "Console");
        String reason = dataConfig.getString(MUTE_REASONS_SECTION + "." + key);
        String template = dataConfig.getString(detailsPath + ".template");
        Instant issuedAt = parseInstant(dataConfig.getString(detailsPath + ".issuedAt"));

        return Optional.of(new MuteRecord(id, uuid, issuerUuid, issuerName,
            reason, template, issuedAt, null, true));
      }

      return Optional.empty();
    }
  }

  /**
   * Get a list of all currently muted players.
   *
   * @return List of UUIDs of muted players.
   */
  public static List<UUID> getMutedPlayers() {
    synchronized (LOCK) {
      ensureLoaded();

      Set<UUID> mutedPlayers = new HashSet<>();

      // Add permanent mutes
      for (String uuidStr : dataConfig.getStringList("hardmutes")) {
        UUID uuid = parseUUID(uuidStr);
        if (uuid != null) {
          mutedPlayers.add(uuid);
        }
      }

      // Add temp mutes
      for (String key : dataConfig.getKeys(false)) {
        if (key.equals("hardmutes") || key.equals(IP_DATA_SECTION) ||
            key.equals(MUTE_REASONS_SECTION) || key.equals(MUTE_DETAILS_SECTION)) {
          continue;
        }
        UUID uuid = parseUUID(key);
        if (uuid != null) {
          OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
          manageMute(player);
          // Re-check after manageMute might have removed expired mute
          if (dataConfig.contains(key)) {
            mutedPlayers.add(uuid);
          }
        }
      }

      return new ArrayList<>(mutedPlayers);
    }
  }

  /**
   * Get the mute expiration time for a player.
   *
   * @param uuid The UUID of the player.
   * @return Optional containing the expiration instant, or empty if not muted or permanent.
   */
  public static Optional<Instant> getMuteExpiration(UUID uuid) {
    synchronized (LOCK) {
      ensureLoaded();

      String key = uuid.toString();

      if (dataConfig.contains(key) && !dataConfig.isConfigurationSection(key)) {
        return MuteDateUtils.parseMuteUntil(dataConfig.getString(key));
      }

      return Optional.empty();
    }
  }

  /**
   * Check if a player's mute is permanent.
   *
   * @param uuid The UUID of the player.
   * @return true if the mute is permanent.
   */
  public static boolean isPermanentlyMuted(UUID uuid) {
    synchronized (LOCK) {
      ensureLoaded();
      return dataConfig.getStringList("hardmutes").contains(uuid.toString());
    }
  }

  private static Instant parseInstant(String str) {
    if (str == null || str.isEmpty()) {
      return null;
    }
    try {
      return Instant.parse(str);
    } catch (Exception e) {
      return MuteDateUtils.parseMuteUntil(str).orElse(null);
    }
  }

  private static UUID parseUUID(String str) {
    if (str == null || str.isEmpty()) {
      return null;
    }
    try {
      return UUID.fromString(str);
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  /**
   * Track a player's IP address.
   *
   * @param player The player whose IP to track.
   */
  public static void trackPlayerIP(Player player) {
    synchronized (LOCK) {
      ensureLoaded();

      String ip = getPlayerIP(player);
      if (ip == null) {
        return;
      }

      String uuid = player.getUniqueId().toString();
      String playerName = player.getName();

      // Store IP -> UUID mapping
      String ipKey = IP_DATA_SECTION + ".by-ip." + ip.replace(".", "_");
      List<String> uuidsForIp = dataConfig.getStringList(ipKey);
      if (!uuidsForIp.contains(uuid)) {
        uuidsForIp = new ArrayList<>(uuidsForIp);
        uuidsForIp.add(uuid);
        dataConfig.set(ipKey, uuidsForIp);
      }

      // Store UUID -> IP mapping (for reverse lookup)
      String uuidKey = IP_DATA_SECTION + ".by-uuid." + uuid;
      dataConfig.set(uuidKey + ".ip", ip);
      dataConfig.set(uuidKey + ".name", playerName);
      dataConfig.set(uuidKey + ".last-seen", Instant.now().toString());

      saveData();
    }
  }

  /**
   * Get all known alt accounts for a player.
   *
   * @param player The player to check.
   * @return Set of UUIDs of known alt accounts (excluding the player themselves).
   */
  public static Set<UUID> getAltAccounts(OfflinePlayer player) {
    synchronized (LOCK) {
      ensureLoaded();

      String uuidKey = IP_DATA_SECTION + ".by-uuid." + player.getUniqueId().toString();
      String ip = dataConfig.getString(uuidKey + ".ip");

      if (ip == null) {
        return Collections.emptySet();
      }

      return getAccountsByIP(ip, player.getUniqueId());
    }
  }

  /**
   * Get all known accounts from a specific IP.
   *
   * @param ip The IP address to check.
   * @param excludeUuid UUID to exclude from results (can be null).
   * @return Set of UUIDs of accounts from that IP.
   */
  public static Set<UUID> getAccountsByIP(String ip, UUID excludeUuid) {
    synchronized (LOCK) {
      ensureLoaded();

      String ipKey = IP_DATA_SECTION + ".by-ip." + ip.replace(".", "_");
      List<String> uuidsForIp = dataConfig.getStringList(ipKey);

      Set<UUID> result = new HashSet<>();
      for (String uuidStr : uuidsForIp) {
        try {
          UUID uuid = UUID.fromString(uuidStr);
          if (excludeUuid == null || !uuid.equals(excludeUuid)) {
            result.add(uuid);
          }
        } catch (IllegalArgumentException ignored) {
          // Invalid UUID, skip
        }
      }

      return result;
    }
  }

  /**
   * Get the stored IP for a player.
   *
   * @param uuid The UUID of the player.
   * @return The stored IP address, or null if not found.
   */
  public static String getStoredIP(UUID uuid) {
    synchronized (LOCK) {
      ensureLoaded();
      String uuidKey = IP_DATA_SECTION + ".by-uuid." + uuid.toString();
      return dataConfig.getString(uuidKey + ".ip");
    }
  }

  /**
   * Get the stored player name for a UUID.
   *
   * @param uuid The UUID to look up.
   * @return The stored player name, or null if not found.
   */
  public static String getStoredPlayerName(UUID uuid) {
    synchronized (LOCK) {
      ensureLoaded();
      String uuidKey = IP_DATA_SECTION + ".by-uuid." + uuid.toString();
      return dataConfig.getString(uuidKey + ".name");
    }
  }

  /**
   * Check if any alt accounts are muted.
   *
   * @param player The player to check alts for.
   * @return Set of UUIDs of muted alt accounts.
   */
  public static Set<UUID> getMutedAlts(OfflinePlayer player) {
    Set<UUID> alts = getAltAccounts(player);
    Set<UUID> mutedAlts = new HashSet<>();

    for (UUID altUuid : alts) {
      if (isMuted(altUuid)) {
        mutedAlts.add(altUuid);
      }
    }

    return mutedAlts;
  }

  /**
   * Get the IP address of an online player.
   *
   * @param player The player.
   * @return The IP address, or null if unavailable.
   */
  private static String getPlayerIP(Player player) {
    if (player.getAddress() == null) {
      return null;
    }
    return player.getAddress().getAddress().getHostAddress();
  }

  private static void manageMute(OfflinePlayer player) {
    String key = player.getUniqueId().toString();
    if (!dataConfig.contains(key)) {
      return;
    }

    MuteDateUtils.parseMuteUntil(dataConfig.getString(key))
        .ifPresentOrElse(
            muteUntil -> {
              if (MuteDateUtils.isMuteExpired(muteUntil)) {
                unMutePlayer(player);
              }
            },
            () -> logWarning("Failed to parse mute date for player " + key)
        );
  }

  public static void setupTool(PistonMute plugin) {
    if (plugin == null) {
      return;
    }

    synchronized (LOCK) {
      if (!PLUGIN.compareAndSet(null, plugin)) {
        return;
      }

      StorageTool.dataFile = new File(plugin.getDataFolder(), "data.yml");

      loadData();
    }
  }

  private static PistonMute plugin() {
    return Objects.requireNonNull(PLUGIN.get(), "StorageTool has not been initialized");
  }

  private static void logWarning(String message) {
    plugin().getLogger().warning(message);
  }

  private static void ensureLoaded() {
    plugin();
    if (dataConfig == null) {
      loadData();
    }
  }

  private static void loadData() {
    generateFile();

    if (dataFile == null) {
      logWarning("Mute data file not initialized.");
      return;
    }

    dataConfig = YamlConfiguration.loadConfiguration(dataFile);
  }

  private static void saveData() {
    generateFile();

    try {
      dataConfig.save(dataFile);
    } catch (IOException e) {
      logWarning("Failed to save mute data: " + e.getMessage());
    }
  }

  private static void generateFile() {
    File dataFolder = plugin().getDataFolder();
    if (!dataFolder.exists() && !dataFolder.mkdir()) {
      logWarning("Failed to create plugin data folder.");
    }

    if (dataFile == null) {
      logWarning("Mute data file not initialized.");
      return;
    }

    if (!dataFile.exists()) {
      try {
        if (!dataFile.createNewFile()) {
          logWarning("Mute data file already exists.");
        }
      } catch (IOException e) {
        logWarning("Failed to create mute data file: " + e.getMessage());
      }
    }
  }
}
