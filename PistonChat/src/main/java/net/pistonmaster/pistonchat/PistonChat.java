package net.pistonmaster.pistonchat;

import com.tcoded.folialib.FoliaLib;
import lombok.Getter;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.md_5.bungee.api.ChatColor;
import net.pistonmaster.pistonchat.api.PistonChatAPI;
import net.pistonmaster.pistonchat.commands.MainCommand;
import net.pistonmaster.pistonchat.commands.ignore.HardIgnoreCommand;
import net.pistonmaster.pistonchat.commands.ignore.IgnoreListCommand;
import net.pistonmaster.pistonchat.commands.ignore.SoftIgnoreCommand;
import net.pistonmaster.pistonchat.commands.toggle.ToggleChatCommand;
import net.pistonmaster.pistonchat.commands.toggle.ToggleWhisperingCommand;
import net.pistonmaster.pistonchat.commands.whisper.LastCommand;
import net.pistonmaster.pistonchat.commands.whisper.ReplyCommand;
import net.pistonmaster.pistonchat.commands.whisper.WhisperCommand;
import net.pistonmaster.pistonchat.events.ChatEvent;
import net.pistonmaster.pistonchat.storage.PCStorage;
import net.pistonmaster.pistonchat.storage.file.FileStorage;
import net.pistonmaster.pistonchat.storage.mysql.MySQLStorage;
import net.pistonmaster.pistonchat.tools.*;
import net.pistonmaster.pistonchat.utils.ConfigManager;
import net.pistonmaster.pistonutils.update.GitHubUpdateChecker;
import net.pistonmaster.pistonutils.update.SemanticVersion;
import org.bstats.bukkit.Metrics;
import org.bukkit.Server;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.logging.Logger;

@Getter
public final class PistonChat extends JavaPlugin {
  private final ConfigManager configManager = new ConfigManager(this, "config.yml");
  private final ConfigManager languageManager = new ConfigManager(this, "language.yml");
  private final TempDataTool tempDataTool = new TempDataTool(this);
  private final SoftIgnoreTool softignoreTool = new SoftIgnoreTool(this);
  private final CacheTool cacheTool = new CacheTool(this);
  private final IgnoreTool ignoreTool = new IgnoreTool(this);
  private final HardIgnoreTool hardIgnoreTool = new HardIgnoreTool(this);
  private final CommonTool commonTool = new CommonTool(this);
  private final FoliaLib foliaLib = new FoliaLib(this);
  private PCStorage storage;
  private BukkitAudiences adventure;

  @Override
  public void onEnable() {
    this.adventure = BukkitAudiences.create(this);
    PistonChatAPI.setInstance(this);

    Logger log = getLogger();
    Server server = getServer();

    log.info("  _____  _       _                  _____  _             _   ");
    log.info(" |  __ \\(_)     | |                / ____|| |           | |  ");
    log.info(" | |__) |_  ___ | |_  ___   _ __  | |     | |__    __ _ | |_ ");
    log.info(" |  ___/| |/ __|| __|/ _ \\ | '_ \\ | |     | '_ \\  / _` || __|");
    log.info(" | |    | |\\__ \\| |_| (_) || | | || |____ | | | || (_| || |_ ");
    log.info(" |_|    |_||___/ \\__|\\___/ |_| |_| \\_____||_| |_| \\__,_| \\__|");
    log.info("                                                             ");

    log.info(ChatColor.DARK_GREEN + "Loading config");
    try {
      configManager.create();
      languageManager.create();
    } catch (IOException e) {
      e.printStackTrace();
      getServer().getPluginManager().disablePlugin(this);
    }

    log.info(ChatColor.DARK_GREEN + "Loading storage");
    var storageType = configManager.get().getString("storage");
    if (storageType.equalsIgnoreCase("mysql")) {
      storage = new MySQLStorage(log, configManager);
    } else if (storageType.equalsIgnoreCase("file")) {
      storage = new FileStorage(log, getDataFolder().toPath());
    }

    log.info(ChatColor.DARK_GREEN + "Registering commands");
    PluginCommand ignorehard = server.getPluginCommand("ignorehard");
    PluginCommand ignore = server.getPluginCommand("ignore");
    PluginCommand whisper = server.getPluginCommand("whisper");
    PluginCommand reply = server.getPluginCommand("reply");
    PluginCommand last = server.getPluginCommand("last");
    PluginCommand ignorelist = server.getPluginCommand("ignorelist");
    PluginCommand toggleWhispering = server.getPluginCommand("togglewhispering");
    PluginCommand toggleChat = server.getPluginCommand("togglechat");
    PluginCommand main = server.getPluginCommand("pistonchat");

    assert ignorehard != null;
    assert ignore != null;
    assert whisper != null;
    assert reply != null;
    assert last != null;
    assert ignorelist != null;
    assert toggleWhispering != null;
    assert toggleChat != null;
    assert main != null;

    ignorehard.setExecutor(new HardIgnoreCommand(this));
    ignorehard.setTabCompleter(new HardIgnoreCommand(this));

    ignore.setExecutor(new SoftIgnoreCommand(this));
    ignore.setTabCompleter(new SoftIgnoreCommand(this));

    whisper.setExecutor(new WhisperCommand(this));
    whisper.setTabCompleter(new WhisperCommand(this));

    reply.setExecutor(new ReplyCommand(this));
    reply.setTabCompleter(new ReplyCommand(this));

    last.setExecutor(new LastCommand(this));
    last.setTabCompleter(new LastCommand(this));

    ignorelist.setExecutor(new IgnoreListCommand(this));
    ignorelist.setTabCompleter(new IgnoreListCommand(this));

    toggleWhispering.setExecutor(new ToggleWhisperingCommand(this));
    toggleWhispering.setTabCompleter(new ToggleWhisperingCommand(this));

    toggleChat.setExecutor(new ToggleChatCommand(this));
    toggleChat.setTabCompleter(new ToggleChatCommand(this));

    main.setExecutor(new MainCommand(this));
    main.setTabCompleter(new MainCommand(this));

    log.info(ChatColor.DARK_GREEN + "Registering listeners");
    server.getPluginManager().registerEvents(new ChatEvent(this), this);

    log.info(ChatColor.DARK_GREEN + "Checking for a newer version");
    try {
      String currentVersionString = this.getDescription().getVersion();
      SemanticVersion gitHubVersion = new GitHubUpdateChecker()
          .getVersion("https://api.github.com/repos/AlexProgrammerDE/PistonChat/releases/latest");
      SemanticVersion currentVersion = SemanticVersion.fromString(currentVersionString);

      if (gitHubVersion.isNewerThan(currentVersion)) {
        log.info(ChatColor.RED + "There is an update available!");
        log.info(ChatColor.RED + "Current version: " + currentVersionString + " New version: " + gitHubVersion);
        log.info(ChatColor.RED + "Download it at: https://modrinth.com/plugin/pistonchat");
      } else {
        log.info(ChatColor.DARK_GREEN + "You're up to date!");
      }
    } catch (IOException e) {
      log.severe("Could not check for updates!");
      e.printStackTrace();
    }

    log.info(ChatColor.DARK_GREEN + "Loading metrics");
    new Metrics(this, 9630);

    log.info(ChatColor.DARK_GREEN + "Done! :D");
  }

  @Override
  public void onDisable() {
    if (this.adventure != null) {
      this.adventure.close();
      this.adventure = null;
    }
  }

  @Override
  public FileConfiguration getConfig() {
    return configManager.get();
  }

  public FileConfiguration getLanguage() {
    return languageManager.get();
  }

  public void runAsync(Runnable runnable) {
    foliaLib.getScheduler().runAsync(task -> runnable.run());
  }
}
