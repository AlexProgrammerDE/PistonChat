package me.alexprogrammerde.pistonchat.api;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Alternative chatevent that is fired when PistonChat executes the chat event.
 */
@SuppressWarnings({"unused"})
public final class PistonChatEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private boolean isCancelled;
    private final Player player;
    private String message;

    public PistonChatEvent(Player player, String message) {
        super(true);

        this.player = player;
        this.message = message;
        this.isCancelled = false;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
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
     * @return the player who sends the message
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Get the message the player sends.
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Set the message that the player sends.
     * @param message the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }
}
