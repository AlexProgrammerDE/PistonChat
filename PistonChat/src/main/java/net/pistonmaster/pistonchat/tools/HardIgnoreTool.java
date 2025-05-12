package net.pistonmaster.pistonchat.tools;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.RequiredArgsConstructor;
import net.pistonmaster.pistonchat.PistonChat;
import net.pistonmaster.pistonchat.storage.PCStorage;
import net.pistonmaster.pistonchat.utils.UniqueSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class HardIgnoreTool {
  private final PistonChat plugin;
  private final LoadingCache<IgnorePair, Boolean> ignoreCache = Caffeine.newBuilder()
      .expireAfterWrite(5, TimeUnit.SECONDS)
      .build(this::loadIsHardIgnored);

  public PCStorage.HardReturn hardIgnorePlayer(Player ignoringReceiver, Player ignoredChatter) {
    var hardReturn = plugin.getStorage().hardIgnorePlayer(ignoringReceiver.getUniqueId(), ignoredChatter.getUniqueId());
    return switch (hardReturn) {
      case IGNORE -> {
        ignoreCache.put(new IgnorePair(ignoredChatter.getUniqueId(), ignoringReceiver.getUniqueId()), true);
        yield PCStorage.HardReturn.IGNORE;
      }
      case UN_IGNORE -> {
        ignoreCache.put(new IgnorePair(ignoredChatter.getUniqueId(), ignoringReceiver.getUniqueId()), false);
        yield PCStorage.HardReturn.UN_IGNORE;
      }
    };
  }

  protected boolean isHardIgnored(CommandSender chatter, Player receiver) {
    UUID chatterUUID = new UniqueSender(chatter).getUniqueId();

    return Boolean.TRUE.equals(ignoreCache.get(new IgnorePair(chatterUUID, receiver.getUniqueId())));
  }

  private boolean loadIsHardIgnored(IgnorePair pair) {
    return plugin.getStorage().isHardIgnored(pair.chatter(), pair.receiver());
  }

  public List<UUID> getStoredList(Player player) {
    return plugin.getStorage().getIgnoredList(player.getUniqueId());
  }

  public void clearIgnoredPlayers(Player player) {
    plugin.getStorage().clearIgnoredPlayers(player.getUniqueId());
    ignoreCache.asMap().keySet().removeIf(pair -> pair.chatter().equals(player.getUniqueId()));
  }

  private record IgnorePair(UUID chatter, UUID receiver) {
  }
}
