package net.pistonmaster.pistonchat.tools;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.RequiredArgsConstructor;
import net.pistonmaster.pistonchat.PistonChat;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class TempDataTool {
  private final PistonChat plugin;
  private final LoadingCache<UUID, Boolean> whisperCache = Caffeine.newBuilder()
      .expireAfterWrite(5, TimeUnit.SECONDS)
      .build(this::loadIsWhisperingEnabled);
  private final LoadingCache<UUID, Boolean> chatCache = Caffeine.newBuilder()
      .expireAfterWrite(5, TimeUnit.SECONDS)
      .build(this::loadIsChatEnabled);

  public void setWhisperingEnabled(Player player, boolean value) {
    plugin.getStorage().setWhisperingEnabled(player.getUniqueId(), value);
    whisperCache.put(player.getUniqueId(), value);
  }

  public void setChatEnabled(Player player, boolean value) {
    plugin.getStorage().setChatEnabled(player.getUniqueId(), value);
    chatCache.put(player.getUniqueId(), value);
  }

  @SuppressWarnings("BooleanMethodIsAlwaysInverted")
  public boolean isWhisperingEnabled(Player player) {
    return Boolean.TRUE.equals(whisperCache.get(player.getUniqueId()));
  }

  private boolean loadIsWhisperingEnabled(UUID uuid) {
    return plugin.getStorage().isWhisperingEnabled(uuid);
  }

  @SuppressWarnings("BooleanMethodIsAlwaysInverted")
  public boolean isChatEnabled(Player player) {
    return Boolean.TRUE.equals(chatCache.get(player.getUniqueId()));
  }

  private boolean loadIsChatEnabled(UUID uuid) {
    return plugin.getStorage().isChatEnabled(uuid);
  }
}
