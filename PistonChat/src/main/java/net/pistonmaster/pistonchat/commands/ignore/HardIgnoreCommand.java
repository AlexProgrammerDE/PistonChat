package net.pistonmaster.pistonchat.commands.ignore;

import lombok.RequiredArgsConstructor;
import net.pistonmaster.pistonchat.PistonChat;
import net.pistonmaster.pistonchat.storage.PCStorage;
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
public class HardIgnoreCommand implements CommandExecutor, TabExecutor {
  private final PistonChat plugin;

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (!(sender instanceof Player player)) {
      plugin.getCommonTool().sendLanguageMessage(sender, "playeronly");
      return true;
    }

    if (args.length == 0) {
      return false;
    }

    Optional<Player> ignored = PlatformUtils.getPlayer(args[0]);

    if (ignored.isEmpty()) {
      plugin.getCommonTool().sendLanguageMessage(player, "notonline");
      return true;
    }

    plugin.runAsync(() -> {
      PCStorage.HardReturn type = plugin.getHardIgnoreTool().hardIgnorePlayer(player, ignored.get());

      if (type == PCStorage.HardReturn.IGNORE) {
        plugin.getCommonTool().sendLanguageMessageNoPrefix(player,
            "ignorehard",
            CommonTool.getStrippedNameResolver(ignored.get()));
      } else if (type == PCStorage.HardReturn.UN_IGNORE) {
        plugin.getCommonTool().sendLanguageMessageNoPrefix(player,
            "unignorehard",
            CommonTool.getStrippedNameResolver(ignored.get()));
      }
    });

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
