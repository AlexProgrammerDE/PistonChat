package net.pistonmaster.pistonchat.api;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Gets executed per player the plugin tries to send a message to.
 */
@SuppressWarnings("unused")
public class PistonChatReceiveEvent extends Event implements Cancellable {
  private static final HandlerList HANDLERS = new HandlerList();
  private final Player sender;
  private final Player receiver;
  private boolean isCancelled;
  private String message;
  private Component format;

  @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Bukkit API convention - Player objects are meant to be shared")
  public PistonChatReceiveEvent(Player sender, Player receiver, String message, boolean isAsync) {
    super(isAsync);

    this.sender = sender;
    this.receiver = receiver;
    this.message = message;
    this.isCancelled = false;
  }

  public static HandlerList getHandlerList() {
    return HANDLERS;
  }

  @Override
  public HandlerList getHandlers() {
    return HANDLERS;
  }

  /**
   * Gets the cancellation state of this event. A cancelled event will not
   * be executed in the server, but will still pass to other plugins.
   *
   * @return true if this event is cancelled
   */
  @Override
  public boolean isCancelled() {
    return isCancelled;
  }

  /**
   * Sets the cancellation state of this event. A cancelled event will not
   * be executed in the server, but will still pass to other plugins.
   *
   * @param cancel true if you wish to cancel this event
   */
  @Override
  public void setCancelled(boolean cancel) {
    isCancelled = cancel;
  }

  /**
   * Get the player who sends the message.
   *
   * @return the player who sends the message
   */
  @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "Bukkit API convention - Player objects are meant to be shared")
  public Player getSender() {
    return sender;
  }

  /**
   * Get the player who receives the message.
   *
   * @return the player who receives the message
   */
  @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "Bukkit API convention - Player objects are meant to be shared")
  public Player getReceiver() {
    return receiver;
  }

  /**
   * Get the message the player sends.
   *
   * @return the message
   */
  public String getMessage() {
    return message;
  }

  /**
   * Set the message that the player sends.
   *
   * @param message the message to set
   */
  public void setMessage(String message) {
    this.message = message;
  }

  /**
   * Get the chat format.
   *
   * @return the format
   */
  @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "Adventure Component is immutable")
  public Component getFormat() {
    return format;
  }

  /**
   * Set the chat format.
   *
   * @param format the format to set
   */
  @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Adventure Component is immutable")
  public void setFormat(Component format) {
    this.format = format;
  }
}
