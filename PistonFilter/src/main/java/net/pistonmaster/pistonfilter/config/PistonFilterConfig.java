package net.pistonmaster.pistonfilter.config;

import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;

import java.util.List;

@Configuration
public class PistonFilterConfig {
  @Comment("Enable verbose logging for debugging")
  public boolean verbose = true;

  @Comment("Send a message to the player when their message is blocked")
  public boolean messageSender = true;

  @Comment("Notify staff members when a message is blocked")
  public boolean notifyStaff = true;

  @Comment({"Format for staff notifications when a message is blocked",
      "Placeholders: %player%, %message%, %reason%"})
  public String staffNotificationFormat = "&c[Filter] &e%player% &7was blocked: &f%message% &7(&c%reason%&7)";

  @Comment("Message shown to players when chat is paused")
  public String chatPausedMessage = "&cChat is currently paused. Please wait for staff to resume it.";

  @Comment("Message shown to staff when chat is paused")
  public String chatPausedStaffMessage = "&e[Filter] &aChat has been paused by &f%player%";

  @Comment("Message shown to staff when chat is unpaused")
  public String chatUnpausedStaffMessage = "&e[Filter] &aChat has been resumed by &f%player%";

  @Comment("Number of blank lines to send when clearing chat")
  public int clearChatLines = 100;

  @Comment("Enable repeat message detection")
  public boolean noRepeat = true;

  @Comment("How many seconds after sending a message should the player wait before sending the same message again")
  public int noRepeatTime = 60;

  @Comment("How many messages are stored that are not allowed to be repeated?")
  public int noRepeatStackSize = 20;

  @Comment("How similar from 1-100 are two messages allowed to be before they are considered the same?")
  public int noRepeatSimilarRatio = 90;

  @Comment("Check words of previous messages for repeated word ratio. -1 to disable. Value means how many percent of the messages has repeated words.")
  public int noRepeatWordRatio = 40;

  @Comment("How many of the last messages of a user to check for number patterns")
  public int noRepeatNumberMessages = 5;

  @Comment("How many messages with numbers are allowed before blocking")
  public int noRepeatNumberAmount = 3;

  @Comment("Store last global messages and compare them for no repeat as well")
  public boolean globalRepeatCheck = true;

  @Comment("Store the last x amount of global messages to compare for no repeat")
  public int globalMessageStackSize = 40;

  @Comment("Should we mute a user if they fail the above checks too often? (Requires PistonMute)")
  public boolean muteOnFail = true;

  @Comment("After how many violations of our checks do we mute someone?")
  public int muteViolations = 3;

  @Comment("How long until violations expire?")
  public int muteViolationsTimeframe = 60;

  @Comment("For how long do we mute someone? (seconds)")
  public int muteTime = 60;

  @Comment("Maximum words containing numbers allowed in a message (e.g. \"2 5f gg 8b\" has 3 words with numbers)")
  public int maxWordsWithNumbers = 5;

  @Comment("Maximum separated number groups allowed in a single word")
  public int maxSeparatedNumbers = 3;

  @Comment("Maximum length of a single word before it's considered spam")
  public int maxWordLength = 20;

  @Comment("Fuzzy match ratio (1-100) for banned text detection. Higher = stricter matching")
  public int bannedTextPartialRatio = 95;

  @Comment("List of banned text patterns (URLs, domains, etc.)")
  public List<String> bannedText = List.of(
      "http",
      "https",
      "://",
      ".org",
      ".com",
      ".net",
      ".co",
      ".cc",
      ".tk",
      ".ga",
      ".gg",
      ".io"
  );

  // ========== Sign Filtering ==========
  @Comment("Enable filtering of text on signs")
  public boolean filterSigns = true;

  @Comment("Cancel sign placement when filtered content is detected (if false, the offending line is cleared)")
  public boolean cancelSignOnFilter = false;

  // ========== Book Filtering ==========
  @Comment("Enable filtering of book content (title and pages)")
  public boolean filterBooks = true;

  @Comment("Cancel book editing when filtered content is detected")
  public boolean cancelBookOnFilter = true;

  // ========== Anvil Filtering ==========
  @Comment("Enable filtering of anvil rename text")
  public boolean filterAnvil = true;

  @Comment("Cancel anvil rename when filtered content is detected")
  public boolean cancelAnvilOnFilter = true;

  // ========== Command Filtering ==========
  @Comment("Enable filtering of commands that contain chat messages")
  public boolean filterCommands = true;

  @Comment("List of commands to filter (without the leading slash). Supports formats: 'me', 'msg', 'tell <player>'")
  public List<String> filteredCommands = List.of(
      "me",
      "say",
      "msg",
      "tell",
      "w",
      "whisper",
      "reply",
      "r",
      "m",
      "pm",
      "dm",
      "message",
      "emsg",
      "etell",
      "ewhisper",
      "er",
      "ereply"
  );

  @Comment("Commands that require skipping one argument (the target player) before the message")
  public List<String> commandsWithTarget = List.of(
      "msg",
      "tell",
      "w",
      "whisper",
      "m",
      "pm",
      "dm",
      "message",
      "emsg",
      "etell",
      "ewhisper"
  );

  // ========== Unicode/Special Character Filtering ==========
  @Comment("Enable Unicode character filtering")
  public boolean filterUnicode = false;

  @Comment("Block all non-ASCII characters (characters outside 0x00-0x7F range)")
  public boolean blockNonAscii = false;

  @Comment("Block Mathematical Alphanumeric Symbols (U+1D400-U+1D7FF) commonly used by hacked clients")
  public boolean blockMathAlphanumeric = true;

  @Comment("Block other common hacked client font ranges (Fullwidth, Circled, etc.)")
  public boolean blockHackedClientFonts = true;

  @Comment({"Allowed Unicode ranges for international servers (format: 'start-end' in hex, e.g., '0400-04FF' for Cyrillic)",
      "Only used when blockNonAscii is true. Common ranges:",
      "  Latin Extended: 0080-024F",
      "  Cyrillic: 0400-04FF",
      "  Greek: 0370-03FF",
      "  Arabic: 0600-06FF",
      "  CJK (Chinese/Japanese/Korean): 4E00-9FFF",
      "  Hiragana: 3040-309F",
      "  Katakana: 30A0-30FF",
      "  Hangul: AC00-D7AF"})
  public List<String> allowedUnicodeRanges = List.of(
      "0080-00FF",  // Latin-1 Supplement (accented chars)
      "0100-017F",  // Latin Extended-A
      "0180-024F"   // Latin Extended-B
  );

  // ============================================
  // Message Cooldown Settings
  // ============================================

  @Comment({"", "Enable message cooldown (players must wait between messages)",
      "Bypass permission: pistonfilter.bypass.cooldown"})
  public boolean cooldownEnabled = true;

  @Comment("Cooldown time in milliseconds between messages (e.g., 3000 = 3 seconds)")
  public long cooldownTime = 3000;

  @Comment("Message shown to players when they send messages too fast")
  public String cooldownMessage = "&cPlease wait before sending another message!";

  // ============================================
  // Anti-Caps Settings
  // ============================================

  @Comment({"", "Enable anti-caps filter to prevent excessive uppercase messages",
      "Bypass permission: pistonfilter.bypass.caps"})
  public boolean antiCapsEnabled = true;

  @Comment("Maximum percentage of uppercase characters allowed (0-100)")
  public int antiCapsMaxPercent = 50;

  @Comment("Minimum message length before caps check applies (shorter messages are ignored)")
  public int antiCapsMinLength = 5;

  @Comment("Auto-lowercase messages instead of blocking them")
  public boolean antiCapsAutoLowercase = true;

  @Comment("Message shown to players when their message has too many caps (only if autoLowercase is false)")
  public String antiCapsMessage = "&cPlease don't use excessive caps!";

  // ============================================
  // Character Repetition Settings
  // ============================================

  @Comment({"", "Enable character repetition filter to prevent messages like 'heeeeeey' or 'noooooo'",
      "Bypass permission: pistonfilter.bypass.repetition"})
  public boolean repetitionEnabled = true;

  @Comment("Maximum consecutive repeated characters allowed (e.g., 3 allows 'heey' but blocks 'heeey')")
  public int repetitionMaxChars = 3;

  @Comment("Auto-fix repeated characters instead of blocking the message")
  public boolean repetitionAutoFix = true;

  @Comment("Message shown to players when their message has too many repeated characters (only if autoFix is false)")
  public String repetitionMessage = "&cPlease don't repeat characters excessively!";

  // ============================================
  // Whitelist Settings
  // ============================================

  @Comment({"", "Enable whitelist system for words/phrases that bypass the filter",
      "Useful for server-specific terms that might trigger false positives"})
  public boolean whitelistEnabled = true;

  @Comment("List of whitelisted words/phrases (case-insensitive)")
  public List<String> whitelistedWords = List.of(
      "example.com",
      "discord.gg/yourserver"
  );
}
