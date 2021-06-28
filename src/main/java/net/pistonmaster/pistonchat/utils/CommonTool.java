package net.pistonmaster.pistonchat.utils;

import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.pistonmaster.pistonchat.PistonChat;
import net.pistonmaster.pistonchat.api.PistonWhisperEvent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;

import java.util.Arrays;
import java.util.Optional;

public class CommonTool {
    private CommonTool() {
    }

    public static Optional<Player> getPlayer(String name) {
        return Optional.ofNullable(Bukkit.getPlayer(name));
    }

    public static void sendWhisperTo(CommandSender sender, String message, CommandSender receiver) {
        if (!PistonChat.getPlugin(PistonChat.class).getConfig().getBoolean("allowpmself") && sender == receiver) {
            sender.sendMessage(LanguageTool.getMessage("pmself"));
            return;
        }

        if (!sender.hasPermission("pistonchat.bypass")) {
            if (!PistonChat.getPlugin(PistonChat.class).getTempDataTool().isWhisperingEnabled(receiver)) {
                if (PistonChat.getPlugin(PistonChat.class).getConfig().getBoolean("onlyhidepms")) {
                    sendSender(sender, message, receiver);
                } else {
                    sender.sendMessage(CommonTool.getPrefix() + "This person has whispering disabled!");
                }
                return;
            }

            if (receiver instanceof Player && isVanished((Player) receiver)) {
                sender.sendMessage(LanguageTool.getMessage("notonline"));
                return;
            }
        }

        PistonWhisperEvent pistonWhisperEvent = new PistonWhisperEvent(sender, receiver, message);

        Bukkit.getPluginManager().callEvent(pistonWhisperEvent);

        if (pistonWhisperEvent.isCancelled())
            return;

        message = pistonWhisperEvent.getMessage();

        sendSender(sender, message, receiver);
        sendReceiver(sender, message, receiver);

        PistonChat.getPlugin(PistonChat.class).getCacheTool().sendMessage(sender, receiver);
    }

    public static void sendSender(CommandSender sender, String message, CommandSender receiver) {
        String senderString = ChatColor.translateAlternateColorCodes('&', PistonChat.getPlugin(PistonChat.class).getConfig().getString("whisper.to")
                .replace("%player%", ChatColor.stripColor(new UniqueSender(receiver).getDisplayName()))
                .replace("%message%", message));

        sender.spigot().sendMessage(new TextComponent(TextComponent.fromLegacyText(senderString)));
    }

    private static void sendReceiver(CommandSender sender, String message, CommandSender receiver) {
        String receiverString = ChatColor.translateAlternateColorCodes('&', PistonChat.getPlugin(PistonChat.class).getConfig().getString("whisper.from")
                .replace("%player%", ChatColor.stripColor(new UniqueSender(sender).getDisplayName()))
                .replace("%message%", message));

        receiver.spigot().sendMessage(new TextComponent(TextComponent.fromLegacyText(receiverString)));
    }

    public static String mergeArgs(String[] args, int start) {
        return String.join(" ", Arrays.copyOfRange(args, start, args.length));
    }

    public static String getPrefix() {
        return ChatColor.translateAlternateColorCodes('&', PistonChat.getPlugin(PistonChat.class).getLanguage().getString("prefix"));
    }

    public static ChatColor getChatColorFor(String message, Player player) {
        FileConfiguration config = PistonChat.getPlugin(PistonChat.class).getConfig();

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

    public static String getFormat(CommandSender sender) {
        String str = ChatColor.translateAlternateColorCodes('&', PistonChat.getPlugin(PistonChat.class).getConfig().getString("chatformat").replace("%player%", getName(sender)));

        if (sender instanceof Player && Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            str = parse((OfflinePlayer) sender, str);
        }

        return str;
    }

    public static void sendChatMessage(Player chatter, String message, Player receiver) {
        ComponentBuilder builder = new ComponentBuilder(CommonTool.getFormat(chatter));

        if (receiver.hasPermission("pistonchat.playernamereply")) {
            builder.event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/w " + ChatColor.stripColor(chatter.getDisplayName()) + " "));

            String hoverText = PistonChat.getPlugin(PistonChat.class).getConfig().getString("hovertext");

            builder.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new ComponentBuilder(
                            ChatColor.translateAlternateColorCodes('&',
                                    hoverText.replace("%player%",
                                            ChatColor.stripColor(chatter.getDisplayName())
                                    )
                            )
                    ).create()
            ));
        }

        builder.append(" ").reset();

        builder.append(new TextComponent(TextComponent.fromLegacyText(message)));

        builder.color(CommonTool.getChatColorFor(message, chatter));

        receiver.spigot().sendMessage(builder.create());
    }

    private static String getName(CommandSender sender) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (PistonChat.getPlugin(PistonChat.class).getConfig().getBoolean("stripnamecolor")) {
                return ChatColor.stripColor(player.getDisplayName());
            } else {
                return player.getDisplayName();
            }
        } else if (sender instanceof ConsoleCommandSender) {
            return ChatColor.translateAlternateColorCodes('&', PistonChat.getPlugin(PistonChat.class).getConfig().getString("consolename"));
        } else {
            return sender.getName();
        }
    }

    public static String parse(OfflinePlayer player, String str) {
        return PlaceholderAPI.setPlaceholders(player, str);
    }

    private static boolean isVanished(Player player) {
        for (MetadataValue meta : player.getMetadata("vanished")) {
            if (meta.asBoolean()) return true;
        }
        return false;
    }
}
