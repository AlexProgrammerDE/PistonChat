package net.pistonmaster.pistonchat.utils;

import net.pistonmaster.pistonchat.PistonChat;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;

@SuppressWarnings("unused")
public class ConfigManager {
    private final PistonChat plugin;
    private final String fileName;
    private FileConfiguration config;

    public ConfigManager(PistonChat plugin, String fileName) {
        this.plugin = plugin;
        this.fileName = fileName;
    }

    public void create() throws IOException {
        createIfAbsent();

        config = getConfig();
        config.setDefaults(getDefaultConfig());
        config.options().copyDefaults(true);
        config.options().copyHeader();

        saveConfig(config);

        plugin.reloadConfig();
    }

    public FileConfiguration get() {
        return config;
    }

    private Configuration getDefaultConfig() {
        return YamlConfiguration.loadConfiguration(new InputStreamReader(getDefaultInput()));
    }

    private FileConfiguration getConfig() {
        return YamlConfiguration.loadConfiguration(getConfigFile());
    }

    private void saveConfig(FileConfiguration config) throws IOException {
        config.save(getConfigFile());
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
