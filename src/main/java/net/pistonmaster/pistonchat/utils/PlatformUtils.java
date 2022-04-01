package net.pistonmaster.pistonchat.utils;

import com.github.puregero.multilib.MultiLib;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

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
}
