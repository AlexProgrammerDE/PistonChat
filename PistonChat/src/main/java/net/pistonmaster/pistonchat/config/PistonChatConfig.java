package net.pistonmaster.pistonchat.config;

import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;

import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class PistonChatConfig {
  @Comment("Whisper message formats using MiniMessage syntax")
  public WhisperConfig whisper = new WhisperConfig();

  @Comment("Hover text shown when hovering over a player's name in chat")
  public String hoverText = "<gold>Message <dark_aqua><player_name>";

  @Comment("Storage type: 'file' or 'mysql'")
  public String storage = "file";

  @Comment("MySQL connection settings (only used when storage is 'mysql')")
  public MySQLConfig mysql = new MySQLConfig();

  @Comment("Strip color codes from player display names")
  public boolean stripNameColor = true;

  @Comment({
      "Chat format templates by permission (pistonchat.chatformat.<name>)",
      "First matching format is used, checked in order"
  })
  public Map<String, String> chatFormats = new LinkedHashMap<>(Map.of(
      "special", "<player_name> »",
      "default", "<<player_name>>"
  ));

  @Comment("Overall message format combining the chat format and message")
  public String messageFormat = "<format> <message>";

  @Comment("Display name for console in whispers")
  public String consoleName = "[console]";

  @Comment("Allow players to send private messages to themselves")
  public boolean allowPmSelf = true;

  @Comment("Allow sending private messages to players who ignore you")
  public boolean allowPmIgnored = true;

  @Comment("When true, blocked PMs are still shown to sender (shadow block)")
  public boolean onlyHidePms = true;

  @Comment("Number of players shown per page in /ignorelist")
  public int ignoreListSize = 9;

  @Comment("Configurable plugin messages using MiniMessage syntax")
  public MessagesConfig messages = new MessagesConfig();

  @Comment({
      "Message color prefixes by permission (pistonchat.prefix.<name>)",
      "Use '/' as prefix to disable. Requires pistonchat.<COLORCODE> permission.",
      "See: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/ChatColor.html"
  })
  public Map<String, PrefixConfig> prefixes = new LinkedHashMap<>(Map.of(
      "green", new PrefixConfig(">", "GREEN")
  ));

  @Configuration
  public static class WhisperConfig {
    @Comment("Format shown to the receiver of a whisper")
    public String from = "<light_purple><player_name> whispers: <message>";
    @Comment("Format shown to the sender of a whisper")
    public String to = "<light_purple>You whisper to <player_name>: <message>";
  }

  @Configuration
  public static class MySQLConfig {
    @Comment("MySQL server hostname")
    public String host = "localhost";
    @Comment("MySQL server port")
    public int port = 3306;
    @Comment("Database name")
    public String database = "pistonchat";
    @Comment("Database username")
    public String username = "root";
    @Comment("Database password")
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
    @Comment("Character(s) that trigger this color prefix when message starts with them")
    public String prefix = ">";
    @Comment("Color name from org.bukkit.ChatColor (e.g. GREEN, RED, GOLD)")
    public String color = "GREEN";

    public PrefixConfig() {}

    public PrefixConfig(String prefix, String color) {
      this.prefix = prefix;
      this.color = color;
    }
  }
}
