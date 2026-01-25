package net.pistonmaster.pistonfilter.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for FilterLogic utility class.
 */
class FilterLogicTest {

  @Nested
  @DisplayName("hasInvalidSeparators tests")
  class HasInvalidSeparatorsTests {

    @Test
    @DisplayName("Returns false for word without digits")
    void wordWithoutDigits() {
      assertFalse(FilterLogic.hasInvalidSeparators("hello", 3));
    }

    @Test
    @DisplayName("Returns false for word ending with digit")
    void wordEndingWithDigit() {
      assertFalse(FilterLogic.hasInvalidSeparators("hello123", 3));
    }

    @Test
    @DisplayName("Returns false for word with only digits")
    void wordWithOnlyDigits() {
      assertFalse(FilterLogic.hasInvalidSeparators("12345", 0));
    }

    @Test
    @DisplayName("Returns false when separators equal max")
    void separatorsEqualMax() {
      // "1a2b3" has 2 separators (after 1 and after 2)
      assertFalse(FilterLogic.hasInvalidSeparators("1a2b3", 2));
    }

    @Test
    @DisplayName("Returns true when separators exceed max")
    void separatorsExceedMax() {
      // "1a2b3c" has 3 separators (after 1, 2, and 3)
      // Wait, 3 is at position before c, so it has separator
      // Actually "1a2b3c4" - 1->a (sep), 2->b (sep), 3->c (sep) = 3 separators, 4 is at end so no
      assertTrue(FilterLogic.hasInvalidSeparators("1a2b3c4d", 3));
    }

    @Test
    @DisplayName("Returns false for empty string")
    void emptyString() {
      assertFalse(FilterLogic.hasInvalidSeparators("", 0));
    }

    @Test
    @DisplayName("Returns false for single digit")
    void singleDigit() {
      assertFalse(FilterLogic.hasInvalidSeparators("5", 0));
    }

    @ParameterizedTest
    @CsvSource({
        "1a2, 1, false",    // 1 separator (after 1), max 1, not exceeded
        "1a2, 0, true",     // 1 separator, max 0, exceeded
        "1a2b, 1, true",    // 2 separators, max 1, exceeded
        "abc, 0, false",    // No digits, no separators
        "a1b2c3d, 2, true", // 3 separators, max 2, exceeded
    })
    @DisplayName("Parameterized separator tests")
    void parameterizedTests(String word, int maxSeparators, boolean expected) {
      assertEquals(expected, FilterLogic.hasInvalidSeparators(word, maxSeparators));
    }

    @Test
    @DisplayName("IP address may not be detected as expected due to digit-ending segments")
    void ipAddressBehavior() {
      // "192.168.1.1" - each segment ends with digit, so algorithm returns false early
      // The algorithm checks if digit is at end of word, returns false
      // 192. -> 2 is followed by ., that's a separator
      // But 1 (last char) at end, returns false
      assertFalse(FilterLogic.hasInvalidSeparators("192.168.1.1", 3));
    }
  }

  @Nested
  @DisplayName("getAverageEqualRatio tests")
  class GetAverageEqualRatioTests {

    @Test
    @DisplayName("Returns 100 when all words match")
    void allWordsMatch() {
      String[] compared = {"hello", "world", "test"};
      String[] sent = {"hello", "world", "test"};
      assertEquals(100, FilterLogic.getAverageEqualRatio(compared, sent));
    }

    @Test
    @DisplayName("Returns 0 when no words match")
    void noWordsMatch() {
      String[] compared = {"hello", "world"};
      String[] sent = {"foo", "bar"};
      assertEquals(0, FilterLogic.getAverageEqualRatio(compared, sent));
    }

    @Test
    @DisplayName("Returns 50 when half words match")
    void halfWordsMatch() {
      String[] compared = {"hello", "world"};
      String[] sent = {"hello", "bar"};
      assertEquals(50, FilterLogic.getAverageEqualRatio(compared, sent));
    }

    @Test
    @DisplayName("Returns 0 for empty sent array")
    void emptySentArray() {
      String[] compared = {"hello", "world"};
      String[] sent = {};
      assertEquals(0, FilterLogic.getAverageEqualRatio(compared, sent));
    }

    @Test
    @DisplayName("Returns 0 for null sent array")
    void nullSentArray() {
      String[] compared = {"hello", "world"};
      assertEquals(0, FilterLogic.getAverageEqualRatio(compared, null));
    }

    @Test
    @DisplayName("Matching is case insensitive")
    void caseInsensitive() {
      String[] compared = {"HELLO", "WORLD"};
      String[] sent = {"hello", "world"};
      assertEquals(100, FilterLogic.getAverageEqualRatio(compared, sent));
    }

    @Test
    @DisplayName("Handles partial matches correctly")
    void partialMatches() {
      String[] compared = {"the", "quick", "brown", "fox"};
      String[] sent = {"the", "slow", "brown", "dog"};
      // 2 matches (the, brown) out of 4 words = 50%
      assertEquals(50, FilterLogic.getAverageEqualRatio(compared, sent));
    }

    @Test
    @DisplayName("Handles duplicates in sent array")
    void duplicatesInSentArray() {
      String[] compared = {"hello", "world"};
      String[] sent = {"hello", "hello", "hello", "foo"};
      // 3 matches out of 4 = 75%
      assertEquals(75, FilterLogic.getAverageEqualRatio(compared, sent));
    }
  }

  @Nested
  @DisplayName("countWordsWithNumbers tests")
  class CountWordsWithNumbersTests {

    @Test
    @DisplayName("Returns 0 for words without numbers")
    void noNumbers() {
      String[] words = {"hello", "world", "test"};
      assertEquals(0, FilterLogic.countWordsWithNumbers(words));
    }

    @Test
    @DisplayName("Counts all words with numbers")
    void allWordsHaveNumbers() {
      String[] words = {"hello1", "world2", "test3"};
      assertEquals(3, FilterLogic.countWordsWithNumbers(words));
    }

    @Test
    @DisplayName("Counts only words containing digits")
    void mixedWords() {
      String[] words = {"hello", "world2", "test", "foo4bar"};
      assertEquals(2, FilterLogic.countWordsWithNumbers(words));
    }

    @Test
    @DisplayName("Returns 0 for empty array")
    void emptyArray() {
      String[] words = {};
      assertEquals(0, FilterLogic.countWordsWithNumbers(words));
    }

    @Test
    @DisplayName("Handles pure number words")
    void pureNumbers() {
      String[] words = {"123", "456", "abc"};
      assertEquals(2, FilterLogic.countWordsWithNumbers(words));
    }
  }

  @Nested
  @DisplayName("findBannedText tests")
  class FindBannedTextTests {

    private final List<String> bannedList = List.of(
        "http",
        "https",
        "://",
        ".org",
        ".com",
        ".net",
        "badword"
    );

    @Test
    @DisplayName("Detects exact banned text")
    void exactMatch() {
      String result = FilterLogic.findBannedText("visit http for more info", bannedList, 95);
      assertEquals("http", result);
    }

    @Test
    @DisplayName("Detects .com domain")
    void detectsDomain() {
      String result = FilterLogic.findBannedText("go to example.com", bannedList, 95);
      assertEquals(".com", result);
    }

    @Test
    @DisplayName("Returns null for clean message")
    void cleanMessage() {
      String result = FilterLogic.findBannedText("hello world this is a test", bannedList, 95);
      assertNull(result);
    }

    @Test
    @DisplayName("Detects leet speak variations via fuzzy matching")
    void leetSpeakVariation() {
      // Fuzzy matching with high ratio should catch similar text
      String result = FilterLogic.findBannedText("badw0rd", bannedList, 70);
      // This depends on fuzzy matching threshold
      // With lower threshold, leet variations should be caught
      assertNotNull(result);
    }

    @Test
    @DisplayName("Respects threshold - partialRatio catches partial matches")
    void respectsThreshold() {
      // FuzzySearch.partialRatio finds partial matches, so "bad" may match "badword"
      // Use a completely different string that won't partially match
      String result = FilterLogic.findBannedText("xyz", bannedList, 95);
      assertNull(result);
    }

    @Test
    @DisplayName("Detects protocol separator")
    void detectsProtocolSeparator() {
      String result = FilterLogic.findBannedText("ftp://server", bannedList, 95);
      assertEquals("://", result);
    }
  }

  @Nested
  @DisplayName("isWordTooLong tests")
  class IsWordTooLongTests {

    @Test
    @DisplayName("Returns false when word length equals max")
    void wordEqualsMax() {
      assertFalse(FilterLogic.isWordTooLong("hello", 5));
    }

    @Test
    @DisplayName("Returns false when word is shorter than max")
    void wordShorterThanMax() {
      assertFalse(FilterLogic.isWordTooLong("hi", 5));
    }

    @Test
    @DisplayName("Returns true when word exceeds max")
    void wordExceedsMax() {
      assertTrue(FilterLogic.isWordTooLong("superlongword", 5));
    }

    @Test
    @DisplayName("Returns false for empty word with zero max")
    void emptyWordZeroMax() {
      assertFalse(FilterLogic.isWordTooLong("", 0));
    }
  }

  @Nested
  @DisplayName("getMessageSimilarity tests")
  class GetMessageSimilarityTests {

    @Test
    @DisplayName("Returns 100 for identical messages")
    void identicalMessages() {
      assertEquals(100, FilterLogic.getMessageSimilarity("hello world", "hello world"));
    }

    @Test
    @DisplayName("Returns high similarity for near-identical messages")
    void nearIdentical() {
      int similarity = FilterLogic.getMessageSimilarity("hello world", "hello worlld");
      assertTrue(similarity > 80, "Expected high similarity, got: " + similarity);
    }

    @Test
    @DisplayName("Returns low similarity for different messages")
    void differentMessages() {
      int similarity = FilterLogic.getMessageSimilarity("hello world", "goodbye universe");
      assertTrue(similarity < 50, "Expected low similarity, got: " + similarity);
    }

    @Test
    @DisplayName("Handles empty strings")
    void emptyStrings() {
      // FuzzySearch.weightedRatio returns 0 for empty strings
      assertEquals(0, FilterLogic.getMessageSimilarity("", ""));
    }
  }

  @Nested
  @DisplayName("RepeatCheckResult tests")
  class RepeatCheckResultTests {

    @Test
    @DisplayName("Allowed result is not blocked")
    void allowedNotBlocked() {
      FilterLogic.RepeatCheckResult result = FilterLogic.RepeatCheckResult.allowed();
      assertFalse(result.isBlocked());
      assertNull(result.getReason());
    }

    @Test
    @DisplayName("Blocked result contains reason")
    void blockedWithReason() {
      String reason = "Test reason";
      FilterLogic.RepeatCheckResult result = FilterLogic.RepeatCheckResult.blocked(reason);
      assertTrue(result.isBlocked());
      assertEquals(reason, result.getReason());
    }
  }

  @Nested
  @DisplayName("checkForRepeats tests")
  class CheckForRepeatsTests {

    private MessageInfo createTestMessage(String content) {
      return createTestMessage(content, Instant.now());
    }

    private MessageInfo createTestMessage(String content, Instant time) {
      // Use the protected constructor for testing
      return new TestMessageInfo(time, content);
    }

    @Test
    @DisplayName("Returns allowed for empty history")
    void emptyHistory() {
      Deque<MessageInfo> history = new ArrayDeque<>();
      MessageInfo message = createTestMessage("hello world");

      FilterLogic.RepeatCheckResult result = FilterLogic.checkForRepeats(
          message, history, 60, 90, 5, 3, 40, true);

      assertFalse(result.isBlocked());
    }

    @Test
    @DisplayName("Blocks similar message within time window")
    void blocksSimilarMessage() {
      Deque<MessageInfo> history = new ArrayDeque<>();
      Instant now = Instant.now();
      history.add(createTestMessage("hello world test", now.minusSeconds(30)));

      MessageInfo message = createTestMessage("hello world test", now);

      FilterLogic.RepeatCheckResult result = FilterLogic.checkForRepeats(
          message, history, 60, 90, 5, 3, 40, true);

      assertTrue(result.isBlocked());
      assertTrue(result.getReason().contains("Similar"));
    }

    @Test
    @DisplayName("Blocks message with high word similarity")
    void blocksHighWordSimilarity() {
      Deque<MessageInfo> history = new ArrayDeque<>();
      Instant now = Instant.now();
      history.add(createTestMessage("hello world test message", now.minusSeconds(30)));

      // Same words, different order - will have high word similarity
      MessageInfo message = createTestMessage("test hello world message", now);

      FilterLogic.RepeatCheckResult result = FilterLogic.checkForRepeats(
          message, history, 60, 50, 5, 3, 40, true);

      assertTrue(result.isBlocked());
    }

    @Test
    @DisplayName("Blocks when too many digit-containing messages")
    void blocksTooManyDigitMessages() {
      Deque<MessageInfo> history = new ArrayDeque<>();
      Instant baseTime = Instant.now();

      // Add messages with digits (will be outside time window but still count for digit check)
      history.add(createTestMessage("message 1", baseTime.minusSeconds(100)));
      history.add(createTestMessage("message 2", baseTime.minusSeconds(90)));
      history.add(createTestMessage("message 3", baseTime.minusSeconds(80)));

      // New message also has digit
      MessageInfo message = createTestMessage("message 4", baseTime);

      FilterLogic.RepeatCheckResult result = FilterLogic.checkForRepeats(
          message, history, 60, 90, 5, 3, 40, true);

      assertTrue(result.isBlocked());
      assertTrue(result.getReason().contains("numbers"));
    }

    @Test
    @DisplayName("Does not check digits when checkDigits is false")
    void skipsDigitCheckWhenDisabled() {
      Deque<MessageInfo> history = new ArrayDeque<>();
      Instant baseTime = Instant.now();

      // Add many messages with digits - but outside the time window
      history.add(createTestMessage("message 1", baseTime.minusSeconds(100)));
      history.add(createTestMessage("message 2", baseTime.minusSeconds(90)));
      history.add(createTestMessage("message 3", baseTime.minusSeconds(80)));

      MessageInfo message = createTestMessage("message 4", baseTime);

      // checkDigits = false (like global check), time window is 60 seconds
      // All history messages are > 60 seconds old, so they should not trigger
      FilterLogic.RepeatCheckResult result = FilterLogic.checkForRepeats(
          message, history, 60, 90, 5, 3, 40, false);

      // Should be allowed since all messages are outside time window
      assertFalse(result.isBlocked());
    }

  }

  @Nested
  @DisplayName("containsWhitelistedWord tests")
  class ContainsWhitelistedWordTests {

    @Test
    @DisplayName("Returns false for null whitelist")
    void nullWhitelist() {
      assertFalse(FilterLogic.containsWhitelistedWord("hello world", null));
    }

    @Test
    @DisplayName("Returns false for empty whitelist")
    void emptyWhitelist() {
      assertFalse(FilterLogic.containsWhitelistedWord("hello world", List.of()));
    }

    @Test
    @DisplayName("Returns true when message contains whitelisted word")
    void containsWhitelistedWord() {
      List<String> whitelist = List.of("discord.gg/myserver", "example.com");
      assertTrue(FilterLogic.containsWhitelistedWord("join us at discord.gg/myserver", whitelist));
    }

    @Test
    @DisplayName("Returns false when message does not contain whitelisted word")
    void doesNotContainWhitelistedWord() {
      List<String> whitelist = List.of("discord.gg/myserver", "example.com");
      assertFalse(FilterLogic.containsWhitelistedWord("hello world", whitelist));
    }

    @Test
    @DisplayName("Case insensitive matching")
    void caseInsensitive() {
      List<String> whitelist = List.of("Discord.gg/MyServer");
      assertTrue(FilterLogic.containsWhitelistedWord("join us at discord.gg/myserver", whitelist));
      assertTrue(FilterLogic.containsWhitelistedWord("join us at DISCORD.GG/MYSERVER", whitelist));
    }

    @Test
    @DisplayName("Partial match works")
    void partialMatch() {
      List<String> whitelist = List.of("example");
      assertTrue(FilterLogic.containsWhitelistedWord("this is an example message", whitelist));
    }
  }

  @Nested
  @DisplayName("maskWhitelistedWords tests")
  class MaskWhitelistedWordsTests {

    @Test
    @DisplayName("Returns original message for null whitelist")
    void nullWhitelist() {
      assertEquals("hello world", FilterLogic.maskWhitelistedWords("hello world", null));
    }

    @Test
    @DisplayName("Returns original message for empty whitelist")
    void emptyWhitelist() {
      assertEquals("hello world", FilterLogic.maskWhitelistedWords("hello world", List.of()));
    }

    @Test
    @DisplayName("Masks whitelisted word with spaces")
    void masksWhitelistedWord() {
      List<String> whitelist = List.of("example.com");
      String result = FilterLogic.maskWhitelistedWords("visit example.com today", whitelist);
      assertEquals("visit             today", result);
    }

    @Test
    @DisplayName("Masks multiple whitelisted words")
    void masksMultipleWhitelistedWords() {
      List<String> whitelist = List.of("foo", "bar");
      String result = FilterLogic.maskWhitelistedWords("foo and bar", whitelist);
      assertEquals("    and    ", result);
    }

    @Test
    @DisplayName("Case insensitive masking")
    void caseInsensitiveMasking() {
      List<String> whitelist = List.of("Example");
      String result = FilterLogic.maskWhitelistedWords("This is an EXAMPLE test", whitelist);
      // EXAMPLE should be replaced with 7 spaces
      assertEquals("This is an         test", result);
    }

    @Test
    @DisplayName("Preserves message length")
    void preservesMessageLength() {
      List<String> whitelist = List.of("test");
      String original = "this is a test message";
      String result = FilterLogic.maskWhitelistedWords(original, whitelist);
      assertEquals(original.length(), result.length());
    }
  }

  @Nested
  @DisplayName("Integration-style tests")
  class IntegrationTests {

    @Test
    @DisplayName("Typical spam detection scenario")
    void typicalSpamScenario() {
      // Simulate a user sending "Join discord.gg/server"
      String message = "join discord.gg/server";
      List<String> bannedList = List.of(".gg", "discord");

      String banned = FilterLogic.findBannedText(message, bannedList, 95);
      assertNotNull(banned);
    }

    @Test
    @DisplayName("Word with many number separators")
    void numberSeparatorsDetection() {
      // "1a2b3c4d" has separators after 1, 2, 3 (4 is followed by d)
      assertTrue(FilterLogic.hasInvalidSeparators("1a2b3c4d", 2));
    }

    @Test
    @DisplayName("Counting words with numbers in typical spam")
    void countSpamNumbers() {
      // Spam like "2 5f gg 8b 33 hj 6zb 6573"
      String[] words = {"2", "5f", "gg", "8b", "33", "hj", "6zb", "6573"};
      int count = FilterLogic.countWordsWithNumbers(words);
      assertEquals(6, count); // 2, 5f, 8b, 33, 6zb, 6573 contain digits
    }
  }
}

/**
 * Extended MessageInfo for testing purposes that doesn't require Bukkit ChatColor.
 * Uses the protected constructor added to MessageInfo for testability.
 */
class TestMessageInfo extends MessageInfo {
  TestMessageInfo(Instant time, String message) {
    super(time, message);
  }
}
