package net.pistonmaster.pistonchat.tools;

import lombok.RequiredArgsConstructor;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.pistonmaster.pistonchat.PistonChat;
import net.pistonmaster.pistonchat.api.PistonWhisperEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;

import java.util.*;

@RequiredArgsConstructor
public class CommonTool {
    private final PistonChat plugin;

    public void sendWhisperTo(CommandSender sender, String message, CommandSender receiver) {
        if (!plugin.getConfig().getBoolean("allow-pm-self") && sender == receiver) {
            sendLanguageMessage(plugin.getAdventure(), sender, "pmself");
            return;
        }

        if (!sender.hasPermission("pistonchat.bypass")) {
            if (receiver instanceof Player player && !plugin.getTempDataTool().isWhisperingEnabled(player)) {
                if (plugin.getConfig().getBoolean("only-hide-pms")) {
                    sendSender(sender, message, receiver);
                } else {
                    sendLanguageMessage(plugin.getAdventure(), sender, "whispering-disabled");
                }
                return;
            }

            if (receiver instanceof Player player && isVanished(player)) {
                sendLanguageMessage(plugin.getAdventure(), sender, "notonline");
                return;
            }
        }

        PistonWhisperEvent pistonWhisperEvent = new PistonWhisperEvent(sender, receiver, message);

        Bukkit.getPluginManager().callEvent(pistonWhisperEvent);

        if (pistonWhisperEvent.isCancelled()) {
            return;
        }

        message = pistonWhisperEvent.getMessage();

        sendSender(sender, message, receiver);
        sendReceiver(sender, message, receiver);

        plugin.getCacheTool().sendMessage(sender, receiver);
    }

    public void sendSender(CommandSender sender, String message, CommandSender receiver) {
        String senderString = plugin.getConfig().getString("whisper.to");
        TagResolver tagResolver = TagResolver.resolver(
                Placeholder.unparsed("message", message),
                getDisplayNameResolver(receiver)
        );

        plugin.getAdventure().sender(sender)
                .sendMessage(MiniMessage.miniMessage().deserialize(senderString, tagResolver));
    }

    private void sendReceiver(CommandSender sender, String message, CommandSender receiver) {
        String senderString = plugin.getConfig().getString("whisper.from");
        TagResolver tagResolver = TagResolver.resolver(
                Placeholder.unparsed("message", message),
                getDisplayNameResolver(sender)
        );

        plugin.getAdventure().sender(receiver)
                .sendMessage(MiniMessage.miniMessage().deserialize(senderString, tagResolver));
    }

    public static String mergeArgs(String[] args, int start) {
        return String.join(" ", Arrays.copyOfRange(args, start, args.length));
    }

    public Component getLanguageMessage(String messageKey, boolean prefix, TagResolver... tagResolvers) {
        String messageString = plugin.getLanguage().getString(messageKey);
        Component messageComponent = MiniMessage.miniMessage().deserialize(messageString, tagResolvers);

        if (!prefix) {
            return messageComponent;
        }

        String formatString = plugin.getLanguage().getString("format");

        TagResolver tagResolver = TagResolver.resolver(
                Placeholder.component("message", messageComponent)
        );

        return MiniMessage.miniMessage().deserialize(formatString, tagResolver);
    }

    public void sendLanguageMessage(BukkitAudiences audiences, CommandSender sender, String messageKey, TagResolver... tagResolvers) {
        audiences.sender(sender).sendMessage(getLanguageMessage(messageKey, true, tagResolvers));
    }

    public void sendLanguageMessageNoPrefix(BukkitAudiences audiences, CommandSender sender, String messageKey, TagResolver... tagResolvers) {
        audiences.sender(sender).sendMessage(getLanguageMessage(messageKey, false, tagResolvers));
    }

    public Optional<TextColor> getChatColorFor(String message, Player player) {
        FileConfiguration config = plugin.getConfig();

        for (String str : config.getConfigurationSection("prefixes").getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection("prefixes." + str);
            String prefix = section.getString("prefix");
            if (!prefix.equalsIgnoreCase("/")
                    && message.toLowerCase().startsWith(prefix)
                    && player.hasPermission("pistonchat.prefix." + str.toLowerCase())) {
                return Optional.of(NamedTextColor.NAMES.valueOrThrow(section.getString("color").toLowerCase(Locale.ROOT)));
            }
        }

        return Optional.empty();
    }

    public Component getFormat(Player sender) {
        String str = null;
        for (String s : plugin.getConfig().getConfigurationSection("chat-formats").getKeys(false)) {
            if (sender.hasPermission("pistonchat.chatformat." + s.toLowerCase(Locale.ROOT))) {
                str = plugin.getConfig().getString("chat-formats." + s);
                break;
            }
        }

        if (str == null)
            str = "<player_name>";

        TagResolver tagResolver = TagResolver.resolver(
                getDisplayNameResolver(sender)
        );

        return MiniMessage.miniMessage().deserialize(str, tagResolver);
    }

    public void sendChatMessage(Player chatter, String message, Player receiver) {
        Component formatComponent = getFormat(chatter);

        if (receiver.hasPermission("pistonchat.playernamereply")) {
            formatComponent = formatComponent.clickEvent(ClickEvent.suggestCommand(String.format("/w %s ", chatter.getName())));

            String hoverText = plugin.getConfig().getString("hover-text");
            TagResolver tagResolver = TagResolver.resolver(
                    getStrippedNameResolver(chatter)
            );
            Component hoverComponent = MiniMessage.miniMessage().deserialize(hoverText, tagResolver);

            formatComponent = formatComponent.hoverEvent(HoverEvent.showText(hoverComponent));
        }

        Component messageComponent = Component.text(message);
        Optional<TextColor> messagePrefixColor = getChatColorFor(message, chatter);
        if (messagePrefixColor.isPresent()) {
            messageComponent = messageComponent.color(messagePrefixColor.get());
        }

        String messageFormat = plugin.getConfig().getString("message-format");
        TagResolver tagResolver = TagResolver.resolver(
                Placeholder.component("message", messageComponent),
                Placeholder.component("format", formatComponent)
        );

        Component finalComponent = MiniMessage.miniMessage().deserialize(messageFormat, tagResolver);

        plugin.getAdventure().player(receiver).sendMessage(finalComponent);
    }

    private static boolean isVanished(Player player) {
        for (MetadataValue meta : player.getMetadata("vanished")) {
            if (meta.asBoolean()) {
                return true;
            }
        }

        return false;
    }

    public TagResolver getDisplayNameResolver(CommandSender sender) {
        if (sender instanceof Player player) {
            if (plugin.getConfig().getBoolean("strip-name-color")) {
                return getStrippedNameResolver(player);
            } else {
                return Placeholder.component("player_name",
                        LegacyComponentSerializer.legacyAmpersand().deserialize(player.getDisplayName()));
            }
        } else if (sender instanceof ConsoleCommandSender) {
            return Placeholder.parsed("player_name", plugin.getConfig().getString("console-name"));
        } else {
            return Placeholder.unparsed("player_name", sender.getName());
        }
    }

    public static TagResolver getStrippedNameResolver(Player player) {
        return TagResolver.resolver(
                Placeholder.unparsed("player_name", ChatColor.stripColor(player.getDisplayName()))
        );
    }
}
