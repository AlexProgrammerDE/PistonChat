package me.alexprogrammerde.pistonchat.utils;

import com.sun.istack.internal.Nullable;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Optional;

public class CacheTool {
    private static final HashMap<Player, PlayerData> map = new HashMap<>();

    public static void sendMessage(Player sender, Player receiver) {
        indexPlayer(sender);
        indexPlayer(receiver);

        map.get(sender).sentTo = receiver;
        map.get(receiver).messagedOf = sender;
    }

    /**
     * Get the last person a player sent a message to.
     * @param player The player to get data from.
     * @return The last person the player sent a message to.
     */
    public static Optional<Player> getLastSentTo(Player player) {
        indexPlayer(player);

        return Optional.ofNullable(map.get(player).sentTo);
    }

    /**
     * Get the last person a player was messaged from.
     * @param player The player to get data from.
     * @return The last person the player was messaged from.
     */
    public static Optional<Player> getLastMessagedOf(Player player) {
        indexPlayer(player);

        return Optional.ofNullable(map.get(player).messagedOf);
    }

    private static void indexPlayer(Player player) {
        if (!map.containsKey(player)) {
            map.put(player, new PlayerData());
        }
    }

    private static class PlayerData {
        public @Nullable Player sentTo = null;
        public @Nullable Player messagedOf = null;
    }
}
