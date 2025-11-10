package net.pistonmaster.pistonchat.tools;

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
      } else {
        return plugin.getHardIgnoreTool().isHardIgnored(chatter, player);
      }
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

  protected List<OfflinePlayer> convertIgnoredPlayer(List<UUID> listUUID) {
    List<OfflinePlayer> returnedPlayers = new ArrayList<>();

    for (UUID str : listUUID) {
      returnedPlayers.add(Bukkit.getOfflinePlayer(str));
    }

    return returnedPlayers;
  }

  public void clearIgnoredPlayers(Player player) {
    plugin.getSoftignoreTool().clearIgnoredPlayers(player);
    plugin.getHardIgnoreTool().clearIgnoredPlayers(player);
  }

  public enum IgnoreType {
    SOFT, HARD
  }
}
