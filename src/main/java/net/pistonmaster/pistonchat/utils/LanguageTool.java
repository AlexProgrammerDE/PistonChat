package net.pistonmaster.pistonchat.utils;

import net.md_5.bungee.api.ChatColor;

public class LanguageTool {
    private LanguageTool() {
    }

    public static String getMessage(String property) {
        return ChatColor.translateAlternateColorCodes('&', CommonTool.getPrefix() + ConfigTool.getLanguage().getString(property));
    }
}
