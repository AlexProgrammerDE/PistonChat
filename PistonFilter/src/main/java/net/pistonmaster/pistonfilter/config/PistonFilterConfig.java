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

  @Comment({"", "Staff notification and chat management settings"})
  public StaffConfig staff = new StaffConfig();

  @Comment({"", "Repeat message detection settings"})
  public RepeatConfig repeat = new RepeatConfig();

  @Comment({"", "Auto-mute settings when players violate filters too often (Requires PistonMute)"})
  public AutoMuteConfig autoMute = new AutoMuteConfig();

  @Comment({"", "Content validation settings (word length, numbers, banned text)"})
  public ContentConfig content = new ContentConfig();

  @Comment({"", "Sign text filtering settings"})
  public SignConfig signs = new SignConfig();

  @Comment({"", "Book content filtering settings"})
  public BookConfig books = new BookConfig();

  @Comment({"", "Anvil rename filtering settings"})
  public AnvilConfig anvil = new AnvilConfig();

  @Comment({"", "Command filtering settings"})
  public CommandConfig commands = new CommandConfig();

  @Comment({"", "Unicode and special character filtering settings"})
  public UnicodeConfig unicode = new UnicodeConfig();

  @Comment({"", "Message cooldown settings", "Bypass permission: pistonfilter.bypass.cooldown"})
  public CooldownConfig cooldown = new CooldownConfig();

  @Comment({"", "Anti-caps settings", "Bypass permission: pistonfilter.bypass.caps"})
  public CapsConfig caps = new CapsConfig();

  @Comment({"", "Character repetition settings", "Bypass permission: pistonfilter.bypass.repetition"})
  public RepetitionConfig repetition = new RepetitionConfig();

  @Comment({"", "Whitelist settings for words/phrases that bypass the filter"})
  public WhitelistConfig whitelist = new WhitelistConfig();

  @Configuration
  public static class StaffConfig {
    @Comment("Notify staff members when a message is blocked")
    public boolean notifyOnBlock = true;

    @Comment({"Format for staff notifications when a message is blocked",
        "Placeholders: %player%, %message%, %reason%"})
    public String notificationFormat = "&c[Filter] &e%player% &7was blocked: &f%message% &7(&c%reason%&7)";

    @Comment("Message shown to players when chat is paused")
    public String chatPausedMessage = "&cChat is currently paused. Please wait for staff to resume it.";

    @Comment("Message shown to staff when chat is paused")
    public String chatPausedStaffMessage = "&e[Filter] &aChat has been paused by &f%player%";

    @Comment("Message shown to staff when chat is unpaused")
    public String chatUnpausedStaffMessage = "&e[Filter] &aChat has been resumed by &f%player%";

    @Comment("Number of blank lines to send when clearing chat")
    public int clearChatLines = 100;
  }

  @Configuration
  public static class RepeatConfig {
    @Comment("Enable repeat message detection")
    public boolean enabled = true;

    @Comment("How many seconds after sending a message should the player wait before sending the same message again")
    public int timeSeconds = 60;

    @Comment("How many messages are stored that are not allowed to be repeated?")
    public int stackSize = 20;

    @Comment("How similar from 1-100 are two messages allowed to be before they are considered the same?")
    public int similarityRatio = 90;

    @Comment("Check words of previous messages for repeated word ratio. -1 to disable. Value means how many percent of the messages has repeated words.")
    public int wordRatio = 40;

    @Comment("How many of the last messages of a user to check for number patterns")
    public int numberPatternMessages = 5;

    @Comment("How many messages with numbers are allowed before blocking")
    public int numberPatternAmount = 3;

    @Comment("Store last global messages and compare them for no repeat as well")
    public boolean globalCheck = true;

    @Comment("Store the last x amount of global messages to compare for no repeat")
    public int globalStackSize = 40;
  }

  @Configuration
  public static class AutoMuteConfig {
    @Comment("Should we mute a user if they fail the above checks too often?")
    public boolean enabled = true;

    @Comment("After how many violations of our checks do we mute someone?")
    public int violationThreshold = 3;

    @Comment("How long until violations expire? (seconds)")
    public int violationTimeframe = 60;

    @Comment("For how long do we mute someone? (seconds)")
    public int muteDuration = 60;
  }

  @Configuration
  public static class ContentConfig {
    @Comment("Maximum words containing numbers allowed in a message (e.g. \"2 5f gg 8b\" has 3 words with numbers)")
    public int maxWordsWithNumbers = 5;

    @Comment("Maximum separated number groups allowed in a single word")
    public int maxSeparatedNumbers = 3;

    @Comment("Maximum length of a single word before it's considered spam")
    public int maxWordLength = 20;

    @Comment("Fuzzy match ratio (1-100) for banned text detection. Higher = stricter matching")
    public int bannedTextMatchRatio = 95;

    @Comment("List of banned text patterns (URLs, domains, etc.)")
    public List<String> bannedPatterns = List.of(
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
  }

  @Configuration
  public static class SignConfig {
    @Comment("Enable filtering of text on signs")
    public boolean enabled = true;

    @Comment("Cancel sign placement when filtered content is detected (if false, the offending line is cleared)")
    public boolean cancelOnFilter = false;
  }

  @Configuration
  public static class BookConfig {
    @Comment("Enable filtering of book content (title and pages)")
    public boolean enabled = true;

    @Comment("Cancel book editing when filtered content is detected")
    public boolean cancelOnFilter = true;
  }

  @Configuration
  public static class AnvilConfig {
    @Comment("Enable filtering of anvil rename text")
    public boolean enabled = true;

    @Comment("Cancel anvil rename when filtered content is detected")
    public boolean cancelOnFilter = true;
  }

  @Configuration
  public static class CommandConfig {
    @Comment("Enable filtering of commands that contain chat messages")
    public boolean enabled = true;

    @Comment("List of commands to filter (without the leading slash). Supports formats: 'me', 'msg', 'tell <player>'")
    public List<String> filtered = List.of(
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
    public List<String> withTarget = List.of(
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
  }

  @Configuration
  public static class UnicodeConfig {
    @Comment("Enable Unicode character filtering")
    public boolean enabled = false;

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
    public List<String> allowedRanges = List.of(
        "0080-00FF",  // Latin-1 Supplement (accented chars)
        "0100-017F",  // Latin Extended-A
        "0180-024F"   // Latin Extended-B
    );
  }

  @Configuration
  public static class CooldownConfig {
    @Comment("Enable message cooldown (players must wait between messages)")
    public boolean enabled = true;

    @Comment("Cooldown time in milliseconds between messages (e.g., 3000 = 3 seconds)")
    public long timeMillis = 3000;

    @Comment("Message shown to players when they send messages too fast")
    public String message = "&cPlease wait before sending another message!";
  }

  @Configuration
  public static class CapsConfig {
    @Comment("Enable anti-caps filter to prevent excessive uppercase messages")
    public boolean enabled = true;

    @Comment("Maximum percentage of uppercase characters allowed (0-100)")
    public int maxPercent = 50;

    @Comment("Minimum message length before caps check applies (shorter messages are ignored)")
    public int minLength = 5;

    @Comment("Auto-lowercase messages instead of blocking them")
    public boolean autoLowercase = true;
  }

  @Configuration
  public static class RepetitionConfig {
    @Comment("Enable character repetition filter to prevent messages like 'heeeeeey' or 'noooooo'")
    public boolean enabled = true;

    @Comment("Maximum consecutive repeated characters allowed (e.g., 3 allows 'heey' but blocks 'heeey')")
    public int maxChars = 3;

    @Comment("Auto-fix repeated characters instead of blocking the message")
    public boolean autoFix = true;
  }

  @Configuration
  public static class WhitelistConfig {
    @Comment("Enable whitelist system for words/phrases that bypass the filter")
    public boolean enabled = true;

    @Comment("List of whitelisted words/phrases (case-insensitive)")
    public List<String> words = List.of(
        "example.com",
        "discord.gg/yourserver"
    );
  }
}
