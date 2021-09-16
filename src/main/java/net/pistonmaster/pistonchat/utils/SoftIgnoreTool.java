package net.pistonmaster.pistonchat.utils;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class SoftIgnoreTool {
    private final Map<UUID, List<UUID>> map = new HashMap<>();

    public SoftReturn softIgnorePlayer(Player player, Player ignored) {
        map.putIfAbsent(player.getUniqueId(), new ArrayList<>());

        List<UUID> list = map.get(player.getUniqueId());

        if (list.contains(ignored.getUniqueId())) {
            list.remove(ignored.getUniqueId());

            return SoftReturn.UN_IGNORE;
        } else {
            list.add(ignored.getUniqueId());

            return SoftReturn.IGNORE;
        }
    }

    protected boolean isSoftIgnored(CommandSender chatter, CommandSender receiver) {
        return map.containsKey(new UniqueSender(receiver).getUniqueId()) && map.get(new UniqueSender(receiver).getUniqueId()).contains(new UniqueSender(chatter).getUniqueId());
    }

    protected List<OfflinePlayer> getSoftIgnoredPlayers(Player player) {
        List<UUID> listUUID = map.getOrDefault(player.getUniqueId(), Collections.emptyList());

        List<OfflinePlayer> returnedPlayers = new ArrayList<>();

        for (UUID uuid : listUUID) {
            returnedPlayers.add(Bukkit.getOfflinePlayer(uuid));
        }

        return returnedPlayers;
    }

    public enum SoftReturn {
        IGNORE, UN_IGNORE
    }
}
