package net.pistonmaster.pistonfilter.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for StringHelper utility class.
 * Includes tests migrated from the original LeetTest.java plus additional coverage.
 */
class StringHelperTest {

  @Nested
  @DisplayName("revertLeet tests")
  class RevertLeetTests {

    @Test
    @DisplayName("Original LeetTest - basic leet conversion")
    void basicLeetConversion() {
      String original = "test12345-/(";
      assertNotEquals(original, StringHelper.revertLeet(original));
    }

    @Test
    @DisplayName("Converts numeric leet speak")
    void numericLeetSpeak() {
      // 0->o, 1->i, 2->z, 3->e, 4->a, 5->s, 6->g, 7->t, 8->b, 9->g
      assertEquals("oizeasgtbg", StringHelper.revertLeet("0123456789"));
    }

    @Test
    @DisplayName("Converts symbol leet speak")
    void symbolLeetSpeak() {
      // &->a, @->a, (->c, #->h, !->i, ]->i, |->i, }->i, ?->o, $->s
      // Note: } also maps to i
      assertEquals("aachiiios", StringHelper.revertLeet("&@(#!]|?$"));
    }

    @ParameterizedTest
    @CsvSource({
        "h3ll0, hello",       // 3->e, 0->o
        "w0r1d, worid",       // 0->o, 1->i
        "t35t, test",         // 3->e, 5->s
        "h4ck3r, hacker",     // 4->a, 3->e
        "1337, ieet",         // classic leet
        "n00b, noob",         // 0->o
        "p4$$w0rd, password", // 4->a, $->s, 0->o
        "b@dw0rd, badword",   // @->a, 0->o
    })
    @DisplayName("Converts common leet patterns")
    void commonLeetPatterns(String input, String expected) {
      assertEquals(expected, StringHelper.revertLeet(input));
    }

    @Test
    @DisplayName("Converts to lowercase")
    void convertsToLowercase() {
      assertEquals("hello world", StringHelper.revertLeet("HELLO WORLD"));
    }

    @Test
    @DisplayName("Handles mixed case with leet")
    void mixedCaseWithLeet() {
      // H3LL0 -> h3ll0 (lowercase first) -> hello (leet convert)
      assertEquals("hello worid", StringHelper.revertLeet("H3LL0 W0R1D"));
    }

    @Test
    @DisplayName("Handles empty string")
    void emptyString() {
      assertEquals("", StringHelper.revertLeet(""));
    }

    @Test
    @DisplayName("Preserves non-leet characters")
    void preservesNonLeetCharacters() {
      assertEquals("hello world", StringHelper.revertLeet("hello world"));
    }

    @Test
    @DisplayName("Handles special characters that are not leet")
    void specialNonLeetCharacters() {
      // Characters like %, ^, *, +, =, etc. should be preserved
      String input = "test%^*+=test";
      String result = StringHelper.revertLeet(input);
      assertTrue(result.contains("%"));
      assertTrue(result.contains("^"));
      assertTrue(result.contains("*"));
    }

    @Test
    @DisplayName("Strips accents from characters")
    void stripsAccents() {
      // Unicode accented characters should be normalized
      assertEquals("cafe", StringHelper.revertLeet("caf\u00e9")); // e with acute
      assertEquals("resume", StringHelper.revertLeet("r\u00e9sum\u00e9"));
    }

    @Test
    @DisplayName("Handles Polish L characters")
    void handlesPolishL() {
      assertEquals("l", StringHelper.revertLeet("\u0142")); // lowercase ł
      assertEquals("l", StringHelper.revertLeet("\u0141")); // uppercase Ł -> L -> l
    }

    @Test
    @DisplayName("Handles Scandinavian O characters")
    void handlesScandinavianO() {
      assertEquals("o", StringHelper.revertLeet("\u00f8")); // lowercase ø
      assertEquals("o", StringHelper.revertLeet("\u00d8")); // uppercase Ø -> O -> o
    }

    @Test
    @DisplayName("Complex leet speak phrase")
    void complexLeetPhrase() {
      // "j01n my d1$c0rd" -> "join my discord"
      assertEquals("join my discord", StringHelper.revertLeet("j01n my d1$c0rd"));
    }

    @Test
    @DisplayName("URL with leet obfuscation")
    void urlWithLeetObfuscation() {
      // Spammers might try to obfuscate URLs
      String obfuscated = "d1sc0rd.gg";
      String result = StringHelper.revertLeet(obfuscated);
      assertEquals("discord.gg", result);
    }
  }

  @Nested
  @DisplayName("containsDigit tests")
  class ContainsDigitTests {

    @ParameterizedTest
    @ValueSource(strings = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"})
    @DisplayName("Returns true for single digits")
    void singleDigits(String digit) {
      assertTrue(StringHelper.containsDigit(digit));
    }

    @Test
    @DisplayName("Returns true for string with digit at start")
    void digitAtStart() {
      assertTrue(StringHelper.containsDigit("1abc"));
    }

    @Test
    @DisplayName("Returns true for string with digit at end")
    void digitAtEnd() {
      assertTrue(StringHelper.containsDigit("abc1"));
    }

    @Test
    @DisplayName("Returns true for string with digit in middle")
    void digitInMiddle() {
      assertTrue(StringHelper.containsDigit("ab1cd"));
    }

    @Test
    @DisplayName("Returns false for string without digits")
    void noDigits() {
      assertFalse(StringHelper.containsDigit("hello world"));
    }

    @Test
    @DisplayName("Returns false for empty string")
    void emptyString() {
      assertFalse(StringHelper.containsDigit(""));
    }

    @Test
    @DisplayName("Returns false for special characters only")
    void specialCharactersOnly() {
      assertFalse(StringHelper.containsDigit("!@#$%^&*()"));
    }

    @Test
    @DisplayName("Returns true for multiple digits")
    void multipleDigits() {
      assertTrue(StringHelper.containsDigit("abc123def"));
    }

    @Test
    @DisplayName("Returns true for only digits")
    void onlyDigits() {
      assertTrue(StringHelper.containsDigit("123456789"));
    }

    @ParameterizedTest
    @CsvSource({
        "hello, false",
        "hello1, true",
        "1hello, true",
        "hel1lo, true",
        "12345, true",
        "'', false",
        "h3llo, true",
    })
    @DisplayName("Parameterized containsDigit tests")
    void parameterizedTests(String input, boolean expected) {
      assertEquals(expected, StringHelper.containsDigit(input));
    }
  }

  @Nested
  @DisplayName("getUppercasePercentage tests")
  class GetUppercasePercentageTests {

    @Test
    @DisplayName("Returns 0 for empty string")
    void emptyString() {
      assertEquals(0, StringHelper.getUppercasePercentage(""));
    }

    @Test
    @DisplayName("Returns 0 for null")
    void nullString() {
      assertEquals(0, StringHelper.getUppercasePercentage(null));
    }

    @Test
    @DisplayName("Returns 100 for all uppercase")
    void allUppercase() {
      assertEquals(100, StringHelper.getUppercasePercentage("HELLO"));
    }

    @Test
    @DisplayName("Returns 0 for all lowercase")
    void allLowercase() {
      assertEquals(0, StringHelper.getUppercasePercentage("hello"));
    }

    @Test
    @DisplayName("Returns 50 for half uppercase")
    void halfUppercase() {
      // "HeLo" has H, L uppercase (2) and e, o lowercase (2) = 50%
      assertEquals(50, StringHelper.getUppercasePercentage("HeLo"));
      // "HEllo" has H, E uppercase (2) and l, l, o lowercase (3) = 40%
      assertEquals(40, StringHelper.getUppercasePercentage("HEllo"));
    }

    @Test
    @DisplayName("Ignores numbers")
    void ignoresNumbers() {
      // "AB12cd" has A, B uppercase (2), c, d lowercase (2) = 50%
      assertEquals(50, StringHelper.getUppercasePercentage("AB12cd"));
    }

    @Test
    @DisplayName("Ignores spaces")
    void ignoresSpaces() {
      assertEquals(100, StringHelper.getUppercasePercentage("HELLO WORLD"));
      assertEquals(0, StringHelper.getUppercasePercentage("hello world"));
    }

    @Test
    @DisplayName("Ignores special characters")
    void ignoresSpecialCharacters() {
      assertEquals(50, StringHelper.getUppercasePercentage("AB!@#cd"));
    }

    @Test
    @DisplayName("Returns 0 for string with only numbers")
    void onlyNumbers() {
      assertEquals(0, StringHelper.getUppercasePercentage("12345"));
    }

    @Test
    @DisplayName("Returns 0 for string with only special chars")
    void onlySpecialChars() {
      assertEquals(0, StringHelper.getUppercasePercentage("!@#$%"));
    }

    @ParameterizedTest
    @CsvSource({
        "hello, 0",
        "HELLO, 100",
        "HeLLo, 60",
        "Hello, 20",
        "hELLO, 80",
        "123, 0",
        "'   ', 0"
    })
    @DisplayName("Parameterized uppercase percentage tests")
    void parameterizedTests(String input, int expected) {
      assertEquals(expected, StringHelper.getUppercasePercentage(input));
    }
  }

  @Nested
  @DisplayName("hasExcessiveRepetition tests")
  class HasExcessiveRepetitionTests {

    @Test
    @DisplayName("Returns false for normal text")
    void normalText() {
      assertFalse(StringHelper.hasExcessiveRepetition("hello", 3));
    }

    @Test
    @DisplayName("Returns true for excessive repetition")
    void excessiveRepetition() {
      assertTrue(StringHelper.hasExcessiveRepetition("heeeey", 3)); // 4 e's
    }

    @Test
    @DisplayName("Returns false for allowed repetition")
    void allowedRepetition() {
      assertFalse(StringHelper.hasExcessiveRepetition("heey", 3)); // 2 e's
      assertFalse(StringHelper.hasExcessiveRepetition("heeey", 3)); // 3 e's (exactly max)
    }

    @Test
    @DisplayName("Returns false for null")
    void nullString() {
      assertFalse(StringHelper.hasExcessiveRepetition(null, 3));
    }

    @Test
    @DisplayName("Returns false for empty string")
    void emptyString() {
      assertFalse(StringHelper.hasExcessiveRepetition("", 3));
    }

    @Test
    @DisplayName("Returns false for short string")
    void shortString() {
      assertFalse(StringHelper.hasExcessiveRepetition("aa", 3));
    }

    @Test
    @DisplayName("Handles mixed case")
    void mixedCase() {
      assertTrue(StringHelper.hasExcessiveRepetition("hEeEey", 3)); // 4 e's (case insensitive)
    }

    @Test
    @DisplayName("Only checks letters")
    void onlyChecksLetters() {
      // Numbers can repeat
      assertFalse(StringHelper.hasExcessiveRepetition("111222333", 3));
    }

    @ParameterizedTest
    @CsvSource({
        "hello, 3, false",
        "heeeeey, 3, true",
        "noooooo, 3, true",
        "yaaay, 3, false",
        "yaaaay, 3, true",
        "HELLOOOO, 3, true"
    })
    @DisplayName("Parameterized excessive repetition tests")
    void parameterizedTests(String input, int maxChars, boolean expected) {
      assertEquals(expected, StringHelper.hasExcessiveRepetition(input, maxChars));
    }
  }

  @Nested
  @DisplayName("fixRepetition tests")
  class FixRepetitionTests {

    @Test
    @DisplayName("Returns null for null input")
    void nullInput() {
      assertNull(StringHelper.fixRepetition(null, 3));
    }

    @Test
    @DisplayName("Returns same string for normal text")
    void normalText() {
      assertEquals("hello", StringHelper.fixRepetition("hello", 3));
    }

    @Test
    @DisplayName("Fixes excessive repetition")
    void fixesExcessiveRepetition() {
      assertEquals("heeey", StringHelper.fixRepetition("heeeeeey", 3));
    }

    @Test
    @DisplayName("Preserves allowed repetition")
    void preservesAllowedRepetition() {
      assertEquals("heey", StringHelper.fixRepetition("heey", 3));
      assertEquals("heeey", StringHelper.fixRepetition("heeey", 3));
    }

    @Test
    @DisplayName("Handles mixed case preservation")
    void mixedCasePreservation() {
      // Preserves case of first N characters (where N = maxChars)
      // "hEeEeEy" -> h, E (new), e, E (count 4 - skipped), e (skipped), E (skipped), y
      // Result: "hEeEy" - keeps first 3 Es as they appear: E, e, E
      assertEquals("hEeEy", StringHelper.fixRepetition("hEeEeEy", 3));
    }

    @Test
    @DisplayName("Fixes multiple repetitions")
    void fixesMultipleRepetitions() {
      assertEquals("nooo waaay", StringHelper.fixRepetition("nooooooo waaaaaay", 3));
    }

    @Test
    @DisplayName("Returns short strings unchanged")
    void shortStrings() {
      assertEquals("aa", StringHelper.fixRepetition("aa", 3));
      assertEquals("aaa", StringHelper.fixRepetition("aaa", 3));
    }

    @ParameterizedTest
    @CsvSource({
        "heeeey, 3, heeey",
        "noooooo, 3, nooo",
        "yaaay, 3, yaaay",
        "yaaaay, 3, yaaay",
        "HELLOOOO, 3, HELLOOO"
    })
    @DisplayName("Parameterized fix repetition tests")
    void parameterizedTests(String input, int maxChars, String expected) {
      assertEquals(expected, StringHelper.fixRepetition(input, maxChars));
    }
  }

  @Nested
  @DisplayName("Edge cases and integration")
  class EdgeCasesAndIntegration {

    @Test
    @DisplayName("Leet speak detection after revert")
    void leetSpeakDetectionAfterRevert() {
      // After reverting leet, "h3ll0" becomes "helio" which has no digits
      String reverted = StringHelper.revertLeet("h3ll0");
      assertFalse(StringHelper.containsDigit(reverted));
    }

    @Test
    @DisplayName("Unicode digits are detected")
    void unicodeDigits() {
      // Arabic-Indic digits
      assertTrue(StringHelper.containsDigit("\u0661")); // Arabic 1
      assertTrue(StringHelper.containsDigit("\u0662")); // Arabic 2
    }

    @Test
    @DisplayName("Very long string with digit at end")
    void longStringWithDigitAtEnd() {
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < 1000; i++) {
        sb.append("a");
      }
      sb.append("1");
      assertTrue(StringHelper.containsDigit(sb.toString()));
    }

    @Test
    @DisplayName("String with only spaces")
    void stringWithOnlySpaces() {
      assertFalse(StringHelper.containsDigit("     "));
    }

    @Test
    @DisplayName("Combination of leet and regular text")
    void combinationLeetAndRegular() {
      // "Hello W0r1d" -> "hello worid"
      String result = StringHelper.revertLeet("Hello W0r1d");
      assertEquals("hello worid", result);
      assertFalse(StringHelper.containsDigit(result));
    }

    @Test
    @DisplayName("Full sentence leet conversion")
    void fullSentenceLeet() {
      // Note: $ -> s, so http$ -> https
      String leet = "j01n u$ 4t http$://d1$c0rd.c0m/1nv1t3";
      String expected = "join us at https://discord.com/invite";
      assertEquals(expected, StringHelper.revertLeet(leet));
    }
  }
}
