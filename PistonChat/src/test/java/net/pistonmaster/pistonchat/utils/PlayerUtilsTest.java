package net.pistonmaster.pistonchat.utils;

import org.bukkit.metadata.MetadataValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlayerUtilsTest {

  @Mock
  private MetadataValue metadataValue1;

  @Mock
  private MetadataValue metadataValue2;

  @Test
  void isVanishedEmptyList() {
    List<MetadataValue> emptyList = new ArrayList<>();
    assertFalse(PlayerUtils.isVanished(emptyList));
  }

  @Test
  void isVanishedSingleTrueValue() {
    when(metadataValue1.asBoolean()).thenReturn(true);

    List<MetadataValue> values = List.of(metadataValue1);
    assertTrue(PlayerUtils.isVanished(values));
  }

  @Test
  void isVanishedSingleFalseValue() {
    when(metadataValue1.asBoolean()).thenReturn(false);

    List<MetadataValue> values = List.of(metadataValue1);
    assertFalse(PlayerUtils.isVanished(values));
  }

  @Test
  void isVanishedMultipleValuesFirstTrue() {
    when(metadataValue1.asBoolean()).thenReturn(true);

    List<MetadataValue> values = List.of(metadataValue1, metadataValue2);
    assertTrue(PlayerUtils.isVanished(values));
  }

  @Test
  void isVanishedMultipleValuesSecondTrue() {
    when(metadataValue1.asBoolean()).thenReturn(false);
    when(metadataValue2.asBoolean()).thenReturn(true);

    List<MetadataValue> values = List.of(metadataValue1, metadataValue2);
    assertTrue(PlayerUtils.isVanished(values));
  }

  @Test
  void isVanishedMultipleValuesBothFalse() {
    when(metadataValue1.asBoolean()).thenReturn(false);
    when(metadataValue2.asBoolean()).thenReturn(false);

    List<MetadataValue> values = List.of(metadataValue1, metadataValue2);
    assertFalse(PlayerUtils.isVanished(values));
  }

  @Test
  void isVanishedMultipleValuesBothTrue() {
    when(metadataValue1.asBoolean()).thenReturn(true);

    List<MetadataValue> values = List.of(metadataValue1, metadataValue2);
    // Should return true after finding the first true value
    assertTrue(PlayerUtils.isVanished(values));
  }
}
