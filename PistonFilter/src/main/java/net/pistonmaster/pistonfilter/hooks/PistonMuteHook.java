package net.pistonmaster.pistonfilter.hooks;

import net.pistonmaster.pistonmute.utils.StorageTool;
import org.bukkit.entity.Player;

import java.time.Instant;

public class PistonMuteHook {
  public static void mute(Player player, Instant unmuteAt) {
    StorageTool.tempMutePlayer(player, unmuteAt);
  }

  private PistonMuteHook() {
  }
}
