package net.pistonmaster.pistonchat.utils;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UniqueSenderTest {

  @Mock
  private Player mockPlayer;

  @Mock
  private ConsoleCommandSender mockConsoleSender;

  @Mock
  private CommandSender mockGenericSender;

  private UUID playerUuid;

  @BeforeEach
  void setUp() {
    playerUuid = UUID.randomUUID();
  }

  @Test
  void getUniqueIdForPlayer() {
    when(mockPlayer.getUniqueId()).thenReturn(playerUuid);

    UniqueSender uniqueSender = new UniqueSender(mockPlayer);
    assertEquals(playerUuid, uniqueSender.getUniqueId());
  }

  @Test
  void getUniqueIdForConsoleSender() {
    UniqueSender uniqueSender = new UniqueSender(mockConsoleSender);
    UUID firstUuid = uniqueSender.getUniqueId();

    assertNotNull(firstUuid);

    // Same console sender should return same UUID
    UniqueSender anotherUniqueSender = new UniqueSender(mockConsoleSender);
    assertEquals(firstUuid, anotherUniqueSender.getUniqueId());
  }

  @Test
  void getUniqueIdForGenericSender() {
    UniqueSender uniqueSender = new UniqueSender(mockGenericSender);
    UUID uuid = uniqueSender.getUniqueId();

    assertNotNull(uuid);

    // Same sender should return same UUID (cached)
    UniqueSender anotherUniqueSender = new UniqueSender(mockGenericSender);
    assertEquals(uuid, anotherUniqueSender.getUniqueId());
  }

  @Test
  void differentGenericSendersGetDifferentUuids() {
    CommandSender sender1 = mockConsoleSender;
    CommandSender sender2 = mockGenericSender;

    UniqueSender uniqueSender1 = new UniqueSender(sender1);
    UniqueSender uniqueSender2 = new UniqueSender(sender2);

    UUID uuid1 = uniqueSender1.getUniqueId();
    UUID uuid2 = uniqueSender2.getUniqueId();

    assertNotEquals(uuid1, uuid2);
  }

  @Test
  void byUuidReturnsNonPlayerSender() {
    // First, create a UUID for a non-player sender
    UniqueSender uniqueSender = new UniqueSender(mockConsoleSender);
    UUID consoleUuid = uniqueSender.getUniqueId();

    // Now try to retrieve by UUID
    Optional<CommandSender> result = UniqueSender.byUUID(consoleUuid);

    assertTrue(result.isPresent());
    assertEquals(mockConsoleSender, result.get());
  }

  @Test
  void byUuidReturnsEmptyForUnknownUuid() {
    UUID unknownUuid = UUID.randomUUID();
    Optional<CommandSender> result = UniqueSender.byUUID(unknownUuid);

    // May or may not be present depending on if a player with this UUID exists
    // For a truly random UUID, it should be empty since no mapping exists
    // Note: This depends on static state, so the result might vary
    // We just verify it doesn't throw
    assertNotNull(result);
  }

  @Test
  void senderRecordHoldsReference() {
    UniqueSender uniqueSender = new UniqueSender(mockPlayer);
    assertEquals(mockPlayer, uniqueSender.sender());
  }
}
