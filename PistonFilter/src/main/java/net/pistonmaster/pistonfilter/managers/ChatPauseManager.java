package net.pistonmaster.pistonfilter.managers;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Manages the chat pause state for the server.
 * When chat is paused, non-staff players cannot send messages.
 */
public class ChatPauseManager {
  private final AtomicBoolean chatPaused = new AtomicBoolean(false);

  /**
   * Check if chat is currently paused.
   *
   * @return true if chat is paused, false otherwise
   */
  public boolean isPaused() {
    return chatPaused.get();
  }

  /**
   * Pause chat for non-staff players.
   *
   * @return true if chat was paused (state changed), false if already paused
   */
  public boolean pause() {
    return chatPaused.compareAndSet(false, true);
  }

  /**
   * Resume chat for all players.
   *
   * @return true if chat was resumed (state changed), false if already unpaused
   */
  public boolean unpause() {
    return chatPaused.compareAndSet(true, false);
  }
}
