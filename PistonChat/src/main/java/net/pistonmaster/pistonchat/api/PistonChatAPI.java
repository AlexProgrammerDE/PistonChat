package net.pistonmaster.pistonchat.api;

import com.google.common.base.Preconditions;
import net.pistonmaster.pistonchat.PistonChat;
import net.pistonmaster.pistonchat.tools.IgnoreTool;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Objects;

/**
 * API for interacting with PistonChat!
 */
@SuppressWarnings({"unused"})
public final class PistonChatAPI {
    private static PistonChat plugin = null;

    private PistonChatAPI() {
    }

    public static void setInstance(PistonChat plugin) {
        if (plugin != null && PistonChatAPI.plugin == null)
            PistonChatAPI.plugin = plugin;
    }

    public static PistonChat getInstance() {
        return Objects.requireNonNull(plugin, "plugin is null");
    }
}
