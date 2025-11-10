package net.pistonmaster.pistonmute.utils;

import net.pistonmaster.pistonmute.PistonMute;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class StorageTool {
  private static final AtomicReference<PistonMute> PLUGIN = new AtomicReference<>();
  private static final DateTimeFormatter LEGACY_FORMATTER = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
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

  /**
   * Mute a player!
   *
   * @param player The player to mute.
   * @return true if player got muted and if already muted false.
   */
  public static boolean hardMutePlayer(Player player) {
    manageMute(player);

    if (!dataConfig.getStringList("hardmutes").contains(player.getUniqueId().toString())) {
      dataConfig.set("hardmutes", Stream.concat(dataConfig.getStringList("hardmutes").stream(), Stream.of(player.getUniqueId().toString())).collect(Collectors.toList()));

      saveData();

      return true;
    } else {
      return false;
    }
  }

  /**
   * Unmute a player!
   *
   * @param player The player to unmute.
   * @return true if player got unmuted and false if not was muted.
   */
  public static boolean unMutePlayer(OfflinePlayer player) {
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

  public static boolean isMuted(OfflinePlayer player) {
    manageMute(player);

    return dataConfig.contains(player.getUniqueId().toString()) || dataConfig.getStringList("hardmutes").contains(player.getUniqueId().toString());
  }

  private static void manageMute(OfflinePlayer player) {
    String key = player.getUniqueId().toString();
    if (!dataConfig.contains(key)) {
      return;
    }

    Instant muteUntil = parseMuteUntil(dataConfig.getString(key), key);
    if (muteUntil == null) {
      return;
    }

    if (!Instant.now().isBefore(muteUntil)) {
      unMutePlayer(player);
    }
  }

  private static void loadData() {
    generateFile();

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

  public static void setupTool(PistonMute plugin) {
    if (plugin == null) {
      return;
    }

    if (!PLUGIN.compareAndSet(null, plugin)) {
      return;
    }

    StorageTool.dataFile = new File(plugin.getDataFolder(), "data.yml");

    loadData();
  }

  private static PistonMute plugin() {
    return Objects.requireNonNull(PLUGIN.get(), "StorageTool has not been initialized");
  }

  private static void logWarning(String message) {
    plugin().getLogger().warning(message);
  }

  private static Instant parseMuteUntil(String raw, String playerId) {
    if (raw == null) {
      return null;
    }

    try {
      return Instant.parse(raw);
    } catch (DateTimeParseException ignored) {
      try {
        return Instant.from(LEGACY_FORMATTER.parse(raw));
      } catch (DateTimeParseException e) {
        logWarning("Failed to parse mute date for player " + playerId + ": " + e.getMessage());
        return null;
      }
    }
  }
}
