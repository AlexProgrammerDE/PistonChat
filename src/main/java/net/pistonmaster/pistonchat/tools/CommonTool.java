package net.pistonmaster.pistonchat.tools;

import io.github.miniplaceholders.api.MiniPlaceholders;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.audience.Audience;
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
            sendLanguageMessage(sender, "pmself");
            return;
        }

        if (!sender.hasPermission("pistonchat.bypass")) {
            if (receiver instanceof Player player && !plugin.getTempDataTool().isWhisperingEnabled(player)) {
                if (plugin.getConfig().getBoolean("only-hide-pms")) {
                    sendSender(sender, message, receiver);
                } else {
                    sendLanguageMessage(sender, "whispering-disabled");
                }
                return;
            }

            if (receiver instanceof Player player && isVanished(player)) {
                sendLanguageMessage(sender, "notonline");
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
        Audience senderAudience = plugin.getAdventure().sender(sender);
        Audience receiverAudience = plugin.getAdventure().sender(receiver);
        String senderString = plugin.getConfig().getString("whisper.to");
        TagResolver tagResolver = TagResolver.resolver(
                getMiniPlaceholdersTagResolver(senderAudience, receiverAudience),
                Placeholder.unparsed("message", message),
                getDisplayNameResolver(receiver)
        );

        senderAudience.sendMessage(MiniMessage.miniMessage().deserialize(senderString, tagResolver));
    }

    private void sendReceiver(CommandSender sender, String message, CommandSender receiver) {
        Audience senderAudience = plugin.getAdventure().sender(sender);
        Audience receiverAudience = plugin.getAdventure().sender(receiver);
        String senderString = plugin.getConfig().getString("whisper.from");
        TagResolver tagResolver = TagResolver.resolver(
                getMiniPlaceholdersTagResolver(senderAudience, receiverAudience),
                Placeholder.unparsed("message", message),
                getDisplayNameResolver(sender)
        );

        senderAudience.sendMessage(MiniMessage.miniMessage().deserialize(senderString, tagResolver));
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

    public void sendLanguageMessage(CommandSender sender, String messageKey, TagResolver... tagResolvers) {
        plugin.getAdventure().sender(sender).sendMessage(getLanguageMessage(messageKey, true, tagResolvers));
    }

    public void sendLanguageMessageNoPrefix(CommandSender sender, String messageKey, TagResolver... tagResolvers) {
        plugin.getAdventure().sender(sender).sendMessage(getLanguageMessage(messageKey, false, tagResolvers));
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

    public Component getFormat(Player chatter, TagResolver miniPlaceholderResolver) {
        String formatString = "<player_name>";
        for (String s : plugin.getConfig().getConfigurationSection("chat-formats").getKeys(false)) {
            if (chatter.hasPermission("pistonchat.chatformat." + s.toLowerCase(Locale.ROOT))) {
                formatString = plugin.getConfig().getString("chat-formats." + s);
                break;
            }
        }

        return MiniMessage.miniMessage().deserialize(formatString, TagResolver.resolver(
                miniPlaceholderResolver,
                getDisplayNameResolver(chatter)
        ));
    }

    public void sendChatMessage(Player chatter, String message, Player receiver) {
        Audience chatterAudience = plugin.getAdventure().player(chatter);
        Audience receiverAudience = plugin.getAdventure().player(receiver);
        TagResolver miniPlaceholderResolver = getMiniPlaceholdersTagResolver(chatterAudience, receiverAudience);
        Component formatComponent = getFormat(chatter, miniPlaceholderResolver);

        if (receiver.hasPermission("pistonchat.playernamereply")) {
            formatComponent = formatComponent.clickEvent(ClickEvent.suggestCommand(String.format("/w %s ", chatter.getName())));

            String hoverText = plugin.getConfig().getString("hover-text");
            Component hoverComponent = MiniMessage.miniMessage().deserialize(hoverText, TagResolver.resolver(
                    miniPlaceholderResolver,
                    getStrippedNameResolver(chatter)
            ));

            formatComponent = formatComponent.hoverEvent(HoverEvent.showText(hoverComponent));
        }

        Component messageComponent = Component.text(message);
        Optional<TextColor> messagePrefixColor = getChatColorFor(message, chatter);
        if (messagePrefixColor.isPresent()) {
            messageComponent = messageComponent.color(messagePrefixColor.get());
        }

        String messageFormat = plugin.getConfig().getString("message-format");

        Component finalComponent = MiniMessage.miniMessage().deserialize(messageFormat, TagResolver.resolver(
                miniPlaceholderResolver,
                Placeholder.component("message", messageComponent),
                Placeholder.component("format", formatComponent)
        ));

        receiverAudience.sendMessage(finalComponent);
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

    private TagResolver getMiniPlaceholdersTagResolver(Audience mainAudience, Audience otherAudience) {
        if (plugin.getServer().getPluginManager().isPluginEnabled("MiniPlaceholders")) {
            return MiniPlaceholders.getRelationalGlobalPlaceholders(mainAudience, otherAudience);
        } else {
            return TagResolver.empty();
        }
    }
}
