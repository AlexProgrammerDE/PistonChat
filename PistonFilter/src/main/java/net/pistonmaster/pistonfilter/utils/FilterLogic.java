package net.pistonmaster.pistonfilter.utils;

import me.xdrop.fuzzywuzzy.FuzzySearch;

import java.time.Duration;
import java.time.Instant;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

/**
 * Utility class containing testable filtering logic extracted from ChatListener.
 * This class provides pure functions that can be unit tested without Bukkit dependencies.
 */
public final class FilterLogic {

  private FilterLogic() {
  }

  /**
   * Check if a word has too many digit separators.
   * A separator is when a digit is followed by a non-digit character.
   * Example: "1a2b3c" has 2 separators (after 1 and after 2, but not after 3 since it's at the end).
   *
   * @param word          the word to check
   * @param maxSeparators the maximum allowed separators
   * @return true if the word has too many separators
   */
  public static boolean hasInvalidSeparators(String word, int maxSeparators) {
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

  /**
   * Calculate the average equal ratio between two arrays of words.
   * Returns the percentage of words in sentWords that have an exact match (case-insensitive)
   * in comparedTo.
   *
   * @param comparedTo the array of words to compare against
   * @param sentWords  the array of words from the sent message
   * @return the percentage (0-100) of matching words
   */
  public static int getAverageEqualRatio(String[] comparedTo, String[] sentWords) {
    if (sentWords == null || sentWords.length == 0) {
      return 0;
    }
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

  /**
   * Count the number of words that contain at least one digit.
   *
   * @param words the array of words to check
   * @return the count of words containing digits
   */
  public static int countWordsWithNumbers(String[] words) {
    int count = 0;
    for (String word : words) {
      if (StringHelper.containsDigit(word)) {
        count++;
      }
    }
    return count;
  }

  /**
   * Check if a message contains banned text using fuzzy partial matching.
   *
   * @param strippedMessage      the message with color codes removed and leet speak converted
   * @param bannedTextList       the list of banned text patterns
   * @param bannedPartialRatio   the minimum fuzzy match ratio (0-100) to consider a match
   * @return the banned text that was matched, or null if no match
   */
  public static String findBannedText(String strippedMessage, List<String> bannedTextList, int bannedPartialRatio) {
    for (String str : bannedTextList) {
      if (FuzzySearch.partialRatio(strippedMessage, StringHelper.revertLeet(str)) > bannedPartialRatio) {
        return str;
      }
    }
    return null;
  }

  /**
   * Check if a word exceeds the maximum allowed length.
   *
   * @param word      the word to check
   * @param maxLength the maximum allowed length
   * @return true if the word is too long
   */
  public static boolean isWordTooLong(String word, int maxLength) {
    return word.length() > maxLength;
  }

  /**
   * Calculate the similarity ratio between two messages using FuzzySearch weighted ratio.
   *
   * @param message1 the first message
   * @param message2 the second message
   * @return the similarity ratio (0-100)
   */
  public static int getMessageSimilarity(String message1, String message2) {
    return FuzzySearch.weightedRatio(message1, message2);
  }

  /**
   * Result of checking a message against message history.
   */
  public static final class RepeatCheckResult {
    private final boolean blocked;
    private final String reason;

    private RepeatCheckResult(boolean blocked, String reason) {
      this.blocked = blocked;
      this.reason = reason;
    }

    public static RepeatCheckResult allowed() {
      return new RepeatCheckResult(false, null);
    }

    public static RepeatCheckResult blocked(String reason) {
      return new RepeatCheckResult(true, reason);
    }

    public boolean isBlocked() {
      return blocked;
    }

    public String getReason() {
      return reason;
    }
  }

  /**
   * Check if a message should be blocked based on repeat detection rules.
   *
   * @param message                the message to check
   * @param lastMessages           the history of previous messages
   * @param noRepeatTime           the time window in seconds for repeat checking (-1 for no time limit)
   * @param similarRatio           the similarity threshold (0-100) for fuzzy matching
   * @param noRepeatNumberMessages the number of recent messages to check for digit-containing messages
   * @param noRepeatNumberAmount   the maximum allowed digit-containing messages in the window
   * @param noRepeatWordRatio      the word similarity threshold (-1 to disable)
   * @param checkDigits            whether to check for repeated digit-containing messages
   * @return the result of the repeat check
   */
  public static RepeatCheckResult checkForRepeats(
      MessageInfo message,
      Deque<MessageInfo> lastMessages,
      int noRepeatTime,
      int similarRatio,
      int noRepeatNumberMessages,
      int noRepeatNumberAmount,
      int noRepeatWordRatio,
      boolean checkDigits) {

    int i = 0;
    int foundDigits = 0;
    for (Iterator<MessageInfo> it = lastMessages.descendingIterator(); it.hasNext(); ) {
      MessageInfo pair = it.next();
      if (checkDigits && message.isContainsDigit() && pair.isContainsDigit()
          && i < noRepeatNumberMessages) {
        foundDigits++;
      }

      if (foundDigits >= noRepeatNumberAmount) {
        return RepeatCheckResult.blocked("Contains too many numbers.");
      } else if (noRepeatTime == -1 || Duration.between(pair.getTime(), message.getTime()).toSeconds() < noRepeatTime) {
        int similarity;
        if ((similarity = FuzzySearch.weightedRatio(pair.getStrippedMessage(), message.getStrippedMessage())) > similarRatio) {
          return RepeatCheckResult.blocked(
              "Similar to previous message (%d%%) (%s)".formatted(similarity, pair.getOriginalMessage()));
        } else if (noRepeatWordRatio > -1 && (similarity = getAverageEqualRatio(pair.getStrippedWords(), message.getStrippedWords())) > noRepeatWordRatio) {
          return RepeatCheckResult.blocked(
              "Word similarity to previous message (%d%%) (%s)".formatted(similarity, pair.getOriginalMessage()));
        }
        return RepeatCheckResult.blocked("Message within no-repeat time window.");
      }
      i++;
    }
    return RepeatCheckResult.allowed();
  }
}
