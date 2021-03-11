package net.pistonmaster.pistonchat;

import net.md_5.bungee.api.ChatColor;
import net.pistonmaster.pistonchat.commands.*;
import net.pistonmaster.pistonchat.commands.ignore.HardIgnoreCommand;
import net.pistonmaster.pistonchat.commands.ignore.IgnoreListCommand;
import net.pistonmaster.pistonchat.commands.ignore.SoftIgnoreCommand;
import net.pistonmaster.pistonchat.commands.toggle.ToggleChatCommand;
import net.pistonmaster.pistonchat.commands.toggle.ToggleWhisperingCommand;
import net.pistonmaster.pistonchat.commands.whisper.LastCommand;
import net.pistonmaster.pistonchat.commands.whisper.ReplyCommand;
import net.pistonmaster.pistonchat.commands.whisper.WhisperCommand;
import net.pistonmaster.pistonchat.events.ChatEvent;
import net.pistonmaster.pistonchat.utils.ConfigManager;
import net.pistonmaster.pistonchat.utils.ConfigTool;
import net.pistonmaster.pistonutils.logging.PistonLogger;
import net.pistonmaster.pistonutils.update.UpdateChecker;
import net.pistonmaster.pistonutils.update.UpdateParser;
import net.pistonmaster.pistonutils.update.UpdateType;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.logging.Logger;

public final class PistonChat extends JavaPlugin {
    private final ConfigManager config = new ConfigManager(this, "config.yml");
    private final ConfigManager language = new ConfigManager(this, "language.yml");

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
        try {
            config.create();
            language.create();
        } catch (IOException e) {
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
        }
        ConfigTool.setupTool(this);

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

        if (toggleWhispering != null) {
            toggleWhispering.setExecutor(new ToggleWhisperingCommand());
            toggleWhispering.setTabCompleter(new ToggleWhisperingCommand());
        }

        if (toggleChat != null) {
            toggleChat.setExecutor(new ToggleChatCommand());
            toggleChat.setTabCompleter(new ToggleChatCommand());
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

    @Override
    public FileConfiguration getConfig() {
        return config.get();
    }

    public FileConfiguration getLanguage() {
        return language.get();
    }
}
