package net.pistonmaster.pistonchat.utils;

import lombok.Getter;
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
    private final File dataFolder;

    public ConfigTool(PistonChat plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "data");
    }

    public HardReturn hardIgnorePlayer(Player player, Player ignored) {
        PlayerDataManager playerData = new PlayerDataManager(player.getUniqueId());
        List<String> list = playerData.getDataConfig().getStringList(player.getUniqueId().toString());

        if (list.contains(ignored.getUniqueId().toString())) {
            list.remove(ignored.getUniqueId().toString());

            playerData.getDataConfig().set(player.getUniqueId().toString(), list);

            playerData.saveData();

            return HardReturn.UN_IGNORE;
        } else {
            list.add(ignored.getUniqueId().toString());

            playerData.getDataConfig().set(player.getUniqueId().toString(), list);

            playerData.saveData();

            return HardReturn.IGNORE;
        }
    }

    protected boolean isHardIgnored(CommandSender chatter, CommandSender receiver) {
        UUID receiverUUID = new UniqueSender(receiver).getUniqueId();
        UUID chatterUUID = new UniqueSender(chatter).getUniqueId();
        PlayerDataManager playerData = new PlayerDataManager(receiverUUID);

        return playerData.getDataConfig().getStringList(receiverUUID.toString()).contains(chatterUUID.toString());
    }

    protected List<OfflinePlayer> getHardIgnoredPlayers(Player player) {
        PlayerDataManager playerData = new PlayerDataManager(player.getUniqueId());
        List<String> listUUID = playerData.getDataConfig().getStringList(player.getUniqueId().toString());

        List<OfflinePlayer> returnedPlayers = new ArrayList<>();

        for (String str : listUUID) {
            returnedPlayers.add(Bukkit.getOfflinePlayer(UUID.fromString(str)));
        }

        return returnedPlayers;
    }

    public String getPreparedString(String str, Player player) {
        return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString(str)
                .replace("%player%", ChatColor.stripColor(player.getDisplayName())));
    }

    public enum HardReturn {
        IGNORE, UN_IGNORE
    }

    private class PlayerDataManager {
        private final File playerFile;
        @Getter
        private FileConfiguration dataConfig;

        public PlayerDataManager(UUID playerUuid) {
            this.playerFile = new File(dataFolder, playerUuid.toString() + ".yml");
            loadData();
        }

        private void loadData() {
            generateFile();

            dataConfig = YamlConfiguration.loadConfiguration(playerFile);
        }

        private void saveData() {
            generateFile();

            try {
                dataConfig.save(playerFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void generateFile() {
            if (!plugin.getDataFolder().exists() && !plugin.getDataFolder().mkdir())
                return;

            if (!playerFile.exists()) {
                try {
                    if (!playerFile.createNewFile())
                        throw new IOException("Couldn't create file " + playerFile.getAbsolutePath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
