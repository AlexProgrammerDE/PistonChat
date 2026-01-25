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

public class ChatListener implements Listener {
  private final PistonFilter plugin;
  private final MessageHistory globalMessages;
  private final Map<UUID, MessageHistory> players = new ConcurrentHashMap<>();
  private final Cache<UUID, AtomicInteger> violationsCache;

  @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Plugin instance is intentionally shared")
  public ChatListener(PistonFilter plugin) {
    this.plugin = plugin;
    PistonFilterConfig config = plugin.getPluginConfig();
    this.globalMessages = new MessageHistory(config.globalMessageStackSize);
    this.violationsCache = CacheBuilder.newBuilder()
        .expireAfterWrite(config.muteViolationsTimeframe, TimeUnit.SECONDS)
        .build();
  }

  @EventHandler(ignoreCancelled = true)
  public void onQuit(PlayerQuitEvent event) {
    players.remove(event.getPlayer().getUniqueId());
  }

  @EventHandler(ignoreCancelled = true)
  public void onChat(PistonChatEvent event) {
    handleMessage(event.getPlayer(), MessageInfo.of(Instant.now(), event.getMessage()),
        () -> event.setCancelled(true),
        message -> PistonChatAPI.getInstance().getCommonTool().sendChatMessage(event.getPlayer(), message, event.getPlayer()));
  }

  @EventHandler(ignoreCancelled = true)
  public void onWhisper(PistonWhisperEvent event) {
    if (event.getSender() == event.getReceiver()) {
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

    String bannedText = FilterLogic.findBannedText(message.getStrippedMessage(), config.bannedText, config.bannedTextPartialRatio);
    if (bannedText != null) {
      cancelMessage(sender, message, cancelEvent, sendEmpty, "Contains banned text: %s".formatted(bannedText), config);
      return;
    }

    int maxWordLength = config.maxWordLength;
    int maxSeparatedNumbers = config.maxSeparatedNumbers;
    int maxWordsWithNumbers = config.maxWordsWithNumbers;
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

    if (config.noRepeat) {
      UUID uuid = senderUuid(sender);
      int noRepeatTime = config.noRepeatTime;
      int similarRatio = config.noRepeatSimilarRatio;
      int noRepeatNumberMessages = config.noRepeatNumberMessages;
      int noRepeatNumberAmount = config.noRepeatNumberAmount;
      int noRepeatWordRatio = config.noRepeatWordRatio;
      MessageHistory lastMessages = players.compute(uuid, (k, v) ->
          Objects.requireNonNullElseGet(v, () -> new MessageHistory(config.noRepeatStackSize)));

      boolean blocked = lastMessages.withLock(() ->
          isBlocked(sender, message, cancelEvent, sendEmpty, noRepeatTime, similarRatio, noRepeatNumberMessages,
              noRepeatNumberAmount, noRepeatWordRatio, lastMessages.messages, false, config));

      if (!blocked && config.globalRepeatCheck) {
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

    if (config.muteOnFail
        && plugin.getServer().getPluginManager().isPluginEnabled("PistonMute")
        && sender instanceof Player player) {
      try {
        UUID senderUuid = senderUuid(sender);
        int violations = violationsCache.get(senderUuid, AtomicInteger::new).incrementAndGet();
        if (violations > config.muteViolations) {
          violationsCache.invalidate(senderUuid);
          int muteTime = config.muteTime;
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
