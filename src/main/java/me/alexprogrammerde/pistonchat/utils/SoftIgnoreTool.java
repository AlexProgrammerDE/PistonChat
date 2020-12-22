package me.alexprogrammerde.pistonchat.utils;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class SoftIgnoreTool {
    private static final HashMap<UUID, List<UUID>> map = new HashMap<>();

    public static IgnoreType softIgnorePlayer(Player player, Player ignored) {
        indexPlayer(player);
        indexPlayer(ignored);

        List<UUID> list = map.get(player.getUniqueId());

        if (list.contains(ignored.getUniqueId())) {
            list.remove(ignored.getUniqueId());

            return IgnoreType.UNIGNORE;
        } else {
            list.add(ignored.getUniqueId());

            return IgnoreType.IGNORE;
        }
    }

    protected static boolean isSoftIgnored(Player chatter, Player receiver) {
        indexPlayer(receiver);
        indexPlayer(chatter);

        return map.get(receiver.getUniqueId()).contains(chatter.getUniqueId());
    }

    public static List<OfflinePlayer> getSoftIgnoredPlayers(Player player) {
        indexPlayer(player);

        List<UUID> listUUID = map.get(player.getUniqueId());

        List<OfflinePlayer> returnedPlayers = new ArrayList<>();

        for (UUID uuid : listUUID) {
            returnedPlayers.add(Bukkit.getOfflinePlayer(uuid));
        }

        return returnedPlayers;
    }

    private static void indexPlayer(Player player) {
        if (!map.containsKey(player.getUniqueId())) {
            map.put(player.getUniqueId(), new ArrayList<>());
        }
    }

    public enum IgnoreType {
        IGNORE, UNIGNORE
    }
}
