package net.pistonmaster.pistonchat.utils;

import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Optional;

public class CommonTool {
    private CommonTool() {
    }

    public static Optional<Player> getPlayer(String name) {
        return Optional.ofNullable(Bukkit.getPlayer(name));
    }

    public static void sendWhisperTo(Player sender, String message, Player receiver) {
        if (!ConfigTool.getConfig().getBoolean("allowpmself") && sender == receiver) {
            sender.sendMessage(LanguageTool.getMessage("pmself"));
            return;
        }

        if (!TempDataTool.isWhisperingEnabled(receiver)) {
            if (ConfigTool.getConfig().getBoolean("onlyhidepms")) {
                sendSender(sender, message, receiver);
            } else {
                sender.sendMessage(CommonTool.getPrefix() + "This person has whispering disabled!");
            }
            return;
        }

        sendSender(sender, message, receiver);
        sendReceiver(sender, message, receiver);

        CacheTool.sendMessage(sender, receiver);
    }

    public static void sendSender(Player sender, String message, Player receiver) {
        String senderString = ChatColor.translateAlternateColorCodes('&', ConfigTool.getConfig().getString("whisper.to")
                .replace("%player%", ChatColor.stripColor(receiver.getDisplayName()))
                .replace("%message%", message));

        sender.spigot().sendMessage(new TextComponent(TextComponent.fromLegacyText(senderString)));
    }

    private static void sendReceiver(Player sender, String message, Player receiver) {
        String receiverString = ChatColor.translateAlternateColorCodes('&', ConfigTool.getConfig().getString("whisper.from")
                .replace("%player%", ChatColor.stripColor(sender.getDisplayName()))
                .replace("%message%", message));

        receiver.spigot().sendMessage(new TextComponent(TextComponent.fromLegacyText(receiverString)));
    }

    public static String mergeArgs(String[] args, int start) {
        return String.join(" ", Arrays.copyOfRange(args, start, args.length));
    }

    public static String getPrefix() {
        return ChatColor.translateAlternateColorCodes('&', ConfigTool.getLanguage().getString("prefix"));
    }

    public static ChatColor getChatColorFor(String message, Player player) {
        FileConfiguration config = ConfigTool.getConfig();

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
        String str = ChatColor.translateAlternateColorCodes('&', ConfigTool.getConfig().getString("chatformat").replace("%player%", getName(player)));

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            str = parse(player, str);
        }

        return str;
    }

    private static String getName(Player player) {
        FileConfiguration config = ConfigTool.getConfig();

        if (config.getBoolean("stripnamecolor")) {
            return ChatColor.stripColor(player.getDisplayName());
        } else {
            return player.getDisplayName();
        }
    }

    public static String parse(OfflinePlayer player, String str) {
        return PlaceholderAPI.setPlaceholders(player, str);
    }
}
