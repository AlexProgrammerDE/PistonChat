package net.pistonmaster.pistonchat.api;

import net.pistonmaster.pistonchat.PistonChat;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * API for interacting with PistonChat!
 */
@SuppressWarnings({"unused"})
public final class PistonChatAPI {
  private static final AtomicReference<PistonChat> INSTANCE = new AtomicReference<>();

  private PistonChatAPI() {
  }

  public static PistonChat getInstance() {
    return Objects.requireNonNull(INSTANCE.get(), "plugin is null");
  }

  public static void setInstance(PistonChat plugin) {
    if (plugin == null) {
      return;
    }

    INSTANCE.compareAndSet(null, plugin);
  }
}
