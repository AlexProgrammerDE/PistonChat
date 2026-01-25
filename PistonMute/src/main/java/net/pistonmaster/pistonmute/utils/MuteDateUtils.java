package net.pistonmaster.pistonmute.utils;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for date parsing and mute time calculations.
 * This class is designed to be easily testable without requiring plugin initialization.
 */
public final class MuteDateUtils {
  /**
   * Legacy date format used by older versions of the plugin.
   * Example: "Sat Jan 25 14:30:00 UTC 2026"
   */
  public static final DateTimeFormatter LEGACY_FORMATTER =
      DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);

  /**
   * Pattern for parsing time duration suffixes.
   * Matches patterns like "5m", "2h", "1d", "1y", "30s".
   */
  private static final Pattern TIME_SUFFIX_PATTERN = Pattern.compile("^(\\d+)([smhdy])$");

  private MuteDateUtils() {
  }

  /**
   * Parses a mute timestamp from storage.
   * Supports both ISO-8601 format (e.g., "2026-01-25T14:30:00Z") and
   * legacy format (e.g., "Sat Jan 25 14:30:00 UTC 2026").
   *
   * @param raw The raw string to parse
   * @return Optional containing the parsed Instant, or empty if parsing failed or input was null
   */
  public static Optional<Instant> parseMuteUntil(String raw) {
    if (raw == null || raw.isBlank()) {
      return Optional.empty();
    }

    // Try ISO format first
    try {
      return Optional.of(Instant.parse(raw));
    } catch (DateTimeParseException ignored) {
      // Fall through to legacy format
    }

    // Try legacy format
    try {
      return Optional.of(Instant.from(LEGACY_FORMATTER.parse(raw)));
    } catch (DateTimeParseException e) {
      return Optional.empty();
    }
  }

  /**
   * Checks if a mute has expired.
   *
   * @param muteUntil The instant when the mute expires
   * @return true if the mute has expired (current time is at or after muteUntil), false otherwise
   */
  public static boolean isMuteExpired(Instant muteUntil) {
    if (muteUntil == null) {
      return true;
    }
    return !Instant.now().isBefore(muteUntil);
  }

  /**
   * Checks if a mute has expired at a specific point in time.
   * This method is useful for testing.
   *
   * @param muteUntil   The instant when the mute expires
   * @param currentTime The current time to check against
   * @return true if the mute has expired at the given time, false otherwise
   */
  public static boolean isMuteExpiredAt(Instant muteUntil, Instant currentTime) {
    if (muteUntil == null || currentTime == null) {
      return true;
    }
    return !currentTime.isBefore(muteUntil);
  }

  /**
   * Parses a time duration suffix string and calculates the resulting mute expiration time.
   * Supports the following suffixes:
   * <ul>
   *   <li>s - seconds (e.g., "30s" = 30 seconds)</li>
   *   <li>m - minutes (e.g., "5m" = 5 minutes)</li>
   *   <li>h - hours (e.g., "2h" = 2 hours)</li>
   *   <li>d - days (e.g., "1d" = 1 day)</li>
   *   <li>y - years (e.g., "1y" = 365 days)</li>
   * </ul>
   *
   * @param timeSuffix The time suffix string (e.g., "5m", "2h", "1d")
   * @param baseTime   The base time to add the duration to
   * @return Optional containing the calculated expiration time, or empty if parsing failed
   */
  public static Optional<Instant> parseTimeSuffix(String timeSuffix, Instant baseTime) {
    if (timeSuffix == null || baseTime == null) {
      return Optional.empty();
    }

    String normalized = timeSuffix.toLowerCase(Locale.ROOT);
    Matcher matcher = TIME_SUFFIX_PATTERN.matcher(normalized);

    if (!matcher.matches()) {
      return Optional.empty();
    }

    try {
      int amount = Integer.parseInt(matcher.group(1));
      String unit = matcher.group(2);

      Instant result = switch (unit) {
        case "s" -> baseTime.plus(amount, ChronoUnit.SECONDS);
        case "m" -> baseTime.plus(amount, ChronoUnit.MINUTES);
        case "h" -> baseTime.plus(amount, ChronoUnit.HOURS);
        case "d" -> baseTime.plus(amount, ChronoUnit.DAYS);
        case "y" -> baseTime.plus((long) amount * 365, ChronoUnit.DAYS);
        default -> null;
      };

      return Optional.ofNullable(result);
    } catch (NumberFormatException e) {
      return Optional.empty();
    }
  }

  /**
   * Parses a time duration suffix string and calculates the resulting mute expiration time
   * from the current moment.
   *
   * @param timeSuffix The time suffix string (e.g., "5m", "2h", "1d")
   * @return Optional containing the calculated expiration time, or empty if parsing failed
   */
  public static Optional<Instant> parseTimeSuffix(String timeSuffix) {
    return parseTimeSuffix(timeSuffix, Instant.now());
  }
}
