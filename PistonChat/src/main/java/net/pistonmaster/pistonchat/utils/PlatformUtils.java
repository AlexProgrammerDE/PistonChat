package net.pistonmaster.pistonchat.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

public class PlatformUtils {
  public static Optional<Player> getPlayer(String name) {
    return Optional.ofNullable(Bukkit.getPlayer(name));
  }

  public static Optional<Player> getPlayer(UUID uuid) {
    return Optional.ofNullable(Bukkit.getPlayer(uuid));
  }
}
