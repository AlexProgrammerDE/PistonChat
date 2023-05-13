package net.pistonmaster.pistonchat.tools;

import lombok.RequiredArgsConstructor;
import net.pistonmaster.pistonchat.PistonChat;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@RequiredArgsConstructor
public class TempDataTool {
    private final PistonChat plugin;

    public void setWhisperingEnabled(Player player, boolean value) {
        try {
            Path whisperingFile = plugin.getPlayerDataFolder().resolve(player.getUniqueId() + "_whispering");
            Files.writeString(whisperingFile, String.valueOf(value));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setChatEnabled(Player player, boolean value) {
        try {
            Path chatFile = plugin.getPlayerDataFolder().resolve(player.getUniqueId() + "_chat");
            Files.writeString(chatFile, String.valueOf(value));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isWhisperingEnabled(Player player) {
        Path whisperingFile = plugin.getPlayerDataFolder().resolve(player.getUniqueId() + "_whispering");
        if (Files.exists(whisperingFile)) {
            try {
                return Boolean.parseBoolean(Files.readString(whisperingFile));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            return true;
        }
    }

    public boolean isChatEnabled(Player player) {
        Path chatFile = plugin.getPlayerDataFolder().resolve(player.getUniqueId() + "_chat");
        if (Files.exists(chatFile)) {
            try {
                return Boolean.parseBoolean(Files.readString(chatFile));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            return true;
        }
    }
}
