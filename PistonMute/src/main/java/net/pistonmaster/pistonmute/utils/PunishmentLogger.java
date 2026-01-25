package net.pistonmaster.pistonmute.utils;

import net.pistonmaster.pistonmute.PistonMute;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Utility class for logging punishment commands to a file.
 */
public final class PunishmentLogger {
  private static final AtomicReference<PistonMute> PLUGIN = new AtomicReference<>();
  private static final Object LOCK = new Object();
  private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
  private static File logFile;

  private PunishmentLogger() {
  }

  /**
   * Initialize the punishment logger.
   *
   * @param plugin The plugin instance.
   */
  public static void setupLogger(PistonMute plugin) {
    if (plugin == null) {
      return;
    }

    synchronized (LOCK) {
      if (!PLUGIN.compareAndSet(null, plugin)) {
        return;
      }

      File dataFolder = plugin.getDataFolder();
      if (!dataFolder.exists() && !dataFolder.mkdir()) {
        plugin.getLogger().warning("Failed to create plugin data folder for logging.");
      }

      logFile = new File(dataFolder, "punishments.log");
    }
  }

  private static PistonMute plugin() {
    return Objects.requireNonNull(PLUGIN.get(), "PunishmentLogger has not been initialized");
  }

  /**
   * Log a punishment action.
   *
   * @param staffName The name of the staff member who performed the action.
   * @param command   The command that was executed.
   * @param target    The target player's name.
   * @param details   Additional details about the punishment.
   */
  public static void log(String staffName, String command, String target, String details) {
    PistonMute pluginInstance = plugin();
    if (!pluginInstance.getPluginConfig().logging.enabled) {
      return;
    }

    synchronized (LOCK) {
      if (logFile == null) {
        return;
      }

      String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
      String format = pluginInstance.getPluginConfig().logging.format;

      String logEntry = format
          .replace("%timestamp%", timestamp)
          .replace("%staff%", staffName)
          .replace("%command%", command)
          .replace("%target%", target)
          .replace("%details%", details != null ? details : "");

      try (BufferedWriter bw = Files.newBufferedWriter(logFile.toPath(), StandardCharsets.UTF_8,
          StandardOpenOption.CREATE, StandardOpenOption.APPEND);
           PrintWriter writer = new PrintWriter(bw)) {
        writer.println(logEntry);
      } catch (IOException e) {
        pluginInstance.getLogger().warning("Failed to write to punishment log: " + e.getMessage());
      }
    }
  }

  /**
   * Log a mute action.
   *
   * @param staffName The name of the staff member.
   * @param target    The muted player's name.
   * @param duration  The duration of the mute (or "permanent").
   */
  public static void logMute(String staffName, String target, String duration) {
    log(staffName, "MUTE", target, "Duration: " + duration);
  }

  /**
   * Log an unmute action.
   *
   * @param staffName The name of the staff member.
   * @param target    The unmuted player's name.
   */
  public static void logUnmute(String staffName, String target) {
    log(staffName, "UNMUTE", target, "");
  }

  /**
   * Log a note addition.
   *
   * @param staffName The name of the staff member.
   * @param target    The player the note was added to.
   * @param noteText  The content of the note.
   */
  public static void logNoteAdd(String staffName, String target, String noteText) {
    log(staffName, "NOTE_ADD", target, "Note: " + noteText);
  }

  /**
   * Log a note deletion.
   *
   * @param staffName The name of the staff member.
   * @param target    The player the note was deleted from.
   * @param noteId    The ID of the deleted note.
   */
  public static void logNoteDelete(String staffName, String target, int noteId) {
    log(staffName, "NOTE_DELETE", target, "Note ID: " + noteId);
  }
}
