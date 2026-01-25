package net.pistonmaster.pistonfilter.utils;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public class StringHelper {
  public static String revertLeet(String str) {
    str = str.toLowerCase(Locale.ROOT);

    str = str.replace("0", "o");
    str = str.replace("1", "i");
    str = str.replace("2", "z");
    str = str.replace("3", "e");
    str = str.replace("4", "a");
    str = str.replace("5", "s");
    str = str.replace("6", "g");
    str = str.replace("7", "t");
    str = str.replace("8", "b");
    str = str.replace("9", "g");
    str = str.replace("&", "a");
    str = str.replace("@", "a");
    str = str.replace("(", "c");
    str = str.replace("#", "h");
    str = str.replace("!", "i");
    str = str.replace("]", "i");
    str = str.replace("|", "i");
    str = str.replace("}", "i");
    str = str.replace("?", "o");
    str = str.replace("$", "s");
    str = stripAccents(str);

    return str;
  }

  public static boolean containsDigit(String s) {
    for (int i = 0; i < s.length(); i++) {
      if (Character.isDigit(s.charAt(i))) {
        return true;
      }
    }

    return false;
  }

  /**
   * Calculates the percentage of uppercase letters in a string.
   * Only counts letters (ignores numbers, spaces, and special characters).
   *
   * @param message the message to check
   * @return the percentage (0-100) of uppercase letters, or 0 if no letters found
   */
  public static int getUppercasePercentage(String message) {
    if (message == null || message.isEmpty()) {
      return 0;
    }

    int letterCount = 0;
    int uppercaseCount = 0;

    for (int i = 0; i < message.length(); i++) {
      char c = message.charAt(i);
      if (Character.isLetter(c)) {
        letterCount++;
        if (Character.isUpperCase(c)) {
          uppercaseCount++;
        }
      }
    }

    if (letterCount == 0) {
      return 0;
    }

    return (int) ((uppercaseCount * 100.0) / letterCount);
  }

  /**
   * Checks if a message contains excessive consecutive repeated characters.
   *
   * @param message  the message to check
   * @param maxChars the maximum allowed consecutive repeated characters
   * @return true if the message has excessive repetition
   */
  public static boolean hasExcessiveRepetition(String message, int maxChars) {
    if (message == null || message.length() < maxChars + 1) {
      return false;
    }

    String lower = message.toLowerCase(Locale.ROOT);
    int count = 1;
    char lastChar = lower.charAt(0);

    for (int i = 1; i < lower.length(); i++) {
      char c = lower.charAt(i);
      if (c == lastChar && Character.isLetter(c)) {
        count++;
        if (count > maxChars) {
          return true;
        }
      } else {
        count = 1;
        lastChar = c;
      }
    }

    return false;
  }

  /**
   * Fixes excessive character repetition in a message by limiting consecutive
   * identical characters to the specified maximum.
   *
   * @param message  the message to fix
   * @param maxChars the maximum consecutive identical characters allowed
   * @return the fixed message
   */
  public static String fixRepetition(String message, int maxChars) {
    if (message == null || message.length() < maxChars + 1) {
      return message;
    }

    StringBuilder result = new StringBuilder();
    int count = 1;
    char lastChar = message.charAt(0);
    char lastCharLower = Character.toLowerCase(lastChar);
    result.append(lastChar);

    for (int i = 1; i < message.length(); i++) {
      char c = message.charAt(i);
      char cLower = Character.toLowerCase(c);

      if (cLower == lastCharLower && Character.isLetter(c)) {
        count++;
        if (count <= maxChars) {
          result.append(c);
        }
      } else {
        count = 1;
        lastChar = c;
        lastCharLower = cLower;
        result.append(c);
      }
    }

    return result.toString();
  }

  private static String stripAccents(final String input) {
    StringBuilder decomposed = new StringBuilder(Normalizer.normalize(input, Normalizer.Form.NFD));
    for (int i = 0; i < decomposed.length(); i++) {
      if (decomposed.charAt(i) == 'Ł') {
        decomposed.setCharAt(i, 'L');
      } else if (decomposed.charAt(i) == 'ł') {
        decomposed.setCharAt(i, 'l');
      } else if (decomposed.charAt(i) == 'Ø') {
        decomposed.setCharAt(i, 'O');
      } else if (decomposed.charAt(i) == 'ø') {
        decomposed.setCharAt(i, 'o');
      }
    }
    return Pattern.compile("\\p{InCombiningDiacriticalMarks}+").matcher(decomposed).replaceAll("");
  }

  private StringHelper() {
  }
}
