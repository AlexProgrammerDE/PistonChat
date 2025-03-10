package net.pistonmaster.pistonfilter;

import net.md_5.bungee.api.ChatColor;
import net.pistonmaster.pistonfilter.commands.FilterCommand;
import net.pistonmaster.pistonfilter.listeners.ChatListener;
import net.pistonmaster.pistonutils.logging.PistonLogger;
import net.pistonmaster.pistonutils.update.UpdateChecker;
import net.pistonmaster.pistonutils.update.UpdateParser;
import net.pistonmaster.pistonutils.update.UpdateType;
import org.bstats.bukkit.Metrics;
import org.bukkit.Server;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

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
        new UpdateChecker(new PistonLogger(getLogger()::info, getLogger()::warning)).getVersion("https://www.pistonmaster.net/PistonFilter/VERSION.txt", version -> new UpdateParser(getDescription().getVersion(), version).parseUpdate(updateType -> {
            if (updateType == UpdateType.NONE || updateType == UpdateType.AHEAD) {
                log.info(ChatColor.AQUA + "You're up to date!");
            } else {
                if (updateType == UpdateType.MAJOR) {
                    log.info(ChatColor.RED + "There is a MAJOR update available!");
                } else if (updateType == UpdateType.MINOR) {
                    log.info(ChatColor.RED + "There is a MINOR update available!");
                } else if (updateType == UpdateType.PATCH) {
                    log.info(ChatColor.RED + "There is a PATCH update available!");
                }

                log.info(ChatColor.RED + "Current version: " + this.getDescription().getVersion() + " New version: " + version);
                log.info(ChatColor.RED + "Download it at: https://github.com/AlexProgrammerDE/PistonFilter/releases");
            }
        }));

        log.info(ChatColor.AQUA + "Done! :D");
    }
}
