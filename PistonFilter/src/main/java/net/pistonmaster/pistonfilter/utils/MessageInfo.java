package net.pistonmaster.pistonfilter.utils;

import lombok.Getter;
import lombok.ToString;
import net.md_5.bungee.api.ChatColor;

import java.time.Instant;
import java.util.Arrays;

@Getter
@ToString
public class MessageInfo {
  private final Instant time;
  private final String originalMessage;
  private final String strippedMessage;
  private final String[] words;
  private final String[] strippedWords;
  private final boolean containsDigit;

  private MessageInfo(Instant time, String originalMessage, String strippedMessage,
                      String[] words, String[] strippedWords, boolean containsDigit) {
    this.time = time;
    this.originalMessage = originalMessage;
    this.strippedMessage = strippedMessage;
    this.words = words;
    this.strippedWords = strippedWords;
    this.containsDigit = containsDigit;
  }

  /**
   * Protected constructor for testing purposes.
   * Allows creating MessageInfo without Bukkit color code processing.
   */
  protected MessageInfo(Instant time, String message) {
    this.time = time;
    this.originalMessage = message;
    this.words = Arrays.stream(message.split("\\s+")).toArray(String[]::new);
    this.strippedWords = Arrays.stream(words)
        .map(StringHelper::revertLeet)
        .toArray(String[]::new);
    this.strippedMessage = String.join("", strippedWords);
    this.containsDigit = message.matches(".*\\d.*");
  }

  public static MessageInfo of(Instant time, String message) {
    String[] words = Arrays.stream(message.split("\\s+")).toArray(String[]::new);
    String[] strippedWords = Arrays.stream(words)
        .map(MessageInfo::removeColorCodes)
        .map(StringHelper::revertLeet).toArray(String[]::new);

    return new MessageInfo(
        time,
        message,
        String.join("", strippedWords),
        words,
        strippedWords,
        message.matches(".*\\d.*")
    );
  }

  private static String removeColorCodes(String string) {
    return ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', string));
  }
}
