package net.pistonmaster.pistonchat.tools;

import lombok.RequiredArgsConstructor;
import net.pistonmaster.pistonchat.PistonChat;
import net.pistonmaster.pistonchat.utils.PlatformUtils;
import net.pistonmaster.pistonchat.utils.UniqueSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import java.util.*;

@RequiredArgsConstructor
public class CacheTool {
  private final PistonChat plugin;
  private final Map<CommandSender, MessageData> customMap = new HashMap<>();

  public void sendMessage(CommandSender sender, CommandSender receiver) {
    UUID senderUUID = new UniqueSender(sender).getUniqueId();
    UUID receiverUUID = new UniqueSender(receiver).getUniqueId();
    if (sender instanceof Player senderPlayer) {
      senderPlayer.setMetadata("pistonchat_sentTo", new FixedMetadataValue(plugin, receiverUUID.toString()));
    } else {
      indexConsole(sender);
      customMap.get(sender).sentTo = receiverUUID;
    }

    if (receiver instanceof Player receiverPlayer) {
      receiverPlayer.setMetadata("pistonchat_messagedOf", new FixedMetadataValue(plugin, senderUUID.toString()));
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
    if (sender instanceof Player player) {
      List<MetadataValue> values = player.getMetadata("pistonchat_sentTo");
      if (values.isEmpty()) {
        return Optional.empty();
      }

      sentTo = UUID.fromString(values.getFirst().asString());
    } else {
      indexConsole(sender);
      sentTo = customMap.get(sender).sentTo;
    }

    if (sentTo == null) {
      return Optional.empty();
    }

    Optional<CommandSender> optionalPlayer = PlatformUtils.getPlayer(sentTo).map(p -> p); // Map to bypass type inference error
    return optionalPlayer.or(() -> UniqueSender.byUUID(sentTo));
  }

  /**
   * Get the last person a player was messaged from.
   *
   * @param sender The player to get data from.
   * @return The last person the player was messaged from.
   */
  public Optional<CommandSender> getLastMessagedOf(CommandSender sender) {
    UUID messagedOf;
    if (sender instanceof Player player) {
      List<MetadataValue> values = player.getMetadata("pistonchat_messagedOf");
      if (values.isEmpty()) {
        return Optional.empty();
      }

      messagedOf = UUID.fromString(values.getFirst().asString());
    } else {
      indexConsole(sender);
      messagedOf = customMap.get(sender).messagedOf;
    }

    if (messagedOf == null) {
      return Optional.empty();
    }

    Optional<CommandSender> optionalPlayer = PlatformUtils.getPlayer(messagedOf).map(p -> p); // Map to bypass type inference error
    return optionalPlayer.or(() -> UniqueSender.byUUID(messagedOf));
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
