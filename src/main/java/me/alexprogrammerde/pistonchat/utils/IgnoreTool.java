package me.alexprogrammerde.pistonchat.utils;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * Parent for both soft and hard banning!
 */
public class IgnoreTool {
    public static boolean isIgnored(Player chatter, Player receiver) {
        if (SoftIgnoreTool.isSoftIgnored(chatter, receiver)) {
            return true;
        } else return ConfigTool.isHardIgnored(chatter, receiver);
    }

    public static Map<OfflinePlayer, IgnoreType> getIgnoredPlayers(Player player) {
        HashMap<OfflinePlayer, IgnoreType> map = new HashMap<>();

        for (OfflinePlayer ignoredPlayer : SoftIgnoreTool.getSoftIgnoredPlayers(player)) {
            map.put(ignoredPlayer, IgnoreType.SOFT);
        }

        for (OfflinePlayer ignoredPlayer : ConfigTool.getHardIgnoredPlayers(player)) {
            map.put(ignoredPlayer, IgnoreType.HARD);
        }

        return map;
    }

    public enum IgnoreType {
        SOFT, HARD
    }
}
