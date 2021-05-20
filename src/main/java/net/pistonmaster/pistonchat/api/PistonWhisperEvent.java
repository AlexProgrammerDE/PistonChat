package net.pistonmaster.pistonchat.api;

import net.pistonmaster.pistonchat.utils.UniqueSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@SuppressWarnings("unused")
public class PistonWhisperEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private final UniqueSender sender;
    private final UniqueSender receiver;
    private boolean isCancelled;
    private String message;

    public PistonWhisperEvent(UniqueSender sender, UniqueSender receiver, String message) {
        super(false);

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
    public UniqueSender getSender() {
        return sender;
    }

    /**
     * Get the player who receives the message.
     *
     * @return the player who receives the message
     */
    public UniqueSender getReceiver() {
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
}
