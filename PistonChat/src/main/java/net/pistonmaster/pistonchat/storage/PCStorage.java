package net.pistonmaster.pistonchat.storage;

import java.util.List;
import java.util.UUID;

public interface PCStorage {
  void setChatEnabled(UUID uuid, boolean enabled);

  boolean isChatEnabled(UUID uuid);

  void setWhisperingEnabled(UUID uuid, boolean enabled);

  boolean isWhisperingEnabled(UUID uuid);

  HardReturn hardIgnorePlayer(UUID ignoringReceiver, UUID ignoredChatter);

  boolean isHardIgnored(UUID chatter, UUID receiver);

  List<UUID> getIgnoredList(UUID uuid);

  void clearIgnoredPlayers(UUID player);

  enum HardReturn {
    IGNORE, UN_IGNORE
  }
}
