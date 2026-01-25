package net.pistonmaster.pistonmute.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MuteDateUtils")
class MuteDateUtilsTest {

  @Nested
  @DisplayName("parseMuteUntil")
  class ParseMuteUntilTests {

    @Test
    @DisplayName("should parse ISO-8601 format correctly")
    void shouldParseIsoFormat() {
      String isoDate = "2026-01-25T14:30:00Z";

      Optional<Instant> result = MuteDateUtils.parseMuteUntil(isoDate);

      assertTrue(result.isPresent(), "Should successfully parse ISO format");
      assertEquals(Instant.parse(isoDate), result.get());
    }

    @Test
    @DisplayName("should parse ISO-8601 format with milliseconds")
    void shouldParseIsoFormatWithMillis() {
      String isoDate = "2026-01-25T14:30:00.123Z";

      Optional<Instant> result = MuteDateUtils.parseMuteUntil(isoDate);

      assertTrue(result.isPresent(), "Should successfully parse ISO format with milliseconds");
      assertEquals(Instant.parse(isoDate), result.get());
    }

    @Test
    @DisplayName("should parse legacy date format correctly")
    void shouldParseLegacyFormat() {
      // Legacy format: "EEE MMM dd HH:mm:ss zzz yyyy"
      // January 25, 2026 is a Sunday
      String legacyDate = "Sun Jan 25 14:30:00 UTC 2026";

      Optional<Instant> result = MuteDateUtils.parseMuteUntil(legacyDate);

      assertTrue(result.isPresent(), "Should successfully parse legacy format");
      // Verify the parsed date is correct
      ZonedDateTime expected = ZonedDateTime.of(2026, 1, 25, 14, 30, 0, 0, ZoneId.of("UTC"));
      assertEquals(expected.toInstant(), result.get());
    }

    @Test
    @DisplayName("should parse legacy date format with different timezone")
    void shouldParseLegacyFormatWithTimezone() {
      // February 10, 2026 is a Tuesday
      String legacyDate = "Tue Feb 10 08:15:30 GMT 2026";

      Optional<Instant> result = MuteDateUtils.parseMuteUntil(legacyDate);

      assertTrue(result.isPresent(), "Should successfully parse legacy format with GMT timezone");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    @DisplayName("should return empty for null, empty, or blank input")
    void shouldReturnEmptyForNullOrEmpty(String input) {
      Optional<Instant> result = MuteDateUtils.parseMuteUntil(input);

      assertTrue(result.isEmpty(), "Should return empty for null, empty, or blank input");
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "invalid-date",
        "2026/01/25",
        "25-01-2026",
        "January 25, 2026",
        "not a date at all",
        "12345678"
    })
    @DisplayName("should return empty for invalid date formats")
    void shouldReturnEmptyForInvalidFormats(String invalidDate) {
      Optional<Instant> result = MuteDateUtils.parseMuteUntil(invalidDate);

      assertTrue(result.isEmpty(), "Should return empty for invalid format: " + invalidDate);
    }

    @Test
    @DisplayName("should prefer ISO format over legacy when both could match")
    void shouldPreferIsoFormat() {
      String isoDate = "2026-01-25T00:00:00Z";

      Optional<Instant> result = MuteDateUtils.parseMuteUntil(isoDate);

      assertTrue(result.isPresent());
      assertEquals(Instant.parse(isoDate), result.get());
    }
  }

  @Nested
  @DisplayName("isMuteExpired")
  class IsMuteExpiredTests {

    @Test
    @DisplayName("should return true for null muteUntil")
    void shouldReturnTrueForNull() {
      assertTrue(MuteDateUtils.isMuteExpired(null));
    }

    @Test
    @DisplayName("should return true for past date")
    void shouldReturnTrueForPastDate() {
      Instant pastDate = Instant.now().minus(1, ChronoUnit.HOURS);

      assertTrue(MuteDateUtils.isMuteExpired(pastDate));
    }

    @Test
    @DisplayName("should return false for future date")
    void shouldReturnFalseForFutureDate() {
      Instant futureDate = Instant.now().plus(1, ChronoUnit.HOURS);

      assertFalse(MuteDateUtils.isMuteExpired(futureDate));
    }

    @Test
    @DisplayName("should return true for date far in the past")
    void shouldReturnTrueForDistantPast() {
      Instant distantPast = Instant.now().minus(365, ChronoUnit.DAYS);

      assertTrue(MuteDateUtils.isMuteExpired(distantPast));
    }

    @Test
    @DisplayName("should return false for date far in the future")
    void shouldReturnFalseForDistantFuture() {
      Instant distantFuture = Instant.now().plus(365, ChronoUnit.DAYS);

      assertFalse(MuteDateUtils.isMuteExpired(distantFuture));
    }
  }

  @Nested
  @DisplayName("isMuteExpiredAt")
  class IsMuteExpiredAtTests {

    @Test
    @DisplayName("should return true when currentTime equals muteUntil")
    void shouldReturnTrueWhenTimesAreEqual() {
      Instant time = Instant.parse("2026-01-25T12:00:00Z");

      assertTrue(MuteDateUtils.isMuteExpiredAt(time, time));
    }

    @Test
    @DisplayName("should return true when currentTime is after muteUntil")
    void shouldReturnTrueWhenCurrentTimeIsAfter() {
      Instant muteUntil = Instant.parse("2026-01-25T12:00:00Z");
      Instant currentTime = Instant.parse("2026-01-25T12:00:01Z");

      assertTrue(MuteDateUtils.isMuteExpiredAt(muteUntil, currentTime));
    }

    @Test
    @DisplayName("should return false when currentTime is before muteUntil")
    void shouldReturnFalseWhenCurrentTimeIsBefore() {
      Instant muteUntil = Instant.parse("2026-01-25T12:00:00Z");
      Instant currentTime = Instant.parse("2026-01-25T11:59:59Z");

      assertFalse(MuteDateUtils.isMuteExpiredAt(muteUntil, currentTime));
    }

    @Test
    @DisplayName("should return true for null muteUntil")
    void shouldReturnTrueForNullMuteUntil() {
      Instant currentTime = Instant.now();

      assertTrue(MuteDateUtils.isMuteExpiredAt(null, currentTime));
    }

    @Test
    @DisplayName("should return true for null currentTime")
    void shouldReturnTrueForNullCurrentTime() {
      Instant muteUntil = Instant.now();

      assertTrue(MuteDateUtils.isMuteExpiredAt(muteUntil, null));
    }

    @Test
    @DisplayName("should return true when both are null")
    void shouldReturnTrueWhenBothNull() {
      assertTrue(MuteDateUtils.isMuteExpiredAt(null, null));
    }

    @Test
    @DisplayName("should handle edge case at exact millisecond boundary")
    void shouldHandleMillisecondBoundary() {
      Instant muteUntil = Instant.parse("2026-01-25T12:00:00.000Z");
      Instant justBefore = Instant.parse("2026-01-25T11:59:59.999Z");
      Instant exactTime = Instant.parse("2026-01-25T12:00:00.000Z");
      Instant justAfter = Instant.parse("2026-01-25T12:00:00.001Z");

      assertFalse(MuteDateUtils.isMuteExpiredAt(muteUntil, justBefore), "Should not be expired just before");
      assertTrue(MuteDateUtils.isMuteExpiredAt(muteUntil, exactTime), "Should be expired at exact time");
      assertTrue(MuteDateUtils.isMuteExpiredAt(muteUntil, justAfter), "Should be expired just after");
    }
  }

  @Nested
  @DisplayName("parseTimeSuffix")
  class ParseTimeSuffixTests {

    private final Instant baseTime = Instant.parse("2026-01-25T12:00:00Z");

    @Test
    @DisplayName("should parse seconds suffix")
    void shouldParseSeconds() {
      Optional<Instant> result = MuteDateUtils.parseTimeSuffix("30s", baseTime);

      assertTrue(result.isPresent());
      assertEquals(baseTime.plus(30, ChronoUnit.SECONDS), result.get());
    }

    @Test
    @DisplayName("should parse minutes suffix")
    void shouldParseMinutes() {
      Optional<Instant> result = MuteDateUtils.parseTimeSuffix("5m", baseTime);

      assertTrue(result.isPresent());
      assertEquals(baseTime.plus(5, ChronoUnit.MINUTES), result.get());
    }

    @Test
    @DisplayName("should parse hours suffix")
    void shouldParseHours() {
      Optional<Instant> result = MuteDateUtils.parseTimeSuffix("2h", baseTime);

      assertTrue(result.isPresent());
      assertEquals(baseTime.plus(2, ChronoUnit.HOURS), result.get());
    }

    @Test
    @DisplayName("should parse days suffix")
    void shouldParseDays() {
      Optional<Instant> result = MuteDateUtils.parseTimeSuffix("7d", baseTime);

      assertTrue(result.isPresent());
      assertEquals(baseTime.plus(7, ChronoUnit.DAYS), result.get());
    }

    @Test
    @DisplayName("should parse years suffix as 365 days")
    void shouldParseYears() {
      Optional<Instant> result = MuteDateUtils.parseTimeSuffix("1y", baseTime);

      assertTrue(result.isPresent());
      assertEquals(baseTime.plus(365, ChronoUnit.DAYS), result.get());
    }

    @Test
    @DisplayName("should parse multiple years correctly")
    void shouldParseMultipleYears() {
      Optional<Instant> result = MuteDateUtils.parseTimeSuffix("2y", baseTime);

      assertTrue(result.isPresent());
      assertEquals(baseTime.plus(730, ChronoUnit.DAYS), result.get());
    }

    @Test
    @DisplayName("should be case insensitive")
    void shouldBeCaseInsensitive() {
      Optional<Instant> lowercase = MuteDateUtils.parseTimeSuffix("5m", baseTime);
      Optional<Instant> uppercase = MuteDateUtils.parseTimeSuffix("5M", baseTime);

      assertEquals(lowercase, uppercase);
    }

    @Test
    @DisplayName("should handle large numbers")
    void shouldHandleLargeNumbers() {
      Optional<Instant> result = MuteDateUtils.parseTimeSuffix("1000h", baseTime);

      assertTrue(result.isPresent());
      assertEquals(baseTime.plus(1000, ChronoUnit.HOURS), result.get());
    }

    @Test
    @DisplayName("should handle zero value")
    void shouldHandleZero() {
      Optional<Instant> result = MuteDateUtils.parseTimeSuffix("0m", baseTime);

      assertTrue(result.isPresent());
      assertEquals(baseTime, result.get());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("should return empty for null or empty input")
    void shouldReturnEmptyForNullOrEmpty(String input) {
      Optional<Instant> result = MuteDateUtils.parseTimeSuffix(input, baseTime);

      assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("should return empty for null baseTime")
    void shouldReturnEmptyForNullBaseTime() {
      Optional<Instant> result = MuteDateUtils.parseTimeSuffix("5m", null);

      assertTrue(result.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "5x",       // invalid unit
        "5",        // no unit
        "m5",       // wrong order
        "5.5m",     // decimal not supported
        "-5m",      // negative
        "5 m",      // space
        "five minutes",
        "5mm",      // double unit
        ""
    })
    @DisplayName("should return empty for invalid formats")
    void shouldReturnEmptyForInvalidFormats(String invalid) {
      Optional<Instant> result = MuteDateUtils.parseTimeSuffix(invalid, baseTime);

      assertTrue(result.isEmpty(), "Should return empty for invalid format: " + invalid);
    }

    @Test
    @DisplayName("single parameter version should use current time as base")
    void singleParameterShouldUseCurrentTime() {
      Instant before = Instant.now();
      Optional<Instant> result = MuteDateUtils.parseTimeSuffix("5m");
      Instant after = Instant.now();

      assertTrue(result.isPresent());
      Instant expected = before.plus(5, ChronoUnit.MINUTES);
      Instant expectedAfter = after.plus(5, ChronoUnit.MINUTES);

      // The result should be between expected and expectedAfter
      assertTrue(
          !result.get().isBefore(expected) && !result.get().isAfter(expectedAfter),
          "Result should be approximately 5 minutes from now"
      );
    }
  }

  @Nested
  @DisplayName("Integration scenarios")
  class IntegrationTests {

    @Test
    @DisplayName("should round-trip ISO format correctly")
    void shouldRoundTripIsoFormat() {
      Instant original = Instant.now().truncatedTo(ChronoUnit.MILLIS);
      String serialized = original.toString();

      Optional<Instant> parsed = MuteDateUtils.parseMuteUntil(serialized);

      assertTrue(parsed.isPresent());
      assertEquals(original, parsed.get());
    }

    @Test
    @DisplayName("should correctly determine mute status for parsed dates")
    void shouldDetermineMuteStatusForParsedDates() {
      // Create a mute that expires in 1 hour
      Instant baseTime = Instant.parse("2026-01-25T12:00:00Z");
      Optional<Instant> muteUntil = MuteDateUtils.parseTimeSuffix("1h", baseTime);

      assertTrue(muteUntil.isPresent());

      // 30 minutes later - should NOT be expired
      Instant thirtyMinutesLater = baseTime.plus(30, ChronoUnit.MINUTES);
      assertFalse(MuteDateUtils.isMuteExpiredAt(muteUntil.get(), thirtyMinutesLater));

      // 2 hours later - should be expired
      Instant twoHoursLater = baseTime.plus(2, ChronoUnit.HOURS);
      assertTrue(MuteDateUtils.isMuteExpiredAt(muteUntil.get(), twoHoursLater));
    }

    @Test
    @DisplayName("should handle typical workflow: parse stored date and check expiration")
    void shouldHandleTypicalWorkflow() {
      // Simulate stored mute date from data.yml
      String storedDate = "2026-06-15T18:30:00Z";

      Optional<Instant> muteUntil = MuteDateUtils.parseMuteUntil(storedDate);
      assertTrue(muteUntil.isPresent());

      // Check at different times
      Instant before = Instant.parse("2026-06-15T18:00:00Z");
      Instant after = Instant.parse("2026-06-15T19:00:00Z");

      assertFalse(MuteDateUtils.isMuteExpiredAt(muteUntil.get(), before), "Should not be expired before mute end");
      assertTrue(MuteDateUtils.isMuteExpiredAt(muteUntil.get(), after), "Should be expired after mute end");
    }
  }
}
