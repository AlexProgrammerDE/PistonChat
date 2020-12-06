package me.alexprogrammerde.pistonchat.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Optional;

public class CommonTool {
    public static Optional<Player> getPlayer(String name) {
        return Optional.ofNullable(Bukkit.getPlayer(name));
    }

    public static void sendWhisperTo(Player sender, String message, Player receiver) {
        if (sender == receiver) {
            sender.sendMessage("Please don't send a message yourself!");
            return;
        }

        receiver.sendMessage(ChatColor.LIGHT_PURPLE + ChatColor.stripColor(sender.getDisplayName()) + " whispers: " + message);

        sender.sendMessage(ChatColor.LIGHT_PURPLE + "You whisper to " + ChatColor.stripColor(receiver.getDisplayName()) + ": " + message);

        CacheTool.sendMessage(sender, receiver);
    }

    public static String mergeArgs(String[] args, int start) {
        return String.join(" ", Arrays.copyOfRange(args, start, args.length));
    }
}
