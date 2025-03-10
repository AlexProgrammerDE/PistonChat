package net.pistonmaster.pistonmute.utils;

import net.pistonmaster.pistonmute.PistonMute;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class StorageTool {
    private static PistonMute plugin;
    private static FileConfiguration dataConfig;
    private static File dataFile;

    private StorageTool() {
    }

    /**
     * Mute a player temporarily!
     *
     * @param player The player to mute.
     * @param date   The date when the player will be unmuted.
     * @return true if player got muted and if already muted false.
     */
    public static boolean tempMutePlayer(Player player, Date date) {
        manageMute(player);

        if (!dataConfig.contains(player.getUniqueId().toString())) {
            dataConfig.set(player.getUniqueId().toString(), date.toString());

            saveData();

            return true;
        } else {
            return false;
        }
    }

    /**
     * Mute a player!
     *
     * @param player The player to mute.
     * @return true if player got muted and if already muted false.
     */
    public static boolean hardMutePlayer(Player player) {
        manageMute(player);

        if (!dataConfig.getStringList("hardmutes").contains(player.getUniqueId().toString())) {
            dataConfig.set("hardmutes", Stream.concat(dataConfig.getStringList("hardmutes").stream(), Stream.of(player.getUniqueId().toString())).collect(Collectors.toList()));

            saveData();

            return true;
        } else {
            return false;
        }
    }

    /**
     * Unmute a player!
     *
     * @param player The player to unmute.
     * @return true if player got unmuted and false if not was muted.
     */
    public static boolean unMutePlayer(OfflinePlayer player) {
        if (dataConfig.contains(player.getUniqueId().toString())) {
            dataConfig.set(player.getUniqueId().toString(), null);

            saveData();

            return true;
        } else if (dataConfig.getStringList("hardmutes").contains(player.getUniqueId().toString())) {
            dataConfig.set("hardmutes", dataConfig.getStringList("hardmutes").stream().filter(uuid -> !uuid.equals(player.getUniqueId().toString())).collect(Collectors.toList()));

            saveData();

            return true;
        } else {

            return false;
        }
    }

    public static boolean isMuted(OfflinePlayer player) {
        manageMute(player);

        return dataConfig.contains(player.getUniqueId().toString()) || dataConfig.getStringList("hardmutes").contains(player.getUniqueId().toString());
    }

    private static void manageMute(OfflinePlayer player) {
        Date now = new Date();

        if (dataConfig.contains(player.getUniqueId().toString())) {
            SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", new Locale("us"));

            try {
                Date date = sdf.parse(dataConfig.getString(player.getUniqueId().toString()));

                if (now.after(date) || (now.equals(date))) {
                    unMutePlayer(player);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
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
        if (!plugin.getDataFolder().exists() && !plugin.getDataFolder().mkdir())
            new IOException("File already exists.").printStackTrace();

        if (!dataFile.exists()) {
            try {
                if (!dataFile.createNewFile())
                    new IOException("File already exists.").printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void setupTool(PistonMute plugin) {
        if (plugin == null || StorageTool.plugin != null)
            return;

        StorageTool.plugin = plugin;
        StorageTool.dataFile = new File(plugin.getDataFolder(), "data.yml");

        loadData();
    }
}
