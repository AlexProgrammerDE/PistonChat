package net.pistonmaster.pistonfilter;

import de.exlll.configlib.ConfigLib;
import de.exlll.configlib.YamlConfigurationProperties;
import de.exlll.configlib.YamlConfigurations;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import net.pistonmaster.pistonfilter.commands.FilterCommand;
import net.pistonmaster.pistonfilter.config.PistonFilterConfig;
import net.pistonmaster.pistonfilter.listeners.AnvilListener;
import net.pistonmaster.pistonfilter.listeners.BookListener;
import net.pistonmaster.pistonfilter.listeners.ChatListener;
import net.pistonmaster.pistonfilter.listeners.CommandListener;
import net.pistonmaster.pistonfilter.listeners.SignListener;
import net.pistonmaster.pistonfilter.managers.ChatPauseManager;
import net.pistonmaster.pistonutils.update.GitHubUpdateChecker;
import net.pistonmaster.pistonutils.update.SemanticVersion;
import org.bstats.bukkit.Metrics;
import org.bukkit.Server;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Logger;
import java.util.concurrent.CompletableFuture;

public class PistonFilter extends JavaPlugin {
  private static final YamlConfigurationProperties CONFIG_PROPERTIES = ConfigLib.BUKKIT_DEFAULT_PROPERTIES.toBuilder()
      .header("PistonFilter Configuration")
      .build();

  private CompletableFuture<Void> updateCheckTask;

  @Getter
  private PistonFilterConfig pluginConfig;

  @Getter
  private final ChatPauseManager chatPauseManager = new ChatPauseManager();

  @Override
  public void onEnable() {
    Logger log = getLogger();
    Server server = getServer();

    log.info(ChatColor.AQUA + "Loading config");
    loadConfig();

    log.info(ChatColor.AQUA + "Registering commands");
    PluginCommand main = server.getPluginCommand("pistonfilter");

    assert main != null;
    main.setExecutor(new FilterCommand(this));
    main.setTabCompleter(new FilterCommand(this));

    log.info(ChatColor.AQUA + "Registering listeners");
    getServer().getPluginManager().registerEvents(new ChatListener(this), this);
    getServer().getPluginManager().registerEvents(new SignListener(this), this);
    getServer().getPluginManager().registerEvents(new BookListener(this), this);
    getServer().getPluginManager().registerEvents(new AnvilListener(this), this);
    getServer().getPluginManager().registerEvents(new CommandListener(this), this);

    log.info(ChatColor.AQUA + "Loading metrics");
    new Metrics(this, 11561);

    log.info(ChatColor.AQUA + "Checking for a newer version");
    String currentVersionString = this.getDescription().getVersion();
    updateCheckTask = CompletableFuture.runAsync(() -> {
      try {
        SemanticVersion gitHubVersion = new GitHubUpdateChecker()
            .getVersion("https://api.github.com/repos/AlexProgrammerDE/PistonChat/releases/latest");
        SemanticVersion currentVersion = SemanticVersion.fromString(currentVersionString);

        if (gitHubVersion.isNewerThan(currentVersion)) {
          log.info(ChatColor.RED + "There is an update available!");
          log.info(ChatColor.RED + "Current version: " + currentVersionString + " New version: " + gitHubVersion);
          log.info(ChatColor.RED + "Download it at: https://modrinth.com/plugin/pistonchat");
        } else {
          log.info(ChatColor.AQUA + "You're up to date!");
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }).exceptionally(throwable -> {
      log.severe("Could not check for updates!");
      throwable.printStackTrace();
      return null;
    });

    log.info(ChatColor.AQUA + "Done! :D");
  }

  @Override
  public void onDisable() {
    if (updateCheckTask != null) {
      updateCheckTask.cancel(true);
      updateCheckTask = null;
    }
  }

  public void loadConfig() {
    Path configPath = getDataFolder().toPath().resolve("config.yml");
    pluginConfig = YamlConfigurations.update(configPath, PistonFilterConfig.class, CONFIG_PROPERTIES);
  }

  public void saveConfig(PistonFilterConfig config) {
    Path configPath = getDataFolder().toPath().resolve("config.yml");
    YamlConfigurations.save(configPath, PistonFilterConfig.class, config, CONFIG_PROPERTIES);
    this.pluginConfig = config;
  }
}
