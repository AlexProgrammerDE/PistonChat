package net.pistonmaster.pistonfilter;

import net.md_5.bungee.api.ChatColor;
import net.pistonmaster.pistonfilter.commands.FilterCommand;
import net.pistonmaster.pistonfilter.listeners.ChatListener;
import net.pistonmaster.pistonutils.update.GitHubUpdateChecker;
import net.pistonmaster.pistonutils.update.SemanticVersion;
import org.bstats.bukkit.Metrics;
import org.bukkit.Server;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.logging.Logger;

public class PistonFilter extends JavaPlugin {
    @Override
    public void onEnable() {
        Logger log = getLogger();
        Server server = getServer();

        log.info(ChatColor.AQUA + "Loading config");
        saveDefaultConfig();

        log.info(ChatColor.AQUA + "Registering commands");
        PluginCommand main = server.getPluginCommand("pistonfilter");

        assert main != null;
        main.setExecutor(new FilterCommand(this));
        main.setTabCompleter(new FilterCommand(this));

        log.info(ChatColor.AQUA + "Registering listeners");
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);

        log.info(ChatColor.AQUA + "Loading metrics");
        new Metrics(this, 11561);

        log.info(ChatColor.AQUA + "Checking for a newer version");
        try {
            String currentVersionString = this.getDescription().getVersion();
            SemanticVersion gitHubVersion = new GitHubUpdateChecker()
                    .getVersion("https://api.github.com/repos/AlexProgrammerDE/PistonChat/releases/latest");
            SemanticVersion currentVersion = SemanticVersion.fromString(currentVersionString);

            if (gitHubVersion.isNewerThan(currentVersion)) {
                log.info(ChatColor.RED + "There is an update available!");
                log.info(ChatColor.RED + "Current version: " + currentVersionString + " New version: " + gitHubVersion);
                log.info(ChatColor.RED + "Download it at: https://github.com/AlexProgrammerDE/PistonChat/releases");
            } else {
                log.info(ChatColor.AQUA + "You're up to date!");
            }
        } catch (IOException e) {
            log.severe("Could not check for updates!");
            e.printStackTrace();
        }

        log.info(ChatColor.AQUA + "Done! :D");
    }
}
