package net.pistonmaster.pistonmute;

import de.exlll.configlib.ConfigLib;
import de.exlll.configlib.YamlConfigurationProperties;
import de.exlll.configlib.YamlConfigurations;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import net.pistonmaster.pistonmute.commands.AltsCommand;
import net.pistonmaster.pistonmute.commands.DelNoteCommand;
import net.pistonmaster.pistonmute.commands.HistoryCommand;
import net.pistonmaster.pistonmute.commands.MuteCommand;
import net.pistonmaster.pistonmute.commands.MuteInfoCommand;
import net.pistonmaster.pistonmute.commands.MuteListCommand;
import net.pistonmaster.pistonmute.commands.NoteCommand;
import net.pistonmaster.pistonmute.commands.NotesCommand;
import net.pistonmaster.pistonmute.commands.UnMuteCommand;
import net.pistonmaster.pistonmute.commands.UnwarnCommand;
import net.pistonmaster.pistonmute.commands.WarnCommand;
import net.pistonmaster.pistonmute.commands.WarningsCommand;
import net.pistonmaster.pistonmute.config.PistonMuteConfig;
import net.pistonmaster.pistonmute.listeners.CommandBlockListener;
import net.pistonmaster.pistonmute.listeners.PistonChatListener;
import net.pistonmaster.pistonmute.listeners.PlayerJoinListener;
import net.pistonmaster.pistonmute.utils.NotesStorage;
import net.pistonmaster.pistonmute.utils.PunishmentHistoryStorage;
import net.pistonmaster.pistonmute.utils.PunishmentLogger;
import net.pistonmaster.pistonmute.utils.StaffUtils;
import net.pistonmaster.pistonmute.utils.StorageTool;
import net.pistonmaster.pistonmute.utils.WarningStorage;
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
    NotesStorage.setupStorage(this);
    WarningStorage.setupStorage(this);
    PunishmentHistoryStorage.setupStorage(this);
    PunishmentLogger.setupLogger(this);
    StaffUtils.setup(this);

    log.info(ChatColor.YELLOW + "Registering commands");
    MuteCommand muteCommand = new MuteCommand(this);
    getServer().getPluginCommand("mute").setExecutor(muteCommand);
    getServer().getPluginCommand("mute").setTabCompleter(muteCommand);

    UnMuteCommand unMuteCommand = new UnMuteCommand(this);
    getServer().getPluginCommand("unmute").setExecutor(unMuteCommand);
    getServer().getPluginCommand("unmute").setTabCompleter(unMuteCommand);

    NoteCommand noteCommand = new NoteCommand(this);
    getServer().getPluginCommand("note").setExecutor(noteCommand);
    getServer().getPluginCommand("note").setTabCompleter(noteCommand);

    NotesCommand notesCommand = new NotesCommand(this);
    getServer().getPluginCommand("notes").setExecutor(notesCommand);
    getServer().getPluginCommand("notes").setTabCompleter(notesCommand);

    DelNoteCommand delNoteCommand = new DelNoteCommand(this);
    getServer().getPluginCommand("delnote").setExecutor(delNoteCommand);
    getServer().getPluginCommand("delnote").setTabCompleter(delNoteCommand);

    AltsCommand altsCommand = new AltsCommand(this);
    getServer().getPluginCommand("alts").setExecutor(altsCommand);
    getServer().getPluginCommand("alts").setTabCompleter(altsCommand);

    MuteListCommand muteListCommand = new MuteListCommand(this);
    getServer().getPluginCommand("mutelist").setExecutor(muteListCommand);
    getServer().getPluginCommand("mutelist").setTabCompleter(muteListCommand);

    MuteInfoCommand muteInfoCommand = new MuteInfoCommand(this);
    getServer().getPluginCommand("muteinfo").setExecutor(muteInfoCommand);
    getServer().getPluginCommand("muteinfo").setTabCompleter(muteInfoCommand);

    WarnCommand warnCommand = new WarnCommand(this);
    getServer().getPluginCommand("warn").setExecutor(warnCommand);
    getServer().getPluginCommand("warn").setTabCompleter(warnCommand);

    WarningsCommand warningsCommand = new WarningsCommand(this);
    getServer().getPluginCommand("warnings").setExecutor(warningsCommand);
    getServer().getPluginCommand("warnings").setTabCompleter(warningsCommand);

    UnwarnCommand unwarnCommand = new UnwarnCommand(this);
    getServer().getPluginCommand("unwarn").setExecutor(unwarnCommand);
    getServer().getPluginCommand("unwarn").setTabCompleter(unwarnCommand);

    HistoryCommand historyCommand = new HistoryCommand(this);
    getServer().getPluginCommand("history").setExecutor(historyCommand);
    getServer().getPluginCommand("history").setTabCompleter(historyCommand);

    log.info(ChatColor.YELLOW + "Registering listeners");
    getServer().getPluginManager().registerEvents(new PistonChatListener(this), this);
    getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
    getServer().getPluginManager().registerEvents(new CommandBlockListener(this), this);

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
