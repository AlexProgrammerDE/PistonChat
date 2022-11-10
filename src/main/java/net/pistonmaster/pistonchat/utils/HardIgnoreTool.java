package net.pistonmaster.pistonchat.utils;

import com.github.puregero.multilib.MultiLib;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import net.pistonmaster.pistonchat.PistonChat;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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

        MultiLib.setPersistentData(player, "pistonchat_hardignore", gson.toJson(list));

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
        String listData = MultiLib.getPersistentData(player, "pistonchat_hardignore");
        return listData == null ? new ArrayList<>() : gson.<List<String>>fromJson(listData, List.class);
    }

    public enum HardReturn {
        IGNORE, UN_IGNORE
    }
}
