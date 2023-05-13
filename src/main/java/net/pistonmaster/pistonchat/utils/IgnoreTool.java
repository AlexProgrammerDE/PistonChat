package net.pistonmaster.pistonchat.utils;

import lombok.RequiredArgsConstructor;
import net.pistonmaster.pistonchat.PistonChat;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Parent for both soft and hard ignoring!
 */
@RequiredArgsConstructor
public class IgnoreTool {
    private final PistonChat plugin;

    public boolean isIgnored(CommandSender chatter, CommandSender receiver) {
        if (receiver instanceof Player player) {
            if (plugin.getSoftignoreTool().isSoftIgnored(chatter, player)) {
                return true;
            } else return plugin.getHardIgnoreTool().isHardIgnored(chatter, player);
        } else {
            return false;
        }
    }

    public Map<OfflinePlayer, IgnoreType> getIgnoredPlayers(Player player) {
        Map<OfflinePlayer, IgnoreType> map = new HashMap<>();

        for (OfflinePlayer ignoredPlayer : convertIgnoredPlayer(plugin.getSoftignoreTool().getStoredList(player))) {
            map.put(ignoredPlayer, IgnoreType.SOFT);
        }

        for (OfflinePlayer ignoredPlayer : convertIgnoredPlayer(plugin.getHardIgnoreTool().getStoredList(player))) {
            map.put(ignoredPlayer, IgnoreType.HARD);
        }

        return map;
    }

    protected List<OfflinePlayer> convertIgnoredPlayer(List<String> listUUID) {
        List<OfflinePlayer> returnedPlayers = new ArrayList<>();

        for (String str : listUUID) {
            returnedPlayers.add(Bukkit.getOfflinePlayer(UUID.fromString(str)));
        }

        return returnedPlayers;
    }

    public enum IgnoreType {
        SOFT, HARD
    }
}
