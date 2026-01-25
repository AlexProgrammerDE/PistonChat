package net.pistonmaster.pistonmute.config;

import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class PistonMuteConfig {
  @Comment({
      "When enabled, muted players can still send messages but only they can see them.",
      "Other players won't receive the messages, making the mute invisible to the muted player."
  })
  public boolean shadowMute = true;

  @Comment({"", "Staff hierarchy settings to prevent lower-ranked staff from muting higher-ranked."})
  public StaffConfig staff = new StaffConfig();

  @Comment({"", "Command logging settings"})
  public LoggingConfig logging = new LoggingConfig();

  @Comment({"", "Staff notes system settings"})
  public NotesConfig notes = new NotesConfig();

  @Comment({"", "Command blocking settings for muted players"})
  public CommandBlockingConfig commandBlocking = new CommandBlockingConfig();

  @Comment({"", "Alt account detection settings"})
  public AltDetectionConfig altDetection = new AltDetectionConfig();

  @Comment({"", "Mute notification settings"})
  public MuteNotificationConfig muteNotification = new MuteNotificationConfig();

  @Comment({"", "Warning system settings"})
  public WarningConfig warnings = new WarningConfig();

  @Comment({
      "",
      "Progressive Punishment Escalation Templates",
      "Define escalation rules for different violation types.",
      "Format: template_name -> offense_number -> duration",
      "Use 'permanent' for permanent mutes.",
      "Duration format: Xs (seconds), Xm (minutes), Xh (hours), Xd (days), Xw (weeks)"
  })
  public Map<String, Map<Integer, String>> escalation = createDefaultEscalation();

  @Comment({"", "Punishment history display settings"})
  public HistoryConfig history = new HistoryConfig();

  @Comment({"", "Mute info message settings"})
  public MuteInfoConfig muteInfo = new MuteInfoConfig();

  @Comment({"", "Mute list message settings"})
  public MuteListConfig muteList = new MuteListConfig();

  private static Map<String, Map<Integer, String>> createDefaultEscalation() {
    Map<String, Map<Integer, String>> templates = new LinkedHashMap<>();

    // Swearing template
    Map<Integer, String> swearing = new LinkedHashMap<>();
    swearing.put(1, "10m");
    swearing.put(2, "1h");
    swearing.put(3, "1d");
    swearing.put(4, "permanent");
    templates.put("swearing", swearing);

    // Spam template
    Map<Integer, String> spam = new LinkedHashMap<>();
    spam.put(1, "5m");
    spam.put(2, "30m");
    spam.put(3, "6h");
    spam.put(4, "1d");
    spam.put(5, "permanent");
    templates.put("spam", spam);

    // Advertising template
    Map<Integer, String> advertising = new LinkedHashMap<>();
    advertising.put(1, "1h");
    advertising.put(2, "1d");
    advertising.put(3, "permanent");
    templates.put("advertising", advertising);

    // Harassment template
    Map<Integer, String> harassment = new LinkedHashMap<>();
    harassment.put(1, "1h");
    harassment.put(2, "1d");
    harassment.put(3, "7d");
    harassment.put(4, "permanent");
    templates.put("harassment", harassment);

    return templates;
  }

  @Configuration
  public static class StaffConfig {
    @Comment("Enable staff immunity/hierarchy system")
    public boolean hierarchyEnabled = true;

    @Comment({
        "Hierarchy mode: 'immune' or 'weight'",
        "immune: Players with pistonmute.immune permission cannot be muted",
        "weight: Players with higher pistonmute.weight.<number> can mute those with lower weights"
    })
    public String hierarchyMode = "immune";
  }

  @Configuration
  public static class LoggingConfig {
    @Comment("Enable logging of all punishment commands to punishments.log")
    public boolean enabled = true;

    @Comment("Format for log entries. Placeholders: %timestamp%, %staff%, %command%, %target%, %details%")
    public String format = "[%timestamp%] [%staff%] [%command%] [%target%] %details%";
  }

  @Configuration
  public static class NotesConfig {
    @Comment("Enable the staff notes system for players")
    public boolean enabled = true;
  }

  @Configuration
  public static class CommandBlockingConfig {
    @Comment("Block muted players from using chat-related commands")
    public boolean enabled = true;

    @Comment({
        "List of commands that muted players cannot use.",
        "Commands should be listed without the leading slash.",
        "Aliases are automatically handled (e.g., 'msg' also blocks 'tell', 'w', etc.)"
    })
    public List<String> blockedCommands = List.of(
        "me",
        "say",
        "msg",
        "tell",
        "w",
        "whisper",
        "reply",
        "r",
        "mail",
        "m",
        "pm",
        "dm",
        "message"
    );

    @Comment({
        "Message shown to muted players when they try to use a blocked command.",
        "Supports color codes with &."
    })
    public String message = "&cYou cannot use this command while muted!";
  }

  @Configuration
  public static class AltDetectionConfig {
    @Comment("Track player IPs to detect and manage alt accounts")
    public boolean enabled = true;

    @Comment({
        "Automatically mute all known alt accounts when muting a player.",
        "This uses IP tracking to identify alt accounts."
    })
    public boolean autoMuteAlts = false;

    @Comment({
        "Notify staff when a muted player's alt joins the server.",
        "Players with pistonmute.notify permission will be notified."
    })
    public boolean notifyStaffOnJoin = true;

    @Comment({
        "Message shown to staff when a muted player's alt joins.",
        "Placeholders: %player%, %alt%, %ip%"
    })
    public String joinNotifyMessage = "&e[PistonMute] &c%player% &emay be an alt of muted player &c%alt%";
  }

  @Configuration
  public static class MuteNotificationConfig {
    @Comment("Notify players about their mute status when they join")
    public boolean notifyOnJoin = true;

    @Comment({
        "Message shown to muted players when they join the server.",
        "Supports color codes with &."
    })
    public String joinMessage = "&c&lYou are currently muted and cannot chat.";

    @Comment("Show mute reason to the player (if one was provided)")
    public boolean showReason = true;

    @Comment({
        "Message format for showing mute reason.",
        "Placeholder: %reason%"
    })
    public String reasonFormat = "&cReason: &7%reason%";
  }

  @Configuration
  public static class WarningConfig {
    @Comment("Enable the warning system for players")
    public boolean enabled = true;

    @Comment({
        "Time in days after which warnings expire.",
        "Set to 0 for warnings that never expire."
    })
    public int expiryDays = 30;

    @Comment("Maximum warnings before auto-escalation kicks in")
    public int maxBeforeAction = 3;

    @Comment({
        "Action to take when a player reaches max warnings.",
        "Options: none, mute, tempmute",
        "If tempmute, uses the duration specified below."
    })
    public String maxAction = "tempmute";

    @Comment("Duration for tempmute when max warnings reached (e.g., 1h, 1d, 1w)")
    public String maxActionDuration = "1h";

    @Comment({"", "Warning-related messages"})
    public WarningMessagesConfig messages = new WarningMessagesConfig();
  }

  @Configuration
  public static class WarningMessagesConfig {
    @Comment("Message shown to player when warned")
    public String warnedMessage = "&c&lYou have been warned! &eReason: &7%reason%";

    @Comment("Message showing who issued the warning")
    public String warnedByMessage = "&7Warned by: &e%issuer%";

    @Comment("Message format for listing warnings")
    public String listFormat = "&7[%id%] &e%reason% &7- by %issuer% (%time_ago%)";

    @Comment("Message shown when a warning is about to expire")
    public String expiryNote = "&7(Expires in %time%)";
  }

  @Configuration
  public static class HistoryConfig {
    @Comment("Number of entries per page")
    public int pageSize = 10;

    @Comment("Format for punishment history entries")
    public String entryFormat = "&7[%type%] &e%reason% &7- %duration% by %issuer% (%date%)";

    @Comment("Header format for punishment history")
    public String header = "&6--- Punishment History for %player% (Page %page%/%total%) ---";

    @Comment("Footer format for punishment history")
    public String footer = "&7Use /history %player% <page> to view more.";
  }

  @Configuration
  public static class MuteInfoConfig {
    @Comment("Header format for mute info display")
    public String header = "&6--- Mute Info for %player% ---";

    @Comment("Format for mute reason")
    public String reason = "&7Reason: &e%reason%";

    @Comment("Format for mute issuer")
    public String issuer = "&7Muted by: &e%issuer%";

    @Comment("Format for mute duration")
    public String duration = "&7Duration: &e%duration%";

    @Comment("Format for mute expiry")
    public String expires = "&7Expires: &e%expires%";

    @Comment("Text for permanent mutes")
    public String permanent = "&cPermanent";

    @Comment("Format for escalation template")
    public String template = "&7Template: &e%template%";
  }

  @Configuration
  public static class MuteListConfig {
    @Comment("Header format for mute list")
    public String header = "&6--- Muted Players (Page %page%/%total%) ---";

    @Comment("Format for mute list entries")
    public String entryFormat = "&e%player% &7- %duration% &8(%reason%)";

    @Comment("Footer format for mute list")
    public String footer = "&7Use /mutelist <page> to view more.";
  }
}
