package net.pistonmaster.pistonfilter.config;

import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;

import java.util.List;

@Configuration
public class PistonFilterConfig {
  public boolean verbose = true;

  public boolean messageSender = true;

  public boolean noRepeat = true;

  @Comment("How many times after sending a message should the player wait before sending the same message again")
  public int noRepeatTime = 60;

  @Comment("How many messages are stored that are not allowed to be repeated?")
  public int noRepeatStackSize = 20;

  @Comment("How similar from 1-100 are two messages allowed to be before they are considered the same?")
  public int noRepeatSimilarRatio = 90;

  @Comment("Check words of previous messages for repeated word ratio. -1 to disable. Value means how many percent of the messages has repeated words.")
  public int noRepeatWordRatio = 40;

  @Comment("How many of the last message of a user are allowed to have a number inside them?")
  public int noRepeatNumberMessages = 5;

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

  @Comment("Example: \"2 5f gg 8b 33 hj 6zb 6573\" has 8 words and 6 words with numbers")
  public int maxWordsWithNumbers = 5;

  public int maxSeparatedNumbers = 3;

  public int maxWordLength = 20;

  public int bannedTextPartialRatio = 95;

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
}
