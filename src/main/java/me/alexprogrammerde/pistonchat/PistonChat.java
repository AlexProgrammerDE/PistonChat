package me.alexprogrammerde.pistonchat;

import me.alexprogrammerde.pistonchat.commands.*;
import me.alexprogrammerde.pistonchat.events.ChatEvent;
import me.alexprogrammerde.pistonchat.utils.ConfigTool;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public final class PistonChat extends JavaPlugin {

    @Override
    public void onEnable() {
        Logger log = getLogger();
        Server server = getServer();

        log.info(ChatColor.GOLD + "  _____  _       _                  _____  _             _   ");
        log.info(ChatColor.GOLD + " |  __ \\(_)     | |                / ____|| |           | |  ");
        log.info(ChatColor.GOLD + " | |__) |_  ___ | |_  ___   _ __  | |     | |__    __ _ | |_ ");
        log.info(ChatColor.GOLD + " |  ___/| |/ __|| __|/ _ \\ | '_ \\ | |     | '_ \\  / _` || __|");
        log.info(ChatColor.GOLD + " | |    | |\\__ \\| |_| (_) || | | || |____ | | | || (_| || |_ ");
        log.info(ChatColor.GOLD + " |_|    |_||___/ \\__|\\___/ |_| |_| \\_____||_| |_| \\__,_| \\__|");

        log.info(ChatColor.GOLD + "Loading config");
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();
        new ConfigTool().setupTool(this);

        log.info(ChatColor.GOLD + "Registering commands");
        PluginCommand ignore = server.getPluginCommand("ignore");
        PluginCommand whisper = server.getPluginCommand("whisper");
        PluginCommand reply = server.getPluginCommand("reply");
        PluginCommand last = server.getPluginCommand("last");
        PluginCommand ignorelist = server.getPluginCommand("ignorelist");
        PluginCommand togglewhispering = server.getPluginCommand("togglewhispering");
        PluginCommand togglechat = server.getPluginCommand("togglechat");

        if (ignore != null) {
            ignore.setExecutor(new IgnoreCommand());
            ignore.setTabCompleter(new IgnoreCommand());
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

        log.info(ChatColor.GOLD + "Registering listeners");
        server.getPluginManager().registerEvents(new ChatEvent(), this);
    }

    @Override
    public void onDisable() {

    }
}
