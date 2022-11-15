package net.pistonmaster.pistonchat.utils;

import com.github.puregero.multilib.MultiLib;
import org.bukkit.entity.Player;

public class TempDataTool {
    public void setWhisperingEnabled(Player player, boolean value) {
        MultiLib.setPersistentData(player, "pistonchat_whispering", String.valueOf(value));
    }

    public void setChatEnabled(Player player, boolean value) {
        MultiLib.setPersistentData(player, "pistonchat_chat", String.valueOf(value));
    }

    public boolean isWhisperingEnabled(Player player) {
        String value = MultiLib.getPersistentData(player, "pistonchat_whispering");
        return value == null || Boolean.parseBoolean(value);
    }

    public boolean isChatEnabled(Player player) {
        String value = MultiLib.getPersistentData(player, "pistonchat_chat");
        return value == null || Boolean.parseBoolean(value);
    }
}
