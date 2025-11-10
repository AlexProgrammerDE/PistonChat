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
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Locale;
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
    String senderString = plugin.getConfig().getString("whisper.to");
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
    String senderString = plugin.getConfig().getString("whisper.from");
    TagResolver tagResolver = TagResolver.resolver(
        miniPlaceholdersTagResolver(),
        Placeholder.unparsed("message", message),
        getDisplayNameResolver(sender)
    );

    receiverAudience.sendMessage(MiniMessage.miniMessage().deserialize(senderString, new RelationalAudience<>(senderAudience, receiverAudience), tagResolver));
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
    senderAudience(sender).sendMessage(getLanguageMessage(messageKey, true, tagResolvers));
  }

  public void sendLanguageMessageNoPrefix(CommandSender sender, String messageKey, TagResolver... tagResolvers) {
    senderAudience(sender).sendMessage(getLanguageMessage(messageKey, false, tagResolvers));
  }

  public Optional<TextColor> getChatColorFor(String message, Player player) {
    FileConfiguration config = plugin.getConfig();

    for (String str : config.getConfigurationSection("prefixes").getKeys(false)) {
      ConfigurationSection section = config.getConfigurationSection("prefixes." + str);
      String prefix = section.getString("prefix");
      if (!prefix.equalsIgnoreCase("/")
          && message.toLowerCase(Locale.ROOT).startsWith(prefix)
          && player.hasPermission("pistonchat.prefix." + str.toLowerCase(Locale.ROOT))) {
        return Optional.of(NamedTextColor.NAMES.valueOrThrow(section.getString("color").toLowerCase(Locale.ROOT)));
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
      String hoverText = plugin.getConfig().getString("hover-text");

      formatComponent = formatComponent
          .clickEvent(ClickEvent.suggestCommand(String.format("/w %s ", chatter.getName())))
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

    String messageFormat = plugin.getConfig().getString("message-format");

    receiverAudience.sendMessage(MiniMessage.miniMessage().deserialize(messageFormat, TagResolver.resolver(
        miniPlaceholderResolver,
        Placeholder.component("message", messageComponent),
        Placeholder.component("format", formatComponent)
    )));
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
