package me.alexprogrammerde.pistonchat.utils;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
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

        String receiverString = ChatColor.translateAlternateColorCodes('&', ConfigTool.getPluginConfig().getString("whisper.from")
                                .replaceAll("%player%", ChatColor.stripColor(sender.getDisplayName()))
                                .replaceAll("%message%", message));

        String senderString = ChatColor.translateAlternateColorCodes('&', ConfigTool.getPluginConfig().getString("whisper.to")
                        .replaceAll("%player%", ChatColor.stripColor(receiver.getDisplayName()))
                        .replaceAll("%message%", message));

        receiver.spigot().sendMessage(new TextComponent(TextComponent.fromLegacyText(receiverString)));

        sender.spigot().sendMessage(new TextComponent(TextComponent.fromLegacyText(senderString)));

        CacheTool.sendMessage(sender, receiver);
    }

    public static String mergeArgs(String[] args, int start) {
        return String.join(" ", Arrays.copyOfRange(args, start, args.length));
    }

    public static String getPrefix() {
        return ChatColor.translateAlternateColorCodes('&', ConfigTool.getPluginConfig().getString("prefix"));
    }
}
