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

    public void sendMessage(CommandSender sender, CommandSender receiver) {
        index(sender);
        index(receiver);

        map.get(new UniqueSender(sender).getUniqueId()).sentTo = new UniqueSender(receiver).getUniqueId();
        map.get(new UniqueSender(receiver).getUniqueId()).messagedOf = new UniqueSender(sender).getUniqueId();
    }

    /**
     * Get the last person a player sent a message to.
     *
     * @param sender The player to get data from.
     * @return The last person the player sent a message to.
     */
    public Optional<CommandSender> getLastSentTo(CommandSender sender) {
        index(sender);
        UUID sentTo = map.get(new UniqueSender(sender).getUniqueId()).sentTo;
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
     * @param sender The player to get data from.
     * @return The last person the player was messaged from.
     */
    public Optional<CommandSender> getLastMessagedOf(CommandSender sender) {
        index(sender);
        UUID messagedOf = map.get(new UniqueSender(sender).getUniqueId()).messagedOf;
        Player nullablePlayer = Bukkit.getPlayer(messagedOf);

        if (nullablePlayer == null) {
            return Optional.ofNullable(UniqueSender.byUUID(messagedOf));
        } else {
            return Optional.of(nullablePlayer);
        }
    }

    private void index(CommandSender sender) {
        if (!map.containsKey(new UniqueSender(sender).getUniqueId())) {
            map.put(new UniqueSender(sender).getUniqueId(), new PlayerData());
        }
    }

    private static class PlayerData {
        @Nullable
        public UUID sentTo = null;
        @Nullable
        public UUID messagedOf = null;
    }
}
