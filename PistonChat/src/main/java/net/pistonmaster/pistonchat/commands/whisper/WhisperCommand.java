package net.pistonmaster.pistonchat.commands.whisper;

import lombok.RequiredArgsConstructor;
import net.pistonmaster.pistonchat.PistonChat;
import net.pistonmaster.pistonchat.tools.CommonTool;
import net.pistonmaster.pistonchat.utils.PlatformUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class WhisperCommand implements CommandExecutor, TabExecutor {
  private final PistonChat plugin;

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (args.length <= 1) {
      return false;
    }

    Optional<Player> receiver = PlatformUtils.getPlayer(args[0]);

    if (receiver.isEmpty()) {
      plugin.getCommonTool().sendLanguageMessage(sender, "notonline");
      return true;
    }

    plugin.runAsync(() -> MessageCommandHelper.sendWhisper(plugin, sender, receiver.get(), CommonTool.mergeArgs(args, 1)));

    return true;
  }

  @Override
  public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
    if (args.length == 1) {
      return null;
    } else {
      return Collections.emptyList();
    }
  }
}
