package me.alexprogrammerde.pistonchat;

import me.alexprogrammerde.pistonchat.commands.*;
import me.alexprogrammerde.pistonchat.events.ChatEvent;
import me.alexprogrammerde.pistonchat.utils.ConfigTool;
import me.alexprogrammerde.pistonutils.PistonLogger;
import me.alexprogrammerde.pistonutils.UpdateChecker;
import me.alexprogrammerde.pistonutils.UpdateParser;
import me.alexprogrammerde.pistonutils.UpdateType;
import net.md_5.bungee.api.ChatColor;
import org.bstats.bukkit.Metrics;
import org.bukkit.Server;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public final class PistonChat extends JavaPlugin {
    @Override
    public void onEnable() {
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
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();
        new ConfigTool().setupTool(this);

        log.info(ChatColor.DARK_GREEN + "Registering commands");
        PluginCommand ignorehard = server.getPluginCommand("ignorehard");
        PluginCommand ignore = server.getPluginCommand("ignore");
        PluginCommand whisper = server.getPluginCommand("whisper");
        PluginCommand reply = server.getPluginCommand("reply");
        PluginCommand last = server.getPluginCommand("last");
        PluginCommand ignorelist = server.getPluginCommand("ignorelist");
        PluginCommand togglewhispering = server.getPluginCommand("togglewhispering");
        PluginCommand togglechat = server.getPluginCommand("togglechat");
        PluginCommand main = server.getPluginCommand("pistonchat");

        if (ignorehard != null) {
            ignorehard.setExecutor(new HardIgnoreCommand());
            ignorehard.setTabCompleter(new HardIgnoreCommand());
        }

        if (ignore != null) {
            ignore.setExecutor(new SoftIgnoreCommand());
            ignore.setTabCompleter(new SoftIgnoreCommand());
        }

        if (whisper != null) {
            whisper.setExecutor(new WhisperCommand());
            whisper.setTabCompleter(new WhisperCommand());
        }

        if (reply != null) {
            reply.setExecutor(new ReplyCommand());
            reply.setTabCompleter(new ReplyCommand());
        }

        if (last != null) {
            last.setExecutor(new LastCommand());
            last.setTabCompleter(new LastCommand());
        }

        if (ignorelist != null) {
            ignorelist.setExecutor(new IgnoreListCommand());
            ignorelist.setTabCompleter(new IgnoreListCommand());
        }

        if (togglewhispering != null) {
            togglewhispering.setExecutor(new ToggleWhisperingCommand());
            togglewhispering.setTabCompleter(new ToggleWhisperingCommand());
        }

        if (togglechat != null) {
            togglechat.setExecutor(new ToggleChatCommand());
            togglechat.setTabCompleter(new ToggleChatCommand());
        }

        if (main != null) {
            main.setExecutor(new MainCommand(this));
            main.setTabCompleter(new MainCommand(this));
        }

        log.info(ChatColor.DARK_GREEN + "Registering listeners");
        server.getPluginManager().registerEvents(new ChatEvent(), this);

        log.info(ChatColor.DARK_GREEN + "Checking for a newer version");
        new UpdateChecker(new PistonLogger(getLogger())).getVersion("https://www.pistonmaster.net/PistonChat/VERSION.txt", version -> new UpdateParser(getDescription().getVersion(), version).parseUpdate(updateType -> {
            if (updateType == UpdateType.NONE || updateType == UpdateType.AHEAD) {
                log.info(ChatColor.DARK_GREEN + "Your up to date!");
            } else {
                if (updateType == UpdateType.MAJOR) {
                    log.info(ChatColor.RED + "There is a MAJOR update available!");
                } else if (updateType == UpdateType.MINOR) {
                    log.info(ChatColor.RED + "There is a MINOR update available!");
                } else if (updateType == UpdateType.PATCH) {
                    log.info(ChatColor.RED + "There is a PATCH update available!");
                }

                log.info(ChatColor.RED + "Current version: " + this.getDescription().getVersion() + " New version: " + version);
                log.info(ChatColor.RED + "Download it at: https://github.com/AlexProgrammerDE/PistonChat/releases");
            }
        }));

        log.info(ChatColor.DARK_GREEN + "Loading metrics");
        new Metrics(this, 9630);

        log.info(ChatColor.DARK_GREEN + "Done! :D");
    }
}
