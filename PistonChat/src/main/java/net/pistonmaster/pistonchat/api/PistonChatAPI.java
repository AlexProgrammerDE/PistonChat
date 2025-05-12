package net.pistonmaster.pistonchat.api;

import net.pistonmaster.pistonchat.PistonChat;

import java.util.Objects;

/**
 * API for interacting with PistonChat!
 */
@SuppressWarnings({"unused"})
public final class PistonChatAPI {
  private static PistonChat plugin = null;

  private PistonChatAPI() {
  }

  public static PistonChat getInstance() {
    return Objects.requireNonNull(plugin, "plugin is null");
  }

  public static void setInstance(PistonChat plugin) {
    if (plugin != null && PistonChatAPI.plugin == null)
      PistonChatAPI.plugin = plugin;
  }
}
