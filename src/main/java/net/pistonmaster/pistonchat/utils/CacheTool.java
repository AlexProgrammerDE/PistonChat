package net.pistonmaster.pistonchat.utils;

import com.github.puregero.multilib.MultiLib;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class CacheTool {
    private final Map<CommandSender, MessageData> customMap = new HashMap<>();

    public void sendMessage(CommandSender sender, CommandSender receiver) {
        UUID senderUUID = new UniqueSender(sender).getUniqueId();
        UUID receiverUUID = new UniqueSender(receiver).getUniqueId();
        if (sender instanceof Player) {
            MultiLib.setData((Player) sender, "sentTo", receiverUUID.toString());
        } else {
            indexConsole(sender);
            customMap.get(sender).sentTo = receiverUUID;
        }
        if (receiver instanceof Player) {
            MultiLib.setData((Player) receiver, "messagedOf", senderUUID.toString());
        } else {
            indexConsole(receiver);
            customMap.get(receiver).messagedOf = senderUUID;
        }
    }

    /**
     * Get the last person a player sent a message to.
     *
     * @param sender The player to get data from.
     * @return The last person the player sent a message to.
     */
    public Optional<CommandSender> getLastSentTo(CommandSender sender) {
        UUID sentTo;
        if (sender instanceof Player) {
            String sentToUUID = MultiLib.getData((Player) sender, "sentTo");
            if (sentToUUID == null) {
                return Optional.empty();
            } else {
                sentTo = UUID.fromString(sentToUUID);
            }
        } else {
            indexConsole(sender);
            sentTo = customMap.get(sender).sentTo;
        }

        if (sentTo == null) {
            return Optional.empty();
        }

        Optional<Player> optionalPlayer = PlatformUtils.getPlayer(sentTo);

        return optionalPlayer.<Optional<CommandSender>>map(Optional::of).orElseGet(() -> UniqueSender.byUUID(sentTo));
    }

    /**
     * Get the last person a player was messaged from.
     *
     * @param sender The player to get data from.
     * @return The last person the player was messaged from.
     */
    public Optional<CommandSender> getLastMessagedOf(CommandSender sender) {
        UUID messagedOf;
        if (sender instanceof Player) {
            String messagedOfUUID = MultiLib.getData((Player) sender, "messagedOf");
            if (messagedOfUUID == null) {
                return Optional.empty();
            } else {
                messagedOf = UUID.fromString(messagedOfUUID);
            }
        } else {
            indexConsole(sender);
            messagedOf = customMap.get(sender).messagedOf;
        }

        if (messagedOf == null) {
            return Optional.empty();
        }

        Optional<Player> optionalPlayer = PlatformUtils.getPlayer(messagedOf);

        return optionalPlayer.<Optional<CommandSender>>map(Optional::of).orElseGet(() -> UniqueSender.byUUID(messagedOf));
    }

    private void indexConsole(CommandSender sender) {
        customMap.computeIfAbsent(sender, k -> new MessageData());
    }

    @RequiredArgsConstructor
    private static class MessageData {
        public UUID sentTo = null;
        public UUID messagedOf = null;
    }
}
