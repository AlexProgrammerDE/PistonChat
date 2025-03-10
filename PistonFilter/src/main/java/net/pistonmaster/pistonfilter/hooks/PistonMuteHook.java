package net.pistonmaster.pistonfilter.hooks;

import net.pistonmaster.pistonmute.utils.StorageTool;
import org.bukkit.entity.Player;

import java.util.Date;

public class PistonMuteHook {
    public static void mute(Player player, Date date) {
        StorageTool.tempMutePlayer(player, date);
    }
}
