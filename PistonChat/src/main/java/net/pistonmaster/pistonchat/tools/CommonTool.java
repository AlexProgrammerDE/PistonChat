package net.pistonmaster.pistonchat.tools;

import io.github.miniplaceholders.api.MiniPlaceholders;
import io.github.miniplaceholders.api.types.RelationalAudience;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.audience.Audience;
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
import net.pistonmaster.pistonchat.config.PistonChatConfig;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class CommonTool {
  private final PistonChat plugin;

  public static String mergeArgs(String[] args, int start) {
    return String.join(" ", Arrays.copyOfRange(args, start, args.length));
  }

  private static boolean isVanished(Player player) {
    for (MetadataValue meta : player.getMetadata("vanished")) {
      if (meta.asBoolean()) {
        return true;
      }
    }

    return false;
  }

  public static TagResolver getStrippedNameResolver(Player player) {
    return Placeholder.unparsed("player_name", ChatColor.stripColor(player.getDisplayName()));
  }

  public void sendWhisperTo(CommandSender sender, String message, CommandSender receiver) {
    PistonChatConfig config = plugin.getPluginConfig();
    if (!config.allowPmSelf && sender == receiver) {
      sendLanguageMessage(sender, "pmself");
      return;
    }

    if (!sender.hasPermission("pistonchat.bypass")) {
      if (receiver instanceof Player player && !plugin.getTempDataTool().isWhisperingEnabled(player)) {
        if (config.onlyHidePms) {
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

    plugin.getServer().getPluginManager().callEvent(pistonWhisperEvent);

    if (pistonWhisperEvent.isCancelled()) {
      return;
    }

    message = pistonWhisperEvent.getMessage();

    sendSender(sender, message, receiver);
    sendReceiver(sender, message, receiver);

    plugin.getCacheTool().sendMessage(sender, receiver);
  }

  public void sendSender(CommandSender sender, String message, CommandSender receiver) {
    Audience senderAudience = senderAudience(sender);
    Audience receiverAudience = senderAudience(receiver);
    String senderString = plugin.getPluginConfig().whisper.to;
    TagResolver tagResolver = TagResolver.resolver(
        miniPlaceholdersTagResolver(),
        Placeholder.unparsed("message", message),
        getDisplayNameResolver(receiver)
    );

    senderAudience.sendMessage(MiniMessage.miniMessage().deserialize(senderString, new RelationalAudience<>(senderAudience, receiverAudience), tagResolver));
  }

  private void sendReceiver(CommandSender sender, String message, CommandSender receiver) {
    Audience senderAudience = senderAudience(sender);
    Audience receiverAudience = senderAudience(receiver);
    String senderString = plugin.getPluginConfig().whisper.from;
    TagResolver tagResolver = TagResolver.resolver(
        miniPlaceholdersTagResolver(),
        Placeholder.unparsed("message", message),
        getDisplayNameResolver(sender)
    );

    receiverAudience.sendMessage(MiniMessage.miniMessage().deserialize(senderString, new RelationalAudience<>(senderAudience, receiverAudience), tagResolver));
  }

  public Component getLanguageMessage(String messageKey, boolean prefix, TagResolver... tagResolvers) {
    String messageString = getMessageByKey(messageKey);
    Component messageComponent = MiniMessage.miniMessage().deserialize(messageString, tagResolvers);

    if (!prefix) {
      return messageComponent;
    }

    String formatString = plugin.getPluginConfig().messages.format;

    TagResolver tagResolver = TagResolver.resolver(
        Placeholder.component("message", messageComponent)
    );

    return MiniMessage.miniMessage().deserialize(formatString, tagResolver);
  }

  private String getMessageByKey(String key) {
    var messages = plugin.getPluginConfig().messages;
    return switch (key) {
      case "help-header" -> messages.helpHeader;
      case "playeronly" -> messages.playeronly;
      case "notonline" -> messages.notonline;
      case "nooneignored" -> messages.nooneignored;
      case "chaton" -> messages.chaton;
      case "chatoff" -> messages.chatoff;
      case "pmson" -> messages.pmson;
      case "pmsoff" -> messages.pmsoff;
      case "pmself" -> messages.pmself;
      case "chatisoff" -> messages.chatisoff;
      case "source-ignored" -> messages.sourceIgnored;
      case "target-ignored" -> messages.targetIgnored;
      case "page-not-exists" -> messages.pageNotExists;
      case "not-a-number" -> messages.notANumber;
      case "whispering-disabled" -> messages.whisperingDisabled;
      case "ignore" -> messages.ignore;
      case "unignore" -> messages.unignore;
      case "ignorehard" -> messages.ignorehard;
      case "unignorehard" -> messages.unignorehard;
      case "ignorelistcleared" -> messages.ignorelistcleared;
      default -> throw new IllegalArgumentException("Unknown message key: " + key);
    };
  }

  public void sendLanguageMessage(CommandSender sender, String messageKey, TagResolver... tagResolvers) {
    senderAudience(sender).sendMessage(getLanguageMessage(messageKey, true, tagResolvers));
  }

  public void sendLanguageMessageNoPrefix(CommandSender sender, String messageKey, TagResolver... tagResolvers) {
    senderAudience(sender).sendMessage(getLanguageMessage(messageKey, false, tagResolvers));
  }

  public Optional<TextColor> getChatColorFor(String message, Player player) {
    PistonChatConfig config = plugin.getPluginConfig();

    for (Map.Entry<String, PistonChatConfig.PrefixConfig> entry : config.prefixes.entrySet()) {
      String name = entry.getKey();
      PistonChatConfig.PrefixConfig prefixConfig = entry.getValue();
      String prefix = prefixConfig.prefix;
      if (!"/".equalsIgnoreCase(prefix)
          && message.toLowerCase(Locale.ROOT).startsWith(prefix)
          && player.hasPermission("pistonchat.prefix." + name.toLowerCase(Locale.ROOT))) {
        return Optional.of(NamedTextColor.NAMES.valueOrThrow(prefixConfig.color.toLowerCase(Locale.ROOT)));
      }
    }

    return Optional.empty();
  }

  public void sendChatMessage(Player chatter, String message, Player receiver) {
    sendChatMessage(chatter, message, receiver, null);
  }

  public void sendChatMessage(Player chatter, String message, Player receiver, @Nullable Component overrideFormat) {
    Audience chatterAudience = senderAudience(chatter);
    Audience receiverAudience = senderAudience(receiver);
    TagResolver miniPlaceholderResolver = miniPlaceholdersTagResolver();
    Component formatComponent = overrideFormat != null
      ? overrideFormat
      : getFormat(chatter, miniPlaceholderResolver);

    if (receiver.hasPermission("pistonchat.playernamereply")) {
      String hoverText = plugin.getPluginConfig().hoverText;

      formatComponent = formatComponent
          .clickEvent(ClickEvent.suggestCommand("/w %s ".formatted(chatter.getName())))
          .hoverEvent(HoverEvent.showText(MiniMessage.miniMessage().deserialize(hoverText, new RelationalAudience<>(chatterAudience, receiverAudience), TagResolver.resolver(
              miniPlaceholderResolver,
              getStrippedNameResolver(chatter)
          ))));
    }

    Component messageComponent = Component.text(message);
    Optional<TextColor> messagePrefixColor = getChatColorFor(message, chatter);
    if (messagePrefixColor.isPresent()) {
      messageComponent = messageComponent.color(messagePrefixColor.get());
    }

    String messageFormat = plugin.getPluginConfig().messageFormat;

    receiverAudience.sendMessage(MiniMessage.miniMessage().deserialize(messageFormat, TagResolver.resolver(
        miniPlaceholderResolver,
        Placeholder.component("message", messageComponent),
        Placeholder.component("format", formatComponent)
    )));
  }

  public Component getFormat(Player chatter, TagResolver miniPlaceholderResolver) {
    PistonChatConfig config = plugin.getPluginConfig();
    String formatString = "<player_name>";
    for (Map.Entry<String, String> entry : config.chatFormats.entrySet()) {
      if (chatter.hasPermission("pistonchat.chatformat." + entry.getKey().toLowerCase(Locale.ROOT))) {
        formatString = entry.getValue();
        break;
      }
    }

    return MiniMessage.miniMessage().deserialize(formatString, TagResolver.resolver(
        miniPlaceholderResolver,
        getDisplayNameResolver(chatter)
    ));
  }

  public TagResolver getDisplayNameResolver(CommandSender sender) {
    PistonChatConfig config = plugin.getPluginConfig();
    if (sender instanceof Player player) {
      if (config.stripNameColor) {
        return getStrippedNameResolver(player);
      } else {
        return Placeholder.component("player_name",
            LegacyComponentSerializer.legacyAmpersand().deserialize(player.getDisplayName()));
      }
    } else if (sender instanceof ConsoleCommandSender) {
      return Placeholder.parsed("player_name", config.consoleName);
    } else {
      return Placeholder.unparsed("player_name", sender.getName());
    }
  }

  private TagResolver miniPlaceholdersTagResolver() {
    if (plugin.getServer().getPluginManager().isPluginEnabled("MiniPlaceholders")) {
      return MiniPlaceholders.relationalGlobalPlaceholders();
    } else {
      return TagResolver.empty();
    }
  }

  private Audience senderAudience(CommandSender sender) {
    if (sender instanceof Audience audience) {
      return audience;
    } else {
      return plugin.getAdventure().sender(sender);
    }
  }
}
