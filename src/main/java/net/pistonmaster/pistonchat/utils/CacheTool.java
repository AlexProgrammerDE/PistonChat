package net.pistonmaster.pistonchat.utils;

import lombok.RequiredArgsConstructor;
import net.pistonmaster.pistonchat.PistonChat;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class CacheTool {
    private final HashMap<UUID, PlayerData> map = new HashMap<>();
    private final PistonChat plugin;

    public void sendMessage(UniqueSender sender, UniqueSender receiver) {
        index(sender);
        index(receiver);

        map.get(sender.getUniqueId()).sentTo = receiver.getUniqueId();
        map.get(receiver.getUniqueId()).messagedOf = sender.getUniqueId();
    }

    /**
     * Get the last person a player sent a message to.
     *
     * @param player The player to get data from.
     * @return The last person the player sent a message to.
     */
    public Optional<CommandSender> getLastSentTo(UniqueSender player) {
        index(player);
        UUID sentTo = map.get(player.getUniqueId()).sentTo;
        Player nullablePlayer = Bukkit.getPlayer(sentTo);

        if (nullablePlayer == null) {
            return Optional.ofNullable(UniqueSender.byUUID(sentTo));
        } else {
            return Optional.of(nullablePlayer);
        }
    }

    /**
     * Get the last person a player was messaged from.
     *
     * @param player The player to get data from.
     * @return The last person the player was messaged from.
     */
    public Optional<CommandSender> getLastMessagedOf(UniqueSender player) {
        index(player);
        UUID messagedOf = map.get(player.getUniqueId()).messagedOf;
        Player nullablePlayer = Bukkit.getPlayer(messagedOf);

        if (nullablePlayer == null) {
            return Optional.ofNullable(UniqueSender.byUUID(messagedOf));
        } else {
            return Optional.of(nullablePlayer);
        }
    }

    private void index(UniqueSender sender) {
        if (!map.containsKey(sender.getUniqueId())) {
            map.put(sender.getUniqueId(), new PlayerData());
        }
    }

    private static class PlayerData {
        @Nullable
        public UUID sentTo = null;
        @Nullable
        public UUID messagedOf = null;
    }
}
