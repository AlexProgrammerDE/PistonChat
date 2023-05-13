package net.pistonmaster.pistonchat.tools;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.pistonmaster.pistonchat.PistonChat;
import net.pistonmaster.pistonchat.api.PistonWhisperEvent;
import net.pistonmaster.pistonchat.utils.UniqueSender;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;

import java.util.Arrays;
import java.util.Optional;

public class CommonTool {
    private CommonTool() {
    }

    public static void sendWhisperTo(CommandSender sender, String message, CommandSender receiver) {
        if (!PistonChat.getInstance().getConfig().getBoolean("allowpmself") && sender == receiver) {
            sender.sendMessage(LanguageTool.getMessage("pmself"));
            return;
        }

        if (!sender.hasPermission("pistonchat.bypass")) {
            if (receiver instanceof Player player && !PistonChat.getInstance().getTempDataTool().isWhisperingEnabled(player)) {
                if (PistonChat.getInstance().getConfig().getBoolean("onlyhidepms")) {
                    sendSender(sender, message, receiver);
                } else {
                    sender.sendMessage(CommonTool.getPrefix() + "This person has whispering disabled!");
                }
                return;
            }

            if (receiver instanceof Player player && isVanished(player)) {
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

        PistonChat.getInstance().getCacheTool().sendMessage(sender, receiver);
    }

    public static void sendSender(CommandSender sender, String message, CommandSender receiver) {
        String senderString = convertAmpersand(PistonChat.getInstance().getConfig().getString("whisper.to"))
                .replace("%player%", new UniqueSender(receiver).getDisplayName())
                .replace("%message%", message);

        sender.spigot().sendMessage(new TextComponent(TextComponent.fromLegacyText(senderString)));
    }

    private static void sendReceiver(CommandSender sender, String message, CommandSender receiver) {
        String receiverString = convertAmpersand(PistonChat.getInstance().getConfig().getString("whisper.from"))
                .replace("%player%", new UniqueSender(sender).getDisplayName())
                .replace("%message%", message);

        receiver.spigot().sendMessage(new TextComponent(TextComponent.fromLegacyText(receiverString)));
    }

    public static String mergeArgs(String[] args, int start) {
        return String.join(" ", Arrays.copyOfRange(args, start, args.length));
    }

    public static String getPrefix() {
        return ChatColor.translateAlternateColorCodes('&', PistonChat.getInstance().getLanguage().getString("prefix"));
    }

    public static Optional<ChatColor> getChatColorFor(String message, Player player) {
        FileConfiguration config = PistonChat.getInstance().getConfig();

        for (String str : config.getConfigurationSection("prefixes").getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection("prefixes." + str);
            String prefix = section.getString("prefix");
            if (!prefix.equalsIgnoreCase("/")
                    && message.toLowerCase().startsWith(prefix)
                    && player.hasPermission("pistonchat.prefix." + str.toLowerCase())) {
                return Optional.of(ChatColor.valueOf(section.getString("color").toUpperCase()));
            }
        }

        return Optional.empty();
    }

    public static String getFormat(Player sender) {
        String str = null;
        for (String s : PistonChat.getInstance().getConfig().getConfigurationSection("chatformats").getKeys(false)) {
            if (sender.hasPermission("pistonchat.chatformat." + s.toLowerCase())) {
                str = PistonChat.getInstance().getConfig().getString("chatformats." + s);
                break;
            }
        }

        if (str == null)
            str = "%player%";

        str = str.replace("%player%", sender.getDisplayName());

        str = parsePlaceholders(sender, str);

        str = ChatColor.translateAlternateColorCodes('&', str);

        return str;
    }

    public static void sendChatMessage(Player chatter, String message, Player receiver) {
        ComponentBuilder builder = new ComponentBuilder(CommonTool.getFormat(chatter));

        if (receiver.hasPermission("pistonchat.playernamereply")) {
            builder.event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, String.format("/w %s ", chatter.getName())));

            String hoverText = PistonChat.getInstance().getConfig().getString("hovertext");

            builder.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new ComponentBuilder(
                            convertAmpersand(
                                    hoverText.replace("%player%",
                                            ChatColor.stripColor(chatter.getDisplayName())
                                    )
                            )
                    ).create()
            ));
        }

        builder.append(" ");

        if (PistonChat.getInstance().getConfig().getBoolean("resetafterformat")) {
            builder.reset();
        }

        String messageFormat = PistonChat.getInstance().getConfig().getString("message-format");
        String finalMessage = convertAmpersand(parsePlaceholders(chatter, messageFormat)).replace("%message%", message);
        builder.append(new TextComponent(finalMessage));

        Optional<ChatColor> messagePrefixColor = CommonTool.getChatColorFor(message, chatter);
        messagePrefixColor.ifPresent(builder::color);
        if (messagePrefixColor.isEmpty() && PistonChat.getInstance().getConfig().getBoolean("forcewhiteifnoprefix")) {
            builder.color(ChatColor.WHITE);
        }

        receiver.spigot().sendMessage(builder.create());
    }

    public static String parsePlaceholders(OfflinePlayer player, String str) {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null) {
            return str;
        } else {
            return me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, str);
        }
    }

    private static boolean isVanished(Player player) {
        for (MetadataValue meta : player.getMetadata("vanished")) {
            if (meta.asBoolean()) return true;
        }
        return false;
    }

    private static String convertAmpersand(String str) {
        return ChatColor.translateAlternateColorCodes('&', str);
    }
}
