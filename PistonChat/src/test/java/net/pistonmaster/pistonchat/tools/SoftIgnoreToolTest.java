package net.pistonmaster.pistonchat.tools;

import net.pistonmaster.pistonchat.PistonChat;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SoftIgnoreToolTest {

  private static final String METADATA_KEY = "pistonchat_softignore";

  @Mock
  private PistonChat mockPlugin;

  @Mock
  private Player mockIgnoringPlayer;

  @Mock
  private Player mockIgnoredPlayer;

  @Mock
  private MetadataValue mockMetadataValue;

  private SoftIgnoreTool softIgnoreTool;

  private UUID ignoringPlayerUuid;
  private UUID ignoredPlayerUuid;

  @BeforeEach
  void setUp() {
    softIgnoreTool = new SoftIgnoreTool(mockPlugin);
    ignoringPlayerUuid = UUID.randomUUID();
    ignoredPlayerUuid = UUID.randomUUID();

    lenient().when(mockIgnoringPlayer.getUniqueId()).thenReturn(ignoringPlayerUuid);
    lenient().when(mockIgnoredPlayer.getUniqueId()).thenReturn(ignoredPlayerUuid);
  }

  @Test
  void softIgnorePlayerAddsToEmptyList() {
    // Setup: empty ignore list
    when(mockIgnoringPlayer.getMetadata(METADATA_KEY)).thenReturn(Collections.emptyList());

    // Act
    SoftIgnoreTool.SoftReturn result = softIgnoreTool.softIgnorePlayer(mockIgnoringPlayer, mockIgnoredPlayer);

    // Assert
    assertEquals(SoftIgnoreTool.SoftReturn.IGNORE, result);

    // Verify metadata was set
    ArgumentCaptor<FixedMetadataValue> captor = ArgumentCaptor.forClass(FixedMetadataValue.class);
    verify(mockIgnoringPlayer).setMetadata(eq(METADATA_KEY), captor.capture());

    // The captured value should contain the ignored player's UUID
    String metadataJson = captor.getValue().asString();
    assertTrue(metadataJson.contains(ignoredPlayerUuid.toString()));
  }

  @Test
  void softIgnorePlayerRemovesExistingPlayer() {
    // Setup: ignore list already contains the player
    String existingJson = "[\"" + ignoredPlayerUuid.toString() + "\"]";
    when(mockMetadataValue.asString()).thenReturn(existingJson);
    when(mockIgnoringPlayer.getMetadata(METADATA_KEY)).thenReturn(List.of(mockMetadataValue));

    // Act
    SoftIgnoreTool.SoftReturn result = softIgnoreTool.softIgnorePlayer(mockIgnoringPlayer, mockIgnoredPlayer);

    // Assert
    assertEquals(SoftIgnoreTool.SoftReturn.UN_IGNORE, result);

    // Verify metadata was set (with the player removed)
    ArgumentCaptor<FixedMetadataValue> captor = ArgumentCaptor.forClass(FixedMetadataValue.class);
    verify(mockIgnoringPlayer).setMetadata(eq(METADATA_KEY), captor.capture());

    String metadataJson = captor.getValue().asString();
    assertFalse(metadataJson.contains(ignoredPlayerUuid.toString()));
  }

  @Test
  void softIgnorePlayerAddsToExistingList() {
    // Setup: ignore list already contains a different player
    UUID otherPlayerUuid = UUID.randomUUID();
    String existingJson = "[\"" + otherPlayerUuid.toString() + "\"]";
    when(mockMetadataValue.asString()).thenReturn(existingJson);
    when(mockIgnoringPlayer.getMetadata(METADATA_KEY)).thenReturn(List.of(mockMetadataValue));

    // Act
    SoftIgnoreTool.SoftReturn result = softIgnoreTool.softIgnorePlayer(mockIgnoringPlayer, mockIgnoredPlayer);

    // Assert
    assertEquals(SoftIgnoreTool.SoftReturn.IGNORE, result);

    ArgumentCaptor<FixedMetadataValue> captor = ArgumentCaptor.forClass(FixedMetadataValue.class);
    verify(mockIgnoringPlayer).setMetadata(eq(METADATA_KEY), captor.capture());

    String metadataJson = captor.getValue().asString();
    assertTrue(metadataJson.contains(otherPlayerUuid.toString()));
    assertTrue(metadataJson.contains(ignoredPlayerUuid.toString()));
  }

  @Test
  void isSoftIgnoredReturnsTrueWhenIgnored() {
    // Setup: ignore list contains the chatter
    String existingJson = "[\"" + ignoredPlayerUuid.toString() + "\"]";
    when(mockMetadataValue.asString()).thenReturn(existingJson);
    when(mockIgnoringPlayer.getMetadata(METADATA_KEY)).thenReturn(List.of(mockMetadataValue));

    // Act & Assert
    assertTrue(softIgnoreTool.isSoftIgnored(mockIgnoredPlayer, mockIgnoringPlayer));
  }

  @Test
  void isSoftIgnoredReturnsFalseWhenNotIgnored() {
    // Setup: empty ignore list
    when(mockIgnoringPlayer.getMetadata(METADATA_KEY)).thenReturn(Collections.emptyList());

    // Act & Assert
    assertFalse(softIgnoreTool.isSoftIgnored(mockIgnoredPlayer, mockIgnoringPlayer));
  }

  @Test
  void isSoftIgnoredReturnsFalseForDifferentPlayer() {
    // Setup: ignore list contains a different player
    UUID otherPlayerUuid = UUID.randomUUID();
    String existingJson = "[\"" + otherPlayerUuid.toString() + "\"]";
    when(mockMetadataValue.asString()).thenReturn(existingJson);
    when(mockIgnoringPlayer.getMetadata(METADATA_KEY)).thenReturn(List.of(mockMetadataValue));

    // Act & Assert
    assertFalse(softIgnoreTool.isSoftIgnored(mockIgnoredPlayer, mockIgnoringPlayer));
  }

  @Test
  void getStoredListReturnsEmptyForNoMetadata() {
    when(mockIgnoringPlayer.getMetadata(METADATA_KEY)).thenReturn(Collections.emptyList());

    List<UUID> result = softIgnoreTool.getStoredList(mockIgnoringPlayer);

    assertTrue(result.isEmpty());
  }

  @Test
  void getStoredListReturnsUuids() {
    UUID uuid1 = UUID.randomUUID();
    UUID uuid2 = UUID.randomUUID();
    String json = "[\"" + uuid1.toString() + "\",\"" + uuid2.toString() + "\"]";
    when(mockMetadataValue.asString()).thenReturn(json);
    when(mockIgnoringPlayer.getMetadata(METADATA_KEY)).thenReturn(List.of(mockMetadataValue));

    List<UUID> result = softIgnoreTool.getStoredList(mockIgnoringPlayer);

    assertEquals(2, result.size());
    assertTrue(result.contains(uuid1));
    assertTrue(result.contains(uuid2));
  }

  @Test
  void clearIgnoredPlayersRemovesMetadata() {
    softIgnoreTool.clearIgnoredPlayers(mockIgnoringPlayer);

    verify(mockIgnoringPlayer).removeMetadata(METADATA_KEY, mockPlugin);
  }

  @Test
  void softReturnEnumValues() {
    // Verify enum values exist
    assertEquals(2, SoftIgnoreTool.SoftReturn.values().length);
    assertEquals(SoftIgnoreTool.SoftReturn.IGNORE, SoftIgnoreTool.SoftReturn.valueOf("IGNORE"));
    assertEquals(SoftIgnoreTool.SoftReturn.UN_IGNORE, SoftIgnoreTool.SoftReturn.valueOf("UN_IGNORE"));
  }
}
