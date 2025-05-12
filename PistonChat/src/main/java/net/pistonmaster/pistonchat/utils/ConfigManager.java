package net.pistonmaster.pistonchat.utils;

import lombok.RequiredArgsConstructor;
import net.pistonmaster.pistonchat.PistonChat;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;

@RequiredArgsConstructor
public class ConfigManager {
  private final PistonChat plugin;
  private final String fileName;
  private FileConfiguration config;

  public void create() throws IOException {
    createIfAbsent();

    config = getConfig();
    config.setDefaults(getDefaultConfig());
    config.options().copyHeader(true);
    config.options().copyDefaults(true);

    config.save(getConfigFile());
  }

  public FileConfiguration get() {
    return config;
  }

  private YamlConfiguration getDefaultConfig() {
    YamlConfiguration config = new YamlConfiguration();

    try (
        InputStream inputStream = getDefaultInput();
        InputStreamReader reader = new InputStreamReader(inputStream)
    ) {
      config.load(reader);
    } catch (IOException | InvalidConfigurationException ex) {
      throw new IllegalStateException("Cannot load default config", ex);
    }

    return config;
  }

  private YamlConfiguration getConfig() {
    YamlConfiguration config = new YamlConfiguration();

    try {
      config.load(getConfigFile());
    } catch (IOException | InvalidConfigurationException ex) {
      throw new IllegalStateException("Cannot load config", ex);
    }

    return config;
  }

  private void createIfAbsent() throws IOException {
    File configFile = getConfigFile();

    if (!plugin.getDataFolder().exists() && !plugin.getDataFolder().mkdir()) {
      throw new IOException("Could not create data folder!");
    }

    if (!configFile.exists()) {
      try (InputStream is = getDefaultInput()) {
        Files.copy(is, configFile.toPath());
      }
    }
  }

  private File getConfigFile() {
    return new File(plugin.getDataFolder(), fileName);
  }

  private InputStream getDefaultInput() {
    return plugin.getResource(fileName);
  }
}
