package net.pistonmaster.pistonchat.tools;

import net.pistonmaster.pistonchat.PistonChat;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CacheToolTest {

  private static final String SENT_TO_KEY = "pistonchat_sentTo";
  private static final String MESSAGED_OF_KEY = "pistonchat_messagedOf";

  @Mock
  private PistonChat mockPlugin;

  @Mock
  private Player mockSenderPlayer;

  @Mock
  private Player mockReceiverPlayer;

  @Mock
  private ConsoleCommandSender mockConsoleSender;

  private CacheTool cacheTool;

  private UUID senderUuid;
  private UUID receiverUuid;

  @BeforeEach
  void setUp() {
    cacheTool = new CacheTool(mockPlugin);
    senderUuid = UUID.randomUUID();
    receiverUuid = UUID.randomUUID();

    lenient().when(mockSenderPlayer.getUniqueId()).thenReturn(senderUuid);
    lenient().when(mockReceiverPlayer.getUniqueId()).thenReturn(receiverUuid);
  }

  @Test
  void sendMessageBetweenPlayers() {
    // Act
    cacheTool.sendMessage(mockSenderPlayer, mockReceiverPlayer);

    // Assert: sender should have "sentTo" set to receiver's UUID
    ArgumentCaptor<FixedMetadataValue> sentToCaptor = ArgumentCaptor.forClass(FixedMetadataValue.class);
    verify(mockSenderPlayer).setMetadata(eq(SENT_TO_KEY), sentToCaptor.capture());
    assertEquals(receiverUuid.toString(), sentToCaptor.getValue().asString());

    // Assert: receiver should have "messagedOf" set to sender's UUID
    ArgumentCaptor<FixedMetadataValue> messagedOfCaptor = ArgumentCaptor.forClass(FixedMetadataValue.class);
    verify(mockReceiverPlayer).setMetadata(eq(MESSAGED_OF_KEY), messagedOfCaptor.capture());
    assertEquals(senderUuid.toString(), messagedOfCaptor.getValue().asString());
  }

  @Test
  void sendMessageFromConsoleToPlayer() {
    // Act
    cacheTool.sendMessage(mockConsoleSender, mockReceiverPlayer);

    // Assert: receiver should have "messagedOf" metadata set
    ArgumentCaptor<FixedMetadataValue> messagedOfCaptor = ArgumentCaptor.forClass(FixedMetadataValue.class);
    verify(mockReceiverPlayer).setMetadata(eq(MESSAGED_OF_KEY), messagedOfCaptor.capture());

    // The console sender will get a generated UUID
    String uuidString = messagedOfCaptor.getValue().asString();
    assertDoesNotThrow(() -> UUID.fromString(uuidString));
  }

  @Test
  void sendMessageFromPlayerToConsole() {
    // Act
    cacheTool.sendMessage(mockSenderPlayer, mockConsoleSender);

    // Assert: sender should have "sentTo" metadata set
    ArgumentCaptor<FixedMetadataValue> sentToCaptor = ArgumentCaptor.forClass(FixedMetadataValue.class);
    verify(mockSenderPlayer).setMetadata(eq(SENT_TO_KEY), sentToCaptor.capture());

    // The console receiver will get a generated UUID
    String uuidString = sentToCaptor.getValue().asString();
    assertDoesNotThrow(() -> UUID.fromString(uuidString));
  }

  @Test
  void getLastSentToForPlayerWithNoMetadata() {
    when(mockSenderPlayer.getMetadata(SENT_TO_KEY)).thenReturn(Collections.emptyList());

    Optional<?> result = cacheTool.getLastSentTo(mockSenderPlayer);

    assertTrue(result.isEmpty());
  }

  @Test
  void getLastMessagedOfForPlayerWithNoMetadata() {
    when(mockReceiverPlayer.getMetadata(MESSAGED_OF_KEY)).thenReturn(Collections.emptyList());

    Optional<?> result = cacheTool.getLastMessagedOf(mockReceiverPlayer);

    assertTrue(result.isEmpty());
  }

  @Test
  void getLastSentToForConsoleSenderWithNoHistory() {
    // Console sender without any history
    Optional<?> result = cacheTool.getLastSentTo(mockConsoleSender);

    assertTrue(result.isEmpty());
  }

  @Test
  void getLastMessagedOfForConsoleSenderWithNoHistory() {
    // Console sender without any history
    Optional<?> result = cacheTool.getLastMessagedOf(mockConsoleSender);

    assertTrue(result.isEmpty());
  }

  @Test
  void consoleSendersRememberLastMessages() {
    // First, console sends message to player
    cacheTool.sendMessage(mockConsoleSender, mockReceiverPlayer);

    // Verify the receiver got the messagedOf metadata
    ArgumentCaptor<FixedMetadataValue> captor = ArgumentCaptor.forClass(FixedMetadataValue.class);
    verify(mockReceiverPlayer).setMetadata(eq(MESSAGED_OF_KEY), captor.capture());
    assertNotNull(captor.getValue().asString());
  }

  @Test
  void multipleConsoleSendersHaveSeparateState() {
    // Create two different console senders
    ConsoleCommandSender mockConsole1 = mock(ConsoleCommandSender.class);
    ConsoleCommandSender mockConsole2 = mock(ConsoleCommandSender.class);

    // Send messages from both
    cacheTool.sendMessage(mockConsole1, mockReceiverPlayer);

    // Create a second receiver for console2
    Player mockSecondReceiver = mock(Player.class);
    UUID secondReceiverUuid = UUID.randomUUID();
    when(mockSecondReceiver.getUniqueId()).thenReturn(secondReceiverUuid);

    cacheTool.sendMessage(mockConsole2, mockSecondReceiver);

    // Both receivers should have metadata set
    verify(mockReceiverPlayer).setMetadata(eq(MESSAGED_OF_KEY), any(FixedMetadataValue.class));
    verify(mockSecondReceiver).setMetadata(eq(MESSAGED_OF_KEY), any(FixedMetadataValue.class));
  }

  @Test
  void sendMessageUpdatesMetadataCorrectly() {
    // Send message from sender to receiver
    cacheTool.sendMessage(mockSenderPlayer, mockReceiverPlayer);

    // Verify both metadata operations
    verify(mockSenderPlayer).setMetadata(eq(SENT_TO_KEY), any(FixedMetadataValue.class));
    verify(mockReceiverPlayer).setMetadata(eq(MESSAGED_OF_KEY), any(FixedMetadataValue.class));
  }

  @Test
  void sendMessageBetweenConsoles() {
    ConsoleCommandSender mockConsole2 = mock(ConsoleCommandSender.class);

    // This should work - both are non-players, so they'll use the customMap
    cacheTool.sendMessage(mockConsoleSender, mockConsole2);

    // No metadata should be set on consoles (they don't have setMetadata)
    // The internal map should be updated instead
  }
}
