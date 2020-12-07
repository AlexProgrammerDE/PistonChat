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
            sender.sendMessage(CommonTool.getPrefix() + "Please do not send a message to yourself!");
            return;
        }

        if (!TempDataTool.isWhisperingEnabled(receiver)) {
            sender.sendMessage(CommonTool.getPrefix() + "This person has whispering disabled!");
            return;
        }

        receiver.sendMessage(ChatColor.translateAlternateColorCodes('&', ConfigTool.getPluginConfig().getString("whisper.from").replaceAll("%player%", ChatColor.stripColor(sender.getDisplayName())).replaceAll("%message%", message)));

        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', ConfigTool.getPluginConfig().getString("whisper.to").replaceAll("%player%", ChatColor.stripColor(sender.getDisplayName())).replaceAll("%message%", message)));

        CacheTool.sendMessage(sender, receiver);
    }

    public static String mergeArgs(String[] args, int start) {
        return String.join(" ", Arrays.copyOfRange(args, start, args.length));
    }

    public static String getPrefix() {
        return ChatColor.translateAlternateColorCodes('&', ConfigTool.getPluginConfig().getString("prefix"));
    }
}
