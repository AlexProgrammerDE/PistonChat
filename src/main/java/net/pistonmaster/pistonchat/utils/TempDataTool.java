package net.pistonmaster.pistonchat.utils;

import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.Map;

public class TempDataTool {
    private final Map<CommandSender, TempData> map = new HashMap<>();

    public void setWhisperingEnabled(CommandSender player, boolean value) {
        indexPlayer(player);

        map.get(player).whispering = value;
    }

    public void setChatEnabled(CommandSender player, boolean value) {
        indexPlayer(player);

        map.get(player).chat = value;
    }

    public boolean isWhisperingEnabled(CommandSender player) {
        indexPlayer(player);

        return map.get(player).whispering;
    }

    public boolean isChatEnabled(CommandSender player) {
        indexPlayer(player);

        return map.get(player).chat;
    }

    private void indexPlayer(CommandSender player) {
        map.putIfAbsent(player, new TempData());
    }

    private static class TempData {
        private boolean whispering = true;
        private boolean chat = true;
    }
}
