package net.pistonmaster.pistonchat.utils;

import net.pistonmaster.pistonchat.config.PistonChatConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class MessageKeyResolverTest {

  private PistonChatConfig.MessagesConfig messages;

  @BeforeEach
  void setUp() {
    messages = new PistonChatConfig.MessagesConfig();
  }

  @Test
  void getHelpHeader() {
    messages.helpHeader = "Test Help Header";
    assertEquals("Test Help Header", MessageKeyResolver.getMessageByKey(messages, "help-header"));
  }

  @Test
  void getPlayerOnly() {
    messages.playeronly = "Players only message";
    assertEquals("Players only message", MessageKeyResolver.getMessageByKey(messages, "playeronly"));
  }

  @Test
  void getNotOnline() {
    messages.notonline = "Player not online";
    assertEquals("Player not online", MessageKeyResolver.getMessageByKey(messages, "notonline"));
  }

  @Test
  void getNoOneIgnored() {
    messages.nooneignored = "No one ignored";
    assertEquals("No one ignored", MessageKeyResolver.getMessageByKey(messages, "nooneignored"));
  }

  @Test
  void getChatOn() {
    messages.chaton = "Chat enabled";
    assertEquals("Chat enabled", MessageKeyResolver.getMessageByKey(messages, "chaton"));
  }

  @Test
  void getChatOff() {
    messages.chatoff = "Chat disabled";
    assertEquals("Chat disabled", MessageKeyResolver.getMessageByKey(messages, "chatoff"));
  }

  @Test
  void getPmsOn() {
    messages.pmson = "PMs enabled";
    assertEquals("PMs enabled", MessageKeyResolver.getMessageByKey(messages, "pmson"));
  }

  @Test
  void getPmsOff() {
    messages.pmsoff = "PMs disabled";
    assertEquals("PMs disabled", MessageKeyResolver.getMessageByKey(messages, "pmsoff"));
  }

  @Test
  void getPmSelf() {
    messages.pmself = "Cannot PM self";
    assertEquals("Cannot PM self", MessageKeyResolver.getMessageByKey(messages, "pmself"));
  }

  @Test
  void getChatIsOff() {
    messages.chatisoff = "Chat is turned off";
    assertEquals("Chat is turned off", MessageKeyResolver.getMessageByKey(messages, "chatisoff"));
  }

  @Test
  void getSourceIgnored() {
    messages.sourceIgnored = "You are ignored by this person";
    assertEquals("You are ignored by this person", MessageKeyResolver.getMessageByKey(messages, "source-ignored"));
  }

  @Test
  void getTargetIgnored() {
    messages.targetIgnored = "You ignore this person";
    assertEquals("You ignore this person", MessageKeyResolver.getMessageByKey(messages, "target-ignored"));
  }

  @Test
  void getPageNotExists() {
    messages.pageNotExists = "Page does not exist";
    assertEquals("Page does not exist", MessageKeyResolver.getMessageByKey(messages, "page-not-exists"));
  }

  @Test
  void getNotANumber() {
    messages.notANumber = "Not a valid number";
    assertEquals("Not a valid number", MessageKeyResolver.getMessageByKey(messages, "not-a-number"));
  }

  @Test
  void getWhisperingDisabled() {
    messages.whisperingDisabled = "Whispering is disabled";
    assertEquals("Whispering is disabled", MessageKeyResolver.getMessageByKey(messages, "whispering-disabled"));
  }

  @Test
  void getIgnore() {
    messages.ignore = "Now ignoring player";
    assertEquals("Now ignoring player", MessageKeyResolver.getMessageByKey(messages, "ignore"));
  }

  @Test
  void getUnignore() {
    messages.unignore = "No longer ignoring player";
    assertEquals("No longer ignoring player", MessageKeyResolver.getMessageByKey(messages, "unignore"));
  }

  @Test
  void getIgnoreHard() {
    messages.ignorehard = "Permanently ignoring player";
    assertEquals("Permanently ignoring player", MessageKeyResolver.getMessageByKey(messages, "ignorehard"));
  }

  @Test
  void getUnignoreHard() {
    messages.unignorehard = "No longer permanently ignoring";
    assertEquals("No longer permanently ignoring", MessageKeyResolver.getMessageByKey(messages, "unignorehard"));
  }

  @Test
  void getIgnoreListCleared() {
    messages.ignorelistcleared = "Ignore list has been cleared";
    assertEquals("Ignore list has been cleared", MessageKeyResolver.getMessageByKey(messages, "ignorelistcleared"));
  }

  @Test
  void unknownKeyThrowsException() {
    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> MessageKeyResolver.getMessageByKey(messages, "unknown-key")
    );
    assertEquals("Unknown message key: unknown-key", exception.getMessage());
  }

  @Test
  void emptyKeyThrowsException() {
    assertThrows(
        IllegalArgumentException.class,
        () -> MessageKeyResolver.getMessageByKey(messages, "")
    );
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "help-header", "playeronly", "notonline", "nooneignored",
      "chaton", "chatoff", "pmson", "pmsoff", "pmself", "chatisoff",
      "source-ignored", "target-ignored", "page-not-exists", "not-a-number",
      "whispering-disabled", "ignore", "unignore", "ignorehard",
      "unignorehard", "ignorelistcleared"
  })
  void allKeysReturnNonNull(String key) {
    assertNotNull(MessageKeyResolver.getMessageByKey(messages, key));
  }

  @Test
  void defaultMessagesAreUsed() {
    // Test with default MessagesConfig values
    PistonChatConfig.MessagesConfig defaultMessages = new PistonChatConfig.MessagesConfig();

    // These should match the defaults defined in PistonChatConfig.MessagesConfig
    assertEquals("---[<dark_green>PistonChat</dark_green>]---",
        MessageKeyResolver.getMessageByKey(defaultMessages, "help-header"));
    assertEquals("You need to be a player to do this.",
        MessageKeyResolver.getMessageByKey(defaultMessages, "playeronly"));
    assertEquals("This player is not online.",
        MessageKeyResolver.getMessageByKey(defaultMessages, "notonline"));
    assertEquals("Please do not private message yourself.",
        MessageKeyResolver.getMessageByKey(defaultMessages, "pmself"));
  }
}
