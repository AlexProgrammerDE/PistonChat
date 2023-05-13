package net.pistonmaster.pistonchat.tools;

import net.md_5.bungee.api.ChatColor;
import net.pistonmaster.pistonchat.PistonChat;

public class LanguageTool {
    private LanguageTool() {
    }

    public static String getMessage(String property) {
        return ChatColor.translateAlternateColorCodes('&',
                CommonTool.getPrefix() + PistonChat.getInstance().getLanguage().getString(property));
    }
}
