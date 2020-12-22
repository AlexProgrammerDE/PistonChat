package me.alexprogrammerde.pistonchat.utils;

import org.bukkit.entity.Player;

/**
 * Parent for both soft and hard banning!
 */
public class IgnoreTool {
    public static boolean isIgnored(Player chatter, Player receiver) {
        if (SoftIgnoreTool.isSoftIgnored(chatter, receiver)) {
            return true;
        } else return ConfigTool.isHardIgnored(chatter, receiver);
    }
}
