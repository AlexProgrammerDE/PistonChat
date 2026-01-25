package net.pistonmaster.pistonchat.commands.whisper;

import net.pistonmaster.pistonchat.PistonChat;
import net.pistonmaster.pistonchat.config.PistonChatConfig;
import net.pistonmaster.pistonchat.tools.CommonTool;
import net.pistonmaster.pistonchat.tools.IgnoreTool;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MessageCommandHelperTest {

  @Mock
  private PistonChat mockPlugin;

  @Mock
  private IgnoreTool mockIgnoreTool;

  @Mock
  private CommonTool mockCommonTool;

  @Mock
  private Player mockSender;

  @Mock
  private Player mockReceiver;

  private PistonChatConfig config;

  @BeforeEach
  void setUp() {
    config = new PistonChatConfig();

    when(mockPlugin.getPluginConfig()).thenReturn(config);
    when(mockPlugin.getIgnoreTool()).thenReturn(mockIgnoreTool);
    when(mockPlugin.getCommonTool()).thenReturn(mockCommonTool);
  }

  @Test
  void sendWhisperWhenSenderIsIgnoredAndOnlyHidePms() {
    // Sender is ignored by receiver
    when(mockIgnoreTool.isIgnored(mockSender, mockReceiver)).thenReturn(true);
    config.onlyHidePms = true;

    MessageCommandHelper.sendWhisper(mockPlugin, mockSender, mockReceiver, "hello");

    // Only sender should see the message
    verify(mockCommonTool).sendSender(mockSender, "hello", mockReceiver);
    // sendWhisperTo should NOT be called
    verify(mockCommonTool, never()).sendWhisperTo(any(), any(), any());
  }

  @Test
  void sendWhisperWhenSenderIsIgnoredAndNotOnlyHidePms() {
    when(mockIgnoreTool.isIgnored(mockSender, mockReceiver)).thenReturn(true);
    config.onlyHidePms = false;

    MessageCommandHelper.sendWhisper(mockPlugin, mockSender, mockReceiver, "hello");

    // Sender should get "source-ignored" message
    verify(mockCommonTool).sendLanguageMessage(mockSender, "source-ignored");
    verify(mockCommonTool, never()).sendWhisperTo(any(), any(), any());
    verify(mockCommonTool, never()).sendSender(any(), any(), any());
  }

  @Test
  void sendWhisperWhenReceiverIsIgnoredBySenderAndNotAllowed() {
    when(mockIgnoreTool.isIgnored(mockSender, mockReceiver)).thenReturn(false);
    when(mockIgnoreTool.isIgnored(mockReceiver, mockSender)).thenReturn(true);
    config.allowPmIgnored = false;

    MessageCommandHelper.sendWhisper(mockPlugin, mockSender, mockReceiver, "hello");

    // Sender should get "target-ignored" message
    verify(mockCommonTool).sendLanguageMessage(mockSender, "target-ignored");
    verify(mockCommonTool, never()).sendWhisperTo(any(), any(), any());
  }

  @Test
  void sendWhisperWhenReceiverIsIgnoredBySenderButAllowed() {
    when(mockIgnoreTool.isIgnored(mockSender, mockReceiver)).thenReturn(false);
    config.allowPmIgnored = true;

    MessageCommandHelper.sendWhisper(mockPlugin, mockSender, mockReceiver, "hello");

    // Should proceed with whisper (receiver's ignore status not checked when allowPmIgnored=true)
    verify(mockCommonTool).sendWhisperTo(mockSender, "hello", mockReceiver);
  }

  @Test
  void sendWhisperSuccessful() {
    when(mockIgnoreTool.isIgnored(mockSender, mockReceiver)).thenReturn(false);
    when(mockIgnoreTool.isIgnored(mockReceiver, mockSender)).thenReturn(false);
    config.allowPmIgnored = false;

    MessageCommandHelper.sendWhisper(mockPlugin, mockSender, mockReceiver, "hello world");

    verify(mockCommonTool).sendWhisperTo(mockSender, "hello world", mockReceiver);
  }

  @Test
  void sendWhisperNoIgnoreCheckedWhenSenderNotIgnored() {
    when(mockIgnoreTool.isIgnored(mockSender, mockReceiver)).thenReturn(false);
    config.allowPmIgnored = true;

    MessageCommandHelper.sendWhisper(mockPlugin, mockSender, mockReceiver, "hello");

    // isIgnored should only be called once (for sender check)
    // when allowPmIgnored is true, the second check is skipped
    verify(mockIgnoreTool).isIgnored(mockSender, mockReceiver);
    verify(mockCommonTool).sendWhisperTo(mockSender, "hello", mockReceiver);
  }

  @Test
  void sendWhisperChecksReceiverIgnoreStatusWhenNotAllowed() {
    when(mockIgnoreTool.isIgnored(mockSender, mockReceiver)).thenReturn(false);
    when(mockIgnoreTool.isIgnored(mockReceiver, mockSender)).thenReturn(false);
    config.allowPmIgnored = false;

    MessageCommandHelper.sendWhisper(mockPlugin, mockSender, mockReceiver, "hello");

    // Both ignore checks should be performed
    verify(mockIgnoreTool).isIgnored(mockSender, mockReceiver);
    verify(mockIgnoreTool).isIgnored(mockReceiver, mockSender);
    verify(mockCommonTool).sendWhisperTo(mockSender, "hello", mockReceiver);
  }

  @Test
  void sendWhisperWithEmptyMessage() {
    when(mockIgnoreTool.isIgnored(mockSender, mockReceiver)).thenReturn(false);
    config.allowPmIgnored = true;

    MessageCommandHelper.sendWhisper(mockPlugin, mockSender, mockReceiver, "");

    verify(mockCommonTool).sendWhisperTo(mockSender, "", mockReceiver);
  }

  @Test
  void sendWhisperWithSpecialCharacters() {
    when(mockIgnoreTool.isIgnored(mockSender, mockReceiver)).thenReturn(false);
    config.allowPmIgnored = true;

    String message = "Hello! How are you? <test> & stuff";
    MessageCommandHelper.sendWhisper(mockPlugin, mockSender, mockReceiver, message);

    verify(mockCommonTool).sendWhisperTo(mockSender, message, mockReceiver);
  }
}
