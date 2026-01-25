package net.pistonmaster.pistonchat.tools;

import net.pistonmaster.pistonchat.PistonChat;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IgnoreToolTest {

  @Mock
  private PistonChat mockPlugin;

  @Mock
  private SoftIgnoreTool mockSoftIgnoreTool;

  @Mock
  private HardIgnoreTool mockHardIgnoreTool;

  @Mock
  private Player mockChatter;

  @Mock
  private Player mockReceiver;

  @Mock
  private ConsoleCommandSender mockConsoleSender;

  private IgnoreTool ignoreTool;

  @BeforeEach
  void setUp() {
    lenient().when(mockPlugin.getSoftignoreTool()).thenReturn(mockSoftIgnoreTool);
    lenient().when(mockPlugin.getHardIgnoreTool()).thenReturn(mockHardIgnoreTool);

    ignoreTool = new IgnoreTool(mockPlugin);
  }

  @Test
  void isIgnoredReturnsTrueWhenSoftIgnored() {
    when(mockSoftIgnoreTool.isSoftIgnored(mockChatter, mockReceiver)).thenReturn(true);

    assertTrue(ignoreTool.isIgnored(mockChatter, mockReceiver));

    // Hard ignore should not be checked if soft ignored
    verify(mockHardIgnoreTool, never()).isHardIgnored(any(), any());
  }

  @Test
  void isIgnoredReturnsTrueWhenHardIgnored() {
    when(mockSoftIgnoreTool.isSoftIgnored(mockChatter, mockReceiver)).thenReturn(false);
    when(mockHardIgnoreTool.isHardIgnored(mockChatter, mockReceiver)).thenReturn(true);

    assertTrue(ignoreTool.isIgnored(mockChatter, mockReceiver));
  }

  @Test
  void isIgnoredReturnsFalseWhenNotIgnored() {
    when(mockSoftIgnoreTool.isSoftIgnored(mockChatter, mockReceiver)).thenReturn(false);
    when(mockHardIgnoreTool.isHardIgnored(mockChatter, mockReceiver)).thenReturn(false);

    assertFalse(ignoreTool.isIgnored(mockChatter, mockReceiver));
  }

  @Test
  void isIgnoredReturnsFalseForNonPlayerReceiver() {
    // When receiver is not a player (e.g., console), always return false
    assertFalse(ignoreTool.isIgnored(mockChatter, mockConsoleSender));

    // Should not check soft or hard ignore
    verify(mockSoftIgnoreTool, never()).isSoftIgnored(any(), any());
    verify(mockHardIgnoreTool, never()).isHardIgnored(any(), any());
  }

  @Test
  void getIgnoredPlayersReturnsEmptyMapWhenNoPlayersIgnored() {
    when(mockSoftIgnoreTool.getStoredList(mockReceiver)).thenReturn(Collections.emptyList());
    when(mockHardIgnoreTool.getStoredList(mockReceiver)).thenReturn(Collections.emptyList());

    Map<OfflinePlayer, IgnoreTool.IgnoreType> result = ignoreTool.getIgnoredPlayers(mockReceiver);

    assertTrue(result.isEmpty());
  }

  @Test
  void clearIgnoredPlayersClearsBothSoftAndHard() {
    ignoreTool.clearIgnoredPlayers(mockReceiver);

    verify(mockSoftIgnoreTool).clearIgnoredPlayers(mockReceiver);
    verify(mockHardIgnoreTool).clearIgnoredPlayers(mockReceiver);
  }

  @Test
  void ignoreTypeEnumValues() {
    assertEquals(2, IgnoreTool.IgnoreType.values().length);
    assertEquals(IgnoreTool.IgnoreType.SOFT, IgnoreTool.IgnoreType.valueOf("SOFT"));
    assertEquals(IgnoreTool.IgnoreType.HARD, IgnoreTool.IgnoreType.valueOf("HARD"));
  }

  @Test
  void convertIgnoredPlayerHandlesEmptyList() {
    List<UUID> emptyList = Collections.emptyList();

    List<OfflinePlayer> result = ignoreTool.convertIgnoredPlayer(emptyList);

    assertTrue(result.isEmpty());
  }

  @Test
  void isIgnoredChecksCorrectOrder() {
    // Setup: both return false
    when(mockSoftIgnoreTool.isSoftIgnored(mockChatter, mockReceiver)).thenReturn(false);
    when(mockHardIgnoreTool.isHardIgnored(mockChatter, mockReceiver)).thenReturn(false);

    ignoreTool.isIgnored(mockChatter, mockReceiver);

    // Verify order: soft ignore is checked first
    var inOrder = inOrder(mockSoftIgnoreTool, mockHardIgnoreTool);
    inOrder.verify(mockSoftIgnoreTool).isSoftIgnored(mockChatter, mockReceiver);
    inOrder.verify(mockHardIgnoreTool).isHardIgnored(mockChatter, mockReceiver);
  }

  @Test
  void isIgnoredShortCircuitsOnSoftIgnore() {
    // If soft ignored, hard ignore should not be checked
    when(mockSoftIgnoreTool.isSoftIgnored(mockChatter, mockReceiver)).thenReturn(true);

    boolean result = ignoreTool.isIgnored(mockChatter, mockReceiver);

    assertTrue(result);
    verify(mockSoftIgnoreTool).isSoftIgnored(mockChatter, mockReceiver);
    verify(mockHardIgnoreTool, never()).isHardIgnored(any(), any());
  }
}
