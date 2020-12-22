package me.alexprogrammerde.pistonchat.utils;

import me.alexprogrammerde.pistonchat.PistonChat;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ConfigTool {
    public static PistonChat plugin;
    private static FileConfiguration dataConfig;
    private static File dataFile;

    public static IgnoreType hardIgnorePlayer(Player player, Player ignored) {
        List<String> list = dataConfig.getStringList(player.getUniqueId().toString());

        if (list.contains(ignored.getUniqueId().toString())) {
            list.remove(ignored.getUniqueId().toString());

            dataConfig.set(player.getUniqueId().toString(), list);

            saveData();

            return IgnoreType.UNIGNORE;
        } else {
            list.add(ignored.getUniqueId().toString());

            dataConfig.set(player.getUniqueId().toString(), list);

            saveData();

            return IgnoreType.IGNORE;
        }
    }

    public static boolean isHardIgnored(Player chatter, Player receiver) {
        return dataConfig.getStringList(receiver.getUniqueId().toString()).contains(chatter.getUniqueId().toString());
    }

    public static List<String> getHardIgnoredPlayers(Player player) {
        List<String> listUUID = dataConfig.getStringList(player.getUniqueId().toString());

        List<String> returnedNames = new ArrayList<>();

        for (String str : listUUID) {
            returnedNames.add(Bukkit.getOfflinePlayer(UUID.fromString(str)).getName());
        }

        return returnedNames;
    }

    private static void loadData() {
        generateFile();

        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    private static void saveData() {
        generateFile();

        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void generateFile() {
        if (!plugin.getDataFolder().exists()) {
            if (!plugin.getDataFolder().mkdir())
                return;
        }

        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setupTool(PistonChat plugin) {
        ConfigTool.plugin = plugin;
        ConfigTool.dataFile = new File(plugin.getDataFolder(), "data.yml");

        loadData();
    }

    public enum IgnoreType {
        IGNORE, UNIGNORE
    }

    public static FileConfiguration getPluginConfig() {
        return plugin.getConfig();
    }

    public static String getPreparedString(String str) {
        return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString(str));
    }
}
