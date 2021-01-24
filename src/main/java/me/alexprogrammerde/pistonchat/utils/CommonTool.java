package me.alexprogrammerde.pistonchat.utils;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Optional;

public class CommonTool {
    public static Optional<Player> getPlayer(String name) {
        return Optional.ofNullable(Bukkit.getPlayer(name));
    }

    private CommonTool() {}

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
                                .replace("%player%", ChatColor.stripColor(sender.getDisplayName()))
                                .replace("%message%", message));

        String senderString = ChatColor.translateAlternateColorCodes('&', ConfigTool.getPluginConfig().getString("whisper.to")
                        .replace("%player%", ChatColor.stripColor(receiver.getDisplayName()))
                        .replace("%message%", message));

        receiver.spigot().sendMessage(new TextComponent(TextComponent.fromLegacyText(receiverString)));

        sender.spigot().sendMessage(new TextComponent(TextComponent.fromLegacyText(senderString)));

        CacheTool.sendMessage(sender, receiver);
    }

    public static String mergeArgs(String[] args, int start) {
        return String.join(" ", Arrays.copyOfRange(args, start, args.length));
    }

    public static String getPrefix() {
        return ChatColor.translateAlternateColorCodes('&', ConfigTool.getPluginConfig().getString("log"));
    }

    public static ChatColor getChatColorFor(String message, Player player) {
        FileConfiguration config = ConfigTool.getPluginConfig();

        for (String str : config.getConfigurationSection("prefixes").getKeys(false)) {
            if (!config.getString("prefixes." + str).equalsIgnoreCase("/") && message.toLowerCase().startsWith(config.getString("prefixes." + str))) {
                if (player.hasPermission("pistonchat." + str)) {
                    return ChatColor.valueOf(str);
                } else {
                    return ChatColor.WHITE;
                }
            }
        }

        return ChatColor.WHITE;
    }

    public static String getFormat(Player player) {
        FileConfiguration config = ConfigTool.getPluginConfig();

        return  ChatColor.translateAlternateColorCodes('&', config.getString("chatformat").replace("%player%", getName(player)));
    }

    private static String getName(Player player) {
        FileConfiguration config = ConfigTool.getPluginConfig();

        if (config.getBoolean("stripnamecolor")) {
            return ChatColor.stripColor(player.getDisplayName());
        } else {
            return player.getDisplayName();
        }
    }
}
