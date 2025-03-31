package net.pistonmaster.pistonmute;

import net.md_5.bungee.api.ChatColor;
import net.pistonmaster.pistonmute.commands.MuteCommand;
import net.pistonmaster.pistonmute.commands.UnMuteCommand;
import net.pistonmaster.pistonmute.listeners.PistonChatListener;
import net.pistonmaster.pistonmute.utils.StorageTool;
import net.pistonmaster.pistonutils.update.GitHubUpdateChecker;
import net.pistonmaster.pistonutils.update.SemanticVersion;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.logging.Logger;

public final class PistonMute extends JavaPlugin {
    @Override
    public void onEnable() {
        Logger log = getLogger();

        log.info(ChatColor.YELLOW + "Loading config");
        saveDefaultConfig();
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
        try {
            String currentVersionString = this.getDescription().getVersion();
            SemanticVersion gitHubVersion = new GitHubUpdateChecker()
                    .getVersion("https://api.github.com/repos/AlexProgrammerDE/PistonChat/releases/latest");
            SemanticVersion currentVersion = SemanticVersion.fromString(currentVersionString);

            if (gitHubVersion.isNewerThan(currentVersion)) {
                log.info(ChatColor.YELLOW + "You're up to date!");
            } else {
                log.info(ChatColor.RED + "There is an update available!");
                log.info(ChatColor.RED + "Current version: " + currentVersionString + " New version: " + gitHubVersion);
                log.info(ChatColor.RED + "Download it at: https://github.com/AlexProgrammerDE/PistonChat/releases");
            }
        } catch (IOException e) {
            log.severe("Could not check for updates!");
            e.printStackTrace();
        }

        getLogger().info(ChatColor.YELLOW + "Done! :D");
    }
}
