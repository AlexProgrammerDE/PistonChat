package net.pistonmaster.pistonchat.utils;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class TempDataTool {
    private final Map<Player, TempData> map = new HashMap<>();

    public void setWhisperingEnabled(Player player, boolean value) {
        indexPlayer(player);

        map.get(player).whispering = value;
    }

    public void setChatEnabled(Player player, boolean value) {
        indexPlayer(player);

        map.get(player).chat = value;
    }

    public boolean isWhisperingEnabled(Player player) {
        indexPlayer(player);

        return map.get(player).whispering;
    }

    public boolean isChatEnabled(Player player) {
        indexPlayer(player);

        return map.get(player).chat;
    }

    private void indexPlayer(Player player) {
        map.putIfAbsent(player, new TempData());
    }

    private static class TempData {
        private boolean whispering = true;
        private boolean chat = true;
    }
}
