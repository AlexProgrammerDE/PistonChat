package net.pistonmaster.pistonchat.tools;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import net.pistonmaster.pistonchat.PistonChat;
import net.pistonmaster.pistonchat.utils.UniqueSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class HardIgnoreTool {
    private final PistonChat plugin;
    private final Gson gson = new Gson();

    public HardReturn hardIgnorePlayer(Player player, Player ignored) {
        List<String> list = getStoredList(player);

        boolean contains = list.contains(ignored.getUniqueId().toString());
        if (contains) {
            list.remove(ignored.getUniqueId().toString());
        } else {
            list.add(ignored.getUniqueId().toString());
        }

        Path hardIgnorePath = plugin.getPlayerDataFolder().resolve(player.getUniqueId() + ".hardignored");
        try {
            Files.writeString(hardIgnorePath, gson.toJson(list));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return contains ? HardReturn.UN_IGNORE : HardReturn.IGNORE;
    }

    protected boolean isHardIgnored(CommandSender chatter, Player receiver) {
        UUID chatterUUID = new UniqueSender(chatter).getUniqueId();
        return getStoredList(receiver).contains(chatterUUID.toString());
    }

    public String getPreparedString(String str, Player player) {
        return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString(str)
                .replace("%player%", ChatColor.stripColor(player.getDisplayName())));
    }

    protected List<String> getStoredList(Player player) {
        Path hardIgnorePath = plugin.getPlayerDataFolder().resolve(player.getUniqueId() + ".hardignored");
        if (!Files.exists(hardIgnorePath)) {
            return new ArrayList<>();
        }

        try {
            return gson.<List<String>>fromJson(Files.readString(hardIgnorePath), List.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public enum HardReturn {
        IGNORE, UN_IGNORE
    }
}
