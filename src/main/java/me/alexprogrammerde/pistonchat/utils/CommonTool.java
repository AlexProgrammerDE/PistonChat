package me.alexprogrammerde.pistonchat.utils;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class CommonTool {
    public static @Nullable Player getPlayer(String name) {
        return Bukkit.getPlayer(name);
    }

    public static @NotNull boolean isOnline(String name) {
        return Bukkit.getPlayer(name) != null;
    }

    public static void sendWhisperTo(Player sender, String message, Player receiver) {
        receiver.sendMessage(ChatColor.DARK_PURPLE + ChatColor.stripColor(sender.getDisplayName()) + " whispers: " + message);

        sender.sendMessage(ChatColor.DARK_PURPLE + "You whisper to " + ChatColor.stripColor(receiver.getDisplayName()) + ": " + message);

        CacheTool.sendMessage(sender, receiver);
    }
}
