package net.pistonmaster.pistonchat.utils;

import net.pistonmaster.pistonchat.config.PistonChatConfig;

/**
 * Utility class for resolving message keys to their configured values.
 * This is extracted from CommonTool for better testability.
 */
public final class MessageKeyResolver {
  private MessageKeyResolver() {
  }

  /**
   * Get a message string by its key from the messages configuration.
   *
   * @param messages The messages configuration
   * @param key      The message key
   * @return The message string
   * @throws IllegalArgumentException if the key is unknown
   */
  public static String getMessageByKey(PistonChatConfig.MessagesConfig messages, String key) {
    return switch (key) {
      case "helpHeader" -> messages.helpHeader;
      case "whisperFrom" -> messages.whisperFrom;
      case "whisperTo" -> messages.whisperTo;
      case "hoverText" -> messages.hoverText;
      case "playeronly" -> messages.playeronly;
      case "notonline" -> messages.notonline;
      case "nooneignored" -> messages.nooneignored;
      case "chaton" -> messages.chaton;
      case "chatoff" -> messages.chatoff;
      case "pmson" -> messages.pmson;
      case "pmsoff" -> messages.pmsoff;
      case "pmself" -> messages.pmself;
      case "chatisoff" -> messages.chatisoff;
      case "sourceIgnored" -> messages.sourceIgnored;
      case "targetIgnored" -> messages.targetIgnored;
      case "pageNotExists" -> messages.pageNotExists;
      case "notANumber" -> messages.notANumber;
      case "whisperingDisabled" -> messages.whisperingDisabled;
      case "ignore" -> messages.ignore;
      case "unignore" -> messages.unignore;
      case "ignorehard" -> messages.ignorehard;
      case "unignorehard" -> messages.unignorehard;
      case "ignorelistcleared" -> messages.ignorelistcleared;
      default -> throw new IllegalArgumentException("Unknown message key: " + key);
    };
  }
}
