package net.pistonmaster.pistonchat.utils;

import org.bukkit.entity.Player;

import java.util.HashMap;

public class TempDataTool {
    private static final HashMap<Player, TempData> map = new HashMap<>();

    private TempDataTool() {}

    public static void setWhisperingEnabled(Player player, boolean value) {
        indexPlayer(player);

        map.get(player).whispering = value;
    }

    public static void setChatEnabled(Player player, boolean value) {
        indexPlayer(player);

        map.get(player).chat = value;
    }

    public static boolean isWhisperingEnabled(Player player) {
        indexPlayer(player);

        return map.get(player).whispering;
    }

    public static boolean isChatEnabled(Player player) {
        indexPlayer(player);

        return map.get(player).chat;
    }

    private static void indexPlayer(Player player) {
        if (!map.containsKey(player)) {
            map.put(player, new TempDataTool.TempData());
        }
    }

    private static class TempData {
        private boolean whispering = true;
        private boolean chat = true;
    }
}
