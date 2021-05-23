package net.pistonmaster.pistonchat.utils;

import lombok.RequiredArgsConstructor;
import net.pistonmaster.pistonchat.PistonChat;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * Parent for both soft and hard banning!
 */
@RequiredArgsConstructor
public class IgnoreTool {
    private final PistonChat plugin;

    public boolean isIgnored(CommandSender chatter, CommandSender receiver) {
        if (plugin.getSoftignoreTool().isSoftIgnored(chatter, receiver)) {
            return true;
        } else return plugin.getConfigTool().isHardIgnored(chatter, receiver);
    }

    public Map<OfflinePlayer, IgnoreType> getIgnoredPlayers(Player player) {
        Map<OfflinePlayer, IgnoreType> map = new HashMap<>();

        for (OfflinePlayer ignoredPlayer : plugin.getSoftignoreTool().getSoftIgnoredPlayers(player)) {
            map.put(ignoredPlayer, IgnoreType.SOFT);
        }

        for (OfflinePlayer ignoredPlayer : plugin.getConfigTool().getHardIgnoredPlayers(player)) {
            map.put(ignoredPlayer, IgnoreType.HARD);
        }

        return map;
    }

    public enum IgnoreType {
        SOFT, HARD
    }
}
