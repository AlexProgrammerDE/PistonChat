package net.pistonmaster.pistonchat.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StringUtilsTest {

  @Test
  void mergeArgsFromStart() {
    String[] args = {"hello", "world", "test"};
    assertEquals("hello world test", StringUtils.mergeArgs(args, 0));
  }

  @Test
  void mergeArgsFromMiddle() {
    String[] args = {"hello", "world", "test"};
    assertEquals("world test", StringUtils.mergeArgs(args, 1));
  }

  @Test
  void mergeArgsFromLastElement() {
    String[] args = {"hello", "world", "test"};
    assertEquals("test", StringUtils.mergeArgs(args, 2));
  }

  @Test
  void mergeArgsEmptyResult() {
    String[] args = {"hello", "world", "test"};
    assertEquals("", StringUtils.mergeArgs(args, 3));
  }

  @Test
  void mergeArgsSingleElement() {
    String[] args = {"single"};
    assertEquals("single", StringUtils.mergeArgs(args, 0));
  }

  @Test
  void mergeArgsEmptyArray() {
    String[] args = {};
    assertEquals("", StringUtils.mergeArgs(args, 0));
  }

  @Test
  void mergeArgsPreservesSpacesInElements() {
    // Note: args typically come from command splitting, so elements don't have spaces
    // but this tests that join works correctly
    String[] args = {"hello", "beautiful", "world"};
    assertEquals("beautiful world", StringUtils.mergeArgs(args, 1));
  }

  @Test
  void mergeArgsWithSpecialCharacters() {
    String[] args = {"player", "Hello!", "How", "are", "you?"};
    assertEquals("Hello! How are you?", StringUtils.mergeArgs(args, 1));
  }
}
