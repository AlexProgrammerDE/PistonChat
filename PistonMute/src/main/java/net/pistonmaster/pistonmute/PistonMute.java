package net.pistonmaster.pistonmute;

import de.exlll.configlib.ConfigLib;
import de.exlll.configlib.YamlConfigurationProperties;
import de.exlll.configlib.YamlConfigurations;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import net.pistonmaster.pistonmute.commands.MuteCommand;
import net.pistonmaster.pistonmute.commands.UnMuteCommand;
import net.pistonmaster.pistonmute.config.PistonMuteConfig;
import net.pistonmaster.pistonmute.listeners.PistonChatListener;
import net.pistonmaster.pistonmute.utils.StorageTool;
import net.pistonmaster.pistonutils.update.GitHubUpdateChecker;
import net.pistonmaster.pistonutils.update.SemanticVersion;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Logger;
import java.util.concurrent.CompletableFuture;

public final class PistonMute extends JavaPlugin {
  private static final YamlConfigurationProperties CONFIG_PROPERTIES = ConfigLib.BUKKIT_DEFAULT_PROPERTIES.toBuilder()
      .header("PistonMute Configuration")
      .build();
  private CompletableFuture<Void> updateCheckTask;
  @Getter
  private PistonMuteConfig pluginConfig;

  @Override
  public void onEnable() {
    Logger log = getLogger();

    log.info(ChatColor.YELLOW + "Loading config");
    loadConfig();
    StorageTool.setupTool(this);

    log.info(ChatColor.YELLOW + "Registering commands");
    getServer().getPluginCommand("mute").setExecutor(new MuteCommand(this));
    getServer().getPluginCommand("mute").setTabCompleter(new MuteCommand(this));

    getServer().getPluginCommand("unmute").setExecutor(new UnMuteCommand(this));
    getServer().getPluginCommand("unmute").setTabCompleter(new UnMuteCommand(this));

    log.info(ChatColor.YELLOW + "Registering listeners");
    getServer().getPluginManager().registerEvents(new PistonChatListener(this), this);

    log.info(ChatColor.YELLOW + "Loading metrics");
    new Metrics(this, 11559);

    log.info(ChatColor.YELLOW + "Checking for a newer version");
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
          log.info(ChatColor.YELLOW + "You're up to date!");
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }).exceptionally(throwable -> {
      log.severe("Could not check for updates!");
      throwable.printStackTrace();
      return null;
    });

    getLogger().info(ChatColor.YELLOW + "Done! :D");
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
    pluginConfig = YamlConfigurations.update(configPath, PistonMuteConfig.class, CONFIG_PROPERTIES);
  }
}
