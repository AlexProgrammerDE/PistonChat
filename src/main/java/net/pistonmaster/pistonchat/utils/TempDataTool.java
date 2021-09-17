package net.pistonmaster.pistonchat.utils;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class TempDataTool {
    private final Map<CommandSender, TempData> map = new HashMap<>();

    public void onQuit(Player player) {
        map.remove(player);
    }

    public void setWhisperingEnabled(CommandSender player, boolean value) {
        map.putIfAbsent(player, new TempData());

        map.get(player).whispering = value;
    }

    public void setChatEnabled(CommandSender player, boolean value) {
        map.putIfAbsent(player, new TempData());

        map.get(player).chat = value;
    }

    public boolean isWhisperingEnabled(CommandSender player) {
        return !map.containsKey(player) || map.get(player).whispering;
    }

    public boolean isChatEnabled(CommandSender player) {
        return !map.containsKey(player) || map.get(player).chat;
    }

    private static class TempData {
        private boolean whispering = true;
        private boolean chat = true;
    }
}
