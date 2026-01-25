package net.pistonmaster.pistonmute.utils;

import net.pistonmaster.pistonmute.PistonMute;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Storage class for managing player notes.
 */
public final class NotesStorage {
  private static final AtomicReference<PistonMute> PLUGIN = new AtomicReference<>();
  private static final Object LOCK = new Object();
  private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
      .withZone(ZoneId.systemDefault());
  private static FileConfiguration notesConfig;
  private static File notesFile;

  private NotesStorage() {
  }

  /**
   * Initialize the notes storage.
   *
   * @param plugin The plugin instance.
   */
  public static void setupStorage(PistonMute plugin) {
    if (plugin == null) {
      return;
    }

    synchronized (LOCK) {
      if (!PLUGIN.compareAndSet(null, plugin)) {
        return;
      }

      notesFile = new File(plugin.getDataFolder(), "notes.yml");
      loadNotes();
    }
  }

  private static PistonMute plugin() {
    return Objects.requireNonNull(PLUGIN.get(), "NotesStorage has not been initialized");
  }

  private static void ensureLoaded() {
    plugin();
    if (notesConfig == null) {
      loadNotes();
    }
  }

  private static void loadNotes() {
    generateFile();

    if (notesFile == null) {
      plugin().getLogger().warning("Notes file not initialized.");
      return;
    }

    notesConfig = YamlConfiguration.loadConfiguration(notesFile);
  }

  private static void saveNotes() {
    generateFile();

    try {
      notesConfig.save(notesFile);
    } catch (IOException e) {
      plugin().getLogger().warning("Failed to save notes data: " + e.getMessage());
    }
  }

  private static void generateFile() {
    File dataFolder = plugin().getDataFolder();
    if (!dataFolder.exists() && !dataFolder.mkdir()) {
      plugin().getLogger().warning("Failed to create plugin data folder.");
    }

    if (notesFile == null) {
      plugin().getLogger().warning("Notes file not initialized.");
      return;
    }

    if (!notesFile.exists()) {
      try {
        if (!notesFile.createNewFile()) {
          plugin().getLogger().warning("Notes file already exists.");
        }
      } catch (IOException e) {
        plugin().getLogger().warning("Failed to create notes file: " + e.getMessage());
      }
    }
  }

  /**
   * Add a note to a player.
   *
   * @param player    The player to add the note to.
   * @param staffName The name of the staff member adding the note.
   * @param text      The note text.
   * @return The ID of the newly created note.
   */
  public static int addNote(OfflinePlayer player, String staffName, String text) {
    synchronized (LOCK) {
      ensureLoaded();

      String playerKey = player.getUniqueId().toString();
      ConfigurationSection playerSection = notesConfig.getConfigurationSection(playerKey);

      if (playerSection == null) {
        playerSection = notesConfig.createSection(playerKey);
      }

      // Find the next available note ID
      int nextId = playerSection.getInt("nextId", 1);
      playerSection.set("nextId", nextId + 1);

      // Create the note
      ConfigurationSection notesSection = playerSection.getConfigurationSection("notes");
      if (notesSection == null) {
        notesSection = playerSection.createSection("notes");
      }

      ConfigurationSection noteSection = notesSection.createSection(String.valueOf(nextId));
      noteSection.set("text", text);
      noteSection.set("staff", staffName);
      noteSection.set("timestamp", Instant.now().toString());

      saveNotes();
      return nextId;
    }
  }

  /**
   * Get all notes for a player.
   *
   * @param player The player to get notes for.
   * @return A list of PlayerNote objects, sorted by ID.
   */
  public static List<PlayerNote> getNotes(OfflinePlayer player) {
    synchronized (LOCK) {
      ensureLoaded();

      String playerKey = player.getUniqueId().toString();
      ConfigurationSection playerSection = notesConfig.getConfigurationSection(playerKey);

      if (playerSection == null) {
        return Collections.emptyList();
      }

      ConfigurationSection notesSection = playerSection.getConfigurationSection("notes");
      if (notesSection == null) {
        return Collections.emptyList();
      }

      List<PlayerNote> notes = new ArrayList<>();
      for (String idKey : notesSection.getKeys(false)) {
        ConfigurationSection noteSection = notesSection.getConfigurationSection(idKey);
        if (noteSection != null) {
          int id = Integer.parseInt(idKey);
          String text = noteSection.getString("text", "");
          String staff = noteSection.getString("staff", "Unknown");
          String timestampStr = noteSection.getString("timestamp");
          Instant timestamp = timestampStr != null ? Instant.parse(timestampStr) : Instant.now();

          notes.add(new PlayerNote(id, text, staff, timestamp));
        }
      }

      notes.sort(Comparator.comparingInt(PlayerNote::id));
      return notes;
    }
  }

  /**
   * Delete a note from a player.
   *
   * @param player The player to delete the note from.
   * @param noteId The ID of the note to delete.
   * @return true if the note was deleted, false if it didn't exist.
   */
  public static boolean deleteNote(OfflinePlayer player, int noteId) {
    synchronized (LOCK) {
      ensureLoaded();

      String playerKey = player.getUniqueId().toString();
      ConfigurationSection playerSection = notesConfig.getConfigurationSection(playerKey);

      if (playerSection == null) {
        return false;
      }

      ConfigurationSection notesSection = playerSection.getConfigurationSection("notes");
      if (notesSection == null) {
        return false;
      }

      String idKey = String.valueOf(noteId);
      if (!notesSection.contains(idKey)) {
        return false;
      }

      notesSection.set(idKey, null);
      saveNotes();
      return true;
    }
  }

  /**
   * Get the count of notes for a player.
   *
   * @param player The player to count notes for.
   * @return The number of notes.
   */
  public static int getNoteCount(OfflinePlayer player) {
    return getNotes(player).size();
  }

  /**
   * Format a timestamp for display.
   *
   * @param timestamp The timestamp to format.
   * @return A formatted string representation.
   */
  public static String formatTimestamp(Instant timestamp) {
    return DISPLAY_FORMAT.format(timestamp);
  }

  /**
   * Record representing a player note.
   */
  public record PlayerNote(int id, String text, String staff, Instant timestamp) {
  }
}
