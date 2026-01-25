package net.pistonmaster.pistonchat.utils;

import java.util.Arrays;

/**
 * Utility class for common string operations.
 */
public final class StringUtils {
  private StringUtils() {
  }

  /**
   * Merges array elements starting from a given index into a single space-separated string.
   *
   * @param args  The array of strings to merge
   * @param start The starting index (inclusive)
   * @return A space-separated string of all elements from start to end
   */
  public static String mergeArgs(String[] args, int start) {
    return String.join(" ", Arrays.copyOfRange(args, start, args.length));
  }
}
