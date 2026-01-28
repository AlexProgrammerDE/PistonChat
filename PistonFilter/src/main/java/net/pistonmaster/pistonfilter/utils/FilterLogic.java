package net.pistonmaster.pistonfilter.utils;

import me.xdrop.fuzzywuzzy.FuzzySearch;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Utility class containing testable filtering logic extracted from ChatListener.
 * This class provides pure functions that can be unit tested without Bukkit dependencies.
 */
public final class FilterLogic {

  // Mathematical Alphanumeric Symbols block (U+1D400-U+1D7FF) - commonly used by hacked clients
  private static final int MATH_ALPHANUMERIC_START = 0x1D400;
  private static final int MATH_ALPHANUMERIC_END = 0x1D7FF;

  // Other common hacked client font ranges
  private static final int[][] HACKED_CLIENT_RANGES = {
      {0xFF00, 0xFFEF},   // Halfwidth and Fullwidth Forms
      {0x2460, 0x24FF},   // Enclosed Alphanumerics (circled numbers/letters)
      {0x2070, 0x209F},   // Superscripts and Subscripts
      {0x2100, 0x214F},   // Letterlike Symbols
      {0x2150, 0x218F},   // Number Forms
      {0x2200, 0x22FF},   // Mathematical Operators
      {0x2300, 0x23FF},   // Miscellaneous Technical
      {0xFB00, 0xFB4F},   // Alphabetic Presentation Forms
      {0x1F100, 0x1F1FF}, // Enclosed Alphanumeric Supplement
  };

  private FilterLogic() {
  }

  /**
   * Represents a parsed Unicode range (start-end in hex).
   */
  public static final class UnicodeRange {
    private final int start;
    private final int end;

    public UnicodeRange(int start, int end) {
      this.start = start;
      this.end = end;
    }

    /**
     * Parse a Unicode range from a string like "0400-04FF".
     *
     * @param rangeStr the range string in format "start-end" (hex values)
     * @return the parsed range, or null if invalid
     */
    public static UnicodeRange parse(String rangeStr) {
      if (rangeStr == null || !rangeStr.contains("-")) {
        return null;
      }
      try {
        String[] parts = rangeStr.split("-");
        if (parts.length != 2) {
          return null;
        }
        int start = Integer.parseInt(parts[0].trim(), 16);
        int end = Integer.parseInt(parts[1].trim(), 16);
        return new UnicodeRange(start, end);
      } catch (NumberFormatException e) {
        return null;
      }
    }

    public boolean contains(int codePoint) {
      return codePoint >= start && codePoint <= end;
    }
  }

  /**
   * Parse a list of Unicode range strings into UnicodeRange objects.
   *
   * @param rangeStrings the list of range strings
   * @return the list of parsed ranges (invalid ranges are skipped)
   */
  public static List<UnicodeRange> parseUnicodeRanges(List<String> rangeStrings) {
    List<UnicodeRange> ranges = new ArrayList<>();
    for (String rangeStr : rangeStrings) {
      UnicodeRange range = UnicodeRange.parse(rangeStr);
      if (range != null) {
        ranges.add(range);
      }
    }
    return ranges;
  }

  /**
   * Result of Unicode character filtering.
   */
  public static final class UnicodeFilterResult {
    private final boolean blocked;
    private final String reason;
    private final int offendingCodePoint;

    private UnicodeFilterResult(boolean blocked, String reason, int offendingCodePoint) {
      this.blocked = blocked;
      this.reason = reason;
      this.offendingCodePoint = offendingCodePoint;
    }

    public static UnicodeFilterResult allowed() {
      return new UnicodeFilterResult(false, null, -1);
    }

    public static UnicodeFilterResult blocked(String reason, int codePoint) {
      return new UnicodeFilterResult(true, reason, codePoint);
    }

    public boolean isBlocked() {
      return blocked;
    }

    public String getReason() {
      return reason;
    }

    public int getOffendingCodePoint() {
      return offendingCodePoint;
    }
  }

  /**
   * Check if a message contains blocked Unicode characters.
   *
   * @param message               the message to check
   * @param blockNonAscii         whether to block all non-ASCII characters
   * @param blockMathAlphanumeric whether to block Mathematical Alphanumeric Symbols
   * @param blockHackedClientFonts whether to block other hacked client font ranges
   * @param allowedRanges         allowed Unicode ranges (only used when blockNonAscii is true)
   * @return the result of the Unicode filter check
   */
  public static UnicodeFilterResult checkUnicodeCharacters(
      String message,
      boolean blockNonAscii,
      boolean blockMathAlphanumeric,
      boolean blockHackedClientFonts,
      List<UnicodeRange> allowedRanges) {

    for (int i = 0; i < message.length(); ) {
      int codePoint = message.codePointAt(i);

      // Check for Mathematical Alphanumeric Symbols
      if (blockMathAlphanumeric && codePoint >= MATH_ALPHANUMERIC_START && codePoint <= MATH_ALPHANUMERIC_END) {
        return UnicodeFilterResult.blocked(
            "Contains Mathematical Alphanumeric Symbol (U+%04X)".formatted(codePoint), codePoint);
      }

      // Check for other hacked client font ranges
      if (blockHackedClientFonts) {
        for (int[] range : HACKED_CLIENT_RANGES) {
          if (codePoint >= range[0] && codePoint <= range[1]) {
            return UnicodeFilterResult.blocked(
                "Contains blocked Unicode character (U+%04X)".formatted(codePoint), codePoint);
          }
        }
      }

      // Check for non-ASCII characters
      if (blockNonAscii && codePoint > 0x7F) {
        // Check if this code point is in an allowed range
        boolean allowed = false;
        for (UnicodeRange range : allowedRanges) {
          if (range.contains(codePoint)) {
            allowed = true;
            break;
          }
        }
        if (!allowed) {
          return UnicodeFilterResult.blocked(
              "Contains non-ASCII character (U+%04X)".formatted(codePoint), codePoint);
        }
      }

      i += Character.charCount(codePoint);
    }

    return UnicodeFilterResult.allowed();
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
      String normalizedPattern = StringHelper.revertLeet(str);
      // Avoid false positives: message must be at least as long as the pattern
      // to be considered a match. This prevents "o" from matching ".org", etc.
      if (strippedMessage.length() >= normalizedPattern.length()
          && FuzzySearch.partialRatio(strippedMessage, normalizedPattern) > bannedPartialRatio) {
        return str;
      }
    }
    return null;
  }

  /**
   * Check if a message matches any banned regex pattern.
   *
   * @param message           the message to check
   * @param regexPatterns     the list of regex patterns to match against
   * @return the regex pattern that matched, or null if no match
   */
  public static String findBannedRegex(String message, List<String> regexPatterns) {
    if (regexPatterns == null || regexPatterns.isEmpty()) {
      return null;
    }

    for (String regexStr : regexPatterns) {
      try {
        Pattern pattern = Pattern.compile(regexStr, Pattern.CASE_INSENSITIVE);
        if (pattern.matcher(message).find()) {
          return regexStr;
        }
      } catch (PatternSyntaxException e) {
        // Invalid regex pattern - skip it
      }
    }
    return null;
  }

  /**
   * Check if a message contains any whitelisted word/phrase.
   * If a whitelisted word is found, certain filter checks should be bypassed.
   *
   * @param message         the original message
   * @param whitelistedWords the list of whitelisted words/phrases
   * @return true if the message contains a whitelisted word
   */
  public static boolean containsWhitelistedWord(String message, List<String> whitelistedWords) {
    if (whitelistedWords == null || whitelistedWords.isEmpty()) {
      return false;
    }

    String lowerMessage = message.toLowerCase(java.util.Locale.ROOT);
    for (String word : whitelistedWords) {
      if (lowerMessage.contains(word.toLowerCase(java.util.Locale.ROOT))) {
        return true;
      }
    }
    return false;
  }

  /**
   * Remove whitelisted words from a message for filtering purposes.
   * This allows the rest of the message to still be filtered.
   *
   * @param message         the original message
   * @param whitelistedWords the list of whitelisted words/phrases
   * @return the message with whitelisted words replaced with placeholders
   */
  public static String maskWhitelistedWords(String message, List<String> whitelistedWords) {
    if (whitelistedWords == null || whitelistedWords.isEmpty()) {
      return message;
    }

    String result = message;
    for (String word : whitelistedWords) {
      // Case-insensitive replacement with a placeholder of equal length
      String placeholder = " ".repeat(word.length());
      result = result.replaceAll("(?i)" + java.util.regex.Pattern.quote(word), placeholder);
    }
    return result;
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
        // Message is within time window but not similar - continue checking other messages
      }
      i++;
    }
    return RepeatCheckResult.allowed();
  }
}
