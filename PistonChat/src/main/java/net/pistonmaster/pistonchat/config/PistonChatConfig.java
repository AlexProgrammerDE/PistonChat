package net.pistonmaster.pistonchat.config;

import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;

import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class PistonChatConfig {
  @Comment({
      "To use these prefixes you need additionally the pistonchat.<COLORCODE>",
      "/ indicates disabled!",
      "This config is configured to be what 2b2t.org has.",
      "https://hub.spigotmc.org/javadocs/spigot/org/bukkit/ChatColor.html",
      "pistonchat.prefix.green and pistonchat.chatformat.default are given by default.",
      "storage is either file or mysql"
  })
  public WhisperConfig whisper = new WhisperConfig();

  public String hoverText = "<gold>Message <dark_aqua><player_name>";

  public String storage = "file";

  public MySQLConfig mysql = new MySQLConfig();

  public boolean stripNameColor = true;

  public Map<String, String> chatFormats = new LinkedHashMap<>(Map.of(
      "special", "<player_name> »",
      "default", "<<player_name>>"
  ));

  public String messageFormat = "<format> <message>";

  public String consoleName = "[console]";

  public boolean allowPmSelf = true;

  public boolean allowPmIgnored = true;

  public boolean onlyHidePms = true;

  public int ignoreListSize = 9;

  public MessagesConfig messages = new MessagesConfig();

  public Map<String, PrefixConfig> prefixes = new LinkedHashMap<>(Map.of(
      "green", new PrefixConfig(">", "GREEN")
  ));

  @Configuration
  public static class WhisperConfig {
    public String from = "<light_purple><player_name> whispers: <message>";
    public String to = "<light_purple>You whisper to <player_name>: <message>";
  }

  @Configuration
  public static class MySQLConfig {
    public String host = "localhost";
    public int port = 3306;
    public String database = "pistonchat";
    public String username = "root";
    public String password = "";
  }

  @Configuration
  public static class MessagesConfig {
    @Comment("The prefix can also be set to just \"&6\"")
    public String format = "[<dark_green>PistonChat</dark_green>] <gold><message>";
    public String helpHeader = "---[<dark_green>PistonChat</dark_green>]---";
    public String playeronly = "You need to be a player to do this.";
    public String notonline = "This player is not online.";
    public String nooneignored = "No players ignored.";
    public String chaton = "Chat messages unhidden.";
    public String chatoff = "Chat messages hidden.";
    public String pmson = "Private messages unhidden.";
    public String pmsoff = "Private messages hidden.";
    public String pmself = "Please do not private message yourself.";
    public String chatisoff = "You have toggled off chat";
    public String sourceIgnored = "This person ignores you!";
    public String targetIgnored = "You ignore this person!";
    public String pageNotExists = "This page doesn't exist.";
    public String notANumber = "Not a number!";
    public String whisperingDisabled = "This person has whispering disabled!";
    public String ignore = "<gold>Now ignoring <dark_aqua><player_name>";
    public String unignore = "<gold>No longer ignoring <dark_aqua><player_name>";
    public String ignorehard = "<gold>Permanently ignoring <player_name>. This is saved in <dark_red>/ignorelist</dark_red>.";
    public String unignorehard = "<gold>No longer permanently ignoring <dark_aqua><player_name>";
    public String ignorelistcleared = "<gold>Ignore list cleared.";
  }

  @Configuration
  public static class PrefixConfig {
    public String prefix = ">";
    public String color = "GREEN";

    public PrefixConfig() {}

    public PrefixConfig(String prefix, String color) {
      this.prefix = prefix;
      this.color = color;
    }
  }
}
