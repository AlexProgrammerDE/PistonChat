package net.pistonmaster.pistonchat.utils;

import java.util.HashMap;
import java.util.Map;

public class TempDataTool {
    private final Map<UniqueSender, TempData> map = new HashMap<>();

    public void setWhisperingEnabled(UniqueSender player, boolean value) {
        indexPlayer(player);

        map.get(player).whispering = value;
    }

    public void setChatEnabled(UniqueSender player, boolean value) {
        indexPlayer(player);

        map.get(player).chat = value;
    }

    public boolean isWhisperingEnabled(UniqueSender player) {
        indexPlayer(player);

        return map.get(player).whispering;
    }

    public boolean isChatEnabled(UniqueSender player) {
        indexPlayer(player);

        return map.get(player).chat;
    }

    private void indexPlayer(UniqueSender player) {
        map.putIfAbsent(player, new TempData());
    }

    private static class TempData {
        private boolean whispering = true;
        private boolean chat = true;
    }
}
