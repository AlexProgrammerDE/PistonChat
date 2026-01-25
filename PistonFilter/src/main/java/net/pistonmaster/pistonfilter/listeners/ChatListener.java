package net.pistonmaster.pistonfilter.listeners;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import net.md_5.bungee.api.ChatColor;
import net.pistonmaster.pistonchat.api.PistonChatAPI;
import net.pistonmaster.pistonchat.api.PistonChatEvent;
import net.pistonmaster.pistonchat.api.PistonWhisperEvent;
import net.pistonmaster.pistonfilter.PistonFilter;
import net.pistonmaster.pistonfilter.config.PistonFilterConfig;
import net.pistonmaster.pistonfilter.hooks.PistonMuteHook;
import net.pistonmaster.pistonfilter.utils.MaxSizeDeque;
import net.pistonmaster.pistonfilter.utils.MessageInfo;
import net.pistonmaster.pistonfilter.utils.StringHelper;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.time.Duration;
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

    int bannedPartialRatio = config.bannedTextPartialRatio;
    for (String str : config.bannedText) {
      if (FuzzySearch.partialRatio(message.getStrippedMessage(), StringHelper.revertLeet(str)) > bannedPartialRatio) {
        cancelMessage(sender, message, cancelEvent, sendEmpty, "Contains banned text: %s".formatted(str), config);
        return;
      }
    }

    int maxWordLength = config.maxWordLength;
    int maxSeparatedNumbers = config.maxSeparatedNumbers;
    int maxWordsWithNumbers = config.maxWordsWithNumbers;
    int wordsWithNumbers = 0;
    for (String word : message.getWords()) {
      if (word.length() > maxWordLength) {
        cancelMessage(sender, message, cancelEvent, sendEmpty, "Contains a word with length (%d) \"%s\".".formatted(word.length(), word), config);
        return;
      } else if (hasInvalidSeparators(word, maxSeparatedNumbers)) {
        cancelMessage(sender, message, cancelEvent, sendEmpty, "Has a word with invalid separators (%s).".formatted(word), config);
        return;
      }

      if (StringHelper.containsDigit(word)) {
        wordsWithNumbers++;
      }
    }

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
    int i = 0;
    int foundDigits = 0;
    for (Iterator<MessageInfo> it = lastMessages.descendingIterator(); it.hasNext(); ) {
      MessageInfo pair = it.next();
      if (!global && message.isContainsDigit() && pair.isContainsDigit()
          && i < noRepeatNumberMessages) {
        foundDigits++;
      }

      if (foundDigits >= noRepeatNumberAmount) {
        cancelMessage(sender, message, cancelEvent, sendEmpty, "Contains too many numbers.", config);
        return true;
      } else if (noRepeatTime == -1 || Duration.between(pair.getTime(), message.getTime()).toSeconds() < noRepeatTime) {
        int similarity;
        if ((similarity = FuzzySearch.weightedRatio(pair.getStrippedMessage(), message.getStrippedMessage())) > similarRatio) {
          cancelMessage(sender, message, cancelEvent, sendEmpty,
              "Similar to previous message (%d%%) (%s)".formatted(similarity, pair.getOriginalMessage()), config);
          return true;
        } else if (noRepeatWordRatio > -1 && (similarity = getAverageEqualRatio(pair.getStrippedWords(), message.getStrippedWords())) > noRepeatWordRatio) {
          cancelMessage(sender, message, cancelEvent, sendEmpty,
              "Word similarity to previous message (%d%%) (%s)".formatted(similarity, pair.getOriginalMessage()), config);
          return true;
        }
        return true;
      }
      i++;
    }
    return false;
  }

  private boolean hasInvalidSeparators(String word, int maxSeparators) {
    List<Character> chars = word.chars().mapToObj(c -> (char) c).toList();
    int separators = 0;
    int index = 0;
    for (char c : chars) {
      if (Character.isDigit(c)) {
        if (index >= (chars.size() - 1)) {
          return false;
        } else if (!Character.isDigit(chars.get(index + 1)) && ++separators > maxSeparators) {
          return true;
        }
      }
      index++;
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

  private int getAverageEqualRatio(String[] comparedTo, String[] sentWords) {
    double total = 0;
    for (String sentWord : sentWords) {
      for (String comparedWord : comparedTo) {
        if (comparedWord.equalsIgnoreCase(sentWord)) {
          total += 1;
          break;
        }
      }
    }
    return (int) ((total / sentWords.length) * 100);
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
