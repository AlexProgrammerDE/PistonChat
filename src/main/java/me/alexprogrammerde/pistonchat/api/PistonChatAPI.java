package me.alexprogrammerde.pistonchat.api;

import com.google.common.base.Preconditions;
import me.alexprogrammerde.pistonchat.utils.CommonTool;
import me.alexprogrammerde.pistonchat.utils.ConfigTool;
import me.alexprogrammerde.pistonchat.utils.IgnoreTool;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * API for interacting with PistonChat!
 */
@SuppressWarnings({"unused"})
public final class PistonChatAPI {
    /**
     * Ignore players! (Can also unignore)
     * @param ignorer The person that ignores someone!
     * @param ignored The person to ignore!
     */
    public static void ignorePlayer(@Nonnull Player ignorer, @Nonnull Player ignored) {
        Preconditions.checkNotNull(ignorer, "Ignorer can not be null!");
        Preconditions.checkNotNull(ignored, "Ignored can not be null!");

        ConfigTool.hardIgnorePlayer(ignorer, ignored);
    }

    /**
     * Get a list of all players a player ignores!
     * @param player The person who ignores players!
     * @return A list of all players this players ignored!
     */
    public static @Nonnull Map<OfflinePlayer, IgnoreTool.IgnoreType> getIgnoreList(@Nonnull Player player) {
        Preconditions.checkNotNull(player, "Player can not be null!");

        return IgnoreTool.getIgnoredPlayers(player);
    }

    /**
     * Send whispers!
     * @param sender The player who sends the whisper!
     * @param message Whisper to send!
     * @param receiver The person who receives the whisper!
     */
    public static void whisperPlayer(@Nonnull Player sender, @Nonnull String message, @Nonnull Player receiver) {
        Preconditions.checkNotNull(sender, "Sender can not be null!");
        Preconditions.checkNotNull(receiver, "Receiver can not be null!");

        CommonTool.sendWhisperTo(sender, message, receiver);
    }
}
