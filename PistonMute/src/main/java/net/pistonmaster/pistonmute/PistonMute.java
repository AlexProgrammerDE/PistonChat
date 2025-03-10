package net.pistonmaster.pistonmute;

import net.md_5.bungee.api.ChatColor;
import net.pistonmaster.pistonmute.commands.MuteCommand;
import net.pistonmaster.pistonmute.commands.UnMuteCommand;
import net.pistonmaster.pistonmute.listeners.PistonChatListener;
import net.pistonmaster.pistonmute.utils.StorageTool;
import net.pistonmaster.pistonutils.logging.PistonLogger;
import net.pistonmaster.pistonutils.update.UpdateChecker;
import net.pistonmaster.pistonutils.update.UpdateParser;
import net.pistonmaster.pistonutils.update.UpdateType;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

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
        new UpdateChecker(new PistonLogger(getLogger()::info, getLogger()::warning)).getVersion("https://www.pistonmaster.net/PistonMute/VERSION.txt", version -> new UpdateParser(getDescription().getVersion(), version).parseUpdate(updateType -> {
            if (updateType == UpdateType.NONE || updateType == UpdateType.AHEAD) {
                log.info(ChatColor.YELLOW + "You're up to date!");
            } else {
                if (updateType == UpdateType.MAJOR) {
                    log.info(ChatColor.RED + "There is a MAJOR update available!");
                } else if (updateType == UpdateType.MINOR) {
                    log.info(ChatColor.RED + "There is a MINOR update available!");
                } else if (updateType == UpdateType.PATCH) {
                    log.info(ChatColor.RED + "There is a PATCH update available!");
                }

                log.info(ChatColor.RED + "Current version: " + this.getDescription().getVersion() + " New version: " + version);
                log.info(ChatColor.RED + "Download it at: https://github.com/AlexProgrammerDE/PistonMute/releases");
            }
        }));

        getLogger().info(ChatColor.YELLOW + "Done! :D");
    }
}
