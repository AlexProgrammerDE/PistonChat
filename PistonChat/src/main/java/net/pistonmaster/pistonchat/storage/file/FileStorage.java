package net.pistonmaster.pistonchat.storage.file;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.md_5.bungee.api.ChatColor;
import net.pistonmaster.pistonchat.storage.PCStorage;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class FileStorage implements PCStorage {
  private final Logger log;
  private final Gson gson;

  private final Path chatSettingsFile;
  private final Path whisperSettingsFile;
  private final Path ignoreListFile;

  private final Map<UUID, Boolean> chatSettings = new ConcurrentHashMap<>();
  private final Map<UUID, Boolean> whisperSettings = new ConcurrentHashMap<>();
  private final Map<UUID, List<UUID>> ignoreList = new ConcurrentHashMap<>();

  @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Logger is a shared resource by design")
  public FileStorage(Logger log, Path dataFolder) {
    this.log = log;

    log.info(ChatColor.DARK_GREEN + "Loading file storage");

    gson = new GsonBuilder().setPrettyPrinting().create();

    chatSettingsFile = dataFolder.resolve("chat_settings.json");
    whisperSettingsFile = dataFolder.resolve("whisper_settings.json");
    ignoreListFile = dataFolder.resolve("ignore_list.json");

    // Load existing data
    loadData();

    log.info(ChatColor.DARK_GREEN + "Loaded file storage");
  }

  private void loadData() {
    try {
      // Load chat settings
      if (Files.exists(chatSettingsFile)) {
        Type type = new TypeToken<Map<UUID, Boolean>>() {
        }.getType();
        try (Reader reader = Files.newBufferedReader(chatSettingsFile, StandardCharsets.UTF_8)) {
          Map<UUID, Boolean> loaded = gson.fromJson(reader, type);
          if (loaded != null) {
            chatSettings.putAll(loaded);
          }
        }
      }

      // Load whisper settings
      if (Files.exists(whisperSettingsFile)) {
        Type type = new TypeToken<Map<UUID, Boolean>>() {
        }.getType();
        try (Reader reader = Files.newBufferedReader(whisperSettingsFile, StandardCharsets.UTF_8)) {
          Map<UUID, Boolean> loaded = gson.fromJson(reader, type);
          if (loaded != null) {
            whisperSettings.putAll(loaded);
          }
        }
      }

      // Load ignore lists
      if (Files.exists(ignoreListFile)) {
        Type type = new TypeToken<Map<UUID, List<UUID>>>() {
        }.getType();
        try (Reader reader = Files.newBufferedReader(ignoreListFile, StandardCharsets.UTF_8)) {
          Map<UUID, List<UUID>> loaded = gson.fromJson(reader, type);
          if (loaded != null) {
            ignoreList.putAll(loaded);
          }
        }
      }
    } catch (Exception e) {
      log.severe("Error loading data: " + e.getMessage());
    }
  }

  private synchronized void saveMap(Map<?, ?> data, Path file) {
    try {
      Path parent = file.getParent();
      if (parent != null) {
        Files.createDirectories(parent);
      }
      try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
        gson.toJson(data, writer);
      }
    } catch (IOException e) {
      log.severe("Could not save data: " + e.getMessage());
    }
  }

  @Override
  public void setChatEnabled(UUID uuid, boolean enabled) {
    chatSettings.put(uuid, enabled);
    saveMap(chatSettings, chatSettingsFile);
  }

  @Override
  public boolean isChatEnabled(UUID uuid) {
    return chatSettings.getOrDefault(uuid, true);
  }

  @Override
  public void setWhisperingEnabled(UUID uuid, boolean enabled) {
    whisperSettings.put(uuid, enabled);
    saveMap(whisperSettings, whisperSettingsFile);
  }

  @Override
  public boolean isWhisperingEnabled(UUID uuid) {
    return whisperSettings.getOrDefault(uuid, true);
  }

  @Override
  public HardReturn hardIgnorePlayer(UUID ignoringReceiver, UUID ignoredChatter) {
    List<UUID> ignored = ignoreList.computeIfAbsent(ignoringReceiver, k -> new ArrayList<>());

    if (ignored.contains(ignoredChatter)) {
      ignored.remove(ignoredChatter);
      saveMap(ignoreList, ignoreListFile);
      return HardReturn.UN_IGNORE;
    } else {
      ignored.add(ignoredChatter);
      saveMap(ignoreList, ignoreListFile);
      return HardReturn.IGNORE;
    }
  }

  @Override
  public boolean isHardIgnored(UUID chatter, UUID receiver) {
    List<UUID> ignored = ignoreList.get(receiver);
    return ignored != null && ignored.contains(chatter);
  }

  @Override
  public List<UUID> getIgnoredList(UUID uuid) {
    List<UUID> ignored = ignoreList.get(uuid);
    return ignored != null ? new ArrayList<>(ignored) : new ArrayList<>();
  }

  @Override
  public void clearIgnoredPlayers(UUID player) {
    ignoreList.remove(player);
    saveMap(ignoreList, ignoreListFile);
  }
}
