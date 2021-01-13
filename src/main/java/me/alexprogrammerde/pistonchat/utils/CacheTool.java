package me.alexprogrammerde.pistonchat.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

public class CacheTool {
    private static final HashMap<UUID, PlayerData> map = new HashMap<>();

    private CacheTool() {}

    public static void sendMessage(Player sender, Player receiver) {
        indexPlayer(sender);
        indexPlayer(receiver);

        map.get(sender.getUniqueId()).sentTo = receiver.getUniqueId();
        map.get(receiver.getUniqueId()).messagedOf = sender.getUniqueId();
    }

    /**
     * Get the last person a player sent a message to.
     * @param player The player to get data from.
     * @return The last person the player sent a message to.
     */
    public static Optional<Player> getLastSentTo(Player player) {
        indexPlayer(player);

        return Optional.ofNullable(Bukkit.getPlayer(map.get(player.getUniqueId()).sentTo));
    }

    /**
     * Get the last person a player was messaged from.
     * @param player The player to get data from.
     * @return The last person the player was messaged from.
     */
    public static Optional<Player> getLastMessagedOf(Player player) {
        indexPlayer(player);

        return Optional.ofNullable(Bukkit.getPlayer(map.get(player.getUniqueId()).messagedOf));
    }

    private static void indexPlayer(Player player) {
        if (!map.containsKey(player.getUniqueId())) {
            map.put(player.getUniqueId(), new PlayerData());
        }
    }

    private static class PlayerData {
        public @Nullable UUID sentTo = null;
        public @Nullable UUID messagedOf = null;
    }
}
