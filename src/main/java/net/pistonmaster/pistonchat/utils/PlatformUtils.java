package net.pistonmaster.pistonchat.utils;

import com.github.puregero.multilib.MultiLib;
import com.github.puregero.multilib.multipaper.MultiPaperImpl;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.UUID;

public class PlatformUtils {
    public static Player getPlayer(String name) {
        return Bukkit.getPlayer(name);
    }

    public static Player getPlayer(UUID uuid) {
        return Bukkit.getPlayer(uuid);
    }

    public static Collection<? extends Player> getOnlinePlayers() {
        return MultiLib.getAllOnlinePlayers();
    }

    public static boolean isMultiPaper() {
        try {
            return MultiLib.class.getDeclaredMethod("get").invoke(null) instanceof MultiPaperImpl;
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
            return false;
        }
    }
}
