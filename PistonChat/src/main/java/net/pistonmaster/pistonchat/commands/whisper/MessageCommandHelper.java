package net.pistonmaster.pistonchat.commands.whisper;

import net.pistonmaster.pistonchat.PistonChat;
import org.bukkit.command.CommandSender;

public class MessageCommandHelper {
  public static void sendWhisper(PistonChat plugin, CommandSender sender, CommandSender receiver, String message) {
    if (plugin.getIgnoreTool().isIgnored(sender, receiver)) {
      if (plugin.getConfig().getBoolean("only-hide-pms")) {
        plugin.getCommonTool().sendSender(sender, message, receiver);
      } else {
        plugin.getCommonTool().sendLanguageMessage(sender, "source-ignored");
      }
      return;
    }

    if (!plugin.getConfig().getBoolean("allow-pm-ignored") && plugin.getIgnoreTool().isIgnored(receiver, sender)) {
      plugin.getCommonTool().sendLanguageMessage(sender, "target-ignored");
      return;
    }

    plugin.getCommonTool().sendWhisperTo(sender, message, receiver);
  }
}
