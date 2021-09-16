package net.pistonmaster.pistonchat.utils;

import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import net.pistonmaster.pistonchat.PistonChat;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class ConfigTool {
    private final PistonChat plugin;
    private final File dataFile;
    private FileConfiguration dataConfig;

    public ConfigTool(PistonChat plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "data.yml");

        loadData();
    }

    public HardReturn hardIgnorePlayer(Player player, Player ignored) {
        List<String> list = dataConfig.getStringList(player.getUniqueId().toString());

        if (list.contains(ignored.getUniqueId().toString())) {
            list.remove(ignored.getUniqueId().toString());

            dataConfig.set(player.getUniqueId().toString(), list);

            saveData();

            return HardReturn.UN_IGNORE;
        } else {
            list.add(ignored.getUniqueId().toString());

            dataConfig.set(player.getUniqueId().toString(), list);

            saveData();

            return HardReturn.IGNORE;
        }
    }

    protected boolean isHardIgnored(CommandSender chatter, CommandSender receiver) {
        return dataConfig.getStringList(new UniqueSender(receiver).getUniqueId().toString()).contains(new UniqueSender(chatter).getUniqueId().toString());
    }

    protected List<OfflinePlayer> getHardIgnoredPlayers(Player player) {
        List<String> listUUID = dataConfig.getStringList(player.getUniqueId().toString());

        List<OfflinePlayer> returnedPlayers = new ArrayList<>();

        for (String str : listUUID) {
            returnedPlayers.add(Bukkit.getOfflinePlayer(UUID.fromString(str)));
        }

        return returnedPlayers;
    }

    private void loadData() {
        generateFile();

        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    private void saveData() {
        generateFile();

        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void generateFile() {
        if (!plugin.getDataFolder().exists() && !plugin.getDataFolder().mkdir())
            return;

        if (!dataFile.exists()) {
            try {
                if (!dataFile.createNewFile())
                    throw new IOException("Couldn't create file " + dataFile.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String getPreparedString(String str, Player player) {
        return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString(str)
                .replace("%player%", ChatColor.stripColor(player.getDisplayName())));
    }

    public enum HardReturn {
        IGNORE, UN_IGNORE
    }
}
