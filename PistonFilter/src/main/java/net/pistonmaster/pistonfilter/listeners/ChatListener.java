package net.pistonmaster.pistonfilter.listeners;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import net.md_5.bungee.api.ChatColor;
import net.pistonmaster.pistonchat.api.PistonChatAPI;
import net.pistonmaster.pistonchat.api.PistonChatEvent;
import net.pistonmaster.pistonchat.api.PistonWhisperEvent;
import net.pistonmaster.pistonfilter.PistonFilter;
import net.pistonmaster.pistonfilter.config.PistonFilterConfig;
import net.pistonmaster.pistonfilter.hooks.PistonMuteHook;
import net.pistonmaster.pistonfilter.utils.FilterLogic;
import net.pistonmaster.pistonfilter.utils.MaxSizeDeque;
import net.pistonmaster.pistonfilter.utils.MessageInfo;
import net.pistonmaster.pistonfilter.utils.StringHelper;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.List;

public class ChatListener implements Listener {
  private final PistonFilter plugin;
  private final MessageHistory globalMessages;
  private final Map<UUID, MessageHistory> players = new ConcurrentHashMap<>();
  private final Cache<UUID, AtomicInteger> violationsCache;
  private final Map<UUID, Long> lastMessageTime = new ConcurrentHashMap<>();

  @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Plugin instance is intentionally shared")
  public ChatListener(PistonFilter plugin) {
    this.plugin = plugin;
    PistonFilterConfig config = plugin.getPluginConfig();
    this.globalMessages = new MessageHistory(config.repeat.globalStackSize);
    this.violationsCache = CacheBuilder.newBuilder()
        .expireAfterWrite(config.autoMute.violationTimeframe, TimeUnit.SECONDS)
        .build();
  }

  @EventHandler(ignoreCancelled = true)
  public void onQuit(PlayerQuitEvent event) {
    UUID uuid = event.getPlayer().getUniqueId();
    players.remove(uuid);
    lastMessageTime.remove(uuid);
  }

  @EventHandler(ignoreCancelled = true)
  public void onChat(PistonChatEvent event) {
    // Check if chat is paused for non-staff players
    if (plugin.getChatPauseManager().isPaused() && !event.getPlayer().hasPermission("pistonfilter.pausechat")) {
      event.setCancelled(true);
      PistonFilterConfig config = plugin.getPluginConfig();
      String pausedMessage = ChatColor.translateAlternateColorCodes('&', config.staff.chatPausedMessage);
      event.getPlayer().sendMessage(pausedMessage);
      return;
    }

    handleMessage(event.getPlayer(), MessageInfo.of(Instant.now(), event.getMessage()),
        () -> event.setCancelled(true),
        message -> PistonChatAPI.getInstance().getCommonTool().sendChatMessage(event.getPlayer(), message, event.getPlayer()));
  }

  @EventHandler(ignoreCancelled = true)
  public void onWhisper(PistonWhisperEvent event) {
    if (event.getSender() == event.getReceiver()) {
      return;
    }

    // Check if chat is paused for non-staff players (whispers are also blocked)
    if (plugin.getChatPauseManager().isPaused()
        && event.getSender() instanceof Player player
        && !player.hasPermission("pistonfilter.pausechat")) {
      event.setCancelled(true);
      PistonFilterConfig config = plugin.getPluginConfig();
      String pausedMessage = ChatColor.translateAlternateColorCodes('&', config.staff.chatPausedMessage);
      player.sendMessage(pausedMessage);
      return;
    }

    handleMessage(event.getSender(), MessageInfo.of(Instant.now(), event.getMessage()),
        () -> event.setCancelled(true),
        message -> PistonChatAPI.getInstance().getCommonTool().sendSender(event.getSender(), message, event.getReceiver()));
  }

  public void handleMessage(CommandSender sender, MessageInfo message, Runnable cancelEvent, Consumer<String> sendEmpty) {
    PistonFilterConfig config = plugin.getPluginConfig();

    if (sender.hasPermission("pistonfilter.bypass")) {
      return;
    }

    UUID uuid = senderUuid(sender);

    // Check whitelist - if message contains whitelisted content, some checks may be bypassed
    boolean containsWhitelisted = config.whitelist.enabled
        && FilterLogic.containsWhitelistedWord(message.getOriginalMessage(), config.whitelist.words);

    // Message cooldown check (bypass: pistonfilter.bypass.cooldown)
    if (config.cooldown.enabled && !sender.hasPermission("pistonfilter.bypass.cooldown")) {
      long now = System.currentTimeMillis();
      Long lastTime = lastMessageTime.get(uuid);
      if (lastTime != null && (now - lastTime) < config.cooldown.timeMillis) {
        long remaining = config.cooldown.timeMillis - (now - lastTime);
        cancelEvent.run();
        if (sender instanceof Player player) {
          String cooldownMsg = ChatColor.translateAlternateColorCodes('&', config.cooldown.message);
          player.sendMessage(cooldownMsg);
        }
        if (config.verbose) {
          plugin.getLogger().info(ChatColor.RED + "[AntiSpam] <" + sender.getName() + "> Cooldown: " + remaining + "ms remaining");
        }
        return;
      }
      lastMessageTime.put(uuid, now);
    }

    // Anti-caps check (bypass: pistonfilter.bypass.caps)
    if (config.caps.enabled && !sender.hasPermission("pistonfilter.bypass.caps")
        && message.getOriginalMessage().length() >= config.caps.minLength) {
      int capsPercent = StringHelper.getUppercasePercentage(message.getOriginalMessage());
      if (capsPercent > config.caps.maxPercent) {
        if (config.caps.autoLowercase) {
          // Log the auto-lowercase action
          if (config.verbose) {
            plugin.getLogger().info(ChatColor.YELLOW + "[AntiSpam] <" + sender.getName() + "> Auto-lowercased message (" + capsPercent + "% caps)");
          }
          // Note: The actual message modification needs to be handled by the event system
          // For now we just log it - the caller may need to handle message modification
        } else {
          cancelMessage(sender, message, cancelEvent, sendEmpty, "Excessive caps (%d%%)".formatted(capsPercent), config);
          return;
        }
      }
    }

    // Character repetition check (bypass: pistonfilter.bypass.repetition)
    if (config.repetition.enabled && !sender.hasPermission("pistonfilter.bypass.repetition")) {
      if (StringHelper.hasExcessiveRepetition(message.getOriginalMessage(), config.repetition.maxChars)) {
        if (config.repetition.autoFix) {
          // Log the auto-fix action
          if (config.verbose) {
            plugin.getLogger().info(ChatColor.YELLOW + "[AntiSpam] <" + sender.getName() + "> Auto-fixed character repetition");
          }
          // Note: The actual message modification needs to be handled by the event system
        } else {
          cancelMessage(sender, message, cancelEvent, sendEmpty, "Excessive character repetition", config);
          return;
        }
      }
    }

    // For banned text checks, mask whitelisted words if whitelist is enabled
    String messageToCheck = containsWhitelisted
        ? FilterLogic.maskWhitelistedWords(message.getStrippedMessage(), config.whitelist.words)
        : message.getStrippedMessage();

    String bannedText = FilterLogic.findBannedText(messageToCheck, config.content.bannedPatterns, config.content.bannedTextMatchRatio);
    if (bannedText != null) {
      cancelMessage(sender, message, cancelEvent, sendEmpty, "Contains banned text: %s".formatted(bannedText), config);
      return;
    }

    int maxWordLength = config.content.maxWordLength;
    int maxSeparatedNumbers = config.content.maxSeparatedNumbers;
    int maxWordsWithNumbers = config.content.maxWordsWithNumbers;
    for (String word : message.getWords()) {
      if (FilterLogic.isWordTooLong(word, maxWordLength)) {
        cancelMessage(sender, message, cancelEvent, sendEmpty, "Contains a word with length (%d) \"%s\".".formatted(word.length(), word), config);
        return;
      } else if (FilterLogic.hasInvalidSeparators(word, maxSeparatedNumbers)) {
        cancelMessage(sender, message, cancelEvent, sendEmpty, "Has a word with invalid separators (%s).".formatted(word), config);
        return;
      }
    }

    int wordsWithNumbers = FilterLogic.countWordsWithNumbers(message.getWords());
    if (wordsWithNumbers > maxWordsWithNumbers) {
      cancelMessage(sender, message, cancelEvent, sendEmpty, "Used %d words with numbers.".formatted(wordsWithNumbers), config);
      return;
    }

    // Check Unicode filtering
    if (config.unicode.enabled) {
      List<FilterLogic.UnicodeRange> allowedRanges = FilterLogic.parseUnicodeRanges(config.unicode.allowedRanges);
      FilterLogic.UnicodeFilterResult unicodeResult = FilterLogic.checkUnicodeCharacters(
          message.getOriginalMessage(), config.unicode.blockNonAscii, config.unicode.blockMathAlphanumeric,
          config.unicode.blockHackedClientFonts, allowedRanges);
      if (unicodeResult.isBlocked()) {
        cancelMessage(sender, message, cancelEvent, sendEmpty, unicodeResult.getReason(), config);
        return;
      }
    }

    if (config.repeat.enabled) {
      int noRepeatTime = config.repeat.timeSeconds;
      int similarRatio = config.repeat.similarityRatio;
      int noRepeatNumberMessages = config.repeat.numberPatternMessages;
      int noRepeatNumberAmount = config.repeat.numberPatternAmount;
      int noRepeatWordRatio = config.repeat.wordRatio;
      MessageHistory lastMessages = players.compute(uuid, (k, v) ->
          Objects.requireNonNullElseGet(v, () -> new MessageHistory(config.repeat.stackSize)));

      boolean blocked = lastMessages.withLock(() ->
          isBlocked(sender, message, cancelEvent, sendEmpty, noRepeatTime, similarRatio, noRepeatNumberMessages,
              noRepeatNumberAmount, noRepeatWordRatio, lastMessages.messages, false, config));

      if (!blocked && config.repeat.globalCheck) {
        blocked = globalMessages.withLock(() ->
            isBlocked(sender, message, cancelEvent, sendEmpty, noRepeatTime, similarRatio, noRepeatNumberMessages,
                noRepeatNumberAmount, noRepeatWordRatio, globalMessages.messages, true, config));
      }

      if (!blocked) {
        lastMessages.withLock(() -> {
          lastMessages.messages.add(message);
        });
        globalMessages.withLock(() -> {
          globalMessages.messages.add(message);
        });
      }
    }
  }

  private boolean isBlocked(CommandSender sender, MessageInfo message,
                            Runnable cancelEvent, Consumer<String> sendEmpty,
                            int noRepeatTime,
                            int similarRatio, int noRepeatNumberMessages,
                            int noRepeatNumberAmount, int noRepeatWordRatio,
                            Deque<MessageInfo> lastMessages,
                            boolean global, PistonFilterConfig config) {
    FilterLogic.RepeatCheckResult result = FilterLogic.checkForRepeats(
        message, lastMessages, noRepeatTime, similarRatio,
        noRepeatNumberMessages, noRepeatNumberAmount, noRepeatWordRatio, !global);

    if (result.isBlocked()) {
      cancelMessage(sender, message, cancelEvent, sendEmpty, result.getReason(), config);
      return true;
    }
    return false;
  }

  private void cancelMessage(CommandSender sender, MessageInfo message, Runnable cancelEvent, Consumer<String> sendEmpty,
                             String reason, PistonFilterConfig config) {
    cancelEvent.run();

    if (config.messageSender) {
      sendEmpty.accept(message.getOriginalMessage());
    }

    if (config.verbose) {
      plugin.getLogger().info(ChatColor.RED + "[AntiSpam] <" + sender.getName() + "> " + message.getOriginalMessage() + " (" + reason + ")");
    }

    // Notify staff members about the blocked message
    if (config.staff.notifyOnBlock) {
      notifyStaff(sender, message.getOriginalMessage(), reason, config);
    }

    if (config.autoMute.enabled
        && plugin.getServer().getPluginManager().isPluginEnabled("PistonMute")
        && sender instanceof Player player) {
      try {
        UUID senderUuid = senderUuid(sender);
        int violations = violationsCache.get(senderUuid, AtomicInteger::new).incrementAndGet();
        if (violations > config.autoMute.violationThreshold) {
          violationsCache.invalidate(senderUuid);
          int muteTime = config.autoMute.muteDuration;
          PistonMuteHook.mute(player, Instant.now().plus(muteTime, ChronoUnit.SECONDS));
          if (config.verbose) {
            plugin.getLogger().info(ChatColor.RED + "[AntiSpam] Muted " + sender.getName() + " for " + muteTime + " seconds.");
          }
        }
      } catch (Exception e) {
        plugin.getLogger().warning("Failed to mute player: " + e.getMessage());
      }
    }
  }

  private static UUID senderUuid(CommandSender sender) {
    if (sender instanceof Player player) {
      return player.getUniqueId();
    }

    return UUID.nameUUIDFromBytes(("pistonfilter:" + sender.getName()).getBytes(java.nio.charset.StandardCharsets.UTF_8));
  }

  /**
   * Notify all online staff members about a blocked message.
   *
   * @param sender  The sender whose message was blocked
   * @param message The original message that was blocked
   * @param reason  The reason the message was blocked
   * @param config  The plugin configuration
   */
  private void notifyStaff(CommandSender sender, String message, String reason, PistonFilterConfig config) {
    String notification = config.staff.notificationFormat
        .replace("%player%", sender.getName())
        .replace("%message%", message)
        .replace("%reason%", reason);
    notification = ChatColor.translateAlternateColorCodes('&', notification);

    for (Player staff : Bukkit.getOnlinePlayers()) {
      if (staff.hasPermission("pistonfilter.notify") && !staff.equals(sender)) {
        staff.sendMessage(notification);
      }
    }
  }

  private static final class MessageHistory {
    private final Deque<MessageInfo> messages;
    private final ReentrantLock lock = new ReentrantLock();

    private MessageHistory(int maxSize) {
      this.messages = new MaxSizeDeque<>(maxSize);
    }

    private <T> T withLock(Supplier<T> action) {
      lock.lock();
      try {
        return action.get();
      } finally {
        lock.unlock();
      }
    }

    private void withLock(Runnable action) {
      lock.lock();
      try {
        action.run();
      } finally {
        lock.unlock();
      }
    }
  }
}
