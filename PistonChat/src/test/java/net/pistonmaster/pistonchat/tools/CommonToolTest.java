package net.pistonmaster.pistonchat.tools;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.pistonmaster.pistonchat.PistonChat;
import net.pistonmaster.pistonchat.config.PistonChatConfig;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CommonTool.
 * Note: Tests for sendWhisperTo and related methods require full integration testing
 * due to MiniPlaceholders API dependencies. This test class focuses on pure logic tests.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CommonToolTest {

  @Mock
  private PistonChat mockPlugin;

  @Mock
  private Player mockPlayer;

  private CommonTool commonTool;
  private PistonChatConfig config;

  @BeforeEach
  void setUp() {
    commonTool = new CommonTool(mockPlugin);
    config = new PistonChatConfig();

    lenient().when(mockPlugin.getPluginConfig()).thenReturn(config);
  }

  // Tests for static mergeArgs method
  @Test
  void mergeArgsFromStart() {
    String[] args = {"hello", "world", "test"};
    assertEquals("hello world test", CommonTool.mergeArgs(args, 0));
  }

  @Test
  void mergeArgsFromMiddle() {
    String[] args = {"player", "Hello", "there"};
    assertEquals("Hello there", CommonTool.mergeArgs(args, 1));
  }

  @Test
  void mergeArgsFromEnd() {
    String[] args = {"player", "Hello"};
    assertEquals("Hello", CommonTool.mergeArgs(args, 1));
  }

  @Test
  void mergeArgsEmpty() {
    String[] args = {"player"};
    assertEquals("", CommonTool.mergeArgs(args, 1));
  }

  @Test
  void mergeArgsEmptyArray() {
    String[] args = {};
    assertEquals("", CommonTool.mergeArgs(args, 0));
  }

  @Test
  void mergeArgsWithSpecialCharacters() {
    String[] args = {"cmd", "Hello!", "How", "are", "you?"};
    assertEquals("Hello! How are you?", CommonTool.mergeArgs(args, 1));
  }

  // Tests for getChatColorFor method
  @Test
  void getChatColorForWithMatchingPrefix() {
    // Setup prefix config
    Map<String, PistonChatConfig.PrefixConfig> prefixes = new LinkedHashMap<>();
    PistonChatConfig.PrefixConfig greenConfig = new PistonChatConfig.PrefixConfig(">", "GREEN");
    prefixes.put("green", greenConfig);
    config.prefixes = prefixes;

    when(mockPlayer.hasPermission("pistonchat.prefix.green")).thenReturn(true);

    Optional<TextColor> result = commonTool.getChatColorFor(">hello world", mockPlayer);

    assertTrue(result.isPresent());
    assertEquals(NamedTextColor.GREEN, result.get());
  }

  @Test
  void getChatColorForWithNoMatchingPrefix() {
    Map<String, PistonChatConfig.PrefixConfig> prefixes = new LinkedHashMap<>();
    PistonChatConfig.PrefixConfig greenConfig = new PistonChatConfig.PrefixConfig(">", "GREEN");
    prefixes.put("green", greenConfig);
    config.prefixes = prefixes;

    // Player has permission, but message doesn't start with the prefix
    Optional<TextColor> result = commonTool.getChatColorFor("hello world", mockPlayer);

    assertTrue(result.isEmpty());
  }

  @Test
  void getChatColorForWithDisabledPrefix() {
    Map<String, PistonChatConfig.PrefixConfig> prefixes = new LinkedHashMap<>();
    PistonChatConfig.PrefixConfig disabledConfig = new PistonChatConfig.PrefixConfig("/", "GREEN");
    prefixes.put("disabled", disabledConfig);
    config.prefixes = prefixes;

    // "/" is the special disabled prefix marker
    Optional<TextColor> result = commonTool.getChatColorFor("/hello world", mockPlayer);

    assertTrue(result.isEmpty());
  }

  @Test
  void getChatColorForWithoutPermission() {
    Map<String, PistonChatConfig.PrefixConfig> prefixes = new LinkedHashMap<>();
    PistonChatConfig.PrefixConfig greenConfig = new PistonChatConfig.PrefixConfig(">", "GREEN");
    prefixes.put("green", greenConfig);
    config.prefixes = prefixes;

    when(mockPlayer.hasPermission("pistonchat.prefix.green")).thenReturn(false);

    Optional<TextColor> result = commonTool.getChatColorFor(">hello world", mockPlayer);

    assertTrue(result.isEmpty());
  }

  @Test
  void getChatColorForCaseInsensitivePrefix() {
    Map<String, PistonChatConfig.PrefixConfig> prefixes = new LinkedHashMap<>();
    PistonChatConfig.PrefixConfig prefixConfig = new PistonChatConfig.PrefixConfig("test", "RED");
    prefixes.put("test", prefixConfig);
    config.prefixes = prefixes;

    when(mockPlayer.hasPermission("pistonchat.prefix.test")).thenReturn(true);

    // Message starts with uppercase TEST, prefix is lowercase test
    Optional<TextColor> result = commonTool.getChatColorFor("TEST hello", mockPlayer);

    assertTrue(result.isPresent());
    assertEquals(NamedTextColor.RED, result.get());
  }

  @Test
  void getChatColorForMultiplePrefixes() {
    Map<String, PistonChatConfig.PrefixConfig> prefixes = new LinkedHashMap<>();
    prefixes.put("green", new PistonChatConfig.PrefixConfig(">", "GREEN"));
    prefixes.put("red", new PistonChatConfig.PrefixConfig("!", "RED"));
    config.prefixes = prefixes;

    when(mockPlayer.hasPermission("pistonchat.prefix.green")).thenReturn(true);
    when(mockPlayer.hasPermission("pistonchat.prefix.red")).thenReturn(true);

    // Should match first in order (green)
    Optional<TextColor> result = commonTool.getChatColorFor(">hello", mockPlayer);
    assertTrue(result.isPresent());
    assertEquals(NamedTextColor.GREEN, result.get());

    // Should match red prefix
    Optional<TextColor> result2 = commonTool.getChatColorFor("!hello", mockPlayer);
    assertTrue(result2.isPresent());
    assertEquals(NamedTextColor.RED, result2.get());
  }

  @Test
  void getChatColorForEmptyPrefixes() {
    config.prefixes = new LinkedHashMap<>();

    Optional<TextColor> result = commonTool.getChatColorFor(">hello", mockPlayer);

    assertTrue(result.isEmpty());
  }

  @Test
  void getChatColorForFirstMatchingPrefixWins() {
    // When multiple prefixes could match, the first one in iteration order wins
    Map<String, PistonChatConfig.PrefixConfig> prefixes = new LinkedHashMap<>();
    prefixes.put("blue", new PistonChatConfig.PrefixConfig(">>", "BLUE"));
    prefixes.put("green", new PistonChatConfig.PrefixConfig(">", "GREEN"));
    config.prefixes = prefixes;

    when(mockPlayer.hasPermission("pistonchat.prefix.blue")).thenReturn(true);
    when(mockPlayer.hasPermission("pistonchat.prefix.green")).thenReturn(true);

    // ">>" should match blue first
    Optional<TextColor> result = commonTool.getChatColorFor(">>hello", mockPlayer);
    assertTrue(result.isPresent());
    assertEquals(NamedTextColor.BLUE, result.get());
  }

  @Test
  void getChatColorForPrefixAtStartOnly() {
    Map<String, PistonChatConfig.PrefixConfig> prefixes = new LinkedHashMap<>();
    prefixes.put("green", new PistonChatConfig.PrefixConfig(">", "GREEN"));
    config.prefixes = prefixes;

    when(mockPlayer.hasPermission("pistonchat.prefix.green")).thenReturn(true);

    // Prefix not at start of message
    Optional<TextColor> result = commonTool.getChatColorFor("hello > world", mockPlayer);

    assertTrue(result.isEmpty());
  }

  @Test
  void getChatColorForVariousColors() {
    Map<String, PistonChatConfig.PrefixConfig> prefixes = new LinkedHashMap<>();
    prefixes.put("aqua", new PistonChatConfig.PrefixConfig("~", "AQUA"));
    config.prefixes = prefixes;

    when(mockPlayer.hasPermission("pistonchat.prefix.aqua")).thenReturn(true);

    Optional<TextColor> result = commonTool.getChatColorFor("~hello", mockPlayer);

    assertTrue(result.isPresent());
    assertEquals(NamedTextColor.AQUA, result.get());
  }
}
