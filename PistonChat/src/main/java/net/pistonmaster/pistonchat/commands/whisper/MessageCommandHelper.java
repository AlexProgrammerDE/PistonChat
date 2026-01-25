package net.pistonmaster.pistonchat.commands.whisper;

import net.pistonmaster.pistonchat.PistonChat;
import net.pistonmaster.pistonchat.config.PistonChatConfig;
import org.bukkit.command.CommandSender;

public class MessageCommandHelper {
  public static void sendWhisper(PistonChat plugin, CommandSender sender, CommandSender receiver, String message) {
    PistonChatConfig config = plugin.getPluginConfig();
    if (plugin.getIgnoreTool().isIgnored(sender, receiver)) {
      if (config.onlyHidePms) {
        plugin.getCommonTool().sendSender(sender, message, receiver);
      } else {
        plugin.getCommonTool().sendLanguageMessage(sender, "source-ignored");
      }
      return;
    }

    if (!config.allowPmIgnored && plugin.getIgnoreTool().isIgnored(receiver, sender)) {
      plugin.getCommonTool().sendLanguageMessage(sender, "target-ignored");
      return;
    }

    plugin.getCommonTool().sendWhisperTo(sender, message, receiver);
  }

  private MessageCommandHelper() {
  }
}
